package press.pelldom.sessionledger.mobile.ui.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity

@Composable
fun CategoryPickerDialog(
    title: String,
    categories: List<CategoryEntity>,
    selectedCategoryId: String,
    onSelectCategory: (String) -> Unit,
    onAddNewCategory: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories, key = { it.id }) { cat ->
                        CategoryPickRow(
                            name = cat.name,
                            selected = cat.id == selectedCategoryId,
                            onClick = { onSelectCategory(cat.id) }
                        )
                    }
                    item(key = "add_new") {
                        Text(
                            text = "+ Add new category",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAdd = true }
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    )

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        val isDuplicate = categories.any { it.name.equals(name.trim(), ignoreCase = true) }

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
                    enabled = name.trim().isNotEmpty() && !isDuplicate,
                    onClick = {
                        onAddNewCategory(name.trim())
                        showAdd = false
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAdd = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CategoryPickRow(name: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = name, style = MaterialTheme.typography.bodyLarge)
    }
}

