package press.pelldom.sessionledger.mobile.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import press.pelldom.sessionledger.mobile.billing.SessionState
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity

object WearSessionStatePublisher {
    private const val TAG = "SessionLedgerWear"

    fun publish(context: Context, activeSession: SessionEntity?) {
        val dataClient = Wearable.getDataClient(context)

        val put = PutDataMapRequest.create(WearSessionPaths.SESSION_STATE)
        val map = put.dataMap
        val nowMs = System.currentTimeMillis()

        if (activeSession == null || activeSession.state == SessionState.ENDED) {
            map.putString(WearSessionPaths.KEY_STATE, "NONE")
            map.remove(WearSessionPaths.KEY_START_TIME_MILLIS)
            map.putLong(WearSessionPaths.KEY_ELAPSED_MILLIS, 0L)
            Log.d(TAG, "Publish /session/state: NONE")
        } else {
            val elapsed = computeElapsedMillis(activeSession, nowMs)
            map.putString(WearSessionPaths.KEY_STATE, activeSession.state.name)
            map.putLong(WearSessionPaths.KEY_START_TIME_MILLIS, activeSession.startTimeMillis)
            map.putLong(WearSessionPaths.KEY_ELAPSED_MILLIS, elapsed)
            Log.d(
                TAG,
                "Publish /session/state: ${activeSession.state.name} start=${activeSession.startTimeMillis} elapsed=$elapsed"
            )
        }

        // Force propagation on every state change.
        map.putLong("updatedAtMs", nowMs)

        Tasks.await(dataClient.putDataItem(put.asPutDataRequest().setUrgent()))
    }

    private fun computeElapsedMillis(session: SessionEntity, nowMs: Long): Long {
        val endForElapsedMs = when (session.state) {
            SessionState.RUNNING -> nowMs
            SessionState.PAUSED -> session.lastStateChangeTimeMs
            SessionState.ENDED -> session.endTimeMs ?: session.lastStateChangeTimeMs
        }
        return kotlin.math.max(0L, (endForElapsedMs - session.startTimeMs) - session.pausedTotalMs)
    }
}

