package com.noosxe.pc_dashboard.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.noosxe.pc_dashboard.data.PlayerState
import com.noosxe.pc_dashboard.ui.theme.PCDashboardTheme

@Composable
fun MediaControlCard(playerState: PlayerState, onCommand: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlbumArt(
                    artUrl = playerState.artUrl,
                    title = playerState.title
                )

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

@Preview(showBackground = true, name = "Normal Track")
@Composable
fun MediaControlCardPreview() {
    PCDashboardTheme {
        MediaControlCard(
            playerState = PlayerState(
                title = "Never Gonna Give You Up",
                artist = "Rick Astley",
                identity = "Spotify",
                status = "Playing",
                positionMs = 30000,
                lengthMs = 213000
            ),
            onCommand = {}
        )
    }
}

@Preview(showBackground = true, name = "Livestream")
@Composable
fun MediaControlCardLivestreamPreview() {
    PCDashboardTheme {
        MediaControlCard(
            playerState = PlayerState(
                title = "Lofi Hip Hop Radio",
                artist = "Lofi Girl",
                identity = "YouTube",
                status = "Playing",
                positionMs = 0,
                lengthMs = 0
            ),
            onCommand = {}
        )
    }
}
