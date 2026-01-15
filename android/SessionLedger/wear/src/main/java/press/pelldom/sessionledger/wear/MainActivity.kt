package press.pelldom.sessionledger.wear

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.alpha
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import java.util.Date
import kotlin.math.max
import kotlinx.coroutines.delay
import press.pelldom.sessionledger.wear.ui.SessionControlViewModel
import press.pelldom.sessionledger.wear.ui.WatchSessionState

class MainActivity : ComponentActivity() {
    private val viewModel: SessionControlViewModel by lazy {
        SessionControlViewModel(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WearRoot(viewModel = viewModel)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val state = viewModel.uiState.value.state

        return when (keyCode) {
            KeyEvent.KEYCODE_STEM_1 -> {
                when (state) {
                    WatchSessionState.NONE -> viewModel.start()
                    WatchSessionState.RUNNING, WatchSessionState.PAUSED -> viewModel.end()
                }
                true
            }

            KeyEvent.KEYCODE_STEM_2 -> {
                when (state) {
                    WatchSessionState.RUNNING -> viewModel.pause()
                    WatchSessionState.PAUSED -> viewModel.resume()
                    WatchSessionState.NONE -> Unit
                }
                state != WatchSessionState.NONE
            }

            else -> super.onKeyDown(keyCode, event)
        }
    }
}

@Composable
private fun WearRoot(viewModel: SessionControlViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // Top clock (HH:mm), updates once per minute.
    val clockFormatter = remember { java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()) }
    var clockText by remember { mutableStateOf(clockFormatter.format(Date())) }
    LaunchedEffect(Unit) {
        while (true) {
            val now = System.currentTimeMillis()
            clockText = clockFormatter.format(Date(now))
            val msToNextMinute = 60_000L - (now % 60_000L)
            delay(msToNextMinute)
        }
    }

    // Center elapsed time. Battery-friendly: update once per second only while RUNNING; static otherwise.
    var runningNowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var pausedElapsedMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(uiState.state, uiState.startTimeMillis) {
        when (uiState.state) {
            WatchSessionState.RUNNING -> {
                while (uiState.state == WatchSessionState.RUNNING) {
                    runningNowMs = System.currentTimeMillis()
                    delay(1000L)
                }
            }

            WatchSessionState.PAUSED -> {
                val start = uiState.startTimeMillis
                pausedElapsedMs = if (start != null) max(0L, System.currentTimeMillis() - start) else 0L
            }

            WatchSessionState.NONE -> {
                pausedElapsedMs = 0L
            }
        }
    }

    val elapsedMs = when (uiState.state) {
        WatchSessionState.NONE -> 0L
        WatchSessionState.RUNNING -> {
            val start = uiState.startTimeMillis
            if (start == null) 0L else max(0L, runningNowMs - start)
        }
        WatchSessionState.PAUSED -> pausedElapsedMs
    }

    val elapsedAlpha = if (uiState.state == WatchSessionState.PAUSED) {
        val transition = rememberInfiniteTransition(label = "pausePulse")
        val alpha by transition.animateFloat(
            initialValue = 1.0f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "elapsedAlpha"
        )
        alpha
    } else {
        1.0f
    }

    val (primaryLabel, onPrimary) = when (uiState.state) {
        WatchSessionState.NONE -> "Start" to { viewModel.start() }
        WatchSessionState.RUNNING, WatchSessionState.PAUSED -> "End" to { viewModel.end() }
    }

    val secondaryLabel: String? = when (uiState.state) {
        WatchSessionState.RUNNING -> "Pause"
        WatchSessionState.PAUSED -> "Resume"
        WatchSessionState.NONE -> null
    }
    val onSecondary: (() -> Unit)? = when (uiState.state) {
        WatchSessionState.RUNNING -> ({ viewModel.pause() })
        WatchSessionState.PAUSED -> ({ viewModel.resume() })
        WatchSessionState.NONE -> null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP
            Text(
                text = clockText,
                color = Color.White,
                style = MaterialTheme.typography.caption2,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // CENTER
            Text(
                text = formatElapsed(elapsedMs),
                color = Color.White,
                style = MaterialTheme.typography.display1,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .alpha(elapsedAlpha)
            )

            // BOTTOM
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onPrimary) { Text(primaryLabel) }

                if (secondaryLabel != null && onSecondary != null) {
                    Button(onClick = onSecondary) { Text(secondaryLabel) }
                }
            }
        }
    }
}

private fun formatElapsed(elapsedMs: Long): String {
    val totalSeconds = max(0L, elapsedMs / 1000L)
    val minutes = (totalSeconds / 60L)
    val seconds = totalSeconds % 60L
    return String.format("%02d:%02d", minutes, seconds)
}

