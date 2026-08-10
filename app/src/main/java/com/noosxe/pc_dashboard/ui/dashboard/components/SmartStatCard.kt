package com.noosxe.pc_dashboard.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import com.noosxe.pc_dashboard.ui.theme.PCDashboardTheme

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
    secondaryLabel: String? = null,
    bottomLeftValue: String? = null,
    bottomRightValue: String? = null
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

                if (bottomLeftValue != null || bottomRightValue != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (bottomLeftValue != null) {
                            Text(
                                text = bottomLeftValue,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = Offset(1f, 1f),
                                        blurRadius = 2f
                                    )
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(1.dp))
                        }

                        if (bottomRightValue != null) {
                            Text(
                                text = bottomRightValue,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = Offset(1f, 1f),
                                        blurRadius = 2f
                                    )
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
fun CpuStatCardPreview() {
    PCDashboardTheme {
        SmartStatCard(
            title = "CPU",
            mainValue = "25%",
            secondaryValue = "50°C",
            mainHistory = listOf(10f, 20f, 30f, 25f, 40f, 35f, 25f),
            mainChartColor = Color(0xFF4FC3F7),
            mainMax = 100f,
            mainLabel = "%",
            secondaryHistory = listOf(45f, 48f, 52f, 50f, 55f, 53f, 50f),
            secondaryChartColor = Color(0xFFFF7043),
            secondaryMax = 100f,
            secondaryLabel = "°C",
            bottomLeftValue = "3600 MHz",
            bottomRightValue = "65 W"
        )
    }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
fun GpuStatCardPreview() {
    PCDashboardTheme {
        SmartStatCard(
            title = "GPU",
            mainValue = "40%",
            secondaryValue = "60°C",
            mainHistory = listOf(20f, 30f, 50f, 40f, 60f, 45f, 40f),
            mainChartColor = Color(0xFF81C784),
            mainMax = 100f,
            mainLabel = "%",
            secondaryHistory = listOf(55f, 58f, 62f, 60f, 65f, 63f, 60f),
            secondaryChartColor = Color(0xFFFF7043),
            secondaryMax = 100f,
            secondaryLabel = "°C",
            bottomLeftValue = "1800 MHz",
            bottomRightValue = "150 W"
        )
    }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
fun RamStatCardPreview() {
    PCDashboardTheme {
        SmartStatCard(
            title = "RAM",
            mainValue = "8.5 GB",
            secondaryValue = "of 16.0 GB",
            mainHistory = listOf(8.0f, 8.2f, 8.5f, 8.4f, 8.6f, 8.5f, 8.5f),
            mainChartColor = Color(0xFFFFB74D),
            mainMax = 16f,
            mainLabel = "GB"
        )
    }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
fun VramStatCardPreview() {
    PCDashboardTheme {
        SmartStatCard(
            title = "VRAM",
            mainValue = "4.2 GB",
            secondaryValue = "of 8.0 GB",
            mainHistory = listOf(3.5f, 3.8f, 4.0f, 4.2f, 4.5f, 4.3f, 4.2f),
            mainChartColor = Color(0xFFBA68C8),
            mainMax = 8f,
            mainLabel = "GB"
        )
    }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
fun SwapStatCardPreview() {
    PCDashboardTheme {
        SmartStatCard(
            title = "Swap",
            mainValue = "1.5 GB",
            secondaryValue = "of 8.0 GB",
            mainHistory = listOf(1.0f, 1.2f, 1.4f, 1.5f, 1.6f, 1.5f, 1.5f),
            mainChartColor = Color(0xFF90A4AE),
            mainMax = 8f,
            mainLabel = "GB"
        )
    }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
fun ZramStatCardPreview() {
    PCDashboardTheme {
        SmartStatCard(
            title = "zRAM",
            mainValue = "0.8 GB",
            secondaryValue = "Ratio: 2.50",
            mainHistory = listOf(0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 0.8f, 0.8f),
            mainChartColor = Color(0xFFD4E157),
            mainMax = 4f,
            mainLabel = "GB"
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 650)
@Composable
fun SmartStatCardsGridPreview() {
    PCDashboardTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) { CpuStatCardPreview() }
                    Box(modifier = Modifier.weight(1f)) { GpuStatCardPreview() }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) { RamStatCardPreview() }
                    Box(modifier = Modifier.weight(1f)) { VramStatCardPreview() }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) { SwapStatCardPreview() }
                    Box(modifier = Modifier.weight(1f)) { ZramStatCardPreview() }
                }
            }
        }
    }
}
