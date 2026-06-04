package com.noosxe.pc_dashboard.ui.dashboard

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noosxe.pc_dashboard.ui.components.DigitalClock
import com.noosxe.pc_dashboard.ui.components.MediaControlCard
import com.noosxe.pc_dashboard.ui.dashboard.components.MemoryCard
import com.noosxe.pc_dashboard.ui.dashboard.components.StatCard
import com.noosxe.pc_dashboard.ui.theme.PCDashboardTheme

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
                    power = stats.cpuPower,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = "GPU",
                    usage = stats.gpuUsage,
                    temp = stats.gpuTemp,
                    freq = stats.gpuFreq,
                    power = stats.gpuPower,
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
