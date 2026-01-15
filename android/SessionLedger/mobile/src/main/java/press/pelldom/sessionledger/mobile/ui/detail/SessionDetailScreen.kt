package press.pelldom.sessionledger.mobile.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SessionDetailScreen(sessionId: String, onDone: () -> Unit) {
    val context = LocalContext.current
    val viewModel = remember {
        SessionDetailViewModel(context.applicationContext as android.app.Application, sessionId)
    }
    val ui by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = "Session Detail", style = MaterialTheme.typography.headlineSmall)

        if (ui.loading) {
            Text(text = "Loading…")
            return@Column
        }

        if (ui.notFound) {
            Text(text = "Session not found.")
            Button(onClick = onDone) { Text("Back") }
            return@Column
        }

        Text(text = "Duration: ${ui.durationText}", style = MaterialTheme.typography.titleMedium)

        if (!ui.isEditable) {
            Text(text = ui.validationError ?: "This session cannot be edited.")
            Button(onClick = onDone) { Text("Back") }
            return@Column
        }

        OutlinedTextField(
            value = ui.startText,
            onValueChange = viewModel::onStartTextChange,
            label = { Text("Start (yyyy-MM-dd HH:mm)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = ui.endText,
            onValueChange = viewModel::onEndTextChange,
            label = { Text("End (yyyy-MM-dd HH:mm)") },
            modifier = Modifier.fillMaxWidth()
        )

        ui.validationError?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            enabled = ui.canSave,
            onClick = { viewModel.save(onSuccess = onDone) }
        ) {
            Text("Save")
        }
    }
}

