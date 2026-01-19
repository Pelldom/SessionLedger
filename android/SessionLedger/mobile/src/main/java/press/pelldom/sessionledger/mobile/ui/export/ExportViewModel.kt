package press.pelldom.sessionledger.mobile.ui.export

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import press.pelldom.sessionledger.mobile.data.db.AppDatabase
import press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity
import press.pelldom.sessionledger.mobile.export.CsvExporter
import press.pelldom.sessionledger.mobile.settings.SettingsRepository
import press.pelldom.sessionledger.mobile.settings.dataStore

data class ExportUiState(
    val loading: Boolean = true,
    val categories: List<CategoryEntity> = emptyList(),

    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,

    val allCategories: Boolean = true,
    val selectedCategoryIds: Set<String> = emptySet(),

    val canSave: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val canExport: Boolean = false,
    val validationError: String? = null,

    val exporting: Boolean = false,
    val lastExportUri: String? = null,
    val statusMessage: String? = null,

    // Post-export bulk archive flow (derived from the sessions included in the last export)
    val postExportArchivePrompt: Boolean = false,
    val suggestedArchiveStart: LocalDate? = null,
    val suggestedArchiveEnd: LocalDate? = null
)

class ExportViewModel(app: Application) : AndroidViewModel(app) {
    companion object {
        private const val TAG = "ExportViewModel"
    }
    private val db = AppDatabase.getInstance(app)
    private val settingsRepo = SettingsRepository(app.dataStore)
    private val zone = ZoneId.systemDefault()

    private val _ui = MutableStateFlow(ExportUiState())
    val ui: StateFlow<ExportUiState> = _ui

    private var baselineStart: LocalDate? = null
    private var baselineEnd: LocalDate? = null
    private var baselineAll: Boolean = true
    private var baselineSelected: Set<String> = emptySet()

    private var lastExportAllCategories: Boolean = true
    private var lastExportSelectedCategoryIds: Set<String> = emptySet()

    init {
        val today = LocalDate.now(zone)
        val defaultStart = today.minusDays(7)
        val defaultEnd = today

        baselineStart = defaultStart
        baselineEnd = defaultEnd
        baselineAll = true
        baselineSelected = emptySet()

        _ui.value = ExportUiState(
            loading = true,
            startDate = defaultStart,
            endDate = defaultEnd,
            allCategories = true,
            selectedCategoryIds = emptySet()
        )

        viewModelScope.launch(Dispatchers.IO) {
            db.categoryDao().observeAllCategories().distinctUntilChanged().collect { cats ->
                _ui.value = _ui.value.copy(loading = false, categories = cats)
                recompute()
            }
        }

        // No MediaStore listing logic; Export screen no longer shows export history.
    }

    fun setStartDate(d: LocalDate) {
        _ui.value = _ui.value.copy(startDate = d)
        recompute()
    }

    fun setEndDate(d: LocalDate) {
        _ui.value = _ui.value.copy(endDate = d)
        recompute()
    }

    fun setAllCategories(all: Boolean) {
        _ui.value = if (all) _ui.value.copy(allCategories = true, selectedCategoryIds = emptySet())
        else _ui.value.copy(allCategories = false)
        recompute()
    }

    fun toggleCategory(id: String) {
        val current = _ui.value.selectedCategoryIds
        val next = if (current.contains(id)) current - id else current + id
        _ui.value = _ui.value.copy(selectedCategoryIds = next)
        recompute()
    }

    fun save() {
        val current = _ui.value
        baselineStart = current.startDate
        baselineEnd = current.endDate
        baselineAll = current.allCategories
        baselineSelected = current.selectedCategoryIds
        _ui.value = current.copy(canSave = false, hasUnsavedChanges = false, statusMessage = "Saved.")
    }

    fun discardEdits() {
        _ui.value = _ui.value.copy(
            startDate = baselineStart,
            endDate = baselineEnd,
            allCategories = baselineAll,
            selectedCategoryIds = baselineSelected,
            canSave = false,
            hasUnsavedChanges = false,
            statusMessage = "Changes discarded."
        )
        recompute()
    }

    fun clearStatus() {
        _ui.value = _ui.value.copy(statusMessage = null)
    }

    suspend fun exportNow(context: Context): Uri {
        val current = _ui.value
        if (!current.canExport) throw IllegalStateException("Export failed: invalid export configuration.")
        if (current.exporting) throw IllegalStateException("Export failed: export already in progress.")

        val start = current.startDate ?: throw IllegalStateException("Export failed: start date missing.")
        val end = current.endDate ?: throw IllegalStateException("Export failed: end date missing.")
        val startMs = start.atStartOfDay(zone).toInstant().toEpochMilli()
        val endExclusiveMs = end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val filters = if (current.allCategories) null else current.selectedCategoryIds
        lastExportAllCategories = current.allCategories
        lastExportSelectedCategoryIds = current.selectedCategoryIds

        _ui.value = _ui.value.copy(exporting = true, statusMessage = null)
        try {
            val uri = kotlinx.coroutines.withContext(Dispatchers.IO) {
                CsvExporter().exportEndedSessions(
                    db = db,
                    settingsRepo = settingsRepo,
                    startFilter = startMs,
                    endFilter = endExclusiveMs,
                    categoryFilter = null,
                    categoryFilters = filters,
                    context = context
                )
            }

            // Compute suggested archive range from the sessions included in the export.
            val exportedSessions = withContext(Dispatchers.IO) {
                db.sessionDao()
                    .getEndedSessionsInRange(startMs = startMs, endMs = endExclusiveMs)
                    .asSequence()
                    .filter { s ->
                        when {
                            filters != null -> filters.contains(s.categoryId)
                            else -> true
                        }
                    }
                    .toList()
            }

            val suggestedStart = exportedSessions.minOfOrNull { it.startTimeMs }
                ?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
                ?: current.startDate
            val suggestedEnd = exportedSessions.maxOfOrNull { requireNotNull(it.endTimeMs) }
                ?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
                ?: current.endDate

            _ui.value = _ui.value.copy(
                lastExportUri = uri.toString(),
                postExportArchivePrompt = true,
                suggestedArchiveStart = suggestedStart,
                suggestedArchiveEnd = suggestedEnd
            )
            return uri
        } finally {
            _ui.value = _ui.value.copy(exporting = false)
        }
    }

    fun dismissPostExportArchivePrompt() {
        _ui.value = _ui.value.copy(postExportArchivePrompt = false)
    }

    suspend fun archiveLastExportedSessions(startDate: LocalDate, endDate: LocalDate): Int {
        val startMs = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val endExclusiveMs = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val nowMs = System.currentTimeMillis()

        val count = withContext(Dispatchers.IO) {
            if (lastExportAllCategories) {
                db.sessionDao().archiveEndedSessionsInRange(
                    startMs = startMs,
                    endExclusiveMs = endExclusiveMs,
                    archivedAtMillis = nowMs
                )
            } else {
                db.sessionDao().archiveEndedSessionsInRangeForCategories(
                    startMs = startMs,
                    endExclusiveMs = endExclusiveMs,
                    categoryIds = lastExportSelectedCategoryIds.toList(),
                    archivedAtMillis = nowMs
                )
            }
        }

        // Clear the prompt once action is taken.
        _ui.value = _ui.value.copy(postExportArchivePrompt = false)
        return count
    }

    fun shareLastExport(context: Context) {
        val uriString = _ui.value.lastExportUri ?: return
        val uri = Uri.parse(uriString)

        viewModelScope.launch(Dispatchers.Main) {
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share SessionLedger CSV"))
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(statusMessage = "Share failed: ${t.message ?: "Unknown error"}")
            }
        }
    }

    // No MediaStore history query; Export screen now provides a direct folder access link instead.

    private fun recompute() {
        val current = _ui.value
        val start = current.startDate
        val end = current.endDate
        val error = when {
            start == null || end == null -> "Start and end dates are required."
            end.isBefore(start) -> "End date must be on or after start date."
            !current.allCategories && current.selectedCategoryIds.isEmpty() -> "Select at least one category (or choose All categories)."
            else -> null
        }

        val dirty =
            current.startDate != baselineStart ||
                current.endDate != baselineEnd ||
                current.allCategories != baselineAll ||
                current.selectedCategoryIds != baselineSelected

        _ui.value = current.copy(
            validationError = error,
            canSave = dirty && error == null,
            hasUnsavedChanges = dirty,
            canExport = error == null
        )
    }
}

