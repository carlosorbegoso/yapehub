package org.sysarp.project.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.DailySalesData
import org.sysarp.project.utils.formatCurrency

/**
 * Componente reutilizable para mostrar un gráfico de barras de ventas diarias
 * 
 * @param dailySales Lista de datos de ventas diarias
 * @param title Título del gráfico (opcional)
 * @param showStats Si mostrar estadísticas adicionales (por defecto true)
 * @param modifier Modificador para el componente
 */
@Composable
fun DailySalesBarChart(
    dailySales: List<DailySalesData>,
    title: String = "Ventas Diarias",
    showStats: Boolean = true,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título del gráfico
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Analytics,
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
            val maxSales = dailySales.maxOfOrNull { it.sales } ?: 1.0
            val chartData = dailySales.take(7) // Últimos 7 días
            val animationProgress = remember { Animatable(0f) }
            
            // Animar la entrada del gráfico
            LaunchedEffect(chartData) {
                animationProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1200, delayMillis = 200)
                )
            }
            
            val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
            val primaryColor = MaterialTheme.colorScheme.primary
            val outlineColor = MaterialTheme.colorScheme.outline
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                surfaceVariantColor.copy(alpha = 0.1f),
                                surfaceVariantColor.copy(alpha = 0.3f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barWidth = canvasWidth / chartData.size * 0.75f
                    val spacing = canvasWidth / chartData.size * 0.25f
                    
                    // Dibujar línea de referencia
                    val referenceY = canvasHeight - 30f
                    drawLine(
                        color = outlineColor.copy(alpha = 0.3f),
                        start = Offset(0f, referenceY),
                        end = Offset(canvasWidth, referenceY),
                        strokeWidth = 1.dp.toPx()
                    )
                    
                    chartData.forEachIndexed { index, dayData ->
                        val x = index * (barWidth + spacing) + spacing / 2
                        val targetHeight = if (maxSales > 0) {
                            (dayData.sales / maxSales * (canvasHeight - 50)).toFloat()
                        } else 0f
                        
                        // Aplicar animación
                        val animatedHeight = targetHeight * animationProgress.value
                        val y = canvasHeight - animatedHeight - 30f
                        
                        // Dibujar sombra de la barra
                        drawRect(
                            color = Color.Black.copy(alpha = 0.1f),
                            topLeft = Offset(x + 2f, y + 2f),
                            size = Size(barWidth, animatedHeight)
                        )
                        
                        // Dibujar barra principal con gradiente
                        val barColor = if (dayData.sales > 0) {
                            primaryColor.copy(alpha = 0.8f)
                        } else {
                            surfaceVariantColor.copy(alpha = 0.5f)
                        }
                        
                        drawRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, animatedHeight)
                        )
                        
                        // Dibujar borde superior de la barra
                        if (animatedHeight > 0) {
                            drawLine(
                                color = barColor.copy(alpha = 1f),
                                start = Offset(x, y),
                                end = Offset(x + barWidth, y),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                        
                        // Dibujar valor en la parte superior de la barra
                        if (dayData.sales > 0 && animationProgress.value > 0.8f) {
                            val valueText = formatCurrency(dayData.sales)
                            // Simular texto con un pequeño rectángulo
                            drawRect(
                                color = Color.White,
                                topLeft = Offset(x + barWidth/2 - 20f, y - 20f),
                                size = Size(40f, 16f)
                            )
                        }
                    }
                }
            }
            
            // Etiquetas de los días
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                chartData.forEach { dayData ->
                    Text(
                        text = dayData.dayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Estadísticas adicionales (opcional)
            if (showStats) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Período: ${dailySales.size} días",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Mejor día: ${dailySales.maxByOrNull { it.sales }?.dayName ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Total: ${formatCurrency(dailySales.sumOf { it.sales })}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Promedio: ${formatCurrency(dailySales.map { it.sales }.average())}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
