package press.pelldom.sessionledger.mobile.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionBillingOverrideScreen(sessionId: String, onDone: () -> Unit) {
    val context = LocalContext.current
    val vm = remember { SessionBillingOverrideViewModel(context.applicationContext as android.app.Application, sessionId) }
    val ui by vm.ui.collectAsState()
    val scroll = rememberScrollState()
    var showBackConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Billing Overrides") },
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
                OutlinedButton(modifier = Modifier.weight(1f), onClick = { vm.discardEdits() }) { Text("Cancel") }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = ui.canSave,
                    onClick = { vm.save(onDone = {}) }
                ) { Text("Save") }
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
            if (ui.notFound) {
                Text("Session not found.")
                return@Column
            }
            if (!ui.isEditable) {
                Text(ui.validationError ?: "This session cannot be edited.")
                return@Column
            }

            ui.validationError?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Text(text = ui.finalAmountText, style = MaterialTheme.typography.headlineSmall)

            Text(text = "Effective (read-only)", style = MaterialTheme.typography.titleMedium)
            Text(text = "Rate: ${ui.effectiveRateText}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Rounding: ${ui.effectiveRoundingText}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Minimum: ${ui.effectiveMinimumText}", style = MaterialTheme.typography.bodyMedium)

            Text(text = "Overrides", style = MaterialTheme.typography.titleMedium)

            OverrideTextField(
                label = "Hourly rate override (CAD/hr)",
                value = ui.hourlyRateOverride,
                placeholder = "Inherit",
                onValueChange = vm::setHourlyRateOverride,
                onClear = vm::clearHourlyRateOverride
            )

            OverrideEnumRow(
                label = "Rounding mode override",
                value = ui.roundingModeOverride?.name ?: "Inherit",
                onSet = vm::toggleRoundingModeOverride,
                onClear = vm::clearRoundingModeOverride
            )

            OverrideEnumRow(
                label = "Rounding direction override",
                value = ui.roundingDirectionOverride?.name ?: "Inherit",
                onSet = vm::cycleRoundingDirectionOverride,
                onClear = vm::clearRoundingDirectionOverride
            )

            Text(text = "Minimum override", style = MaterialTheme.typography.titleMedium)
            MinimumRow(
                label = "Inherit",
                selected = ui.minimumSelection == SessionMinimumSelection.INHERIT,
                onClick = { vm.setMinimumSelection(SessionMinimumSelection.INHERIT) }
            )
            MinimumRow(
                label = "None (override)",
                selected = ui.minimumSelection == SessionMinimumSelection.NONE,
                onClick = { vm.setMinimumSelection(SessionMinimumSelection.NONE) }
            )
            MinimumRow(
                label = "Minimum time (hours)",
                selected = ui.minimumSelection == SessionMinimumSelection.TIME,
                onClick = { vm.setMinimumSelection(SessionMinimumSelection.TIME) }
            )
            MinimumRow(
                label = "Minimum charge (CAD)",
                selected = ui.minimumSelection == SessionMinimumSelection.CHARGE,
                onClick = { vm.setMinimumSelection(SessionMinimumSelection.CHARGE) }
            )

            if (ui.minimumSelection == SessionMinimumSelection.TIME) {
                OutlinedTextField(
                    value = ui.minHours,
                    onValueChange = vm::setMinHours,
                    label = { Text("Hours") },
                    placeholder = { Text("e.g. 1.50") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else if (ui.minimumSelection == SessionMinimumSelection.CHARGE) {
                OutlinedTextField(
                    value = ui.minChargeAmount,
                    onValueChange = vm::setMinChargeAmount,
                    label = { Text("CAD") },
                    placeholder = { Text("e.g. 50.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
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
}

@Composable
private fun OverrideTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = onClear) { Text("Clear") }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun OverrideEnumRow(
    label: String,
    value: String,
    onSet: () -> Unit,
    onClear: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onClear) { Text("Clear") }
            Button(onClick = onSet) { Text("Set") }
        }
    }
}

@Composable
private fun MinimumRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

