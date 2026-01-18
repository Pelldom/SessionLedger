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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
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
        val ui = viewModel.uiState.value
        val state = ui.state

        return when (keyCode) {
            KeyEvent.KEYCODE_STEM_1 -> {
                when (state) {
                    WatchSessionState.NONE -> viewModel.onStartPressed()
                    WatchSessionState.RUNNING, WatchSessionState.PAUSED -> viewModel.end()
                }
                true
            }

            KeyEvent.KEYCODE_STEM_2 -> {
                if (ui.showCategoryPicker) {
                    viewModel.dismissCategoryPicker()
                    return true
                }
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

    // Elapsed time is phone-authoritative; watch only displays and ticks locally for smoothness.
    val elapsedMs = uiState.displayedElapsedMillis

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
        WatchSessionState.NONE -> "Start" to { viewModel.onStartPressed() }
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
            if (uiState.showCategoryPicker) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.55f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Pick category",
                        color = Color.White,
                        style = MaterialTheme.typography.caption1
                    )
                    for (cat in uiState.categories) {
                        Button(onClick = { viewModel.startWithCategory(cat.id) }) {
                            Text(cat.name)
                        }
                    }
                }
            } else {
                Text(
                    text = formatElapsed(elapsedMs),
                    color = Color.White,
                    style = MaterialTheme.typography.display1,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .alpha(elapsedAlpha)
                )
            }

            // BOTTOM
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.showCategoryPicker) {
                    Button(onClick = { viewModel.dismissCategoryPicker() }) { Text("Cancel") }
                } else {
                    Button(onClick = onPrimary) { Text(primaryLabel) }

                    if (secondaryLabel != null && onSecondary != null) {
                        Button(onClick = onSecondary) { Text(secondaryLabel) }
                    }
                }
            }
        }
    }
}

private fun formatElapsed(elapsedMs: Long): String {
    val totalSeconds = (elapsedMs / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return String.format("%02d:%02d", minutes, seconds)
}


