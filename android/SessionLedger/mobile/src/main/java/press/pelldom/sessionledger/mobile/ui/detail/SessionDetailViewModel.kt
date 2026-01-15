package press.pelldom.sessionledger.mobile.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import press.pelldom.sessionledger.mobile.billing.SessionState
import press.pelldom.sessionledger.mobile.data.db.AppDatabase
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity

data class SessionDetailUiState(
    val loading: Boolean = true,
    val notFound: Boolean = false,
    val isEditable: Boolean = false,
    val validationError: String? = null,
    val canSave: Boolean = false,
    val startText: String = "",
    val endText: String = "",
    val durationText: String = "00:00",
)

class SessionDetailViewModel(
    app: Application,
    private val sessionId: String
) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)
    private val _uiState = MutableStateFlow(SessionDetailUiState())
    val uiState: StateFlow<SessionDetailUiState> = _uiState

    private val zone = ZoneId.systemDefault()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    private var loadedSession: SessionEntity? = null
    private var startMs: Long? = null
    private var endMs: Long? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val session = db.sessionDao().getById(sessionId)
            if (session == null) {
                _uiState.value = SessionDetailUiState(loading = false, notFound = true)
                return@launch
            }

            loadedSession = session
            startMs = session.startTimeMs
            endMs = session.endTimeMs

            val isEditable = session.state == SessionState.ENDED && session.endTimeMs != null
            _uiState.value = SessionDetailUiState(
                loading = false,
                notFound = false,
                isEditable = isEditable,
                validationError = if (isEditable) null else "Active sessions cannot be edited.",
                canSave = false,
                startText = formatLocal(startMs!!),
                endText = formatLocal(endMs ?: startMs!!),
                durationText = formatDuration(derivedDurationMs(session, startMs!!, endMs))
            )
        }
    }

    fun onStartTextChange(text: String) {
        _uiState.value = _uiState.value.copy(startText = text)
        recompute()
    }

    fun onEndTextChange(text: String) {
        _uiState.value = _uiState.value.copy(endText = text)
        recompute()
    }

    fun save(onSuccess: () -> Unit) {
        val session = loadedSession ?: return
        if (!_uiState.value.canSave) return
        val newStart = startMs ?: return
        val newEnd = endMs ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val nowMs = System.currentTimeMillis()
            val updated = session.copy(
                startTimeMs = newStart,
                endTimeMs = newEnd,
                lastStateChangeTimeMs = newEnd,
                updatedAtMs = nowMs
            )
            db.sessionDao().update(updated)
            onSuccess()
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

        val startParsed = parseLocalOrNull(_uiState.value.startText)
        val endParsed = parseLocalOrNull(_uiState.value.endText)

        startMs = startParsed
        endMs = endParsed

        val error = when {
            startParsed == null || endParsed == null -> "Enter date/time as yyyy-MM-dd HH:mm"
            endParsed < startParsed -> "End time must be after start time."
            else -> null
        }

        val canSave = error == null && startParsed != session.startTimeMs && endParsed != session.endTimeMs
        val durationText = if (startParsed != null && endParsed != null) {
            formatDuration(derivedDurationMs(session, startParsed, endParsed))
        } else {
            _uiState.value.durationText
        }

        _uiState.value = _uiState.value.copy(
            validationError = error,
            canSave = canSave,
            durationText = durationText,
            isEditable = true
        )
    }

    private fun derivedDurationMs(session: SessionEntity, start: Long, end: Long?): Long {
        val safeEnd = end ?: start
        return max(0L, (safeEnd - start) - session.pausedTotalMs)
    }

    private fun parseLocalOrNull(text: String): Long? {
        return try {
            val ldt = LocalDateTime.parse(text.trim(), formatter)
            ldt.atZone(zone).toInstant().toEpochMilli()
        } catch (_: Throwable) {
            null
        }
    }

    private fun formatLocal(epochMs: Long): String {
        return Instant.ofEpochMilli(epochMs).atZone(zone).toLocalDateTime().format(formatter)
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

