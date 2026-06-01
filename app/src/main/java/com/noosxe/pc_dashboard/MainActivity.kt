package com.noosxe.pc_dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.noosxe.pc_dashboard.data.PcNotification
import com.noosxe.pc_dashboard.service.PcStatsService
import com.noosxe.pc_dashboard.ui.components.NotificationBanner
import com.noosxe.pc_dashboard.ui.dashboard.DashboardScreen
import com.noosxe.pc_dashboard.ui.dashboard.DashboardViewModel
import com.noosxe.pc_dashboard.ui.dashboard.LockedScreen
import com.noosxe.pc_dashboard.ui.settings.SettingsScreen
import com.noosxe.pc_dashboard.ui.theme.PCDashboardTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

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
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
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
