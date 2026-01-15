package press.pelldom.sessionledger.mobile.ui.sessions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import press.pelldom.sessionledger.mobile.data.db.AppDatabase
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity

class SessionListViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)
    private val _items = MutableStateFlow<List<SessionListItemUiModel>>(emptyList())
    val items: StateFlow<List<SessionListItemUiModel>> = _items

    init {
        val zone = ZoneId.systemDefault()
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        viewModelScope.launch(Dispatchers.IO) {
            db.sessionDao()
                .observeEndedSessionsNewestFirst()
                .distinctUntilChanged()
                .collect { sessions ->
                    val sorted = sessions.sortedByDescending { it.endTimeMs ?: 0L }
                    _items.value = sorted.map { s -> s.toUiModel(zone, fmt) }
                }
        }
    }

    private fun SessionEntity.toUiModel(zone: ZoneId, fmt: DateTimeFormatter): SessionListItemUiModel {
        val dateMs = this.endTimeMs ?: this.startTimeMs
        val dateText = Instant.ofEpochMilli(dateMs).atZone(zone).format(fmt)

        val durationMs = max(0L, ((this.endTimeMs ?: this.startTimeMs) - this.startTimeMs) - this.pausedTotalMs)
        val durationText = formatDuration(durationMs)

        val categoryText = if (this.categoryId == null) "Uncategorized" else "Category"

        return SessionListItemUiModel(
            id = this.id,
            dateText = dateText,
            durationText = durationText,
            categoryText = categoryText,
            amountText = null
        )
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

