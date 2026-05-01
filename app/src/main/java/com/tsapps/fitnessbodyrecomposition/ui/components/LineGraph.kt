package com.tsapps.fitnessbodyrecomposition.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.tsapps.fitnessbodyrecomposition.ui.theme.NeonGreen

@Composable
fun LineGraph(
    dataPoints: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = NeonGreen
) {
    if (dataPoints.isEmpty()) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val isSinglePoint = dataPoints.size == 1
        val spacing = if (isSinglePoint) 0f else width / (dataPoints.size - 1)
        
        val maxData = dataPoints.maxOrNull() ?: 1.0
        val minData = dataPoints.minOrNull() ?: 0.0
        val range = maxData - minData
        
        // Helper to map data value to Y coordinate
        fun yCoord(value: Double): Float {
            val normalized = (value - minData) / (if (range == 0.0) 1.0 else range)
            return height - (normalized.toFloat() * height * 0.8f) - (height * 0.1f)
        }

        val path = Path()
        val fillPath = Path()
        
        dataPoints.forEachIndexed { index, value ->
            val x = if (isSinglePoint) width / 2 else index * spacing
            val y = yCoord(value)
            
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                val prevX = (index - 1) * spacing
                val prevY = yCoord(dataPoints[index - 1])
                
                val controlPoint1X = prevX + (x - prevX) / 2
                val controlPoint1Y = prevY
                val controlPoint2X = prevX + (x - prevX) / 2
                val controlPoint2Y = y
                
                path.cubicTo(controlPoint1X, controlPoint1Y, controlPoint2X, controlPoint2Y, x, y)
                fillPath.cubicTo(controlPoint1X, controlPoint1Y, controlPoint2X, controlPoint2Y, x, y)
            }
        }
        
        if (!isSinglePoint) {
            fillPath.lineTo(width, height)
            fillPath.close()

            // Draw Gradient Fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )

            // Draw Line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
        
        // Draw Points
        dataPoints.forEachIndexed { index, value ->
            val x = if (isSinglePoint) width / 2 else index * spacing
            val y = yCoord(value)
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
            drawCircle(
                color = Color.Black,
                radius = 2.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}
