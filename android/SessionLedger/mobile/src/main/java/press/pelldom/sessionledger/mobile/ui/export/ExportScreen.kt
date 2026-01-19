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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.util.Log
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.DocumentsContract
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ExportScreen(onDone: () -> Unit, onArchivedDone: () -> Unit) {
    val context = LocalContext.current
    val viewModel = remember { ExportViewModel(context.applicationContext as android.app.Application) }
    val ui by viewModel.ui.collectAsState()
    val scroll = rememberScrollState()
    val zone = remember { ZoneId.systemDefault() }
    val dateFmt = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showBackConfirm by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var showPostExportArchivePrompt by remember { mutableStateOf(false) }
    var showArchiveRangeDialog by remember { mutableStateOf(false) }
    var archiveStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var archiveEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var showArchiveStartPicker by remember { mutableStateOf(false) }
    var showArchiveEndPicker by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    onClick = {
                        Log.d("SL_EXPORT", "Export button clicked")
                        scope.launch { snackbarHostState.showSnackbar("Export started...") }

                        scope.launch {
                            try {
                                val uri = viewModel.exportNow(context)
                                Log.d("SL_EXPORT", "Export success: uri=$uri")
                                snackbarHostState.showSnackbar("Export saved to Downloads/SessionLedger")
                                // Trigger post-export archive prompt.
                                showPostExportArchivePrompt = true
                            } catch (t: Throwable) {
                                Log.e("SL_EXPORT", "Export failed", t)
                                snackbarHostState.showSnackbar("Export failed: ${t.message ?: "Unknown error"}")
                            }
                        }
                    }
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

            Text(text = "Export Location", style = MaterialTheme.typography.titleMedium)
            Text(text = "Downloads / SessionLedger", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    Log.d("SL_EXPORT", "Open Export Folder clicked")
                    // Try (1) Downloads UI, (2) direct folder URI, (3) SAF folder picker.
                    try {
                        context.startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        return@OutlinedButton
                    } catch (_: Throwable) {
                        // fall through
                    }

                    try {
                        val downloadDoc = "primary:Download"
                        val uri = DocumentsContract.buildDocumentUri(
                            "com.android.externalstorage.documents",
                            downloadDoc
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, DocumentsContract.Document.MIME_TYPE_DIR)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                        return@OutlinedButton
                    } catch (_: Throwable) {
                        // fall through
                    }

                    try {
                        val treeUri = DocumentsContract.buildTreeDocumentUri(
                            "com.android.externalstorage.documents",
                            "primary:Download"
                        )
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                            putExtra(DocumentsContract.EXTRA_INITIAL_URI, treeUri)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (t: ActivityNotFoundException) {
                        scope.launch {
                            snackbarHostState.showSnackbar("No file manager found. Exports are in Downloads/SessionLedger.")
                        }
                    } catch (_: Throwable) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Exports are saved to Downloads/SessionLedger.")
                        }
                    }
                }
            ) { Text("Open Export Folder") }
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

    if (showPostExportArchivePrompt && ui.postExportArchivePrompt) {
        AlertDialog(
            onDismissRequest = {
                showPostExportArchivePrompt = false
                viewModel.dismissPostExportArchivePrompt()
            },
            title = { Text("Archive exported sessions?") },
            text = { Text("The sessions included in this export can be archived to keep future exports clean.") },
            confirmButton = {
                Button(
                    onClick = {
                        // Initialize archive range from export contents.
                        archiveStartDate = ui.suggestedArchiveStart
                        archiveEndDate = ui.suggestedArchiveEnd
                        showPostExportArchivePrompt = false
                        showArchiveRangeDialog = true
                    }
                ) { Text("Archive") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showPostExportArchivePrompt = false
                        viewModel.dismissPostExportArchivePrompt()
                    }
                ) { Text("Not now") }
            }
        )
    }

    if (showArchiveRangeDialog) {
        val start = archiveStartDate
        val end = archiveEndDate
        val canArchive = start != null && end != null && !end.isBefore(start)

        AlertDialog(
            onDismissRequest = { showArchiveRangeDialog = false },
            title = { Text("Archive sessions") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PickerRow(
                        label = "Start date",
                        value = start?.format(dateFmt) ?: "Select",
                        onClick = { showArchiveStartPicker = true }
                    )
                    PickerRow(
                        label = "End date",
                        value = end?.format(dateFmt) ?: "Select",
                        onClick = { showArchiveEndPicker = true }
                    )
                    if (!canArchive) {
                        Text(
                            text = "End date must be on or after start date.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = canArchive,
                    onClick = {
                        val s = start ?: return@Button
                        val e = end ?: return@Button
                        showArchiveRangeDialog = false
                        scope.launch {
                            try {
                                val count = viewModel.archiveLastExportedSessions(s, e)
                                if (count <= 0) snackbarHostState.showSnackbar("No sessions to archive")
                                else snackbarHostState.showSnackbar("Sessions archived")
                                onArchivedDone()
                            } catch (t: Throwable) {
                                snackbarHostState.showSnackbar("Archive failed: ${t.message ?: "Unknown error"}")
                            }
                        }
                    }
                ) { Text("Archive") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showArchiveRangeDialog = false }) { Text("Cancel") }
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

    if (showArchiveStartPicker) {
        val initial = archiveStartDate ?: ui.startDate ?: LocalDate.now(zone)
        val initialSelectedUtcMidnightMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = initialSelectedUtcMidnightMillis
        )
        DatePickerDialog(
            onDismissRequest = { showArchiveStartPicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) {
                            val pickedDate = Instant.ofEpochMilli(selected).atZone(ZoneOffset.UTC).toLocalDate()
                            archiveStartDate = pickedDate
                        }
                        showArchiveStartPicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = { OutlinedButton(onClick = { showArchiveStartPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showArchiveEndPicker) {
        val initial = archiveEndDate ?: ui.endDate ?: LocalDate.now(zone)
        val initialSelectedUtcMidnightMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = initialSelectedUtcMidnightMillis
        )
        DatePickerDialog(
            onDismissRequest = { showArchiveEndPicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (selected != null) {
                            val pickedDate = Instant.ofEpochMilli(selected).atZone(ZoneOffset.UTC).toLocalDate()
                            archiveEndDate = pickedDate
                        }
                        showArchiveEndPicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = { OutlinedButton(onClick = { showArchiveEndPicker = false }) { Text("Cancel") } }
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

