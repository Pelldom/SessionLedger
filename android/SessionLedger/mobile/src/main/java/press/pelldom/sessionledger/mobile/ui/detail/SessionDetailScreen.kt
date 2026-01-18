package press.pelldom.sessionledger.mobile.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import press.pelldom.sessionledger.mobile.ui.categories.CategoryPickerDialog

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SessionDetailScreen(
    sessionId: String,
    onDone: () -> Unit,
    onOpenTimingEdit: () -> Unit,
    onOpenBillingOverrides: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember {
        SessionDetailViewModel(context.applicationContext as android.app.Application, sessionId)
    }
    val ui by viewModel.uiState.collectAsState()

    var showCategoryPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Details") },
                navigationIcon = {
                    IconButton(
                        onClick = onDone
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {

        if (ui.loading) {
            Text(text = "Loading…")
            return@Column
        }

        if (ui.notFound) {
            Text(text = "Session not found.")
            Button(onClick = onDone) { Text("Back") }
            return@Column
        }

        // Session Details (read-only)
        Text(text = "Session Details", style = MaterialTheme.typography.titleMedium)
        ReadonlySummaryRow(label = "Duration", value = ui.durationText)
        ReadonlySummaryRow(label = "Category", value = ui.categoryName)
        ReadonlySummaryRow(label = "Start time", value = ui.startText)
        ReadonlySummaryRow(label = "End time", value = ui.endText)

        // Edit entry points (exactly three)
        Text(text = "Edit", style = MaterialTheme.typography.titleMedium)
        EditEntryRow(
            label = "Timing",
            value = "${ui.startText} → ${ui.endText}",
            enabled = ui.isEditable,
            onClick = onOpenTimingEdit
        )
        EditEntryRow(
            label = "Category",
            value = ui.categoryName,
            enabled = ui.isEditable,
            onClick = { showCategoryPicker = true }
        )
        EditEntryRow(
            label = "Billing",
            value = ui.billingFinalAmountText.ifBlank { "—" },
            enabled = ui.isEditable && ui.billingFinalAmountText.isNotBlank(),
            onClick = onOpenBillingOverrides
        )

        if (!ui.isEditable) {
            Text(
                text = ui.validationError ?: "This session cannot be edited.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Billing Summary (fully read-only)
        if (ui.billingFinalAmountText.isNotBlank()) {
            Text(text = "Billing Summary", style = MaterialTheme.typography.titleMedium)
            ReadonlySummaryRow(label = "Rate", value = ui.billingHourlyRateText)
            ReadonlySummaryRow(label = "Rounding", value = ui.billingRoundingText)
            ReadonlySummaryRow(label = "Minimum", value = ui.billingMinimumText)
            Text(text = ui.billingFinalAmountText, style = MaterialTheme.typography.headlineSmall)
        }
        }
    }

    if (showCategoryPicker) {
        CategoryPickerDialog(
            title = "Pick category",
            categories = ui.categories,
            selectedCategoryId = ui.categoryId,
            onSelectCategory = { id ->
                viewModel.persistCategorySelection(id)
                showCategoryPicker = false
            },
            onAddNewCategory = { name ->
                viewModel.createCategoryAndSelect(name)
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
        )
    }
}

@Composable
private fun ReadonlySummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EditEntryRow(label: String, value: String, enabled: Boolean, onClick: () -> Unit) {
    val modifier = if (enabled) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(text = value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Edit $label",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
