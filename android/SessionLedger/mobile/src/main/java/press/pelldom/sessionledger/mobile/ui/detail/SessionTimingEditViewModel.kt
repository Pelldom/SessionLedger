package press.pelldom.sessionledger.mobile.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import press.pelldom.sessionledger.mobile.billing.SessionState
import press.pelldom.sessionledger.mobile.data.db.AppDatabase
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity

data class SessionTimingEditUiState(
    val loading: Boolean = true,
    val notFound: Boolean = false,
    val isEditable: Boolean = false,
    val validationError: String? = null,
    val canSave: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val startText: String = "",
    val endText: String = "",
    val startMillis: Long? = null,
    val endMillis: Long? = null,
    val durationText: String = ""
)

class SessionTimingEditViewModel(
    app: Application,
    private val sessionId: String
) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)
    private val zone = ZoneId.systemDefault()

    private val _ui = MutableStateFlow(SessionTimingEditUiState())
    val ui: StateFlow<SessionTimingEditUiState> = _ui

    private var loaded: SessionEntity? = null
    private var baselineStartMs: Long? = null
    private var baselineEndMs: Long? = null
    private var startMs: Long? = null
    private var endMs: Long? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val s = db.sessionDao().getById(sessionId)
            if (s == null) {
                _ui.value = SessionTimingEditUiState(loading = false, notFound = true)
                return@launch
            }

            loaded = s
            baselineStartMs = s.startTimeMs
            baselineEndMs = s.endTimeMs
            startMs = baselineStartMs
            endMs = baselineEndMs

            val editable = s.state == SessionState.ENDED && s.endTimeMs != null
            _ui.value = SessionTimingEditUiState(
                loading = false,
                notFound = false,
                isEditable = editable,
                validationError = if (editable) null else "Active sessions cannot be edited.",
                canSave = false,
                hasUnsavedChanges = false,
                startText = formatLocal(startMs!!),
                endText = formatLocal(endMs ?: startMs!!),
                startMillis = startMs,
                endMillis = endMs,
                durationText = formatDuration(derivedDurationMs(s, startMs!!, endMs))
            )

            recompute()
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
        recompute()
    }

    fun save(onDone: () -> Unit) {
        val s = loaded ?: return
        val current = _ui.value
        if (!current.isEditable || !current.canSave) return
        val newStart = startMs ?: return
        val newEnd = endMs ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val nowMs = System.currentTimeMillis()
            val updated = s.copy(
                startTimeMs = newStart,
                endTimeMs = newEnd,
                lastStateChangeTimeMs = newEnd,
                updatedAtMs = nowMs
            )
            db.sessionDao().update(updated)
            loaded = updated
            baselineStartMs = newStart
            baselineEndMs = newEnd
            startMs = newStart
            endMs = newEnd
            _ui.value = _ui.value.copy(
                startText = formatLocal(newStart),
                endText = formatLocal(newEnd),
                startMillis = newStart,
                endMillis = newEnd,
                durationText = formatDuration(derivedDurationMs(updated, newStart, newEnd)),
                validationError = null,
                canSave = false,
                hasUnsavedChanges = false
            )
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    private fun recompute() {
        val s = loaded ?: return
        if (s.state != SessionState.ENDED) {
            _ui.value = _ui.value.copy(
                validationError = "Active sessions cannot be edited.",
                canSave = false,
                hasUnsavedChanges = false
            )
            return
        }

        val start = startMs
        val end = endMs
        val error = if (start == null || end == null) {
            "Missing start/end time."
        } else if (end < start) {
            "End time must be after start time."
        } else null

        val baseStart = baselineStartMs
        val baseEnd = baselineEndMs
        val dirty = start != null && end != null && baseStart != null && baseEnd != null &&
            (start != baseStart || end != baseEnd)

        val durationText = if (start != null && end != null) {
            formatDuration(derivedDurationMs(s, start, end))
        } else _ui.value.durationText

        _ui.value = _ui.value.copy(
            validationError = error,
            canSave = dirty && error == null,
            hasUnsavedChanges = dirty,
            durationText = durationText,
            startText = start?.let { formatLocal(it) } ?: _ui.value.startText,
            endText = end?.let { formatLocal(it) } ?: _ui.value.endText,
            startMillis = start,
            endMillis = end
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

