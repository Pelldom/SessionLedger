package press.pelldom.sessionledger.wear.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import press.pelldom.sessionledger.wear.datalayer.WearSessionPaths

enum class WatchSessionState { NONE, RUNNING, PAUSED }

data class WatchSessionUiState(
    val state: WatchSessionState = WatchSessionState.NONE,
    val startTimeMillis: Long? = null,
)

class SessionControlViewModel(app: Application) : AndroidViewModel(app), DataClient.OnDataChangedListener {

    private val dataClient = Wearable.getDataClient(app)
    private val messageClient = Wearable.getMessageClient(app)
    private val nodeClient = Wearable.getNodeClient(app)

    private val _uiState = MutableStateFlow(WatchSessionUiState())
    val uiState: StateFlow<WatchSessionUiState> = _uiState

    init {
        dataClient.addListener(this)
        loadInitialState()
    }

    fun start() = sendCommand(WearSessionPaths.START)
    fun pause() = sendCommand(WearSessionPaths.PAUSE)
    fun resume() = sendCommand(WearSessionPaths.RESUME)
    fun end() = sendCommand(WearSessionPaths.END)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type != DataEvent.TYPE_CHANGED) continue
            val item = event.dataItem
            if (item.uri.path != WearSessionPaths.ACTIVE_SESSION_STATE) continue
            applyDataItem(item)
        }
    }

    override fun onCleared() {
        dataClient.removeListener(this)
        super.onCleared()
    }

    private fun loadInitialState() {
        viewModelScope.launch(Dispatchers.IO) {
            val items = Tasks.await(dataClient.dataItems)
            try {
                val found = items.firstOrNull { it.uri.path == WearSessionPaths.ACTIVE_SESSION_STATE }
                if (found != null) applyDataItem(found)
            } finally {
                items.release()
            }
        }
    }

    private fun applyDataItem(item: com.google.android.gms.wearable.DataItem) {
        val map = DataMapItem.fromDataItem(item).dataMap
        val rawState = map.getString(WearSessionPaths.KEY_STATE, "NONE")
        val start = if (map.containsKey(WearSessionPaths.KEY_START_TIME_MILLIS)) {
            map.getLong(WearSessionPaths.KEY_START_TIME_MILLIS)
        } else {
            null
        }

        val state = when (rawState) {
            "RUNNING" -> WatchSessionState.RUNNING
            "PAUSED" -> WatchSessionState.PAUSED
            else -> WatchSessionState.NONE
        }

        _uiState.value = WatchSessionUiState(state = state, startTimeMillis = start)
    }

    private fun sendCommand(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val nodes = Tasks.await(nodeClient.connectedNodes)
            for (node in nodes) {
                Tasks.await(messageClient.sendMessage(node.id, path, ByteArray(0)))
            }
        }
    }
}

