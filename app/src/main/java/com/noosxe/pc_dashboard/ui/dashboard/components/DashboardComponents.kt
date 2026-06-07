package com.noosxe.pc_dashboard.ui.dashboard.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
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

@Composable
fun TelemetryChart(
    mainData: List<Float>,
    mainColor: Color,
    mainMax: Float,
    modifier: Modifier = Modifier,
    secondaryData: List<Float>? = null,
    secondaryColor: Color? = null,
    secondaryMax: Float? = null
) {
    if (mainData.isEmpty()) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val spacing = width / 59f // Assuming 60 points max

        // Draw Secondary Data First (Glow + Line)
        if (secondaryData != null && secondaryColor != null && secondaryMax != null && secondaryData.isNotEmpty()) {
            val secondaryPath = Path()
            secondaryData.forEachIndexed { index, value ->
                val x = index * spacing
                val y = height - (value / secondaryMax).coerceIn(0f, 1f) * height
                if (index == 0) secondaryPath.moveTo(x, y) else secondaryPath.lineTo(x, y)
            }
            
            // Glow effect (wide, faint line)
            drawPath(
                path = secondaryPath,
                color = secondaryColor.copy(alpha = 0.2f),
                style = Stroke(width = 6.dp.toPx())
            )
            
            // Solid line
            drawPath(
                path = secondaryPath,
                color = secondaryColor.copy(alpha = 0.6f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Draw Main Data (Area + Line)
        val mainPath = Path()
        val mainAreaPath = Path()

        mainData.forEachIndexed { index, value ->
            val x = index * spacing
            val y = height - (value / mainMax).coerceIn(0f, 1f) * height

            if (index == 0) {
                mainPath.moveTo(x, y)
                mainAreaPath.moveTo(x, height)
                mainAreaPath.lineTo(x, y)
            } else {
                mainPath.lineTo(x, y)
                mainAreaPath.lineTo(x, y)
            }
            
            if (index == mainData.lastIndex) {
                mainAreaPath.lineTo(x, height)
                mainAreaPath.close()
            }
        }

        drawPath(
            path = mainAreaPath,
            brush = Brush.verticalGradient(
                colors = listOf(mainColor.copy(alpha = 0.3f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        drawPath(
            path = mainPath,
            color = mainColor.copy(alpha = 0.5f),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
private fun MetricLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Canvas(modifier = Modifier.size(width = 8.dp, height = 2.dp)) {
            drawRect(color = color.copy(alpha = 0.7f))
        }
    }
}
