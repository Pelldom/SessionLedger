package press.pelldom.sessionledger.mobile.ui.session

import android.content.Context
import kotlin.math.max
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import press.pelldom.sessionledger.mobile.billing.SessionState
import press.pelldom.sessionledger.mobile.data.db.dao.SessionDao
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity
import press.pelldom.sessionledger.mobile.wear.WearSessionStatePublisher

/**
 * Single source of truth for phone session state transitions.
 *
 * - Enforces at most one active session.
 * - Persists via Room (SessionDao).
 * - Publishes state changes to Wear Data Layer.
 */
class SessionRepository(
    private val sessionDao: SessionDao,
    private val appContext: Context
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var runningPublishJob: Job? = null

    suspend fun startSession() {
        val existing = sessionDao.getActiveSession()
        if (existing != null) return

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
        WearSessionStatePublisher.publish(appContext, session)
        startRunningPublisher(session)
    }

    suspend fun pauseSession() {
        val s = sessionDao.getActiveSession() ?: return
        if (s.state != SessionState.RUNNING) return

        val nowMs = System.currentTimeMillis()
        val updated = s.copy(
            state = SessionState.PAUSED,
            lastStateChangeTimeMs = nowMs,
            updatedAtMs = nowMs
        )
        sessionDao.update(updated)
        WearSessionStatePublisher.publish(appContext, updated)
        stopRunningPublisher()
    }

    suspend fun resumeSession() {
        val s = sessionDao.getActiveSession() ?: return
        if (s.state != SessionState.PAUSED) return

        val nowMs = System.currentTimeMillis()
        val pausedDelta = max(0L, nowMs - s.lastStateChangeTimeMs)

        val updated = s.copy(
            state = SessionState.RUNNING,
            pausedTotalMs = s.pausedTotalMs + pausedDelta,
            lastStateChangeTimeMs = nowMs,
            updatedAtMs = nowMs
        )
        sessionDao.update(updated)
        WearSessionStatePublisher.publish(appContext, updated)
        startRunningPublisher(updated)
    }

    suspend fun endSession() {
        val s = sessionDao.getActiveSession() ?: return

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
        WearSessionStatePublisher.publish(appContext, null)
        stopRunningPublisher()
    }

    private fun startRunningPublisher(session: SessionEntity) {
        stopRunningPublisher()
        if (session.state != SessionState.RUNNING) return

        // Lightweight periodic updates while RUNNING so watch can converge to phone truth.
        runningPublishJob = scope.launch {
            while (true) {
                val current = sessionDao.getActiveSession()
                if (current == null || current.state != SessionState.RUNNING) break
                WearSessionStatePublisher.publish(appContext, current)
                delay(1000L)
            }
        }
    }

    private fun stopRunningPublisher() {
        runningPublishJob?.cancel()
        runningPublishJob = null
    }
}

