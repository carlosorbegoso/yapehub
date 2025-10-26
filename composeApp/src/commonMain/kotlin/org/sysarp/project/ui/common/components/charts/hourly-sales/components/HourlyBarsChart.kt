package org.sysarp.project.ui.common.components.charts.hourly.sales.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.HourlySalesData
import org.sysarp.project.ui.common.components.charts.hourly.sales.data.calculateBarColor

/**
 * Componente de gráfico de barras para ventas por hora
 */
@Composable
fun HourlyBarsChart(
    hourlySales: List<HourlySalesData>,
    onHourSelected: (HourlySalesData) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxSales = hourlySales.maxOfOrNull { it.sales } ?: 0.0
    val animationProgress = remember { Animatable(0f) }
    var selectedIndex by remember { mutableStateOf(-1) }
    val density = LocalDensity.current
    
    // Obtener colores del tema fuera del Canvas
    val primaryColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
    val secondaryColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary
    val outlineColor = androidx.compose.material3.MaterialTheme.colorScheme.outline

    LaunchedEffect(hourlySales) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(hourlySales) {
                detectTapGestures { offset ->
                    val barWidth = size.width / 24f
                    val tappedIndex = (offset.x / barWidth).toInt().coerceIn(0, hourlySales.size - 1)
                    selectedIndex = tappedIndex
                    onHourSelected(hourlySales[tappedIndex])
                }
            }
    ) {
        drawHourlyBars(
            hourlySales = hourlySales,
            maxSales = maxSales,
            animationProgress = animationProgress.value,
            canvasSize = size,
            selectedIndex = selectedIndex,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            outlineColor = outlineColor,
            density = density
        )
    }
}

/**
 * Función de dibujo para las barras del gráfico
 */
private fun DrawScope.drawHourlyBars(
    hourlySales: List<HourlySalesData>,
    maxSales: Double,
    animationProgress: Float,
    canvasSize: Size,
    selectedIndex: Int = -1,
    primaryColor: Color,
    secondaryColor: Color,
    outlineColor: Color,
    density: androidx.compose.ui.unit.Density
) {
    val barWidth = canvasSize.width / 24f
    val maxBarHeight = canvasSize.height * 0.7f
    val startY = canvasSize.height * 0.1f
    val labelAreaHeight = canvasSize.height * 0.2f

    // Dibujar líneas de referencia
    drawReferenceLines(maxSales, canvasSize, maxBarHeight, startY, primaryColor, secondaryColor)
    
    // Dibujar etiquetas de horas
    drawHourLabels(hourlySales, canvasSize, barWidth, labelAreaHeight, outlineColor)

    hourlySales.forEachIndexed { index, hourData ->
        val barHeight = if (maxSales > 0) {
            (hourData.sales / maxSales * maxBarHeight * animationProgress).toFloat()
        } else 0f

        val x = index * barWidth
        val y = startY + maxBarHeight - barHeight
        val isSelected = index == selectedIndex

        // Sistema de colores mejorado
        val baseColor = calculateBarColor(hourData.sales, maxSales, primaryColor)

        // Color final con efecto de selección
        val finalColor = if (isSelected) {
            baseColor.copy(alpha = 0.9f)
        } else {
            baseColor
        }

        // Dibujar barra con gradiente sutil
        drawRect(
            color = finalColor,
            topLeft = Offset(x + 2, y),
            size = Size(barWidth - 4, barHeight)
        )

        // Dibujar borde de selección mejorado
        if (isSelected) {
            drawRect(
                color = primaryColor,
                topLeft = Offset(x + 1, y - 2),
                size = Size(barWidth - 2, barHeight + 4),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }
    }
}

/**
 * Dibuja líneas de referencia en el gráfico
 */
private fun DrawScope.drawReferenceLines(
    maxSales: Double,
    canvasSize: Size,
    maxBarHeight: Float,
    startY: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    // Línea de valor máximo
    val maxLineY = startY
    drawLine(
        color = primaryColor.copy(alpha = 0.3f),
        start = Offset(0f, maxLineY),
        end = Offset(canvasSize.width, maxLineY),
        strokeWidth = 1.dp.toPx()
    )
    
    // Línea de promedio
    val avgLineY = startY + maxBarHeight * 0.5f
    drawLine(
        color = secondaryColor.copy(alpha = 0.3f),
        start = Offset(0f, avgLineY),
        end = Offset(canvasSize.width, avgLineY),
        strokeWidth = 1.dp.toPx()
    )
}

/**
 * Dibuja etiquetas de horas en el gráfico
 */
private fun DrawScope.drawHourLabels(
    hourlySales: List<HourlySalesData>,
    canvasSize: Size,
    barWidth: Float,
    labelAreaHeight: Float,
    outlineColor: Color
) {
    // Dibujar etiquetas cada 3 horas para mejor legibilidad
    hourlySales.forEachIndexed { index, hourData ->
        if (index % 3 == 0) {
            val x = index * barWidth + barWidth / 2
            
            // Dibujar línea vertical de referencia
            drawLine(
                color = outlineColor.copy(alpha = 0.2f),
                start = Offset(x, canvasSize.height * 0.1f),
                end = Offset(x, canvasSize.height * 0.8f),
                strokeWidth = 0.5.dp.toPx()
            )
        }
    }
}
