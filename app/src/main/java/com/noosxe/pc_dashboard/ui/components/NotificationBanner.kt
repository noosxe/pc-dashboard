package com.noosxe.pc_dashboard.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.noosxe.pc_dashboard.data.PcNotification
import com.noosxe.pc_dashboard.ui.theme.PCDashboardTheme

@Composable
fun NotificationBanner(
    notification: PcNotification,
    visible: Boolean,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(notification.id) {
        Log.d("PERF_LATENCY", "UI_SHOW_NOTIFICATION id=${notification.id} ts=${System.currentTimeMillis()}")
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.98f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .widthIn(min = 200.dp, max = 500.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // App Icon Resolution (Tier 1 & Tier 2)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val simpleIcon = remember(notification.appIcon, notification.appName) {
                            val icon = IconMapper.getSimpleIcon(notification.appIcon)
                                ?: IconMapper.getSimpleIcon(notification.appName)
                            if (icon == null) {
                                Log.d("IconMapper", "Failed to map icon: appIcon='${notification.appIcon}', appName='${notification.appName}'")
                            }
                            icon
                        }

                        if (simpleIcon != null) {
                            // Tier 1: Local High-Fidelity Vector
                            Icon(
                                imageVector = simpleIcon,
                                contentDescription = notification.appName,
                                modifier = Modifier.size(40.dp),
                                tint = Color.Unspecified
                            )
                        } else if (notification.appIconBase64?.isNotBlank() == true) {
                            // Tier 2: Host-provided Base64 (Coil 3 handles data URIs)
                            AsyncImage(
                                model = notification.appIconBase64,
                                contentDescription = notification.appName,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else if (notification.appIcon.isNotBlank() && !notification.appIcon.contains("/")) {
                            // Try loading by name via Coil
                            AsyncImage(
                                model = notification.appIcon,
                                contentDescription = notification.appName,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Fallback: Placeholder
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.padding(12.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Text Content
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = notification.summary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (notification.body.isNotBlank()) {
                            Text(
                                text = notification.body,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Action Resolution
                    val actions = remember(notification.actions) {
                        if (notification.actions.size >= 2) {
                            notification.actions.chunked(2).mapNotNull { 
                                if (it.size == 2) it[0] to it[1] else null
                            }
                        } else {
                            notification.actions.map { it to it }
                        }
                    }

                    if (actions.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val displayAction = actions.find { it.first != "default" } ?: actions.first()
                            
                            TextButton(
                                onClick = { onActionClick(displayAction.first) },
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = displayAction.second,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun NotificationBannerPreview() {
    PCDashboardTheme {
        NotificationBanner(
            notification = PcNotification(
                id = 1,
                appName = "Discord",
                summary = "New message from User",
                body = "This is a notification body that might be quite long and should be readable.",
                appIcon = "discord",
                actions = listOf("reply", "Reply", "dismiss", "Dismiss"),
                timestamp = System.currentTimeMillis()
            ),
            visible = true,
            onActionClick = {}
        )
    }
}
