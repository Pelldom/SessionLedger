package press.pelldom.sessionledger.mobile.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import press.pelldom.sessionledger.mobile.billing.SessionState
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity

object WearSessionStatePublisher {
    private const val TAG = "SessionLedgerWear"

    fun publish(context: Context, activeSession: SessionEntity?) {
        try {
            val dataClient = Wearable.getDataClient(context)

            val put = PutDataMapRequest.create(WearSessionPaths.SESSION_STATE)
            val map = put.dataMap
            val nowMs = System.currentTimeMillis()

            if (activeSession == null || activeSession.state == SessionState.ENDED) {
                map.putString(WearSessionPaths.KEY_STATE, "NONE")
                map.remove(WearSessionPaths.KEY_START_TIME_MILLIS)
                map.remove(WearSessionPaths.KEY_CATEGORY_ID)
                map.putLong(WearSessionPaths.KEY_ELAPSED_MILLIS, 0L)
                map.putLong(WearSessionPaths.KEY_TOTAL_PAUSED_MILLIS, 0L)
                map.putLong(WearSessionPaths.KEY_LAST_STATE_CHANGE_TIME_MILLIS, 0L)
                Log.d(TAG, "Publish /session/state: NONE")
            } else {
                val elapsed = computeElapsedMillis(activeSession, nowMs)
                map.putString(WearSessionPaths.KEY_STATE, activeSession.state.name)
                map.putLong(WearSessionPaths.KEY_START_TIME_MILLIS, activeSession.startTimeMillis)
                map.putLong(WearSessionPaths.KEY_ELAPSED_MILLIS, elapsed)
                map.putLong(WearSessionPaths.KEY_TOTAL_PAUSED_MILLIS, activeSession.pausedTotalMs)
                map.putLong(WearSessionPaths.KEY_LAST_STATE_CHANGE_TIME_MILLIS, activeSession.lastStateChangeTimeMs)
                // categoryId is always non-null (has default value), so always include it
                map.putString(WearSessionPaths.KEY_CATEGORY_ID, activeSession.categoryId)
                Log.d(
                    TAG,
                    "Publish /session/state: ${activeSession.state.name} start=${activeSession.startTimeMillis} elapsed=$elapsed categoryId=${activeSession.categoryId}"
                )
            }

            // Force propagation on every state change.
            map.putLong("updatedAtMs", nowMs)

            Tasks.await(dataClient.putDataItem(put.asPutDataRequest().setUrgent()))
        } catch (e: ApiException) {
            if (e.statusCode == ConnectionResult.API_UNAVAILABLE) {
                Log.d(TAG, "Wear API unavailable - skipping session state publish")
                return
            }
            Log.w(TAG, "Wear API error during session state publish: ${e.message}", e)
        } catch (e: Exception) {
            Log.w(TAG, "Error publishing session state to Wear: ${e.message}", e)
        }
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

