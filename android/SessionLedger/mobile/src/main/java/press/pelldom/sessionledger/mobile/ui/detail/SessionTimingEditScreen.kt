package press.pelldom.sessionledger.mobile.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SessionTimingEditScreen(sessionId: String, onDone: () -> Unit) {
    val context = LocalContext.current
    val vm = remember { SessionTimingEditViewModel(context.applicationContext as android.app.Application, sessionId) }
    val ui by vm.ui.collectAsState()

    val zone = remember { ZoneId.systemDefault() }

    var editingTarget by remember { mutableStateOf<EditingTarget?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDate by remember { mutableStateOf<LocalDate?>(null) }
    var pendingInitialTime by remember { mutableStateOf<LocalTime?>(null) }
    var showBackConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Timing") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!ui.hasUnsavedChanges) onDone()
                            else showBackConfirm = true
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            if (ui.loading) {
                Text("Loading…")
                return@Column
            }
            if (ui.notFound) {
                Text("Session not found.")
                return@Column
            }

            Text(text = "Duration: ${ui.durationText}", style = MaterialTheme.typography.titleMedium)

            if (!ui.isEditable) {
                Text(text = ui.validationError ?: "This session cannot be edited.")
                return@Column
            }

            PickerRow(
                label = "Start time",
                value = ui.startText,
                onClick = { editingTarget = EditingTarget.START; showDatePicker = true }
            )
            PickerRow(
                label = "End time",
                value = ui.endText,
                onClick = { editingTarget = EditingTarget.END; showDatePicker = true }
            )

            ui.validationError?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { vm.discardEdits() }) { Text("Cancel") }
                Button(enabled = ui.canSave, onClick = { vm.save(onDone = {}) }) { Text("Save") }
            }
        }
    }

    if (showBackConfirm) {
        AlertDialog(
            onDismissRequest = { showBackConfirm = false },
            title = { Text("Save changes?") },
            text = { Text("You have unsaved changes.") },
            confirmButton = {
                Button(
                    enabled = ui.canSave,
                    onClick = {
                        showBackConfirm = false
                        vm.save(onDone = onDone)
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            showBackConfirm = false
                            vm.discardEdits()
                            onDone()
                        }
                    ) { Text("Discard") }
                    OutlinedButton(onClick = { showBackConfirm = false }) { Text("Cancel") }
                }
            }
        )
    }

    if (showDatePicker) {
        val sourceMillis = when (editingTarget) {
            EditingTarget.START -> ui.startMillis
            EditingTarget.END -> ui.endMillis
            null -> null
        }

        // Convert existing timestamp -> local date -> UTC midnight millis for DatePicker (avoids day drift).
        val initialLocalDate = (sourceMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() } ?: LocalDate.now(zone))
        val initialSelectedUtcMidnightMillis = initialLocalDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = initialSelectedUtcMidnightMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false; editingTarget = null },
            confirmButton = {
                Button(
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) {
                            pendingDate = Instant.ofEpochMilli(selected).atZone(ZoneOffset.UTC).toLocalDate()
                            pendingInitialTime = sourceMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalTime() }
                            showDatePicker = false
                            showTimePicker = true
                        }
                    }
                ) { Text("Next") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePicker = false; editingTarget = null }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val initialTime = pendingInitialTime ?: LocalTime.now(zone)
        val timePickerState = androidx.compose.material3.rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = android.text.format.DateFormat.is24HourFormat(context)
        )

        AlertDialog(
            onDismissRequest = {
                showTimePicker = false
                pendingDate = null
                pendingInitialTime = null
                editingTarget = null
            },
            title = { Text("Pick time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                Button(
                    onClick = {
                        val date = pendingDate ?: return@Button
                        val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        val ldt = LocalDateTime.of(date, time)
                        val epoch = ldt.atZone(zone).toInstant().toEpochMilli()
                        when (editingTarget) {
                            EditingTarget.START -> vm.setStartMillis(epoch)
                            EditingTarget.END -> vm.setEndMillis(epoch)
                            null -> Unit
                        }
                        showTimePicker = false
                        pendingDate = null
                        pendingInitialTime = null
                        editingTarget = null
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showTimePicker = false
                        pendingDate = null
                        pendingInitialTime = null
                        editingTarget = null
                    }
                ) { Text("Cancel") }
            }
        )
    }
}

private enum class EditingTarget { START, END }

@Composable
private fun PickerRow(label: String, value: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
            OutlinedButton(onClick = onClick) { Text("Edit") }
        }
    }
}

