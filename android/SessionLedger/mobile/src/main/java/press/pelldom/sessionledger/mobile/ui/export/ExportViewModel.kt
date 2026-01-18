package press.pelldom.sessionledger.mobile.ui.export

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

data class ExportHistoryItem(
    val name: String,
    val uriString: String,
    val timestampText: String
)

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
    val history: List<ExportHistoryItem> = emptyList(),
    val statusMessage: String? = null
)

class ExportViewModel(app: Application) : AndroidViewModel(app) {
    companion object {
        private const val TAG = "ExportViewModel"
    }
    private val db = AppDatabase.getInstance(app)
    private val settingsRepo = SettingsRepository(app.dataStore)
    private val zone = ZoneId.systemDefault()
    private val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    private val _ui = MutableStateFlow(ExportUiState())
    val ui: StateFlow<ExportUiState> = _ui

    private var baselineStart: LocalDate? = null
    private var baselineEnd: LocalDate? = null
    private var baselineAll: Boolean = true
    private var baselineSelected: Set<String> = emptySet()

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

        viewModelScope.launch(Dispatchers.IO) {
            refreshHistory()
        }
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

            _ui.value = _ui.value.copy(lastExportUri = uri.toString())
            refreshHistory()
            return uri
        } finally {
            _ui.value = _ui.value.copy(exporting = false)
        }
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

    fun openExport(context: Context, uriString: String) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val uri = Uri.parse(uriString)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, CsvExporter.EXPORT_MIME_TYPE)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(intent)
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(statusMessage = "Unable to open export: ${t.message ?: "Unknown error"}")
            }
        }
    }

    private fun refreshHistory() {
        val resolver = getApplication<Application>().contentResolver
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_ADDED
        )

        // Do NOT filter by RELATIVE_PATH; it is not consistent across devices/OS versions.
        // Instead, match by MIME type and our known export filename prefix.
        val selection = "${MediaStore.MediaColumns.MIME_TYPE} = ? AND ${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf(
            CsvExporter.EXPORT_MIME_TYPE,
            "SessionLedger_Export_%"
        )

        val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        val items = mutableListOf<ExportHistoryItem>()

        resolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = cursor.getString(nameCol) ?: ""
                val dateAddedSec = cursor.getLong(dateCol)
                val timestampText = try {
                    Instant.ofEpochSecond(dateAddedSec).atZone(zone).format(dateFmt)
                } catch (_: Throwable) {
                    ""
                }
                val uri = ContentUris.withAppendedId(collection, id)
                items.add(
                    ExportHistoryItem(
                        name = name,
                        uriString = uri.toString(),
                        timestampText = timestampText
                    )
                )
            }
        }

        Log.d("SL_EXPORT", "Query history count=${items.size}")
        _ui.value = _ui.value.copy(history = items)
    }

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

