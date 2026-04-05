package dev.yakitori.barkforwarder

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.yakitori.barkforwarder.data.model.AppRule
import dev.yakitori.barkforwarder.domain.BarkEndpointParser
import dev.yakitori.barkforwarder.ui.MainViewModel
import dev.yakitori.barkforwarder.ui.theme.BarkForwarderTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarkForwarderTheme {
                BarkForwarderApp(viewModel = viewModel)
            }
        }
    }
}

private enum class Destination(val label: String) {
    Setup("Setup"),
    Bark("Bark"),
    Filters("Transfer"),
    Apps("Apps"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarkForwarderApp(viewModel: MainViewModel) {
    val barkConfig by viewModel.barkConfig.collectAsStateWithLifecycle()
    val cryptoConfig by viewModel.cryptoConfig.collectAsStateWithLifecycle()
    val filterConfig by viewModel.notificationFilterConfig.collectAsStateWithLifecycle()
    val appRules by viewModel.filteredRules.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val barkSettingsMessage by viewModel.barkSettingsMessage.collectAsStateWithLifecycle()
    var destination by rememberSaveable { mutableStateOf(Destination.Setup) }
    val context = LocalContext.current

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { }
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { }

    val permissionSnapshot by viewModel.permissionSnapshot.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshInstalledApps()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = destination == Destination.Setup,
                    onClick = { destination = Destination.Setup },
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                    label = { Text(Destination.Setup.label) },
                )
                NavigationBarItem(
                    selected = destination == Destination.Bark,
                    onClick = { destination = Destination.Bark },
                    icon = { Icon(Icons.Outlined.Security, contentDescription = null) },
                    label = { Text(Destination.Bark.label) },
                )
                NavigationBarItem(
                    selected = destination == Destination.Filters,
                    onClick = { destination = Destination.Filters },
                    icon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
                    label = { Text(Destination.Filters.label) },
                )
                NavigationBarItem(
                    selected = destination == Destination.Apps,
                    onClick = { destination = Destination.Apps },
                    icon = { Icon(Icons.Outlined.Apps, contentDescription = null) },
                    label = { Text(Destination.Apps.label) },
                )
            }
        },
    ) { padding ->
        when (destination) {
            Destination.Setup -> SetupScreen(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                status = permissionSnapshot,
                onOpenNotificationListenerSettings = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                },
                onRequestSmsPermissions = {
                    smsPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.READ_SMS,
                        ),
                    )
                },
                onRequestCallPermissions = {
                    callPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_CALL_LOG,
                        ),
                    )
                },
                onRequestNotificationPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onRefresh = viewModel::refreshPermissionSnapshot,
            )

            Destination.Bark -> BarkSettingsScreen(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                barkUrlOrKey = BarkEndpointParser.toDisplayValue(barkConfig),
                cryptoKey = cryptoConfig.key,
                fixedIv = cryptoConfig.fixedIv,
                statusMessage = barkSettingsMessage,
                onSave = { barkUrlOrKey, cryptoKey, fixedIv ->
                    viewModel.saveBarkSettings(barkUrlOrKey, cryptoKey, fixedIv)
                },
            )

            Destination.Filters -> FilterSettingsScreen(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                barkConfig = barkConfig,
                duplicateWindowSeconds = filterConfig.duplicateWindowSeconds,
                onNotificationsEnabledChanged = viewModel::setNotificationsEnabled,
                onSmsEnabledChanged = viewModel::setSmsEnabled,
                onCallsEnabledChanged = viewModel::setCallsEnabled,
                onRingIncomingCallsChanged = viewModel::setRingIncomingCalls,
                onDuplicateWindowChanged = viewModel::setDuplicateWindowSeconds,
            )

            Destination.Apps -> AppRulesScreen(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                query = query,
                rules = appRules,
                onQueryChanged = viewModel::setSearchQuery,
                onRefresh = viewModel::refreshInstalledApps,
                onExcludedChanged = viewModel::setAppExcluded,
                onManualIconSave = viewModel::updateManualIconUrl,
            )
        }
    }
}

@Composable
private fun SetupScreen(
    modifier: Modifier,
    status: MainViewModel.PermissionSnapshot,
    onOpenNotificationListenerSettings: () -> Unit,
    onRequestSmsPermissions: () -> Unit,
    onRequestCallPermissions: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onRefresh: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Initial setup", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Notification forwarding works best when notification access is enabled. SMS and call access will fall back to app notifications on devices that restrict direct permissions.",
            style = MaterialTheme.typography.bodyMedium,
        )
        StatusCard(
            title = "Notification access",
            enabled = status.notificationListenerEnabled,
            buttonLabel = "Open settings",
            onClick = onOpenNotificationListenerSettings,
        )
        StatusCard(
            title = "SMS permissions",
            enabled = status.smsPermissionsGranted,
            buttonLabel = "Request SMS",
            onClick = onRequestSmsPermissions,
        )
        StatusCard(
            title = "Call permissions",
            enabled = status.callPermissionsGranted,
            buttonLabel = "Request call access",
            onClick = onRequestCallPermissions,
        )
        StatusCard(
            title = "Post notifications",
            enabled = status.postNotificationsGranted,
            buttonLabel = "Request permission",
            onClick = onRequestNotificationPermission,
        )
        OutlinedButton(onClick = onRefresh) {
            Text("Refresh status")
        }
        OutlinedCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Open source license", style = MaterialTheme.typography.titleMedium)
                Text(
                    "This app is open source and distributed under the MIT License. The full license text is available in this repository.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(
                    onClick = {
                        uriHandler.openUri("https://github.com/notisfer-dev/notification-transfer-for-bark")
                    },
                ) {
                    Text("View repository")
                }
            }
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    enabled: Boolean,
    buttonLabel: String,
    onClick: () -> Unit,
) {
    OutlinedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(if (enabled) "Enabled" else "Not enabled")
            Button(onClick = onClick) {
                Text(buttonLabel)
            }
        }
    }
}

@Composable
private fun BarkSettingsScreen(
    modifier: Modifier,
    barkUrlOrKey: String,
    cryptoKey: String,
    fixedIv: String,
    statusMessage: String?,
    onSave: (String, String, String) -> Unit,
) {
    var barkEndpoint by rememberSaveable(barkUrlOrKey) { mutableStateOf(barkUrlOrKey) }
    var aesKey by rememberSaveable(cryptoKey) { mutableStateOf(cryptoKey) }
    var iv by rememberSaveable(fixedIv) { mutableStateOf(fixedIv) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Bark settings", style = MaterialTheme.typography.headlineSmall)
        Text("Paste `https://api.day.app/<your-key>` or just the key. Encryption is fixed to AES256 / GCM / noPadding.")
        OutlinedCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("Thanks to Bark", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Special thanks to Bark and its developer Finb for building and sharing the iOS app that makes this workflow possible.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        FixedSettingCard(title = "Algorithm", value = "AES256")
        FixedSettingCard(title = "Mode", value = "GCM")
        FixedSettingCard(title = "Padding", value = "noPadding")
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = barkEndpoint,
            onValueChange = { barkEndpoint = it },
            label = { Text("Bark URL or Key") },
            supportingText = {
                Text("Examples: https://api.day.app/XXXXXXXXXXXXXXXXXX or just the device key")
            },
            singleLine = true,
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = aesKey,
            onValueChange = { aesKey = it.trim() },
            label = { Text("AES-256-GCM key (32 chars)") },
            supportingText = { Text("${aesKey.toByteArray(Charsets.UTF_8).size}/32 bytes") },
            singleLine = true,
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = iv,
            onValueChange = { iv = it.trim() },
            label = { Text("Fixed IV (12 chars)") },
            supportingText = { Text("${iv.toByteArray(Charsets.UTF_8).size}/12 bytes") },
            singleLine = true,
        )
        statusMessage?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium)
        }
        Button(onClick = { onSave(barkEndpoint, aesKey, iv) }) {
            Text("Save Bark settings")
        }
    }
}

@Composable
private fun FixedSettingCard(
    title: String,
    value: String,
) {
    OutlinedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun FilterSettingsScreen(
    modifier: Modifier,
    barkConfig: dev.yakitori.barkforwarder.data.model.BarkConfig,
    duplicateWindowSeconds: Int,
    onNotificationsEnabledChanged: (Boolean) -> Unit,
    onSmsEnabledChanged: (Boolean) -> Unit,
    onCallsEnabledChanged: (Boolean) -> Unit,
    onRingIncomingCallsChanged: (Boolean) -> Unit,
    onDuplicateWindowChanged: (Int) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Transfer settings", style = MaterialTheme.typography.headlineSmall)
        ToggleRow("Forward app notifications", barkConfig.notificationsEnabled, onNotificationsEnabledChanged)
        ToggleRow("Forward SMS", barkConfig.smsEnabled, onSmsEnabledChanged)
        ToggleRow("Forward calls", barkConfig.callsEnabled, onCallsEnabledChanged)
        ToggleRow("Use Bark call=1 for incoming calls", barkConfig.ringIncomingCalls, onRingIncomingCallsChanged)
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Duplicate notification timeout")
                Text("$duplicateWindowSeconds seconds")
                Slider(
                    value = duplicateWindowSeconds.toFloat(),
                    onValueChange = { onDuplicateWindowChanged(it.toInt()) },
                    valueRange = 1f..120f,
                )
                Text("Applies only to notification events. Same-content notifications are normalized before comparison so grouped apps such as LINE are less likely to forward duplicates.")
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    OutlinedCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun AppRulesScreen(
    modifier: Modifier,
    query: String,
    rules: List<AppRule>,
    onQueryChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    onExcludedChanged: (String, Boolean) -> Unit,
    onManualIconSave: (String, String?) -> Unit,
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("Excluded apps", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Bark icons must be public URLs. This app tries Play Store first, then manual override, and falls back to no icon if nothing public is available.",
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = query,
            onValueChange = onQueryChanged,
            label = { Text("Search installed apps") },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onRefresh) {
            Text("Refresh installed apps")
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(rules, key = { it.packageName }) { rule ->
                AppRuleRow(rule = rule, onExcludedChanged = onExcludedChanged, onManualIconSave = onManualIconSave)
            }
        }
    }
}

@Composable
private fun AppRuleRow(
    rule: AppRule,
    onExcludedChanged: (String, Boolean) -> Unit,
    onManualIconSave: (String, String?) -> Unit,
) {
    var expanded by rememberSaveable(rule.packageName) { mutableStateOf(false) }
    var manualUrl by rememberSaveable(rule.packageName, rule.manualIconUrl) { mutableStateOf(rule.manualIconUrl.orEmpty()) }

    OutlinedCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = rule.excluded,
                    onCheckedChange = { onExcludedChanged(rule.packageName, it) },
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(rule.appLabel, style = MaterialTheme.typography.titleMedium)
                    Text(rule.packageName, style = MaterialTheme.typography.bodySmall)
                    Text(
                        when {
                            !rule.manualIconUrl.isNullOrBlank() -> "Manual icon URL set"
                            !rule.cachedResolvedIconUrl.isNullOrBlank() -> "Play icon cached"
                            else -> "No icon cached yet"
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(if (expanded) "Hide" else "Edit")
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = manualUrl,
                    onValueChange = { manualUrl = it },
                    label = { Text("Manual icon URL override") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onManualIconSave(rule.packageName, manualUrl) }) {
                        Text("Save icon URL")
                    }
                    OutlinedButton(onClick = {
                        manualUrl = ""
                        onManualIconSave(rule.packageName, null)
                    }) {
                        Text("Clear")
                    }
                }
            }
        }
    }
}
