package press.pelldom.sessionledger.wear.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import press.pelldom.sessionledger.wear.datalayer.WearSessionPaths

enum class WatchSessionState { NONE, RUNNING, PAUSED }

data class WatchSessionUiState(
    val state: WatchSessionState = WatchSessionState.NONE,
    val startTimeMillis: Long? = null,
    val elapsedMillis: Long = 0L,
    val displayedElapsedMillis: Long = 0L,
)

class SessionControlViewModel(app: Application) : AndroidViewModel(app), DataClient.OnDataChangedListener {
    private val tag = "SessionLedgerWear"

    private val dataClient = Wearable.getDataClient(app)
    private val messageClient = Wearable.getMessageClient(app)
    private val nodeClient = Wearable.getNodeClient(app)

    private val _uiState = MutableStateFlow(WatchSessionUiState())
    val uiState: StateFlow<WatchSessionUiState> = _uiState
    private var tickerJob: Job? = null

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
            if (item.uri.path != WearSessionPaths.SESSION_STATE) continue
            applyDataItem(item)
        }
    }

    override fun onCleared() {
        dataClient.removeListener(this)
        tickerJob?.cancel()
        super.onCleared()
    }

    private fun loadInitialState() {
        viewModelScope.launch(Dispatchers.IO) {
            val items = Tasks.await(dataClient.dataItems)
            try {
                val found = items.firstOrNull { it.uri.path == WearSessionPaths.SESSION_STATE }
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
        val elapsed = if (map.containsKey(WearSessionPaths.KEY_ELAPSED_MILLIS)) {
            map.getLong(WearSessionPaths.KEY_ELAPSED_MILLIS)
        } else {
            0L
        }

        val state = when (rawState) {
            "RUNNING" -> WatchSessionState.RUNNING
            "PAUSED" -> WatchSessionState.PAUSED
            else -> WatchSessionState.NONE
        }

        Log.d(tag, "Received /session/state: $rawState start=$start elapsed=$elapsed")

        _uiState.value = WatchSessionUiState(
            state = state,
            startTimeMillis = start,
            elapsedMillis = elapsed,
            displayedElapsedMillis = elapsed
        )

        // Drive a lightweight 1-second ticker for smooth UI only when RUNNING.
        tickerJob?.cancel()
        if (state == WatchSessionState.RUNNING) {
            tickerJob = viewModelScope.launch(Dispatchers.Main.immediate) {
                while (true) {
                    delay(1000L)
                    val current = _uiState.value
                    if (current.state != WatchSessionState.RUNNING) break
                    _uiState.value = current.copy(
                        displayedElapsedMillis = current.displayedElapsedMillis + 1000L
                    )
                }
            }
        }
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

