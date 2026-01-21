package press.pelldom.sessionledger.mobile.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import press.pelldom.sessionledger.mobile.appsettings.AppSettingsViewModel
import press.pelldom.sessionledger.mobile.appsettings.ThemeMode
import press.pelldom.sessionledger.mobile.ui.AppVersion
import press.pelldom.sessionledger.mobile.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AppSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vm = remember { AppSettingsViewModel(context.applicationContext as android.app.Application) }
    val settings by vm.settings.collectAsState()
    val scroll = rememberScrollState()
    var showProDialog by remember { mutableStateOf(false) }
    var showWearDialog by remember { mutableStateOf(false) }
    val buildNumber = remember {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            info.longVersionCode
        }.getOrNull()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionTitle("Appearance")
            ThemeRow(
                label = "System default",
                selected = settings.themeMode == ThemeMode.SYSTEM,
                enabled = true,
                onClick = { vm.setThemeMode(ThemeMode.SYSTEM) }
            )
            ThemeRow(
                label = "Light",
                selected = settings.themeMode == ThemeMode.LIGHT,
                enabled = true,
                onClick = { vm.setThemeMode(ThemeMode.LIGHT) }
            )
            ThemeRow(
                label = "Dark",
                selected = settings.themeMode == ThemeMode.DARK,
                enabled = true,
                onClick = { vm.setThemeMode(ThemeMode.DARK) }
            )

            HorizontalDivider()

            SectionTitle("System Defaults")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Use system defaults", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "When enabled, use system language/locale/time zone defaults (future: currency).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.useSystemDefaults,
                    onCheckedChange = { vm.setUseSystemDefaults(it) }
                )
            }

            HorizontalDivider()

            SectionTitle("Language")
            Text(
                text = "System default",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            SectionTitle("App Info")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.sessionledger_icon_a_monochrome),
                    contentDescription = "SessionLedger",
                    modifier = Modifier
                        .size(18.dp)
                        .alpha(0.75f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SessionLedger",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            InfoRow(label = "App", value = "SessionLedger")
            InfoRow(label = "Version", value = AppVersion.VERSION_NAME)
            buildNumber?.let { InfoRow(label = "Build", value = it.toString()) }

            HorizontalDivider()

            SectionTitle("Wear OS")
            Text(
                text = "SessionLedger for Watch",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showWearDialog = true }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Install on Watch", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Coming soon",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            SectionTitle("Pro")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showProDialog = true }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Upgrade to Pro", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Coming soon",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showProDialog) {
        AlertDialog(
            onDismissRequest = { showProDialog = false },
            title = { Text("Upgrade to Pro") },
            text = { Text("Pro features coming soon") },
            confirmButton = {
                TextButton(onClick = { showProDialog = false }) { Text("OK") }
            }
        )
    }

    if (showWearDialog) {
        AlertDialog(
            onDismissRequest = { showWearDialog = false },
            title = { Text("Install on Watch") },
            text = { Text("Install via Play Store (coming soon)") },
            confirmButton = {
                TextButton(onClick = { showWearDialog = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun ThemeRow(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (enabled) it.clickable(onClick = onClick) else it }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(selected = selected, onClick = if (enabled) onClick else null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

