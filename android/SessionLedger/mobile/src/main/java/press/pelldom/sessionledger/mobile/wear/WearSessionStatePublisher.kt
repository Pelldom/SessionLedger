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

        if (activeSession == null || activeSession.state == SessionState.ENDED) {
            map.putString(WearSessionPaths.KEY_STATE, "NONE")
            map.remove(WearSessionPaths.KEY_START_TIME_MILLIS)
            Log.d(TAG, "Publish /session/state: NONE")
        } else {
            map.putString(WearSessionPaths.KEY_STATE, activeSession.state.name)
            map.putLong(WearSessionPaths.KEY_START_TIME_MILLIS, activeSession.startTimeMillis)
            Log.d(TAG, "Publish /session/state: ${activeSession.state.name} start=${activeSession.startTimeMillis}")
        }

        // Force propagation on every state change.
        map.putLong("updatedAtMs", System.currentTimeMillis())

        Tasks.await(dataClient.putDataItem(put.asPutDataRequest().setUrgent()))
    }
}

