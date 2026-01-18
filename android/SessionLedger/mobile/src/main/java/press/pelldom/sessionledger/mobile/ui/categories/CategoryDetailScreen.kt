package press.pelldom.sessionledger.mobile.ui.categories

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(categoryId: String, onDone: () -> Unit) {
    val context = LocalContext.current
    val vm = remember {
        CategoryDetailViewModel(context.applicationContext as android.app.Application, categoryId)
    }
    val ui by vm.ui.collectAsState()
    val scrollState = rememberScrollState()
    var showBackConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Category Defaults") },
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
        ,
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
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
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (ui.loading) {
                Text("Loading…")
                return@Column
            }
            if (ui.notFound) {
                Text("Category not found.")
                OutlinedButton(onClick = onDone) { Text("Back") }
                return@Column
            }

            OutlinedTextField(
                value = ui.nameEdit,
                onValueChange = vm::setName,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (!ui.isEditable) {
                Text(ui.validationError ?: "This category cannot be edited.")
                OutlinedButton(onClick = onDone) { Text("Back") }
                return@Column
            }

            if (ui.validationError != null) {
                Text(
                    text = ui.validationError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            SectionTitle("Hourly rate")
            Text(
                text = effectiveRateText(ui),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OverrideTextField(
                label = "Override (CAD/hr)",
                value = ui.hourlyRate,
                placeholder = "Default: CAD ${String.format(Locale.CANADA, "%.2f", ui.globalHourlyRate)}/hr",
                onValueChange = vm::setHourlyRate,
                onClear = vm::clearHourlyRate
            )

            SectionTitle("Rounding")
            Text(
                text = effectiveRoundingText(ui),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OverrideEnumRow(
                label = "Rounding mode",
                value = ui.roundingMode?.name ?: "${ui.globalRoundingMode.name} (Default)",
                onSet = vm::toggleRoundingMode,
                onClear = vm::clearRoundingMode
            )
            OverrideEnumRow(
                label = "Rounding direction",
                value = ui.roundingDirection?.name ?: "${ui.globalRoundingDirection.name} (Default)",
                onSet = vm::cycleRoundingDirection,
                onClear = vm::clearRoundingDirection
            )

            SectionTitle("Minimums")
            Text(
                text = "Default: ${formatGlobalMinimums(ui)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MinimumSelector(ui = ui, onSelect = vm::setMinimumSelection)

            if (ui.minimumSelection == CategoryMinimumSelection.TIME) {
                OverrideTextField(
                    label = "Minimum time (hours)",
                    value = ui.minHours,
                    placeholder = "e.g. 1.50",
                    onValueChange = vm::setMinHours,
                    onClear = { vm.setMinHours("") }
                )
            } else if (ui.minimumSelection == CategoryMinimumSelection.CHARGE) {
                OverrideTextField(
                    label = "Minimum charge (CAD)",
                    value = ui.minChargeAmount,
                    placeholder = "e.g. 50.00",
                    onValueChange = vm::setMinChargeAmount,
                    onClear = { vm.setMinChargeAmount("") }
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
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun MinimumSelector(ui: CategoryDetailUiState, onSelect: (CategoryMinimumSelection) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        MinimumRow(
            label = "Inherits global (${formatGlobalMinimums(ui)}) (Default)",
            selected = ui.minimumSelection == CategoryMinimumSelection.INHERIT,
            onClick = { onSelect(CategoryMinimumSelection.INHERIT) }
        )
        MinimumRow(
            label = "None (Override)",
            selected = ui.minimumSelection == CategoryMinimumSelection.NONE,
            onClick = { onSelect(CategoryMinimumSelection.NONE) }
        )
        MinimumRow(
            label = "Minimum time (Override)",
            selected = ui.minimumSelection == CategoryMinimumSelection.TIME,
            onClick = { onSelect(CategoryMinimumSelection.TIME) }
        )
        MinimumRow(
            label = "Minimum charge (Override)",
            selected = ui.minimumSelection == CategoryMinimumSelection.CHARGE,
            onClick = { onSelect(CategoryMinimumSelection.CHARGE) }
        )
    }
}

@Composable
private fun MinimumRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 12.dp))
    }
}

private fun effectiveRateText(ui: CategoryDetailUiState): String {
    val defaultText = "CAD ${String.format(Locale.CANADA, "%.2f", ui.globalHourlyRate)}/hr (Default)"
    val override = ui.hourlyRate.trim()
    return if (override.isEmpty()) {
        "Effective: $defaultText"
    } else {
        "Effective: CAD $override/hr (Override)"
    }
}

private fun effectiveRoundingText(ui: CategoryDetailUiState): String {
    val mode = ui.roundingMode?.name ?: ui.globalRoundingMode.name
    val dir = ui.roundingDirection?.name ?: ui.globalRoundingDirection.name
    val suffix = if (ui.roundingMode == null && ui.roundingDirection == null) " (Default)" else " (Override)"
    return "Effective: $mode / $dir$suffix"
}

private fun formatGlobalMinimums(ui: CategoryDetailUiState): String {
    val time = ui.globalMinBillableSeconds?.let { "${String.format(Locale.CANADA, "%.2f", it.toDouble() / 3600.0)}h" }
    val amt = ui.globalMinChargeAmount?.let { "CAD ${String.format(Locale.CANADA, "%.2f", it)}" }
    return when {
        time == null && amt == null -> "None"
        time != null && amt != null -> "time $time, amount $amt"
        time != null -> "time $time"
        else -> "amount $amt"
    }
}
