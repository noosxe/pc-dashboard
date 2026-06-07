package com.noosxe.pc_dashboard.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SmartStatCard(
    title: String,
    mainValue: String,
    secondaryValue: String,
    mainHistory: List<Float>,
    mainChartColor: Color,
    mainMax: Float,
    modifier: Modifier = Modifier,
    mainLabel: String? = null,
    secondaryHistory: List<Float>? = null,
    secondaryChartColor: Color? = null,
    secondaryMax: Float? = null,
    secondaryLabel: String? = null
) {
    Card(
        modifier = modifier.aspectRatio(1f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            TelemetryChart(
                mainData = mainHistory,
                mainColor = mainChartColor,
                mainMax = mainMax,
                secondaryData = secondaryHistory,
                secondaryColor = secondaryChartColor ?: mainChartColor.copy(alpha = 0.7f),
                secondaryMax = secondaryMax ?: 100f,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (mainLabel != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MetricLegendItem(label = mainLabel, color = mainChartColor)
                    }
                    
                    if (secondaryLabel != null && secondaryHistory != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MetricLegendItem(label = secondaryLabel, color = secondaryChartColor ?: mainChartColor.copy(alpha = 0.7f))
                    }
                }
                
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Radial scrim to desaturate/dim the chart behind the text
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.8f)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = mainValue,
                            style = MaterialTheme.typography.displaySmall.copy(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = secondaryValue,
                            style = MaterialTheme.typography.bodySmall.copy(
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
