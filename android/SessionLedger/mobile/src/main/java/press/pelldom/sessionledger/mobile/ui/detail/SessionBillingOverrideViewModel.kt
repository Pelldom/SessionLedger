package press.pelldom.sessionledger.mobile.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.util.Locale
import kotlin.math.roundToLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import press.pelldom.sessionledger.mobile.billing.BillingEngine
import press.pelldom.sessionledger.mobile.billing.RoundingDirection
import press.pelldom.sessionledger.mobile.billing.RoundingMode
import press.pelldom.sessionledger.mobile.billing.SessionState
import press.pelldom.sessionledger.mobile.billing.SourceType
import press.pelldom.sessionledger.mobile.data.db.AppDatabase
import press.pelldom.sessionledger.mobile.data.db.DefaultCategory
import press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity
import press.pelldom.sessionledger.mobile.settings.SettingsRepository
import press.pelldom.sessionledger.mobile.settings.dataStore

enum class SessionMinimumSelection { INHERIT, NONE, TIME, CHARGE }

data class SessionBillingOverrideUiState(
    val loading: Boolean = true,
    val notFound: Boolean = false,
    val isEditable: Boolean = false,
    val validationError: String? = null,

    val hourlyRateOverride: String = "", // empty = inherit
    val roundingModeOverride: RoundingMode? = null,
    val roundingDirectionOverride: RoundingDirection? = null,
    val minimumSelection: SessionMinimumSelection = SessionMinimumSelection.INHERIT,
    val minHours: String = "",
    val minChargeAmount: String = "",

    // Effective display (from BillingEngine)
    val effectiveRateText: String = "",
    val effectiveRoundingText: String = "",
    val effectiveMinimumText: String = "",
    val finalAmountText: String = "",

    val canSave: Boolean = false,
    val hasUnsavedChanges: Boolean = false
)

class SessionBillingOverrideViewModel(
    app: Application,
    private val sessionId: String
) : AndroidViewModel(app) {
    private val db = AppDatabase.getInstance(app)
    private val settingsRepo = SettingsRepository(app.dataStore)

    private val _ui = MutableStateFlow(SessionBillingOverrideUiState())
    val ui: StateFlow<SessionBillingOverrideUiState> = _ui

    private var baseline: SessionEntity? = null
    private var category: CategoryEntity? = null
    private var settings = press.pelldom.sessionledger.mobile.settings.GlobalSettings(
        defaultCurrency = "CAD",
        defaultHourlyRate = 0.0,
        defaultRoundingMode = RoundingMode.EXACT,
        defaultRoundingDirection = RoundingDirection.UP,
        minBillableSeconds = null,
        minChargeAmount = null,
        lastUsedCategoryId = null
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val s = db.sessionDao().getById(sessionId)
            if (s == null) {
                _ui.value = SessionBillingOverrideUiState(loading = false, notFound = true)
                return@launch
            }
            baseline = s
            settings = settingsRepo.observeGlobalSettings().first()
            val cats = db.categoryDao().getAll()
            category = cats.firstOrNull { it.id == s.categoryId } ?: cats.firstOrNull { it.id == DefaultCategory.UNCATEGORIZED_ID }

            val editable = s.state == SessionState.ENDED && s.endTimeMs != null
            val (minSel, minHours, minCharge) = toMinimumUi(s)
            _ui.value = SessionBillingOverrideUiState(
                loading = false,
                notFound = false,
                isEditable = editable,
                validationError = if (editable) null else "Only ended sessions can be overridden.",
                hourlyRateOverride = s.hourlyRateOverride?.toString().orEmpty(),
                roundingModeOverride = s.roundingModeOverride,
                roundingDirectionOverride = s.roundingDirectionOverride,
                minimumSelection = minSel,
                minHours = minHours,
                minChargeAmount = minCharge,
                canSave = false
            )
            recompute()
        }
    }

    fun setHourlyRateOverride(text: String) {
        _ui.value = _ui.value.copy(hourlyRateOverride = text)
        recompute()
    }

    fun clearHourlyRateOverride() {
        _ui.value = _ui.value.copy(hourlyRateOverride = "")
        recompute()
    }

    fun toggleRoundingModeOverride() {
        val next = when (_ui.value.roundingModeOverride) {
            null -> RoundingMode.EXACT
            RoundingMode.EXACT -> RoundingMode.SIX_MINUTE
            RoundingMode.SIX_MINUTE -> RoundingMode.EXACT
        }
        _ui.value = _ui.value.copy(roundingModeOverride = next)
        recompute()
    }

    fun clearRoundingModeOverride() {
        _ui.value = _ui.value.copy(roundingModeOverride = null)
        recompute()
    }

    fun cycleRoundingDirectionOverride() {
        val next = when (_ui.value.roundingDirectionOverride) {
            null -> RoundingDirection.UP
            RoundingDirection.UP -> RoundingDirection.NEAREST
            RoundingDirection.NEAREST -> RoundingDirection.DOWN
            RoundingDirection.DOWN -> RoundingDirection.UP
        }
        _ui.value = _ui.value.copy(roundingDirectionOverride = next)
        recompute()
    }

    fun clearRoundingDirectionOverride() {
        _ui.value = _ui.value.copy(roundingDirectionOverride = null)
        recompute()
    }

    fun setMinimumSelection(sel: SessionMinimumSelection) {
        val next = when (sel) {
            SessionMinimumSelection.INHERIT -> _ui.value.copy(minimumSelection = sel, minHours = "", minChargeAmount = "")
            SessionMinimumSelection.NONE -> _ui.value.copy(minimumSelection = sel, minHours = "", minChargeAmount = "")
            SessionMinimumSelection.TIME -> _ui.value.copy(minimumSelection = sel, minChargeAmount = "")
            SessionMinimumSelection.CHARGE -> _ui.value.copy(minimumSelection = sel, minHours = "")
        }
        _ui.value = next
        recompute()
    }

    fun setMinHours(text: String) {
        _ui.value = _ui.value.copy(minHours = text)
        recompute()
    }

    fun setMinChargeAmount(text: String) {
        _ui.value = _ui.value.copy(minChargeAmount = text)
        recompute()
    }

    fun save(onDone: () -> Unit) {
        val base = baseline ?: return
        val current = _ui.value
        if (!current.isEditable || !current.canSave) return

        val rate = current.hourlyRateOverride.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
        val (minSeconds, minAmt) = minimumToPersist(current)

        val updated = base.copy(
            hourlyRateOverride = rate,
            roundingModeOverride = current.roundingModeOverride,
            roundingDirectionOverride = current.roundingDirectionOverride,
            minBillableSecondsOverride = minSeconds,
            minChargeAmountOverride = minAmt,
            updatedAtMs = System.currentTimeMillis()
        )

        viewModelScope.launch(Dispatchers.IO) {
            db.sessionDao().update(updated)
            baseline = updated
            // Refresh derived text and clear dirty state, but let caller decide whether to navigate.
            recompute()
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun discardEdits() {
        val base = baseline ?: return
        val current = _ui.value
        val (minSel, minHours, minCharge) = toMinimumUi(base)
        _ui.value = current.copy(
            hourlyRateOverride = base.hourlyRateOverride?.toString().orEmpty(),
            roundingModeOverride = base.roundingModeOverride,
            roundingDirectionOverride = base.roundingDirectionOverride,
            minimumSelection = minSel,
            minHours = minHours,
            minChargeAmount = minCharge,
            canSave = false,
            hasUnsavedChanges = false,
            validationError = null
        )
        recompute()
    }

    private fun recompute() {
        val base = baseline ?: return
        val current = _ui.value
        if (!current.isEditable) return

        val rateStr = current.hourlyRateOverride.trim()
        val hoursStr = current.minHours.trim()
        val chargeStr = current.minChargeAmount.trim()

        val rateOk = rateStr.isEmpty() || rateStr.toDoubleOrNull() != null
        val hoursOk = hoursStr.isNotEmpty() && Regex("^\\d+(\\.\\d{0,2})?$").matches(hoursStr)
        val chargeOk = chargeStr.isNotEmpty() && chargeStr.toDoubleOrNull() != null

        val error = when {
            !rateOk -> "Hourly rate override must be a number."
            current.minimumSelection == SessionMinimumSelection.TIME && !hoursOk -> "Minimum time is required (hours, up to 2 decimals)."
            current.minimumSelection == SessionMinimumSelection.CHARGE && !chargeOk -> "Minimum charge is required (CAD)."
            else -> null
        }

        val (minSeconds, minAmt) = minimumToPersist(current)
        val dirty =
            (rateStr.isEmpty() && base.hourlyRateOverride != null) ||
                (rateStr.isNotEmpty() && base.hourlyRateOverride?.toString() != rateStr) ||
                (current.roundingModeOverride != base.roundingModeOverride) ||
                (current.roundingDirectionOverride != base.roundingDirectionOverride) ||
                (base.minBillableSecondsOverride != minSeconds) ||
                (base.minChargeAmountOverride != minAmt)

        val staged = base.copy(
            hourlyRateOverride = rateStr.takeIf { it.isNotEmpty() }?.toDoubleOrNull(),
            roundingModeOverride = current.roundingModeOverride,
            roundingDirectionOverride = current.roundingDirectionOverride,
            minBillableSecondsOverride = minSeconds,
            minChargeAmountOverride = minAmt
        )

        val cat = category
        val resolved = BillingEngine.resolveConfig(session = staged, category = cat, settings = settings)
        val amountText = try {
            val result = BillingEngine.calculateForEndedSession(session = staged, category = cat, settings = settings)
            "$" + String.format(Locale.CANADA, "%.2f", result.finalCost)
        } catch (_: Throwable) {
            ""
        }

        _ui.value = current.copy(
            validationError = error,
            canSave = dirty && error == null,
            hasUnsavedChanges = dirty,
            effectiveRateText = formatRate(resolved.ratePerHour, resolved.rateSource),
            effectiveRoundingText = formatRounding(resolved.roundingMode, resolved.roundingDirection, resolved.roundingSource),
            effectiveMinimumText = formatMinimum(resolved.minTimeSeconds, resolved.minTimeSource, resolved.minCharge, resolved.minChargeSource),
            finalAmountText = amountText
        )
    }

    private fun formatRate(rate: Double, src: SourceType): String {
        val tag = when (src) {
            SourceType.SESSION -> "(Session)"
            SourceType.CATEGORY -> "(Category)"
            SourceType.GLOBAL -> "(Default)"
            SourceType.NONE -> "(Default)"
        }
        return "$" + String.format(Locale.CANADA, "%.2f", rate) + "/hr $tag"
    }

    private fun formatRounding(mode: RoundingMode, dir: RoundingDirection?, src: SourceType): String {
        val tag = when (src) {
            SourceType.SESSION -> "(Session)"
            SourceType.CATEGORY -> "(Category)"
            SourceType.GLOBAL -> "(Default)"
            SourceType.NONE -> "(Default)"
        }
        return if (mode == RoundingMode.SIX_MINUTE) {
            "SIX_MINUTE / ${(dir ?: settings.defaultRoundingDirection).name} $tag"
        } else {
            "EXACT $tag"
        }
    }

    private fun formatMinimum(minSeconds: Long, minSecondsSrc: SourceType, minCharge: Double, minChargeSrc: SourceType): String {
        // For display, show whichever non-zero minimum exists (time preferred), matching our exclusive UI.
        return when {
            minSeconds > 0L -> {
                val tag = when (minSecondsSrc) {
                    SourceType.SESSION -> "(Session)"
                    SourceType.CATEGORY -> "(Category)"
                    SourceType.GLOBAL -> "(Default)"
                    SourceType.NONE -> "(Default)"
                }
                String.format(Locale.CANADA, "%.2f", minSeconds.toDouble() / 3600.0) + " hr $tag"
            }
            minCharge > 0.0 -> {
                val tag = when (minChargeSrc) {
                    SourceType.SESSION -> "(Session)"
                    SourceType.CATEGORY -> "(Category)"
                    SourceType.GLOBAL -> "(Default)"
                    SourceType.NONE -> "(Default)"
                }
                "$" + String.format(Locale.CANADA, "%.2f", minCharge) + " $tag"
            }
            else -> "None (Default)"
        }
    }

    private fun minimumToPersist(current: SessionBillingOverrideUiState): Pair<Long?, Double?> {
        return when (current.minimumSelection) {
            SessionMinimumSelection.INHERIT -> null to null
            SessionMinimumSelection.NONE -> 0L to 0.0
            SessionMinimumSelection.TIME -> {
                val h = current.minHours.trim().toDoubleOrNull() ?: 0.0
                (h * 3600.0).roundToLong().coerceAtLeast(0L) to null
            }
            SessionMinimumSelection.CHARGE -> {
                null to (current.minChargeAmount.trim().toDoubleOrNull() ?: 0.0)
            }
        }
    }

    private fun toMinimumUi(s: SessionEntity): Triple<SessionMinimumSelection, String, String> {
        val t = s.minBillableSecondsOverride
        val a = s.minChargeAmountOverride
        return when {
            t == null && a == null -> Triple(SessionMinimumSelection.INHERIT, "", "")
            (t ?: 0L) == 0L && (a ?: 0.0) == 0.0 -> Triple(SessionMinimumSelection.NONE, "", "")
            t != null -> Triple(SessionMinimumSelection.TIME, String.format(Locale.CANADA, "%.2f", t.toDouble() / 3600.0), "")
            else -> Triple(SessionMinimumSelection.CHARGE, "", a?.toString().orEmpty())
        }
    }
}

