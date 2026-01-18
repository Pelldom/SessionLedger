package press.pelldom.sessionledger.mobile.ui.active

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date
import kotlin.math.max
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import press.pelldom.sessionledger.mobile.billing.SessionState
import press.pelldom.sessionledger.mobile.data.db.AppDatabase
import press.pelldom.sessionledger.mobile.data.db.DefaultCategory
import press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity
import press.pelldom.sessionledger.mobile.ui.categories.CategoryPickerDialog
import press.pelldom.sessionledger.mobile.ui.session.SessionViewModel

@Composable
fun ActiveSessionScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val viewModel = remember {
        SessionViewModel(
            sessionDao = db.sessionDao(),
            appContext = context.applicationContext
        )
    }

    val activeSession by viewModel.activeSession.collectAsState()
    val session = activeSession

    val categories by db.categoryDao().observeAllCategories().collectAsState(initial = emptyList())
    val categoryById = remember(categories) { categories.associateBy { it.id } }
    val currentCategoryName = session?.let {
        categoryById[it.categoryId]?.name
            ?: categoryById[DefaultCategory.UNCATEGORIZED_ID]?.name
            ?: DefaultCategory.UNCATEGORIZED_NAME
    } ?: DefaultCategory.UNCATEGORIZED_NAME

    var showPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(session?.id, session?.state) {
        while (session?.state == SessionState.RUNNING) {
            nowMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val statusText = when (session?.state) {
        null -> "No active session"
        SessionState.RUNNING -> "Running"
        SessionState.PAUSED -> "Paused"
        SessionState.ENDED -> "No active session"
    }

    val elapsedMs = session?.let { sessionElapsedMs(it, nowMs) } ?: 0L
    val startedAtText = remember(session?.startTimeMillis) {
        session?.startTimeMillis?.let {
            val formatted = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(it))
            "Started at: $formatted"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "SessionLedger", style = MaterialTheme.typography.headlineLarge)
            Text(text = statusText, style = MaterialTheme.typography.bodyLarge)
            Text(text = formatElapsed(elapsedMs), style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Category: $currentCategoryName",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable(enabled = session != null) { showPicker = true }
            )
            if (session != null && (session.state == SessionState.RUNNING || session.state == SessionState.PAUSED)) {
                Text(text = startedAtText.orEmpty(), style = MaterialTheme.typography.bodyMedium)
            }
        }

        Column(modifier = Modifier.padding(top = 24.dp)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(enabled = session == null, onClick = { viewModel.startSession() }) {
                    Text(text = "Start Session")
                }

                Button(enabled = session?.state == SessionState.RUNNING, onClick = { viewModel.pauseSession() }) {
                    Text(text = "Pause")
                }

                Button(enabled = session?.state == SessionState.PAUSED, onClick = { viewModel.resumeSession() }) {
                    Text(text = "Resume")
                }

                Button(
                    enabled = session?.state == SessionState.RUNNING || session?.state == SessionState.PAUSED,
                    onClick = { viewModel.endSession() }
                ) {
                    Text(text = "End Session")
                }
            }
        }
    }

    if (showPicker && session != null) {
        CategoryPickerDialog(
            title = "Pick category",
            categories = categories,
            selectedCategoryId = session.categoryId,
            onSelectCategory = { id ->
                viewModel.setActiveSessionCategory(id)
                showPicker = false
            },
            onAddNewCategory = { name ->
                val now = System.currentTimeMillis()
                val newId = java.util.UUID.randomUUID().toString()
                val entity = CategoryEntity(
                    id = newId,
                    name = name,
                    isDefault = false,
                    archived = false,
                    createdAtMs = now,
                    updatedAtMs = now
                )
                scope.launch(Dispatchers.IO) {
                    db.categoryDao().insert(entity)
                    withContext(Dispatchers.Main) {
                        viewModel.setActiveSessionCategory(newId)
                    }
                }
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}

private fun sessionElapsedMs(session: SessionEntity, nowMs: Long): Long {
    val endForElapsedMs = when (session.state) {
        SessionState.RUNNING -> nowMs
        SessionState.PAUSED -> session.lastStateChangeTimeMs
        SessionState.ENDED -> session.endTimeMs ?: session.lastStateChangeTimeMs
    }
    return max(0L, (endForElapsedMs - session.startTimeMs) - session.pausedTotalMs)
}

private fun formatElapsed(elapsedMs: Long): String {
    val totalSeconds = max(0L, elapsedMs / 1000L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

