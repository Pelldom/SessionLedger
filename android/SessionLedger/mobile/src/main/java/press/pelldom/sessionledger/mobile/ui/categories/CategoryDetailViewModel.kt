package press.pelldom.sessionledger.mobile.ui.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToLong
import press.pelldom.sessionledger.mobile.billing.RoundingDirection
import press.pelldom.sessionledger.mobile.billing.RoundingMode
import press.pelldom.sessionledger.mobile.data.db.AppDatabase
import press.pelldom.sessionledger.mobile.data.db.DefaultCategory
import press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity
import press.pelldom.sessionledger.mobile.settings.SettingsRepository
import press.pelldom.sessionledger.mobile.settings.dataStore

enum class CategoryMinimumSelection { INHERIT, NONE, TIME, CHARGE }

data class CategoryDetailUiState(
    val loading: Boolean = true,
    val notFound: Boolean = false,
    val isEditable: Boolean = true,
    val name: String = "",
    val nameEdit: String = "",

    // editable staged overrides
    val hourlyRate: String = "", // empty = inherit
    val roundingMode: RoundingMode? = null,
    val roundingDirection: RoundingDirection? = null,
    val minimumSelection: CategoryMinimumSelection = CategoryMinimumSelection.INHERIT,
    val minHours: String = "", // TIME selection (decimal hours, 2dp max)
    val minChargeAmount: String = "", // CHARGE selection

    // effective global defaults (for inherited display)
    val globalHourlyRate: Double = 0.0,
    val globalRoundingMode: RoundingMode = RoundingMode.EXACT,
    val globalRoundingDirection: RoundingDirection = RoundingDirection.UP,
    val globalMinBillableSeconds: Long? = null,
    val globalMinChargeAmount: Double? = null,

    val canSave: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val validationError: String? = null,
)

class CategoryDetailViewModel(
    app: Application,
    private val categoryId: String
) : AndroidViewModel(app) {
    private val db = AppDatabase.getInstance(app)
    private val settingsRepo = SettingsRepository(app.dataStore)

    private val _ui = MutableStateFlow(CategoryDetailUiState())
    val ui: StateFlow<CategoryDetailUiState> = _ui

    private var loaded: CategoryEntity? = null
    private var allCategories: List<CategoryEntity> = emptyList()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val cat = db.categoryDao().getById(categoryId)
            if (cat == null) {
                _ui.value = CategoryDetailUiState(loading = false, notFound = true, isEditable = false)
                return@launch
            }

            allCategories = db.categoryDao().getAll()
            val globals = settingsRepo.observeGlobalSettings().first()

            val editable = cat.id != DefaultCategory.UNCATEGORIZED_ID
            loaded = cat

            val (minSel, minHoursText, minChargeText) = toMinimumUi(cat)
            _ui.value = CategoryDetailUiState(
                loading = false,
                notFound = false,
                isEditable = editable,
                name = cat.name,
                nameEdit = cat.name,
                hourlyRate = cat.defaultHourlyRate?.toString().orEmpty(),
                roundingMode = cat.roundingMode,
                roundingDirection = cat.roundingDirection,
                minimumSelection = minSel,
                minHours = minHoursText,
                minChargeAmount = minChargeText,

                globalHourlyRate = globals.defaultHourlyRate,
                globalRoundingMode = globals.defaultRoundingMode,
                globalRoundingDirection = globals.defaultRoundingDirection,
                globalMinBillableSeconds = globals.minBillableSeconds,
                globalMinChargeAmount = globals.minChargeAmount,

                canSave = false,
                validationError = if (editable) null else "Uncategorized cannot be edited."
            )
        }
    }

    fun setName(text: String) {
        _ui.value = _ui.value.copy(nameEdit = text)
        recompute()
    }

    fun setHourlyRate(text: String) {
        _ui.value = _ui.value.copy(hourlyRate = text)
        recompute()
    }

    fun clearHourlyRate() {
        _ui.value = _ui.value.copy(hourlyRate = "")
        recompute()
    }

    fun toggleRoundingMode() {
        val current = _ui.value.roundingMode
        val next = when (current) {
            null -> RoundingMode.EXACT
            RoundingMode.EXACT -> RoundingMode.SIX_MINUTE
            RoundingMode.SIX_MINUTE -> RoundingMode.EXACT
        }
        _ui.value = _ui.value.copy(roundingMode = next)
        recompute()
    }

    fun clearRoundingMode() {
        _ui.value = _ui.value.copy(roundingMode = null)
        recompute()
    }

    fun cycleRoundingDirection() {
        val current = _ui.value.roundingDirection
        val next = when (current) {
            null -> RoundingDirection.UP
            RoundingDirection.UP -> RoundingDirection.NEAREST
            RoundingDirection.NEAREST -> RoundingDirection.DOWN
            RoundingDirection.DOWN -> RoundingDirection.UP
        }
        _ui.value = _ui.value.copy(roundingDirection = next)
        recompute()
    }

    fun clearRoundingDirection() {
        _ui.value = _ui.value.copy(roundingDirection = null)
        recompute()
    }

    fun setMinimumSelection(sel: CategoryMinimumSelection) {
        val next = when (sel) {
            CategoryMinimumSelection.INHERIT -> _ui.value.copy(minimumSelection = sel, minHours = "", minChargeAmount = "")
            CategoryMinimumSelection.NONE -> _ui.value.copy(minimumSelection = sel, minHours = "", minChargeAmount = "")
            CategoryMinimumSelection.TIME -> _ui.value.copy(minimumSelection = sel, minChargeAmount = "")
            CategoryMinimumSelection.CHARGE -> _ui.value.copy(minimumSelection = sel, minHours = "")
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
        val baseline = loaded ?: return
        val current = _ui.value
        if (!current.isEditable || !current.canSave) return

        val newName = current.nameEdit.trim()
        if (newName.isEmpty()) return

        val rate = current.hourlyRate.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
        val (minSeconds, minAmt) = minimumToPersist(current)

        viewModelScope.launch(Dispatchers.IO) {
            // Enforce unique names (case-insensitive).
            val dup = db.categoryDao().getByNameIgnoreCase(newName)
            if (dup != null && dup.id != baseline.id) {
                _ui.value = _ui.value.copy(validationError = "A category with this name already exists.", canSave = false)
                return@launch
            }

            val now = System.currentTimeMillis()
            val updated = baseline.copy(
                name = newName,
                defaultHourlyRate = rate,
                roundingMode = current.roundingMode,
                roundingDirection = current.roundingDirection,
                minBillableSeconds = minSeconds,
                minChargeAmount = minAmt,
                updatedAtMs = now
            )
            db.categoryDao().update(updated)
            loaded = updated
            _ui.value = current.copy(canSave = false, hasUnsavedChanges = false, validationError = null, name = updated.name)
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun discardEdits() {
        val base = loaded ?: return
        val current = _ui.value
        val (minSel, minHoursText, minChargeText) = toMinimumUi(base)
        _ui.value = current.copy(
            name = base.name,
            nameEdit = base.name,
            hourlyRate = base.defaultHourlyRate?.toString().orEmpty(),
            roundingMode = base.roundingMode,
            roundingDirection = base.roundingDirection,
            minimumSelection = minSel,
            minHours = minHoursText,
            minChargeAmount = minChargeText,
            canSave = false,
            hasUnsavedChanges = false,
            validationError = null
        )
    }

    private fun recompute() {
        val baseline = loaded ?: return
        val current = _ui.value

        if (!current.isEditable) {
            _ui.value = current.copy(canSave = false)
            return
        }

        val nameTrimmed = current.nameEdit.trim()
        val nameOk = nameTrimmed.isNotEmpty()
        val nameDup = allCategories.any { it.id != baseline.id && it.name.equals(nameTrimmed, ignoreCase = true) }

        val rateStr = current.hourlyRate.trim()
        val hoursStr = current.minHours.trim()
        val chargeStr = current.minChargeAmount.trim()

        val rateOk = rateStr.isEmpty() || rateStr.toDoubleOrNull() != null
        val hoursOk = hoursStr.isNotEmpty() && isValidHours(hoursStr)
        val chargeOk = chargeStr.isNotEmpty() && chargeStr.toDoubleOrNull() != null

        val error = when {
            !nameOk -> "Name is required."
            nameDup -> "A category with this name already exists."
            !rateOk -> "Hourly rate must be a number."
            current.minimumSelection == CategoryMinimumSelection.TIME && !hoursOk -> "Minimum time is required (hours, up to 2 decimals)."
            current.minimumSelection == CategoryMinimumSelection.CHARGE && !chargeOk -> "Minimum charge is required (CAD)."
            else -> null
        }

        val (minSeconds, minAmt) = minimumToPersist(current)
        val (baseSel, baseHours, baseCharge) = toMinimumUi(baseline)
        val dirty =
            (nameTrimmed != baseline.name) ||
                (rateStr.isEmpty() && baseline.defaultHourlyRate != null) ||
                (rateStr.isNotEmpty() && baseline.defaultHourlyRate?.toString() != rateStr) ||
                (current.roundingMode != baseline.roundingMode) ||
                (current.roundingDirection != baseline.roundingDirection) ||
                (current.minimumSelection != baseSel) ||
                (current.minimumSelection == CategoryMinimumSelection.TIME && hoursStr != baseHours) ||
                (current.minimumSelection == CategoryMinimumSelection.CHARGE && chargeStr != baseCharge) ||
                (baseline.minBillableSeconds != minSeconds) ||
                (baseline.minChargeAmount != minAmt)

        _ui.value = current.copy(
            validationError = error,
            canSave = dirty && error == null,
            hasUnsavedChanges = dirty
        )
    }

    private fun minimumToPersist(current: CategoryDetailUiState): Pair<Long?, Double?> {
        return when (current.minimumSelection) {
            CategoryMinimumSelection.INHERIT -> null to null
            CategoryMinimumSelection.NONE -> 0L to 0.0
            CategoryMinimumSelection.TIME -> {
                val h = current.minHours.trim().toDoubleOrNull() ?: 0.0
                val secs = (h * 3600.0).roundToLong().coerceAtLeast(0L)
                secs to null
            }
            CategoryMinimumSelection.CHARGE -> {
                val amt = current.minChargeAmount.trim().toDoubleOrNull() ?: 0.0
                null to amt
            }
        }
    }

    private fun toMinimumUi(category: CategoryEntity): Triple<CategoryMinimumSelection, String, String> {
        val s = category.minBillableSeconds
        val a = category.minChargeAmount
        return when {
            s == null && a == null -> Triple(CategoryMinimumSelection.INHERIT, "", "")
            (s ?: 0L) == 0L && (a ?: 0.0) == 0.0 -> Triple(CategoryMinimumSelection.NONE, "", "")
            s != null -> {
                val hours = s.toDouble() / 3600.0
                Triple(CategoryMinimumSelection.TIME, String.format(java.util.Locale.CANADA, "%.2f", hours), "")
            }
            else -> Triple(CategoryMinimumSelection.CHARGE, "", a?.toString().orEmpty())
        }
    }

    private fun isValidHours(text: String): Boolean {
        return Regex("^\\d+(\\.\\d{0,2})?$").matches(text)
    }
}

