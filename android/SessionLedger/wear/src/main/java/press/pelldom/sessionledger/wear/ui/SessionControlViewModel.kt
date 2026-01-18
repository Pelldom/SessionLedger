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

data class WatchCategory(
    val id: String,
    val name: String
)

data class WatchSessionUiState(
    val state: WatchSessionState = WatchSessionState.NONE,
    val startTimeMillis: Long? = null,
    val elapsedMillis: Long = 0L,
    val displayedElapsedMillis: Long = 0L,
    val categories: List<WatchCategory> = listOf(
        WatchCategory(
            id = "00000000-0000-0000-0000-000000000000",
            name = "Uncategorized"
        )
    ),
    val showCategoryPicker: Boolean = false,
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

    fun onStartPressed() {
        val cats = _uiState.value.categories
        if (cats.size <= 1) {
            sendStartWithCategory(cats.firstOrNull()?.id)
        } else {
            _uiState.value = _uiState.value.copy(showCategoryPicker = true)
        }
    }

    fun dismissCategoryPicker() {
        _uiState.value = _uiState.value.copy(showCategoryPicker = false)
    }

    fun startWithCategory(categoryId: String) {
        _uiState.value = _uiState.value.copy(showCategoryPicker = false)
        sendStartWithCategory(categoryId)
    }

    fun pause() = sendCommand(WearSessionPaths.PAUSE)
    fun resume() = sendCommand(WearSessionPaths.RESUME)
    fun end() = sendCommand(WearSessionPaths.END)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type != DataEvent.TYPE_CHANGED) continue
            val item = event.dataItem
            when (item.uri.path) {
                WearSessionPaths.SESSION_STATE -> applySessionStateDataItem(item)
                WearSessionPaths.CATEGORIES -> applyCategoriesDataItem(item)
            }
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
                val sessionState = items.firstOrNull { it.uri.path == WearSessionPaths.SESSION_STATE }
                if (sessionState != null) applySessionStateDataItem(sessionState)

                val categories = items.firstOrNull { it.uri.path == WearSessionPaths.CATEGORIES }
                if (categories != null) applyCategoriesDataItem(categories)
            } finally {
                items.release()
            }
        }
    }

    private fun applySessionStateDataItem(item: com.google.android.gms.wearable.DataItem) {
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
            displayedElapsedMillis = elapsed,
            categories = _uiState.value.categories,
            showCategoryPicker = _uiState.value.showCategoryPicker
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

    private fun applyCategoriesDataItem(item: com.google.android.gms.wearable.DataItem) {
        val map = DataMapItem.fromDataItem(item).dataMap
        val ids = map.getStringArrayList(WearSessionPaths.KEY_CATEGORY_IDS) ?: arrayListOf()
        val names = map.getStringArrayList(WearSessionPaths.KEY_CATEGORY_NAMES) ?: arrayListOf()

        val list = ids.zip(names).map { (id, name) -> WatchCategory(id = id, name = name) }
        val fallback = if (list.isNotEmpty()) list else _uiState.value.categories
        Log.d(tag, "Received /categories: ${fallback.size} items")

        _uiState.value = _uiState.value.copy(categories = fallback)
    }

    private fun sendStartWithCategory(categoryId: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val nodes = Tasks.await(nodeClient.connectedNodes)
            val payload = categoryId?.toByteArray(Charsets.UTF_8) ?: ByteArray(0)
            for (node in nodes) {
                Tasks.await(messageClient.sendMessage(node.id, WearSessionPaths.START, payload))
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

