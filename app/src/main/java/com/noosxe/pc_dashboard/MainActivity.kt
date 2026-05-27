package com.noosxe.pc_dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.noosxe.pc_dashboard.service.PcStatsService
import com.noosxe.pc_dashboard.ui.dashboard.DashboardViewModel
import com.noosxe.pc_dashboard.ui.theme.AppTheme
import com.noosxe.pc_dashboard.ui.theme.PCDashboardTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure app is visible when launched via ADB, even if screen is locked or off
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        setContent {
            val repository = (LocalContext.current.applicationContext as PCDashboardApplication).pcRepository
            val viewModel: DashboardViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return DashboardViewModel(repository) as T
                    }
                }
            )

            // Request notification permission for Foreground Service on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        startPcStatsService()
                    }
                }
                LaunchedEffect(Unit) {
                    if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        startPcStatsService()
                    }
                }
            } else {
                LaunchedEffect(Unit) {
                    startPcStatsService()
                }
            }

            val currentTheme by viewModel.theme.collectAsStateWithLifecycle()
            val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
            val shouldKeepScreenOn by viewModel.shouldKeepScreenOn.collectAsStateWithLifecycle()
            
            val view = LocalView.current
            if (!view.isInEditMode) {
                LaunchedEffect(view) {
                    val window = (view.context as ComponentActivity).window
                    val windowInsetsController = WindowCompat.getInsetsController(window, view)
                    windowInsetsController.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                }

                LaunchedEffect(isLocked) {
                    val window = (view.context as ComponentActivity).window
                    val params = window.attributes
                    if (isLocked) {
                        params.screenBrightness = 0.01f
                    } else {
                        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                    }
                    window.attributes = params
                }

                LaunchedEffect(shouldKeepScreenOn) {
                    val window = (view.context as ComponentActivity).window
                    if (shouldKeepScreenOn) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
            }

            PCDashboardTheme(appTheme = currentTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    if (isLocked) {
                        LockedScreen()
                    } else {
                        PCDashboardApp(viewModel)
                    }
                }
            }
        }
    }

    private fun startPcStatsService() {
        val intent = Intent(this, PcStatsService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}

@Composable
fun PCDashboardApp(viewModel: DashboardViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current

    LaunchedEffect(viewModel.notifications) {
        viewModel.notifications.collectLatest { notification ->
            Toast.makeText(
                context,
                "${notification.appName}: ${notification.summary}\n${notification.body}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
            ) { navController.navigate("settings") }
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
            ) { navController.popBackStack() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onSettingsClick: () -> Unit,
) {
    val stats by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        DigitalClock()
                        Text("PC Dashboard", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StatCard(
                    title = "CPU",
                    usage = stats.cpuUsage,
                    temp = stats.cpuTemp,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = "GPU",
                    usage = stats.gpuUsage,
                    temp = stats.gpuTemp,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MemoryCard(
                    title = "RAM",
                    usage = stats.ramUsage,
                    total = stats.ramTotal,
                    modifier = Modifier.weight(1f),
                )
                MemoryCard(
                    title = "VRAM",
                    usage = stats.vramUsage,
                    total = stats.vramTotal,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DashboardViewModel,
    onBackClick: () -> Unit,
) {
    val currentTheme by viewModel.theme.collectAsStateWithLifecycle()

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
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
            items(AppTheme.entries.size) { index ->
                val theme = AppTheme.entries[index]
                ThemeOption(
                    theme = theme,
                    isSelected = theme == currentTheme,
                ) { viewModel.setTheme(theme) }
            }
        }
    }
}

@Composable
fun LockedScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DigitalClock(modifier = Modifier.padding(16.dp))
        Text(
            text = "Host Session Locked",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun DigitalClock(modifier: Modifier = Modifier) {
    var time by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            time = System.currentTimeMillis()
            delay(1000)
        }
    }

    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    Text(
        text = formatter.format(Date(time)),
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
    )
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
fun StatCard(title: String, usage: Float, temp: Float, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = "Usage: ${"%.1f".format(usage)}%")
            Text(text = "Temp: ${"%.1f".format(temp)}°C")
        }
    }
}

@Composable
fun MemoryCard(title: String, usage: Float, total: Float, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = "Usage: ${"%.1f".format(usage)} GB")
            Text(text = "Total: ${"%.1f".format(total)} GB")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    PCDashboardTheme {
        DashboardScreen(
            viewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return DashboardViewModel(com.noosxe.pc_dashboard.data.MockPcRepository()) as T
                    }
                }
            ),
        ) {}
    }
}
