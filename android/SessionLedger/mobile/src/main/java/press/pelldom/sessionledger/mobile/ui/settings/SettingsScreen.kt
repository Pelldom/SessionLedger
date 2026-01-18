package press.pelldom.sessionledger.mobile.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import press.pelldom.sessionledger.mobile.billing.RoundingDirection
import press.pelldom.sessionledger.mobile.billing.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vm = remember { GlobalDefaultsViewModel(context.applicationContext as android.app.Application) }
    val ui by vm.ui.collectAsState()
    val scrollState = rememberScrollState()
    var showBackConfirm by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
            title = { Text("Global Billing Defaults") },
            navigationIcon = {
                IconButton(
                    onClick = {
                        if (!ui.hasUnsavedChanges) onBack()
                        else showBackConfirm = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
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
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { vm.discardEdits() }
                ) { Text("Cancel") }
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = ui.canSave,
                    onClick = { vm.save(onDone = {}) }
                ) { Text("Save") }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Currency: CAD", style = MaterialTheme.typography.bodyMedium)

            Divider()

        Text(text = "Defaults", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = ui.hourlyRate,
            onValueChange = vm::setHourlyRate,
            label = { Text("Default hourly rate (CAD/hr)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Rounding: ${ui.roundingMode.name}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    vm.toggleRoundingMode()
                }
            ) {
                Text("Toggle")
            }
        }

        if (ui.roundingMode == RoundingMode.SIX_MINUTE) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Direction: ${ui.roundingDirection.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        vm.cycleRoundingDirection()
                    }
                ) {
                    Text("Cycle")
                }
            }
        }

        Text(text = "Minimums", style = MaterialTheme.typography.titleMedium)
        MinimumRow(
            label = "None",
            selected = ui.minimumSelection == GlobalMinimumSelection.NONE,
            onClick = { vm.setMinimumSelection(GlobalMinimumSelection.NONE) }
        )
        MinimumRow(
            label = "Minimum time (hours)",
            selected = ui.minimumSelection == GlobalMinimumSelection.TIME,
            onClick = { vm.setMinimumSelection(GlobalMinimumSelection.TIME) }
        )
        MinimumRow(
            label = "Minimum charge (CAD)",
            selected = ui.minimumSelection == GlobalMinimumSelection.CHARGE,
            onClick = { vm.setMinimumSelection(GlobalMinimumSelection.CHARGE) }
        )

        if (ui.minimumSelection == GlobalMinimumSelection.TIME) {
            OutlinedTextField(
                value = ui.minHours,
                onValueChange = vm::setMinHours,
                label = { Text("Minimum time (hours)") },
                placeholder = { Text("e.g. 1.50") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else if (ui.minimumSelection == GlobalMinimumSelection.CHARGE) {
            OutlinedTextField(
                value = ui.minChargeAmount,
                onValueChange = vm::setMinChargeAmount,
                label = { Text("Minimum charge (CAD)") },
                placeholder = { Text("e.g. 50.00") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        ui.validationError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
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
                        vm.save(onDone = onBack)
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            showBackConfirm = false
                            vm.discardEdits()
                            onBack()
                        }
                    ) { Text("Discard") }
                    Button(onClick = { showBackConfirm = false }) { Text("Cancel") }
                }
            }
        )
    }
}

@Composable
private fun MinimumRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 12.dp))
    }
}

