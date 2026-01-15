package press.pelldom.sessionledger.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import java.text.DateFormat
import java.util.Date
import press.pelldom.sessionledger.wear.ui.SessionControlViewModel
import press.pelldom.sessionledger.wear.ui.WatchSessionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WearRoot()
            }
        }
    }
}

@Composable
private fun WearRoot() {
    val context = LocalContext.current
    val viewModel = remember {
        SessionControlViewModel(context.applicationContext as android.app.Application)
    }
    val uiState by viewModel.uiState.collectAsState()

    val statusText = when (uiState.state) {
        WatchSessionState.NONE -> "No Active Session"
        WatchSessionState.RUNNING -> "Running"
        WatchSessionState.PAUSED -> "Paused"
    }

    val startedAtText = uiState.startTimeMillis?.let {
        val formatted = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(it))
        "Started at: $formatted"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = statusText, style = MaterialTheme.typography.title2)
            if (startedAtText != null && uiState.state != WatchSessionState.NONE) {
                Text(text = startedAtText, style = MaterialTheme.typography.caption2)
            }
        }

        Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                enabled = uiState.state == WatchSessionState.NONE,
                onClick = { viewModel.start() }
            ) { Text("Start") }

            Button(
                enabled = uiState.state == WatchSessionState.RUNNING,
                onClick = { viewModel.pause() }
            ) { Text("Pause") }

            Button(
                enabled = uiState.state == WatchSessionState.PAUSED,
                onClick = { viewModel.resume() }
            ) { Text("Resume") }

            Button(
                enabled = uiState.state == WatchSessionState.RUNNING || uiState.state == WatchSessionState.PAUSED,
                onClick = { viewModel.end() }
            ) { Text("End") }
        }
    }
}

