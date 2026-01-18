package press.pelldom.sessionledger.mobile.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.UUID
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import press.pelldom.sessionledger.mobile.billing.SessionState
import press.pelldom.sessionledger.mobile.data.db.AppDatabase
import press.pelldom.sessionledger.mobile.data.db.DefaultCategory
import press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity

data class SessionDetailUiState(
    val loading: Boolean = true,
    val notFound: Boolean = false,
    val isEditable: Boolean = false,
    val validationError: String? = null,
    val canSave: Boolean = false,
    val startText: String = "",
    val endText: String = "",
    val startMillis: Long? = null,
    val endMillis: Long? = null,
    val durationText: String = "00:00",
    val categoryId: String = DefaultCategory.UNCATEGORIZED_ID,
    val categoryName: String = DefaultCategory.UNCATEGORIZED_NAME,
    val categories: List<CategoryEntity> = emptyList(),
)

class SessionDetailViewModel(
    app: Application,
    private val sessionId: String
) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)
    private val _uiState = MutableStateFlow(SessionDetailUiState())
    val uiState: StateFlow<SessionDetailUiState> = _uiState

    private val zone = ZoneId.systemDefault()

    private var loadedSession: SessionEntity? = null
    private var baselineStartMs: Long? = null
    private var baselineEndMs: Long? = null
    private var startMs: Long? = null
    private var endMs: Long? = null
    private var baselineCategoryId: String? = null
    private var categoryId: String = DefaultCategory.UNCATEGORIZED_ID

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val session = db.sessionDao().getById(sessionId)
            if (session == null) {
                _uiState.value = SessionDetailUiState(loading = false, notFound = true)
                return@launch
            }

            val categories = db.categoryDao().getAll()

            loadedSession = session
            baselineStartMs = session.startTimeMs
            baselineEndMs = session.endTimeMs
            startMs = baselineStartMs
            endMs = baselineEndMs
            baselineCategoryId = session.categoryId
            categoryId = session.categoryId

            val isEditable = session.state == SessionState.ENDED && session.endTimeMs != null
            val categoryName = categories.firstOrNull { it.id == session.categoryId }?.name
                ?: categories.firstOrNull { it.id == DefaultCategory.UNCATEGORIZED_ID }?.name
                ?: DefaultCategory.UNCATEGORIZED_NAME
            _uiState.value = SessionDetailUiState(
                loading = false,
                notFound = false,
                isEditable = isEditable,
                validationError = if (isEditable) null else "Active sessions cannot be edited.",
                canSave = false,
                startText = formatLocal(startMs!!),
                endText = formatLocal(endMs ?: startMs!!),
                startMillis = startMs,
                endMillis = endMs,
                durationText = formatDuration(derivedDurationMs(session, startMs!!, endMs)),
                categoryId = session.categoryId,
                categoryName = categoryName,
                categories = categories
            )

            // Keep categories up to date (e.g. after add/rename/delete elsewhere).
            viewModelScope.launch(Dispatchers.IO) {
                db.categoryDao()
                    .observeAllCategories()
                    .distinctUntilChanged()
                    .collect { updatedCats ->
                        val name = updatedCats.firstOrNull { it.id == categoryId }?.name
                            ?: updatedCats.firstOrNull { it.id == DefaultCategory.UNCATEGORIZED_ID }?.name
                            ?: DefaultCategory.UNCATEGORIZED_NAME
                        _uiState.value = _uiState.value.copy(
                            categories = updatedCats,
                            categoryName = name
                        )
                    }
            }
        }
    }

    fun setStartMillis(epochMs: Long) {
        startMs = epochMs
        recompute()
    }

    fun setEndMillis(epochMs: Long) {
        endMs = epochMs
        recompute()
    }

    fun discardEdits() {
        startMs = baselineStartMs
        endMs = baselineEndMs
        categoryId = baselineCategoryId ?: DefaultCategory.UNCATEGORIZED_ID
        recompute()
    }

    fun setCategoryId(id: String) {
        categoryId = id
        recompute()
    }

    fun createCategoryAndSelect(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val current = _uiState.value.categories
        if (current.any { it.name.equals(trimmed, ignoreCase = true) }) return

        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val entity = CategoryEntity(
                id = UUID.randomUUID().toString(),
                name = trimmed,
                isDefault = false,
                archived = false,
                createdAtMs = now,
                updatedAtMs = now
            )
            db.categoryDao().insert(entity)

            val updatedList = (current + entity).sortedBy { it.name.lowercase() }
            categoryId = entity.id

            _uiState.value = _uiState.value.copy(
                categories = updatedList
            )
            recompute()
        }
    }

    fun save(onSuccess: () -> Unit) {
        val session = loadedSession ?: return
        if (!_uiState.value.canSave) return
        val newStart = startMs ?: return
        val newEnd = endMs ?: return
        val newCategoryId = categoryId

        viewModelScope.launch(Dispatchers.IO) {
            val nowMs = System.currentTimeMillis()
            val updated = session.copy(
                startTimeMs = newStart,
                endTimeMs = newEnd,
                lastStateChangeTimeMs = newEnd,
                categoryId = newCategoryId,
                updatedAtMs = nowMs
            )
            db.sessionDao().update(updated)
            loadedSession = updated
            baselineStartMs = newStart
            baselineEndMs = newEnd
            startMs = newStart
            endMs = newEnd
            baselineCategoryId = newCategoryId
            categoryId = newCategoryId
            val categories = _uiState.value.categories
            val categoryName = categories.firstOrNull { it.id == newCategoryId }?.name
                ?: categories.firstOrNull { it.id == DefaultCategory.UNCATEGORIZED_ID }?.name
                ?: DefaultCategory.UNCATEGORIZED_NAME
            _uiState.value = _uiState.value.copy(
                startText = formatLocal(newStart),
                endText = formatLocal(newEnd),
                startMillis = newStart,
                endMillis = newEnd,
                durationText = formatDuration(derivedDurationMs(updated, newStart, newEnd)),
                categoryId = newCategoryId,
                categoryName = categoryName,
                validationError = null,
                canSave = false
            )
            withContext(Dispatchers.Main) { onSuccess() }
        }
    }

    private fun recompute() {
        val session = loadedSession ?: return
        if (session.state != SessionState.ENDED) {
            _uiState.value = _uiState.value.copy(
                validationError = "Active sessions cannot be edited.",
                canSave = false
            )
            return
        }

        val start = startMs
        val end = endMs

        val error = if (start == null || end == null) {
            "Missing start/end time."
        } else if (end < start) {
            "End time must be after start time."
        } else {
            null
        }

        val durationText = if (start != null && end != null) {
            formatDuration(derivedDurationMs(session, start, end))
        } else _uiState.value.durationText

        val baseStart = baselineStartMs
        val baseEnd = baselineEndMs
        val isDirty = start != null && end != null && baseStart != null && baseEnd != null &&
            (start != baseStart || end != baseEnd)
        val categoryDirty = (baselineCategoryId != null && categoryId != baselineCategoryId)
        val canSave = error == null && (isDirty || categoryDirty)

        val categories = _uiState.value.categories
        val categoryName = categories.firstOrNull { it.id == categoryId }?.name
            ?: categories.firstOrNull { it.id == DefaultCategory.UNCATEGORIZED_ID }?.name
            ?: DefaultCategory.UNCATEGORIZED_NAME

        _uiState.value = _uiState.value.copy(
            validationError = error,
            canSave = canSave,
            durationText = durationText,
            isEditable = true,
            startText = start?.let { formatLocal(it) } ?: _uiState.value.startText,
            endText = end?.let { formatLocal(it) } ?: _uiState.value.endText,
            startMillis = start,
            endMillis = end,
            categoryId = categoryId,
            categoryName = categoryName
        )
    }

    private fun derivedDurationMs(session: SessionEntity, start: Long, end: Long?): Long {
        val safeEnd = end ?: start
        return max(0L, (safeEnd - start) - session.pausedTotalMs)
    }

    private fun formatLocal(epochMs: Long): String {
        val zdt = Instant.ofEpochMilli(epochMs).atZone(zone)
        val df = java.text.DateFormat.getDateTimeInstance(
            java.text.DateFormat.SHORT,
            java.text.DateFormat.SHORT
        )
        return df.format(Date.from(zdt.toInstant()))
    }

    private fun formatDuration(ms: Long): String {
        val totalSeconds = max(0L, ms / 1000L)
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return if (hours > 0L) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}

