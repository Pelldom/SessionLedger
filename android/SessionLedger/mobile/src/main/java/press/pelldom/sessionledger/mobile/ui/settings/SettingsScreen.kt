package press.pelldom.sessionledger.mobile.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import kotlinx.coroutines.launch
import press.pelldom.sessionledger.mobile.billing.RoundingDirection
import press.pelldom.sessionledger.mobile.billing.RoundingMode
import press.pelldom.sessionledger.mobile.settings.SettingsRepository
import press.pelldom.sessionledger.mobile.settings.dataStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { SettingsRepository(context.dataStore) }
    val settings by repo.observeGlobalSettings().collectAsState(
        initial = press.pelldom.sessionledger.mobile.settings.GlobalSettings(
            defaultCurrency = "CAD",
            defaultHourlyRate = 0.0,
            defaultRoundingMode = RoundingMode.EXACT,
            defaultRoundingDirection = RoundingDirection.UP,
            minBillableSeconds = null,
            minChargeAmount = null,
            lastUsedCategoryId = null
        )
    )

    var rateText by remember(settings.defaultHourlyRate) { mutableStateOf(settings.defaultHourlyRate.toString()) }
    var minMinutesText by remember(settings.minBillableSeconds) {
        mutableStateOf(settings.minBillableSeconds?.let { (it / 60L).toString() } ?: "")
    }
    var minChargeText by remember(settings.minChargeAmount) {
        mutableStateOf(settings.minChargeAmount?.toString() ?: "")
    }

    Scaffold(
        topBar = {
            TopAppBar(
            title = { Text("Global Billing Defaults") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Currency: CAD", style = MaterialTheme.typography.bodyMedium)

            Divider()

            Text(text = "Defaults", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = rateText,
            onValueChange = {
                rateText = it
                val parsed = it.toDoubleOrNull() ?: return@OutlinedTextField
                scope.launch { repo.setDefaultHourlyRate(parsed) }
            },
            label = { Text("Default hourly rate (CAD/hr)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Rounding: ${settings.defaultRoundingMode.name}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    val next = if (settings.defaultRoundingMode == RoundingMode.EXACT) {
                        RoundingMode.SIX_MINUTE
                    } else {
                        RoundingMode.EXACT
                    }
                    scope.launch { repo.setDefaultRoundingMode(next) }
                }
            ) {
                Text("Toggle")
            }
        }

        if (settings.defaultRoundingMode == RoundingMode.SIX_MINUTE) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Direction: ${settings.defaultRoundingDirection.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        val next = when (settings.defaultRoundingDirection) {
                            RoundingDirection.UP -> RoundingDirection.NEAREST
                            RoundingDirection.NEAREST -> RoundingDirection.DOWN
                            RoundingDirection.DOWN -> RoundingDirection.UP
                        }
                        scope.launch { repo.setDefaultRoundingDirection(next) }
                    }
                ) {
                    Text("Cycle")
                }
            }
        }

        OutlinedTextField(
            value = minMinutesText,
            onValueChange = {
                minMinutesText = it
                val trimmed = it.trim()
                if (trimmed.isEmpty()) {
                    scope.launch { repo.setMinBillableSeconds(null) }
                    return@OutlinedTextField
                }
                val minutes = trimmed.toLongOrNull() ?: return@OutlinedTextField
                scope.launch { repo.setMinBillableSeconds(minutes * 60L) }
            },
            label = { Text("Minimum billable time (minutes, optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = minChargeText,
            onValueChange = {
                minChargeText = it
                val trimmed = it.trim()
                if (trimmed.isEmpty()) {
                    scope.launch { repo.setMinChargeAmount(null) }
                    return@OutlinedTextField
                }
                val amount = trimmed.toDoubleOrNull() ?: return@OutlinedTextField
                scope.launch { repo.setMinChargeAmount(amount) }
            },
            label = { Text("Minimum charge (CAD, optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        }
    }
}

