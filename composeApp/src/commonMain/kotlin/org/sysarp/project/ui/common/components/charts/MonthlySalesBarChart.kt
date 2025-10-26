package org.sysarp.project.ui.common.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.MonthlySalesData
import org.sysarp.project.utils.formatCurrency

/**
 * Componente reutilizable para mostrar un gráfico de barras de ventas mensuales
 * 
 * @param monthlySales Lista de datos de ventas mensuales
 * @param title Título del gráfico (opcional)
 * @param showStats Si mostrar estadísticas adicionales (por defecto true)
 * @param showCard Si mostrar el componente dentro de un Card
 * @param modifier Modificador para el componente
 */
@Composable
fun MonthlySalesBarChart(
    monthlySales: List<MonthlySalesData>,
    title: String = "Ventas Mensuales",
    showStats: Boolean = true,
    showCard: Boolean = true,
    modifier: Modifier = Modifier
) {
    var selectedMonth by remember { mutableStateOf<MonthlySalesData?>(null) }
    
    val chartContent = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (showCard) 16.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título del gráfico
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.BarChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Gráfico de barras animado
            val maxSales = if (monthlySales.isNotEmpty()) {
                monthlySales.maxOfOrNull { it.sales } ?: 1.0
            } else {
                1.0
            }
            val chartData = if (monthlySales.isNotEmpty()) {
                monthlySales.takeLast(6) // Últimos 6 meses
            } else {
                emptyList()
            }
            val animationProgress = remember { Animatable(0f) }
            
            // Animar la entrada del gráfico
            LaunchedEffect(chartData) {
                animationProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1500, delayMillis = 300)
                )
            }
            
            val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
            val primaryColor = MaterialTheme.colorScheme.primary
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                surfaceVariantColor.copy(alpha = 0.1f),
                                surfaceVariantColor.copy(alpha = 0.3f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                if (chartData.isEmpty()) {
                    // Estado vacío para el gráfico
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Sin datos mensuales",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(chartData) {
                                detectTapGestures { offset ->
                                    val canvasWidth = size.width
                                    val canvasHeight = size.height
                                    val padding = 40f
                                    val barWidth = (canvasWidth - 2 * padding) / chartData.size
                                    
                                    // Calcular qué barra fue tocada
                                    val tappedBarIndex = ((offset.x - padding) / barWidth).toInt()
                                    
                                    if (tappedBarIndex >= 0 && tappedBarIndex < chartData.size) {
                                        selectedMonth = chartData[tappedBarIndex]
                                    }
                                }
                            }
                    ) {
                        drawBarChart(
                            chartData = chartData,
                            maxSales = maxSales,
                            animationProgress = animationProgress.value,
                            primaryColor = primaryColor
                        )
                    }
                }
            }
            
            // Etiquetas de los meses (solo si hay datos)
            if (chartData.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    chartData.forEach { monthData ->
                        Text(
                            text = formatMonthName(monthData.month),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Estadísticas adicionales (opcional)
            if (showStats && monthlySales.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Período: ${monthlySales.size} meses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Promedio: ${formatCurrency(monthlySales.map { it.sales }.average())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Total: ${formatCurrency(monthlySales.sumOf { it.sales })}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Mejor mes: ${monthlySales.maxByOrNull { it.sales }?.let { formatMonthName(it.month) } ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Renderizar con o sin Card según el parámetro
    if (showCard) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            chartContent()
        }
    } else {
        Box(modifier = modifier.fillMaxWidth()) {
            chartContent()
        }
    }
}

/**
 * Función helper para dibujar el gráfico de barras
 */
private fun DrawScope.drawBarChart(
    chartData: List<MonthlySalesData>,
    maxSales: Double,
    animationProgress: Float,
    primaryColor: Color
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val padding = 40f
    val barWidth = (canvasWidth - 2 * padding) / chartData.size
    val maxBarHeight = canvasHeight - 2 * padding
    
    chartData.forEachIndexed { index, monthData ->
        val barHeight = if (maxSales > 0) {
            (monthData.sales / maxSales * maxBarHeight * animationProgress).toFloat()
        } else {
            0f
        }
        
        val x = padding + index * barWidth + barWidth * 0.1f
        val y = canvasHeight - padding - barHeight
        val actualBarWidth = barWidth * 0.8f
        
        // Dibujar sombra de la barra
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.2f),
            topLeft = Offset(x + 2f, y + 2f),
            size = Size(actualBarWidth, barHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
        
        // Dibujar barra principal con gradiente
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor,
                    primaryColor.copy(alpha = 0.8f)
                )
            ),
            topLeft = Offset(x, y),
            size = Size(actualBarWidth, barHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
        
        // Dibujar valor en la parte superior de la barra
        if (barHeight > 30f) {
            val textX = x + actualBarWidth / 2
            val textY = y - 5f
            
            // Fondo para el texto
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = 15f,
                center = Offset(textX, textY)
            )
            
            // Texto del valor (simulado con círculo de color)
            drawCircle(
                color = primaryColor,
                radius = 8f,
                center = Offset(textX, textY)
            )
        }
    }
}

/**
 * Función helper para formatear el nombre del mes desde el formato YYYY-MM
 */
private fun formatMonthName(monthString: String): String {
    return try {
        val parts = monthString.split("-")
        if (parts.size == 2) {
            val monthNumber = parts[1].toInt()
            val monthNames = listOf(
                "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
            )
            if (monthNumber in 1..12) {
                monthNames[monthNumber - 1]
            } else {
                monthString
            }
        } else {
            monthString
        }
    } catch (e: Exception) {
        monthString
    }
}
