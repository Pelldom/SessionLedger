package press.pelldom.sessionledger.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import press.pelldom.sessionledger.mobile.billing.SessionState
import press.pelldom.sessionledger.mobile.data.db.AppDatabase
import press.pelldom.sessionledger.mobile.data.db.entities.SessionEntity
import press.pelldom.sessionledger.mobile.ui.session.SessionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MobileRoot()
            }
        }
    }
}

@Composable
private fun MobileRoot() {
    val context = LocalContext.current
    val viewModel = remember {
        SessionViewModel(AppDatabase.getInstance(context).sessionDao())
    }
    val activeSession by viewModel.activeSession.collectAsState()

    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(activeSession?.id, activeSession?.state) {
        while (activeSession?.state == SessionState.RUNNING) {
            nowMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val statusText = when (activeSession?.state) {
        null -> "No active session"
        SessionState.RUNNING -> "Running"
        SessionState.PAUSED -> "Paused"
        SessionState.ENDED -> "No active session"
    }

    val elapsedMs = activeSession?.let { sessionElapsedMs(it, nowMs) } ?: 0L
    val startedAtText = remember(activeSession?.startTimeMillis) {
        activeSession?.startTimeMillis?.let {
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
            Text(
                text = "SessionLedger",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = formatElapsed(elapsedMs),
                style = MaterialTheme.typography.headlineMedium
            )
            if (activeSession != null &&
                (activeSession.state == SessionState.RUNNING || activeSession.state == SessionState.PAUSED)
            ) {
                Text(
                    text = startedAtText.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Column(modifier = Modifier.padding(top = 24.dp)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    enabled = activeSession == null,
                    onClick = { viewModel.startSession() }
                ) {
                    Text(text = "Start Session")
                }

                Button(
                    enabled = activeSession?.state == SessionState.RUNNING,
                    onClick = { viewModel.pauseSession() }
                ) {
                    Text(text = "Pause")
                }

                Button(
                    enabled = activeSession?.state == SessionState.PAUSED,
                    onClick = { viewModel.resumeSession() }
                ) {
                    Text(text = "Resume")
                }

                Button(
                    enabled = activeSession?.state == SessionState.RUNNING || activeSession?.state == SessionState.PAUSED,
                    onClick = { viewModel.endSession() }
                ) {
                    Text(text = "End Session")
                }
            }
        }
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

