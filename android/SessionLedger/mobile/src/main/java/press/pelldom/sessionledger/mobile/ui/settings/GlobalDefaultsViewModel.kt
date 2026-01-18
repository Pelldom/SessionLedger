package press.pelldom.sessionledger.mobile.ui.settings

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
import press.pelldom.sessionledger.mobile.billing.RoundingDirection
import press.pelldom.sessionledger.mobile.billing.RoundingMode
import press.pelldom.sessionledger.mobile.settings.GlobalSettings
import press.pelldom.sessionledger.mobile.settings.SettingsRepository
import press.pelldom.sessionledger.mobile.settings.dataStore

enum class GlobalMinimumSelection { NONE, TIME, CHARGE }

data class GlobalDefaultsUiState(
    val loading: Boolean = true,
    val hourlyRate: String = "",
    val roundingMode: RoundingMode = RoundingMode.EXACT,
    val roundingDirection: RoundingDirection = RoundingDirection.UP,
    val minimumSelection: GlobalMinimumSelection = GlobalMinimumSelection.NONE,
    val minHours: String = "",
    val minChargeAmount: String = "",
    val canSave: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val validationError: String? = null,
)

class GlobalDefaultsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository(app.dataStore)

    private val _ui = MutableStateFlow(GlobalDefaultsUiState())
    val ui: StateFlow<GlobalDefaultsUiState> = _ui

    private var baseline: GlobalSettings? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val s = repo.observeGlobalSettings().first()
            baseline = s
            val (sel, hours, charge) = toMinimumUi(s)
            _ui.value = GlobalDefaultsUiState(
                loading = false,
                hourlyRate = String.format(Locale.CANADA, "%.2f", s.defaultHourlyRate),
                roundingMode = s.defaultRoundingMode,
                roundingDirection = s.defaultRoundingDirection,
                minimumSelection = sel,
                minHours = hours,
                minChargeAmount = charge,
                canSave = false,
                validationError = null
            )
        }
    }

    fun setHourlyRate(text: String) {
        _ui.value = _ui.value.copy(hourlyRate = text)
        recompute()
    }

    fun toggleRoundingMode() {
        val current = _ui.value.roundingMode
        val next = if (current == RoundingMode.EXACT) RoundingMode.SIX_MINUTE else RoundingMode.EXACT
        _ui.value = _ui.value.copy(roundingMode = next)
        recompute()
    }

    fun cycleRoundingDirection() {
        val next = when (_ui.value.roundingDirection) {
            RoundingDirection.UP -> RoundingDirection.NEAREST
            RoundingDirection.NEAREST -> RoundingDirection.DOWN
            RoundingDirection.DOWN -> RoundingDirection.UP
        }
        _ui.value = _ui.value.copy(roundingDirection = next)
        recompute()
    }

    fun setMinimumSelection(sel: GlobalMinimumSelection) {
        val next = when (sel) {
            GlobalMinimumSelection.NONE -> _ui.value.copy(minimumSelection = sel, minHours = "", minChargeAmount = "")
            GlobalMinimumSelection.TIME -> _ui.value.copy(minimumSelection = sel, minChargeAmount = "")
            GlobalMinimumSelection.CHARGE -> _ui.value.copy(minimumSelection = sel, minHours = "")
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
        if (!current.canSave) return

        val rate = current.hourlyRate.trim().toDoubleOrNull() ?: return
        val (minSeconds, minAmt) = minimumToPersist(current)

        viewModelScope.launch(Dispatchers.IO) {
            repo.setDefaultHourlyRate(rate)
            repo.setDefaultRoundingMode(current.roundingMode)
            repo.setDefaultRoundingDirection(current.roundingDirection)
            repo.setMinBillableSeconds(minSeconds)
            repo.setMinChargeAmount(minAmt)

            baseline = base.copy(
                defaultHourlyRate = rate,
                defaultRoundingMode = current.roundingMode,
                defaultRoundingDirection = current.roundingDirection,
                minBillableSeconds = minSeconds,
                minChargeAmount = minAmt
            )

            _ui.value = current.copy(canSave = false, validationError = null)
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun discardEdits() {
        val base = baseline ?: return
        val (sel, hours, charge) = toMinimumUi(base)
        _ui.value = _ui.value.copy(
            hourlyRate = String.format(Locale.CANADA, "%.2f", base.defaultHourlyRate),
            roundingMode = base.defaultRoundingMode,
            roundingDirection = base.defaultRoundingDirection,
            minimumSelection = sel,
            minHours = hours,
            minChargeAmount = charge,
            canSave = false,
            hasUnsavedChanges = false,
            validationError = null
        )
    }

    private fun recompute() {
        val base = baseline ?: return
        val current = _ui.value

        val rateStr = current.hourlyRate.trim()
        val rateOk = rateStr.isNotEmpty() && rateStr.toDoubleOrNull() != null

        val hoursStr = current.minHours.trim()
        val chargeStr = current.minChargeAmount.trim()
        val hoursOk = hoursStr.isNotEmpty() && isValidHours(hoursStr)
        val chargeOk = chargeStr.isNotEmpty() && chargeStr.toDoubleOrNull() != null

        val error = when {
            !rateOk -> "Hourly rate is required (CAD/hr)."
            current.minimumSelection == GlobalMinimumSelection.TIME && !hoursOk -> "Minimum time is required (hours, up to 2 decimals)."
            current.minimumSelection == GlobalMinimumSelection.CHARGE && !chargeOk -> "Minimum charge is required (CAD)."
            else -> null
        }

        val (minSeconds, minAmt) = minimumToPersist(current)
        val dirty =
            (base.defaultHourlyRate.toString() != rateStr && !(base.defaultHourlyRate == (rateStr.toDoubleOrNull() ?: base.defaultHourlyRate))) ||
                (current.roundingMode != base.defaultRoundingMode) ||
                (current.roundingDirection != base.defaultRoundingDirection) ||
                (base.minBillableSeconds != minSeconds) ||
                (base.minChargeAmount != minAmt)

        _ui.value = current.copy(
            validationError = error,
            canSave = dirty && error == null,
            hasUnsavedChanges = dirty
        )
    }

    private fun minimumToPersist(current: GlobalDefaultsUiState): Pair<Long?, Double?> {
        return when (current.minimumSelection) {
            GlobalMinimumSelection.NONE -> null to null
            GlobalMinimumSelection.TIME -> {
                val h = current.minHours.trim().toDoubleOrNull() ?: 0.0
                val secs = (h * 3600.0).roundToLong().coerceAtLeast(0L)
                secs to null
            }
            GlobalMinimumSelection.CHARGE -> {
                val amt = current.minChargeAmount.trim().toDoubleOrNull() ?: 0.0
                null to amt
            }
        }
    }

    private fun toMinimumUi(s: GlobalSettings): Triple<GlobalMinimumSelection, String, String> {
        val hasTime = s.minBillableSeconds != null
        val hasCharge = s.minChargeAmount != null
        return when {
            hasTime -> {
                val hours = (s.minBillableSeconds ?: 0L).toDouble() / 3600.0
                Triple(GlobalMinimumSelection.TIME, String.format(Locale.CANADA, "%.2f", hours), "")
            }
            hasCharge -> Triple(GlobalMinimumSelection.CHARGE, "", s.minChargeAmount?.toString().orEmpty())
            else -> Triple(GlobalMinimumSelection.NONE, "", "")
        }
    }

    private fun isValidHours(text: String): Boolean {
        return Regex("^\\d+(\\.\\d{0,2})?$").matches(text)
    }
}

