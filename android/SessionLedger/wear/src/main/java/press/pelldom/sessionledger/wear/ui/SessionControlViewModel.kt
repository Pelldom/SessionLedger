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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import press.pelldom.sessionledger.wear.comms.WearCommandSender
import press.pelldom.sessionledger.wear.datalayer.WearSessionPaths

enum class WatchSessionState { NONE, RUNNING, PAUSED }

data class WatchCategory(
    val id: String,
    val name: String
)

data class WatchSessionUiState(
    val state: WatchSessionState = WatchSessionState.NONE,
    val startTimeMillis: Long? = null,
    // Phone-authoritative session timing fields (timestamps only; used to compute elapsed purely).
    val totalPausedDurationMillis: Long = 0L,
    val pauseStartedAtMillis: Long? = null,
    val isPaused: Boolean = false,
    // If we ever end up paused without a pauseStartedAtMillis, freeze elapsed by using this fixed end time.
    val pauseFallbackEndMillis: Long? = null,
    // Local reconciliation to avoid a resume "jump" when phone publishes RUNNING before paused-total is updated.
    val pausedTotalAtPauseStart: Long = 0L,
    val localPauseCarryMillis: Long = 0L,
    // Recomposition trigger only; elapsed must never depend on this.
    val tickMillis: Long = 0L,
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

    private val _uiState = MutableStateFlow(WatchSessionUiState())
    val uiState: StateFlow<WatchSessionUiState> = _uiState
    private var tickerJob: Job? = null

    // Deprecated: hourly haptics are out of scope for this bugfix; keep the flow so UI compiles,
    // but do not emit from background jobs.
    private val _hourlyHapticEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val hourlyHapticEvents: SharedFlow<Unit> = _hourlyHapticEvents

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

    fun pause() {
        viewModelScope.launch(Dispatchers.IO) {
            WearCommandSender.sendPause(getApplication())
        }
    }
    
    fun resume() {
        viewModelScope.launch(Dispatchers.IO) {
            WearCommandSender.sendResume(getApplication())
        }
    }
    
    fun end() {
        viewModelScope.launch(Dispatchers.IO) {
            WearCommandSender.sendStop(getApplication())
        }
    }

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
        val totalPaused = if (map.containsKey(WearSessionPaths.KEY_TOTAL_PAUSED_MILLIS)) {
            map.getLong(WearSessionPaths.KEY_TOTAL_PAUSED_MILLIS)
        } else 0L
        val lastStateChange = if (map.containsKey(WearSessionPaths.KEY_LAST_STATE_CHANGE_TIME_MILLIS)) {
            map.getLong(WearSessionPaths.KEY_LAST_STATE_CHANGE_TIME_MILLIS)
        } else 0L

        val state = when (rawState) {
            "RUNNING" -> WatchSessionState.RUNNING
            "PAUSED" -> WatchSessionState.PAUSED
            else -> WatchSessionState.NONE
        }

        Log.d(tag, "Received /session/state: $rawState start=$start pausedTotal=$totalPaused lastChange=$lastStateChange")

        val prior = _uiState.value
        val now = System.currentTimeMillis()

        val isPaused = state == WatchSessionState.PAUSED
        val phonePauseStartedAtMillis =
            if (state == WatchSessionState.PAUSED) lastStateChange.takeIf { it > 0L } else null

        val enteringPaused = (prior.state != WatchSessionState.PAUSED) && (state == WatchSessionState.PAUSED)
        val resuming = (prior.state == WatchSessionState.PAUSED) && (state == WatchSessionState.RUNNING)

        val pauseStartedAtMillis = when {
            state == WatchSessionState.PAUSED -> phonePauseStartedAtMillis ?: now
            else -> null
        }

        val pauseFallbackEndMillis = if (state == WatchSessionState.PAUSED && pauseStartedAtMillis == null) {
            // Safety: freeze if we somehow end up paused without a pause start time.
            prior.pauseFallbackEndMillis ?: now
        } else {
            null
        }

        val pausedTotalAtPauseStart = when {
            state == WatchSessionState.NONE -> 0L
            enteringPaused -> totalPaused
            else -> prior.pausedTotalAtPauseStart
        }

        // If we resume but paused total hasn't updated yet, carry the paused duration locally once.
        val localPauseCarryMillis = when {
            state == WatchSessionState.NONE -> 0L
            enteringPaused -> 0L
            resuming && totalPaused == pausedTotalAtPauseStart -> {
                val pauseStartForCarry = prior.pauseStartedAtMillis ?: prior.pauseFallbackEndMillis ?: now
                (now - pauseStartForCarry).coerceAtLeast(0L)
            }
            // Once phone reports a paused total greater than what we had at pause start, trust phone fully.
            totalPaused > pausedTotalAtPauseStart -> 0L
            else -> prior.localPauseCarryMillis
        }

        _uiState.value = WatchSessionUiState(
            state = state,
            startTimeMillis = start,
            totalPausedDurationMillis = totalPaused,
            pauseStartedAtMillis = pauseStartedAtMillis,
            isPaused = isPaused,
            pauseFallbackEndMillis = pauseFallbackEndMillis,
            pausedTotalAtPauseStart = pausedTotalAtPauseStart,
            localPauseCarryMillis = localPauseCarryMillis,
            tickMillis = prior.tickMillis,
            categories = prior.categories,
            showCategoryPicker = prior.showCategoryPicker
        )

        // Drive a lightweight 1-second ticker for smooth UI only when RUNNING.
        tickerJob?.cancel()
        if (state == WatchSessionState.RUNNING) {
            tickerJob = viewModelScope.launch(Dispatchers.Main.immediate) {
                while (true) {
                    delay(1000L)
                    val current = _uiState.value
                    if (current.state != WatchSessionState.RUNNING) break
                    val now = System.currentTimeMillis()
                    _uiState.value = current.copy(tickMillis = now)
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
            WearCommandSender.sendStart(getApplication(), categoryId)
        }
    }
}

