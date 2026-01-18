package press.pelldom.sessionledger.mobile.ui.export

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ExportScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val viewModel = remember { ExportViewModel(context.applicationContext as android.app.Application) }
    val ui by viewModel.ui.collectAsState()
    val scroll = rememberScrollState()
    val zone = remember { ZoneId.systemDefault() }
    val dateFmt = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    var showBackConfirm by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Sessions") },
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
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.discardEdits() }
                ) { Text("Cancel") }

                Button(
                    modifier = Modifier.weight(1f),
                    enabled = ui.canSave,
                    onClick = { viewModel.save() }
                ) { Text("Save") }

                Button(
                    modifier = Modifier.weight(1f),
                    enabled = ui.canExport && !ui.exporting,
                    onClick = { viewModel.export(context) }
                ) { Text(if (ui.exporting) "Exporting…" else "Export") }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (ui.loading) {
                Text("Loading…")
                return@Column
            }

            Text(text = "Date range", style = MaterialTheme.typography.titleMedium)
            PickerRow(
                label = "Start date",
                value = ui.startDate?.format(dateFmt) ?: "Select",
                onClick = { showStartPicker = true }
            )
            PickerRow(
                label = "End date",
                value = ui.endDate?.format(dateFmt) ?: "Select",
                onClick = { showEndPicker = true }
            )

            Text(text = "Category filter", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setAllCategories(true) }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(checked = ui.allCategories, onCheckedChange = { viewModel.setAllCategories(true) })
                Text("All categories")
            }

            if (!ui.allCategories) {
                ui.categories.forEach { cat ->
                    val checked = ui.selectedCategoryIds.contains(cat.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleCategory(cat.id) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(checked = checked, onCheckedChange = { viewModel.toggleCategory(cat.id) })
                        Text(cat.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                OutlinedButton(onClick = { viewModel.setAllCategories(false) }) { Text("Select categories…") }
            }

            ui.validationError?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
            ui.statusMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (ui.lastExportUri != null) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.shareLastExport(context) }
                ) {
                    Text("Share last export")
                }
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
                        viewModel.save()
                        onDone()
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            showBackConfirm = false
                            viewModel.discardEdits()
                            onDone()
                        }
                    ) { Text("Discard") }
                    OutlinedButton(onClick = { showBackConfirm = false }) { Text("Cancel") }
                }
            }
        )
    }

    if (showStartPicker) {
        val initial = ui.startDate ?: LocalDate.now(zone)
        val initialSelectedUtcMidnightMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = initialSelectedUtcMidnightMillis
        )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) {
                            val pickedDate = Instant.ofEpochMilli(selected).atZone(ZoneOffset.UTC).toLocalDate()
                            viewModel.setStartDate(pickedDate)
                        }
                        showStartPicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = { OutlinedButton(onClick = { showStartPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showEndPicker) {
        val initial = ui.endDate ?: LocalDate.now(zone)
        val initialSelectedUtcMidnightMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = initialSelectedUtcMidnightMillis
        )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) {
                            val pickedDate = Instant.ofEpochMilli(selected).atZone(ZoneOffset.UTC).toLocalDate()
                            viewModel.setEndDate(pickedDate)
                        }
                        showEndPicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = { OutlinedButton(onClick = { showEndPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
private fun PickerRow(label: String, value: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = value, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

