package press.pelldom.sessionledger.mobile.ui.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SessionListScreen(onSessionClick: (String) -> Unit, onExportClick: () -> Unit) {
    val context = LocalContext.current
    val viewModel = remember {
        SessionListViewModel(context.applicationContext as android.app.Application)
    }
    val items by viewModel.items.collectAsState()
    val filter by viewModel.filter.collectAsState()

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "filter") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filter == SessionListFilter.ACTIVE,
                    onClick = { viewModel.setFilter(SessionListFilter.ACTIVE) },
                    label = { Text("Active") }
                )
                FilterChip(
                    selected = filter == SessionListFilter.ARCHIVED,
                    onClick = { viewModel.setFilter(SessionListFilter.ARCHIVED) },
                    label = { Text("Archived") }
                )
            }
        }
        item(key = "export") {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onExportClick
            ) {
                Text("Export")
            }
        }
        items(items, key = { it.id }) { item ->
            SessionRow(item = item, onClick = { onSessionClick(item.id) })
        }
    }
}

@Composable
private fun SessionRow(item: SessionListItemUiModel, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = item.dateText, style = MaterialTheme.typography.titleMedium)
        Text(text = "Duration: ${item.durationText}", style = MaterialTheme.typography.bodyMedium)
        Text(text = item.categoryText, style = MaterialTheme.typography.bodyMedium)
        item.amountText?.let { Text(text = it, style = MaterialTheme.typography.titleMedium) }
    }
}

