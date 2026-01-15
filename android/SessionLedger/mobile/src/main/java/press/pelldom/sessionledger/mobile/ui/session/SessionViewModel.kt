package press.pelldom.sessionledger.mobile.ui.session

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.max
import java.util.UUID
import androidx.lifecycle.ViewModel
import press.pelldom.sessionledger.mobile.billing.SessionState
import press.pelldom.sessionledger.mobile.data.db.dao.SessionDao
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity
import press.pelldom.sessionledger.mobile.wear.WearSessionStatePublisher

class SessionViewModel(
    private val sessionDao: SessionDao,
    private val appContext: Context? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val _activeSession = MutableStateFlow<SessionEntity?>(null)
    val activeSession: StateFlow<SessionEntity?> = _activeSession

    init {
        scope.launch {
            sessionDao.observeActiveSession()
                .distinctUntilChanged()
                .collect {
                    _activeSession.value = it
                    appContext?.let { ctx ->
                        // Publish state for watch UI whenever phone state changes.
                        WearSessionStatePublisher.publish(ctx, it)
                    }
                }
        }
    }

    fun startSession() {
        scope.launch {
            val existing = sessionDao.getActiveSession()
            if (existing != null) return@launch

            val nowMs = System.currentTimeMillis()
            val session = SessionEntity(
                id = UUID.randomUUID().toString(),
                startTimeMs = nowMs,
                endTimeMs = null,
                state = SessionState.RUNNING,
                pausedTotalMs = 0L,
                lastStateChangeTimeMs = nowMs,
                categoryId = null,
                notes = null,
                hourlyRateOverride = null,
                roundingModeOverride = null,
                roundingDirectionOverride = null,
                minBillableSecondsOverride = null,
                minChargeAmountOverride = null,
                createdOnDevice = "phone",
                updatedAtMs = nowMs
            )
            sessionDao.insert(session)
            appContext?.let { WearSessionStatePublisher.publish(it, session) }
        }
    }

    fun pauseSession() {
        scope.launch {
            val s = sessionDao.getActiveSession() ?: return@launch
            if (s.state != SessionState.RUNNING) return@launch

            val nowMs = System.currentTimeMillis()
            val updated = s.copy(
                state = SessionState.PAUSED,
                lastStateChangeTimeMs = nowMs,
                updatedAtMs = nowMs
            )
            sessionDao.update(updated)
            appContext?.let { WearSessionStatePublisher.publish(it, updated) }
        }
    }

    fun resumeSession() {
        scope.launch {
            val s = sessionDao.getActiveSession() ?: return@launch
            if (s.state != SessionState.PAUSED) return@launch

            val nowMs = System.currentTimeMillis()
            val pausedDelta = max(0L, nowMs - s.lastStateChangeTimeMs)

            val updated = s.copy(
                state = SessionState.RUNNING,
                pausedTotalMs = s.pausedTotalMs + pausedDelta,
                lastStateChangeTimeMs = nowMs,
                updatedAtMs = nowMs
            )
            sessionDao.update(updated)
            appContext?.let { WearSessionStatePublisher.publish(it, updated) }
        }
    }

    fun endSession() {
        scope.launch {
            val s = sessionDao.getActiveSession() ?: return@launch

            val nowMs = System.currentTimeMillis()
            val finalPausedTotalMs = if (s.state == SessionState.PAUSED) {
                val pausedDelta = max(0L, nowMs - s.lastStateChangeTimeMs)
                s.pausedTotalMs + pausedDelta
            } else {
                s.pausedTotalMs
            }

            val updated = s.copy(
                state = SessionState.ENDED,
                endTimeMs = nowMs,
                pausedTotalMs = finalPausedTotalMs,
                lastStateChangeTimeMs = nowMs,
                updatedAtMs = nowMs
            )
            sessionDao.update(updated)
            appContext?.let { WearSessionStatePublisher.publish(it, null) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.coroutineContext.cancel()
    }
}

