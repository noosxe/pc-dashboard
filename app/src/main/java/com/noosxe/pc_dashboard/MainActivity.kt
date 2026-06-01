package com.noosxe.pc_dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.noosxe.pc_dashboard.data.PcNotification
import com.noosxe.pc_dashboard.data.PlayerState
import com.noosxe.pc_dashboard.service.PcStatsService
import com.noosxe.pc_dashboard.ui.components.NotificationBanner
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
    
    var currentNotification by remember { mutableStateOf<PcNotification?>(null) }
    var notificationVisible by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.notifications) {
        viewModel.notifications.collectLatest { notification ->
            Log.d("PERF_LATENCY", "UI_RECV_NOTIFICATION id=${notification.id} ts=${System.currentTimeMillis()}")
            currentNotification = notification
            notificationVisible = true
            delay(5000)
            notificationVisible = false
            delay(500)
            currentNotification = null
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
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

        currentNotification?.let { notification ->
            NotificationBanner(
                notification = notification,
                visible = notificationVisible,
                onActionClick = { actionKey ->
                    viewModel.onNotificationAction(notification.id, actionKey)
                    notificationVisible = false
                }
            )
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
                    freq = stats.cpuFreq,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = "GPU",
                    usage = stats.gpuUsage,
                    temp = stats.gpuTemp,
                    freq = stats.gpuFreq,
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

            MediaPager(viewModel = viewModel)
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
fun MediaPager(viewModel: DashboardViewModel) {
    val mediaState by viewModel.mediaState.collectAsStateWithLifecycle()

    if (mediaState.players.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "No players active",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        val pagerState = rememberPagerState(pageCount = { mediaState.players.size })
        
        // Track the player we're currently looking at to maintain stability across list updates
        var lastSeenPlayerId by remember { mutableStateOf<String?>(null) }
        
        // Update lastSeenPlayerId when the page changes (e.g., user swipe)
        LaunchedEffect(pagerState.currentPage) {
            if (mediaState.players.isNotEmpty() && pagerState.currentPage < mediaState.players.size) {
                lastSeenPlayerId = mediaState.players[pagerState.currentPage].player
            }
        }
        
        // When the list of players changes, ensure the pager stays on the same player
        LaunchedEffect(mediaState.players) {
            if (pagerState.isScrollInProgress) return@LaunchedEffect

            val targetIndex = mediaState.players.indexOfFirst { it.player == lastSeenPlayerId }
            if (targetIndex != -1) {
                // If the player we're watching moved, follow it
                if (targetIndex != pagerState.currentPage) {
                    pagerState.scrollToPage(targetIndex)
                }
            } else if (mediaState.players.isNotEmpty()) {
                // If the player we were watching is gone, or we haven't picked one yet, 
                // default to the first "Playing" one or just the first one
                val playingIndex = mediaState.players.indexOfFirst { it.status == "Playing" }
                val newIndex = if (playingIndex != -1) playingIndex else 0
                lastSeenPlayerId = mediaState.players[newIndex].player
                pagerState.scrollToPage(newIndex)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 8.dp,
            key = { index -> 
                if (index < mediaState.players.size) mediaState.players[index].player else index 
            }
        ) { page ->
            if (page < mediaState.players.size) {
                MediaControlCard(
                    playerState = mediaState.players[page],
                    onCommand = { command -> viewModel.onMediaCommand(mediaState.players[page].player, command) }
                )
            }
        }
    }
}

@Composable
fun MediaControlCard(playerState: PlayerState, onCommand: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (playerState.artBytes != null || playerState.artUrl.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        val cacheKey = remember(playerState.trackId, playerState.title, playerState.artist) {
                            val raw = "${playerState.trackId}|${playerState.title}|${playerState.artist}"
                            // Use a simple hash for the cache key to keep it clean
                            raw.hashCode().toString()
                        }

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(playerState.artBytes ?: playerState.artUrl)
                                .crossfade(true)
                                .size(512, 512)
                                .diskCacheKey(cacheKey)
                                .memoryCacheKey(cacheKey)
                                .build(),
                            contentDescription = "Album Art",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            onLoading = { Log.d("MediaArt", "Loading art for ${playerState.title}") },
                            onSuccess = { Log.d("MediaArt", "Successfully loaded art for ${playerState.title}") },
                            onError = { Log.e("MediaArt", "Failed to load art for ${playerState.title}: ${it.result.throwable.message}") }
                        )
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.padding(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = playerState.title.ifBlank { "Unknown Track" },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                    Text(
                        text = playerState.artist.ifBlank { "Unknown Artist" },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                    Text(
                        text = playerState.identity,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (playerState.lengthMs > 0) {
                val progressValue = playerState.positionMs.toFloat() / playerState.lengthMs.toFloat()
                LinearProgressIndicator(
                    progress = { progressValue.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onCommand("Previous") }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
                }
                IconButton(onClick = { onCommand("PlayPause") }) {
                    val icon = if (playerState.status == "Playing") {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    }
                    Icon(icon, contentDescription = if (playerState.status == "Playing") "Pause" else "Play")
                }
                IconButton(onClick = { onCommand("Next") }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next")
                }
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
fun StatCard(title: String, usage: Float, temp: Float, freq: Float, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = "Usage: ${"%.1f".format(usage)}%")
            Text(text = "Temp: ${"%.1f".format(temp)}°C")
            Text(text = "Freq: ${"%.2f".format(freq / 1000f)} GHz")
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
