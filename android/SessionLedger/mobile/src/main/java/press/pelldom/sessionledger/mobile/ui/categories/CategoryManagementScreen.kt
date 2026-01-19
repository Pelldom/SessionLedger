package press.pelldom.sessionledger.mobile.ui.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Locale
import press.pelldom.sessionledger.mobile.data.db.DefaultCategory
import press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity
import press.pelldom.sessionledger.mobile.settings.GlobalSettings
import press.pelldom.sessionledger.mobile.settings.SettingsRepository
import press.pelldom.sessionledger.mobile.settings.dataStore

@Composable
fun CategoryManagementScreen(
    onOpenSettings: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onOpenCategory: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { CategoryListViewModel(context.applicationContext as android.app.Application) }
    val categories by viewModel.categories.collectAsState()
    val settingsRepo = remember { SettingsRepository(context.dataStore) }
    val global by settingsRepo.observeGlobalSettings().collectAsState(
        initial = GlobalSettings(
            defaultCurrency = "CAD",
            defaultHourlyRate = 0.0,
            defaultRoundingMode = press.pelldom.sessionledger.mobile.billing.RoundingMode.EXACT,
            defaultRoundingDirection = press.pelldom.sessionledger.mobile.billing.RoundingDirection.UP,
            minBillableSeconds = null,
            minChargeAmount = null,
            lastUsedCategoryId = null
        )
    )

    var showAdd by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<CategoryEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<CategoryEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Categories", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { showAdd = true }) { Text("Add") }
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenAppSettings
        ) {
            Text("App Settings")
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenSettings
        ) {
            Text("Global Billing Defaults")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories, key = { it.id }) { cat ->
                CategoryRow(
                    category = cat,
                    global = global,
                    onOpen = { if (cat.id != DefaultCategory.UNCATEGORIZED_ID) onOpenCategory(cat.id) },
                    onRename = { renameTarget = cat },
                    onDelete = { deleteTarget = cat }
                )
            }
        }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        val trimmed = name.trim()
        val isDuplicate = categories.any { it.name.equals(trimmed, ignoreCase = true) }

        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") }
                    )
                    if (isDuplicate) {
                        Text(
                            text = "A category with this name already exists.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = trimmed.isNotEmpty() && !isDuplicate,
                    onClick = {
                        viewModel.addCategory(trimmed)
                        showAdd = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAdd = false }) { Text("Cancel") }
            }
        )
    }

    renameTarget?.let { target ->
        if (target.id == DefaultCategory.UNCATEGORIZED_ID) {
            renameTarget = null
        } else {
            var name by remember { mutableStateOf(target.name) }
            val trimmed = name.trim()
            val isDuplicate = categories.any { it.id != target.id && it.name.equals(trimmed, ignoreCase = true) }

            AlertDialog(
                onDismissRequest = { renameTarget = null },
                title = { Text("Rename category") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") }
                        )
                        if (isDuplicate) {
                            Text(
                                text = "A category with this name already exists.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        enabled = trimmed.isNotEmpty() && !isDuplicate,
                        onClick = {
                            viewModel.renameCategory(target, trimmed)
                            renameTarget = null
                        }
                    ) { Text("Save") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { renameTarget = null }) { Text("Cancel") }
                }
            )
        }
    }

    deleteTarget?.let { target ->
        if (target.id == DefaultCategory.UNCATEGORIZED_ID) {
            deleteTarget = null
        } else {
            AlertDialog(
                onDismissRequest = { deleteTarget = null },
                title = { Text("Delete category") },
                text = { Text("Delete \"${target.name}\"? Sessions will be reassigned to Uncategorized.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCategory(target)
                            deleteTarget = null
                        }
                    ) { Text("Delete") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { deleteTarget = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
private fun CategoryRow(
    category: CategoryEntity,
    global: GlobalSettings,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    val protected = category.id == DefaultCategory.UNCATEGORIZED_ID
    val suffix = if (protected) " (protected)" else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = !protected, onClick = onOpen),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = category.name + suffix, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = billingSummaryRate(category, global),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = billingSummaryMinimum(category, global),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(enabled = !protected, onClick = onRename) { Text("Rename") }
            TextButton(enabled = !protected, onClick = onDelete) { Text("Delete") }
        }
    }
}

private fun billingSummaryRate(category: CategoryEntity, global: GlobalSettings): String {
    val isDefault = category.defaultHourlyRate == null || category.id == DefaultCategory.UNCATEGORIZED_ID
    val effective = category.defaultHourlyRate ?: global.defaultHourlyRate
    val suffix = if (isDefault) " (Default)" else ""
    return "Rate: $${String.format(Locale.CANADA, "%.2f", effective)}/hr$suffix"
}

private fun billingSummaryMinimum(category: CategoryEntity, global: GlobalSettings): String {
    val catTime = category.minBillableSeconds
    val catAmt = category.minChargeAmount
    val catNone = (catTime ?: Long.MIN_VALUE) == 0L && (catAmt ?: Double.NaN) == 0.0
    val catHasOverride = catTime != null || catAmt != null

    val (effectiveText, isDefault) = when {
        category.id == DefaultCategory.UNCATEGORIZED_ID -> formatGlobalMinimum(global) to true
        catNone -> "None" to false
        catTime != null && catTime > 0L -> "${String.format(Locale.CANADA, "%.2f", catTime.toDouble() / 3600.0)} hr" to false
        catAmt != null && catAmt > 0.0 -> "$${String.format(Locale.CANADA, "%.2f", catAmt)}" to false
        !catHasOverride -> formatGlobalMinimum(global) to true
        else -> formatGlobalMinimum(global) to true
    }

    val suffix = if (isDefault) " (Default)" else ""
    return "Minimum: $effectiveText$suffix"
}

private fun formatGlobalMinimum(global: GlobalSettings): String {
    return when {
        global.minBillableSeconds != null -> "${String.format(Locale.CANADA, "%.2f", global.minBillableSeconds.toDouble() / 3600.0)} hr"
        global.minChargeAmount != null -> "$${String.format(Locale.CANADA, "%.2f", global.minChargeAmount)}"
        else -> "None"
    }
}
