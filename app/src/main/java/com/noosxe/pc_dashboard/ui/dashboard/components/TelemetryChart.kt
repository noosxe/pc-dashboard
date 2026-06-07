package com.noosxe.pc_dashboard.ui.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

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
