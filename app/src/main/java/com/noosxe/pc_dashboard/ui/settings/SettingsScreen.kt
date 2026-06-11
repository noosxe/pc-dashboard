package com.noosxe.pc_dashboard.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noosxe.pc_dashboard.ui.theme.PCDashboardTheme
import com.noosxe.pc_dashboard.ui.theme.AppTheme
import kotlin.math.roundToInt

@Preview(showBackground = true)
@Composable
fun NotificationSettingsContentPreview() {
    PCDashboardTheme {
        NotificationSettingsContent(
            respectExpireTimeout = false,
            notificationTimeout = 8,
            onRespectToggle = {},
            onTimeoutChange = {},
            onBackClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMainScreen(
    onBackClick: () -> Unit,
    onNetworkingClick: () -> Unit,
    onThemeClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAboutClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item {
                ListItem(
                    headlineContent = { Text("Networking") },
                    supportingContent = { Text("Configure server address and port") },
                    leadingContent = {
                        Icon(Icons.Default.SettingsInputComponent, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable(onClick = onNetworkingClick)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Theme") },
                    supportingContent = { Text("Customize the look and feel") },
                    leadingContent = {
                        Icon(Icons.Default.Palette, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable(onClick = onThemeClick)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Notifications") },
                    supportingContent = { Text("Configure banner behavior") },
                    leadingContent = {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable(onClick = onNotificationsClick)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("About") },
                    supportingContent = { Text("App information and version") },
                    leadingContent = {
                        Icon(Icons.Default.Info, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    },
                    modifier = Modifier.clickable(onClick = onAboutClick)
                )
            }
        }
    }
}

@Composable
fun NotificationSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
) {
    val respectExpireTimeout by viewModel.respectExpireTimeout.collectAsStateWithLifecycle()
    val notificationTimeout by viewModel.notificationTimeout.collectAsStateWithLifecycle()

    NotificationSettingsContent(
        respectExpireTimeout = respectExpireTimeout,
        notificationTimeout = notificationTimeout,
        onRespectToggle = { viewModel.setRespectExpireTimeout(it) },
        onTimeoutChange = { viewModel.setNotificationTimeout(it) },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsContent(
    respectExpireTimeout: Boolean,
    notificationTimeout: Int,
    onRespectToggle: (Boolean) -> Unit,
    onTimeoutChange: (Int) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item {
                ListItem(
                    headlineContent = { Text("Respect expire timeout") },
                    supportingContent = { Text("Use timeout from the notification itself") },
                    trailingContent = {
                        Switch(
                            checked = respectExpireTimeout,
                            onCheckedChange = onRespectToggle
                        )
                    },
                    modifier = Modifier.clickable { onRespectToggle(!respectExpireTimeout) }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Timeout: $notificationTimeout seconds") },
                    supportingContent = {
                        Slider(
                            value = notificationTimeout.toFloat(),
                            onValueChange = { onTimeoutChange(it.roundToInt()) },
                            valueRange = 1f..30f,
                            steps = 28,
                            enabled = !respectExpireTimeout
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ThemeSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
) {
    val currentTheme by viewModel.theme.collectAsStateWithLifecycle()

    ThemeSettingsContent(
        currentTheme = currentTheme,
        onThemeSelect = { viewModel.setTheme(it) },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsContent(
    currentTheme: AppTheme,
    onThemeSelect: (AppTheme) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(AppTheme.entries.size) { index ->
                val theme = AppTheme.entries[index]
                ThemeOption(
                    theme = theme,
                    isSelected = theme == currentTheme,
                    onClick = { onThemeSelect(theme) }
                )
            }
        }
    }
}

@Composable
fun ThemeOption(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(theme.name.replace("([a-z])([A-Z])".toRegex(), "$1 $2")) },
        leadingContent = {
            RadioButton(selected = isSelected, onClick = null)
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
fun NetworkingSettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
) {
    val serverHost by viewModel.serverHost.collectAsStateWithLifecycle()
    val serverPort by viewModel.serverPort.collectAsStateWithLifecycle()

    NetworkingSettingsContent(
        serverHost = serverHost,
        serverPort = serverPort,
        onHostChange = { viewModel.setServerHost(it) },
        onPortChange = { viewModel.setServerPort(it) },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkingSettingsContent(
    serverHost: String,
    serverPort: Int,
    onHostChange: (String) -> Unit,
    onPortChange: (Int) -> Unit,
    onBackClick: () -> Unit,
) {
    var hostInput by remember(serverHost) { mutableStateOf(serverHost) }
    var portInput by remember(serverPort) { mutableStateOf(serverPort.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Networking") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp)) {
            item {
                OutlinedTextField(
                    value = hostInput,
                    onValueChange = {
                        hostInput = it
                        if (it.isNotBlank()) {
                            onHostChange(it)
                        }
                    },
                    label = { Text("Server Host (IP or Hostname)") },
                    modifier = Modifier.fillParentMaxWidth().padding(vertical = 8.dp),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = portInput,
                    onValueChange = {
                        portInput = it
                        it.toIntOrNull()?.let { port ->
                            if (port in 1..65535) {
                                onPortChange(port)
                            }
                        }
                    },
                    label = { Text("Server Port") },
                    modifier = Modifier.fillParentMaxWidth().padding(vertical = 8.dp),
                    singleLine = true
                )
            }
            item {
                Text(
                    text = "The app will automatically reconnect when these settings are changed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
