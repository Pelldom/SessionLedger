package press.pelldom.sessionledger.mobile.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.LocalDate
import java.time.ZoneOffset
import press.pelldom.sessionledger.mobile.ui.categories.CategoryPickerDialog

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SessionDetailScreen(sessionId: String, onDone: () -> Unit) {
    val context = LocalContext.current
    val viewModel = remember {
        SessionDetailViewModel(context.applicationContext as android.app.Application, sessionId)
    }
    val ui by viewModel.uiState.collectAsState()

    val zone = remember { ZoneId.systemDefault() }

    var editingTarget by remember { mutableStateOf<EditingTarget?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDate by remember { mutableStateOf<LocalDate?>(null) }
    var pendingInitialTime by remember { mutableStateOf<LocalTime?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }

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

        ReadonlyPickerField(label = "Category", value = ui.categoryName, onClick = { showCategoryPicker = true })

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ReadonlyPickerField(
                label = "Start",
                value = ui.startText,
                onClick = {
                    editingTarget = EditingTarget.START
                    showDatePicker = true
                }
            )

            ReadonlyPickerField(
                label = "End",
                value = ui.endText,
                onClick = {
                    editingTarget = EditingTarget.END
                    showDatePicker = true
                }
            )
        }

        // Billing Summary (read-only) - placed after session detail fields
        if (ui.billingFinalAmountText.isNotBlank()) {
            Text(text = "Billing Summary", style = MaterialTheme.typography.titleMedium)
            Text(text = "Rate: ${ui.billingHourlyRateText}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Rounding: ${ui.billingRoundingText}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Minimum: ${ui.billingMinimumText}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = ui.billingFinalAmountText,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        ui.validationError?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = {
                    viewModel.discardEdits()
                    onDone()
                }
            ) {
                Text("Cancel")
            }

            Button(
                enabled = ui.canSave,
                onClick = { viewModel.save(onSuccess = onDone) }
            ) {
                Text("Save")
            }
        }
    }

    if (showDatePicker) {
        val sourceMillis = when (editingTarget) {
            EditingTarget.START -> ui.startMillis
            EditingTarget.END -> ui.endMillis
            null -> null
        }

        // Convert existing timestamp -> local date -> UTC midnight millis for DatePicker (avoids day drift).
        val initialLocalDate = (sourceMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
            ?: LocalDate.now(zone))
        val initialSelectedUtcMidnightMillis = initialLocalDate
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = initialSelectedUtcMidnightMillis
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
                editingTarget = null
            },
            confirmButton = {
                Button(
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) {
                            val pickedDate = Instant.ofEpochMilli(selected).atZone(ZoneOffset.UTC).toLocalDate()
                            pendingDate = pickedDate

                            // Preserve the existing time for the field being edited, if present.
                            val existing = sourceMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalTime() }
                            pendingInitialTime = existing

                            showDatePicker = false
                            showTimePicker = true
                        }
                    }
                ) { Text("Next") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDatePicker = false
                        editingTarget = null
                    }
                ) { Text("Cancel") }
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

        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showTimePicker = false
                pendingDate = null
                pendingInitialTime = null
                editingTarget = null
            },
            confirmButton = {
                Button(
                    onClick = {
                        val date = pendingDate ?: return@Button
                        val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        val ldt = LocalDateTime.of(date, time)
                        val epoch = ldt.atZone(zone).toInstant().toEpochMilli()
                        when (editingTarget) {
                            EditingTarget.START -> viewModel.setStartMillis(epoch)
                            EditingTarget.END -> viewModel.setEndMillis(epoch)
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
            },
            title = { Text("Pick time") },
            text = { TimePicker(state = timePickerState) }
        )
    }

    if (showCategoryPicker) {
        CategoryPickerDialog(
            title = "Pick category",
            categories = ui.categories,
            selectedCategoryId = ui.categoryId,
            onSelectCategory = { id ->
                viewModel.setCategoryId(id)
                showCategoryPicker = false
            },
            onAddNewCategory = { name ->
                viewModel.createCategoryAndSelect(name)
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
        )
    }
}

private enum class EditingTarget { START, END }

@Composable
private fun ReadonlyPickerField(label: String, value: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

