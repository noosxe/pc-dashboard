package com.noosxe.pc_dashboard.ui.components

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.noosxe.pc_dashboard.data.PcNotification

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

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

    var iconModel by remember(notification.id) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(notification.appIconBase64, visible) {
        val base64 = notification.appIconBase64
        if (visible && base64 != null && base64.isNotBlank()) {
            withContext(Dispatchers.Default) {
                val startTime = System.currentTimeMillis()
                val decoded = try {
                    val pureBase64 = base64.substringAfter("base64,")
                    android.util.Base64.decode(pureBase64, android.util.Base64.DEFAULT)
                } catch (_: Exception) {
                    null
                }
                val duration = System.currentTimeMillis() - startTime
                Log.d("PERF_LATENCY", "UI_DECODE_BASE64 id=${notification.id} duration=${duration}ms size=${base64.length}")
                iconModel = decoded
            }
        } else {
            iconModel = null
        }
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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // App Icon Resolution (Tier 1 & Tier 2)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val simpleIcon = remember(notification.appIcon) {
                            IconMapper.getSimpleIcon(notification.appIcon)
                        }

                        if (simpleIcon != null) {
                            // Tier 1: Local High-Fidelity Vector
                            Icon(
                                imageVector = simpleIcon,
                                contentDescription = notification.appName,
                                modifier = Modifier.size(28.dp),
                                tint = Color.Unspecified // SimpleIcons are often colored or we use onSurfaceVariant
                            )
                        } else if (iconModel != null) {
                            // Tier 2: Host-provided Base64
                            AsyncImage(
                                model = iconModel,
                                contentDescription = notification.appName,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else if (notification.appIcon.isNotBlank() && !notification.appIcon.contains("/")) {
                            // Try loading by name via Coil (might resolve some system icons)
                            AsyncImage(
                                model = notification.appIcon,
                                contentDescription = notification.appName,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Fallback: Placeholder
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    modifier = Modifier.padding(8.dp),
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
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (notification.body.isNotBlank()) {
                            Text(
                                text = notification.body,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
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
                                    style = MaterialTheme.typography.labelLarge,
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
