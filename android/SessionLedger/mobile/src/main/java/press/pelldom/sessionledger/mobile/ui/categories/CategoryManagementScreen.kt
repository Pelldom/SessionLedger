package press.pelldom.sessionledger.mobile.ui.categories

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
import press.pelldom.sessionledger.mobile.billing.RoundingMode

@Composable
fun CategoryManagementScreen(onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val viewModel = remember { CategoryListViewModel(context.applicationContext as android.app.Application) }
    val categories by viewModel.categories.collectAsState()

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
            onClick = onOpenSettings
        ) {
            Text("Global Billing Defaults")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories, key = { it.id }) { cat ->
                CategoryRow(
                    category = cat,
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
private fun CategoryRow(category: CategoryEntity, onRename: () -> Unit, onDelete: () -> Unit) {
    val protected = category.id == DefaultCategory.UNCATEGORIZED_ID
    val suffix = if (protected) " (protected)" else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = category.name + suffix, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = billingSummaryLine1(category),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = billingSummaryLine2(category),
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

private fun billingSummaryLine1(category: CategoryEntity): String {
    val rate = category.defaultHourlyRate?.let { "CAD ${String.format(Locale.CANADA, "%.2f", it)}/hr" } ?: "Inherits global"

    val rounding = if (category.roundingMode == null) {
        "Inherits global"
    } else {
        when (category.roundingMode) {
            RoundingMode.EXACT -> "EXACT"
            RoundingMode.SIX_MINUTE -> {
                val dir = category.roundingDirection?.name ?: "Inherits global"
                "SIX_MINUTE ($dir)"
            }
        }
    }

    return "Rate: $rate • Rounding: $rounding"
}

private fun billingSummaryLine2(category: CategoryEntity): String {
    val minTime = category.minBillableSeconds?.let { "${it / 60L} min" } ?: "Inherits global"
    val minAmt = category.minChargeAmount?.let { "CAD ${String.format(Locale.CANADA, "%.2f", it)}" } ?: "Inherits global"
    return "Minimums: time $minTime • amount $minAmt"
}
