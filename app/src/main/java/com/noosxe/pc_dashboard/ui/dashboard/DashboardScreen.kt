package com.noosxe.pc_dashboard.ui.dashboard

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noosxe.pc_dashboard.ui.components.DigitalClock
import com.noosxe.pc_dashboard.ui.components.MediaControlCard
import com.noosxe.pc_dashboard.ui.dashboard.components.SmartStatCard
import com.noosxe.pc_dashboard.ui.theme.PCDashboardTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onSettingsClick: () -> Unit,
) {
    val stats by viewModel.uiState.collectAsStateWithLifecycle()
    val history by viewModel.statsHistory.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SmartStatCard(
                        title = "CPU",
                        mainValue = "${stats.cpuUsage.toInt()}%",
                        secondaryValue = "${stats.cpuTemp.toInt()}°C",
                        mainHistory = history.map { it.cpuUsage },
                        mainChartColor = Color(0xFF4FC3F7),
                        mainMax = 100f,
                        mainLabel = "%",
                        secondaryHistory = history.map { it.cpuTemp },
                        secondaryChartColor = Color(0xFFFF7043),
                        secondaryMax = 100f,
                        secondaryLabel = "°C",
                        modifier = Modifier.weight(1f)
                    )
                    SmartStatCard(
                        title = "GPU",
                        mainValue = "${stats.gpuUsage.toInt()}%",
                        secondaryValue = "${stats.gpuTemp.toInt()}°C",
                        mainHistory = history.map { it.gpuUsage },
                        mainChartColor = Color(0xFF81C784),
                        mainMax = 100f,
                        mainLabel = "%",
                        secondaryHistory = history.map { it.gpuTemp },
                        secondaryChartColor = Color(0xFFFF7043),
                        secondaryMax = 100f,
                        secondaryLabel = "°C",
                        modifier = Modifier.weight(1f)
                    )
                    SmartStatCard(
                        title = "RAM",
                        mainValue = "${stats.ramUsage.toInt()} GB",
                        secondaryValue = "of ${stats.ramTotal.toInt()} GB",
                        mainHistory = history.map { it.ramUsage },
                        mainChartColor = Color(0xFFFFB74D),
                        mainMax = stats.ramTotal.coerceAtLeast(1f),
                        mainLabel = "GB",
                        modifier = Modifier.weight(1f)
                    )
                    SmartStatCard(
                        title = "VRAM",
                        mainValue = "${stats.vramUsage.toInt()} GB",
                        secondaryValue = "of ${stats.vramTotal.toInt()} GB",
                        mainHistory = history.map { it.vramUsage },
                        mainChartColor = Color(0xFFBA68C8),
                        mainMax = stats.vramTotal.coerceAtLeast(1f),
                        mainLabel = "GB",
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SmartStatCard(
                        title = "CPU",
                        mainValue = "${stats.cpuUsage.toInt()}%",
                        secondaryValue = "${stats.cpuTemp.toInt()}°C",
                        mainHistory = history.map { it.cpuUsage },
                        mainChartColor = Color(0xFF4FC3F7),
                        mainMax = 100f,
                        mainLabel = "%",
                        secondaryHistory = history.map { it.cpuTemp },
                        secondaryChartColor = Color(0xFFFF7043),
                        secondaryMax = 100f,
                        secondaryLabel = "°C",
                        modifier = Modifier.weight(1f)
                    )
                    SmartStatCard(
                        title = "GPU",
                        mainValue = "${stats.gpuUsage.toInt()}%",
                        secondaryValue = "${stats.gpuTemp.toInt()}°C",
                        mainHistory = history.map { it.gpuUsage },
                        mainChartColor = Color(0xFF81C784),
                        mainMax = 100f,
                        mainLabel = "%",
                        secondaryHistory = history.map { it.gpuTemp },
                        secondaryChartColor = Color(0xFFFF7043),
                        secondaryMax = 100f,
                        secondaryLabel = "°C",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SmartStatCard(
                        title = "RAM",
                        mainValue = "${stats.ramUsage.toInt()} GB",
                        secondaryValue = "of ${stats.ramTotal.toInt()} GB",
                        mainHistory = history.map { it.ramUsage },
                        mainChartColor = Color(0xFFFFB74D),
                        mainMax = stats.ramTotal.coerceAtLeast(1f),
                        mainLabel = "GB",
                        modifier = Modifier.weight(1f)
                    )
                    SmartStatCard(
                        title = "VRAM",
                        mainValue = "${stats.vramUsage.toInt()} GB",
                        secondaryValue = "of ${stats.vramTotal.toInt()} GB",
                        mainHistory = history.map { it.vramUsage },
                        mainChartColor = Color(0xFFBA68C8),
                        mainMax = stats.vramTotal.coerceAtLeast(1f),
                        mainLabel = "GB",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            MediaPager(viewModel = viewModel)
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

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    PCDashboardTheme {
        DashboardScreen(
            viewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return DashboardViewModel(com.noosxe.pc_dashboard.data.MockPcRepository()) as T
                    }
                }
            ),
        ) {}
    }
}
