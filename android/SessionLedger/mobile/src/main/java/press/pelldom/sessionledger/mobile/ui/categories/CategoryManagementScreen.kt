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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import press.pelldom.sessionledger.mobile.data.db.DefaultCategory
import press.pelldom.sessionledger.mobile.data.db.entities.CategoryEntity

@Composable
fun CategoryManagementScreen() {
    val context = LocalContext.current
    val viewModel = remember { CategoryListViewModel(context.applicationContext as android.app.Application) }
    val categories by viewModel.categories.collectAsState()

    var showAdd by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<CategoryEntity?>(null) }

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

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories, key = { it.id }) { cat ->
                CategoryRow(
                    category = cat,
                    onRename = { if (cat.id != DefaultCategory.UNCATEGORIZED_ID) renameTarget = cat }
                )
            }
        }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add category") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addCategory(name)
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
        var name by remember { mutableStateOf(target.name) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename category") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameCategory(target, name)
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

@Composable
private fun CategoryRow(category: CategoryEntity, onRename: () -> Unit) {
    val suffix = when {
        category.id == DefaultCategory.UNCATEGORIZED_ID -> " (protected)"
        category.isDefault -> " (default)"
        else -> ""
    }
    Text(
        text = category.name + suffix,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRename)
            .padding(12.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}

