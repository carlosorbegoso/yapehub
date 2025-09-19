package org.sysarp.project.ui.components.charts

import androidx.compose.animation.core.Animatable
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
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.DailySalesData
import org.sysarp.project.utils.formatCurrency

/**
 * Componente reutilizable para mostrar un gráfico de líneas de tendencia de ventas
 * 
 * @param dailySales Lista de datos de ventas diarias
 * @param title Título del gráfico (opcional)
 * @param showPoints Si mostrar puntos en los datos (por defecto true)
 * @param showStats Si mostrar estadísticas adicionales (por defecto true)
 * @param modifier Modificador para el componente
 */
@Composable
fun SalesTrendLineChart(
    dailySales: List<DailySalesData>,
    title: String = "Tendencia de Ventas",
    showPoints: Boolean = true,
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
                    imageVector = Icons.Filled.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Gráfico de líneas animado
            val maxSales = dailySales.maxOfOrNull { it.sales } ?: 1.0
            val chartData = dailySales.take(7) // Últimos 7 días
            val animationProgress = remember { Animatable(0f) }
            
            // Animar la entrada del gráfico
            LaunchedEffect(chartData) {
                animationProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1800, delayMillis = 400)
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
                    val padding = 25f
                    
                    // Calcular puntos de la línea
                    val points = chartData.mapIndexed { index, dayData ->
                        val x = padding + (index * (canvasWidth - 2 * padding) / (chartData.size - 1))
                        val y = canvasHeight - padding - if (maxSales > 0) {
                            (dayData.sales / maxSales * (canvasHeight - 2 * padding)).toFloat()
                        } else {
                            canvasHeight - 2 * padding
                        }
                        Offset(x, y)
                    }
                    
                    // Dibujar línea de referencia (promedio)
                    val averageSales = chartData.map { it.sales }.average()
                    if (averageSales > 0 && maxSales > 0) {
                        val averageY = canvasHeight - padding - (averageSales / maxSales * (canvasHeight - 2 * padding)).toFloat()
                        drawLine(
                            color = outlineColor.copy(alpha = 0.3f),
                            start = Offset(padding, averageY),
                            end = Offset(canvasWidth - padding, averageY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    
                    // Dibujar línea de tendencia animada
                    if (points.size > 1) {
                        val animatedPoints = points.mapIndexed { index, point ->
                            val progress = (index.toFloat() / (points.size - 1)) * animationProgress.value
                            if (progress <= animationProgress.value) {
                                point
                            } else {
                                // Interpolar desde el punto anterior
                                val prevIndex = maxOf(0, index - 1)
                                val prevPoint = points[prevIndex]
                                val t = (animationProgress.value - (prevIndex.toFloat() / (points.size - 1))) * (points.size - 1)
                                Offset(
                                    prevPoint.x + (point.x - prevPoint.x) * t,
                                    prevPoint.y + (point.y - prevPoint.y) * t
                                )
                            }
                        }
                        
                        val path = Path()
                        path.moveTo(animatedPoints[0].x, animatedPoints[0].y)
                        
                        for (i in 1 until animatedPoints.size) {
                            path.lineTo(animatedPoints[i].x, animatedPoints[i].y)
                        }
                        
                        // Dibujar línea principal
                        drawPath(
                            path = path,
                            color = primaryColor,
                            style = Stroke(width = 4.dp.toPx())
                        )
                        
                        // Dibujar línea de resplandor
                        drawPath(
                            path = path,
                            color = primaryColor.copy(alpha = 0.3f),
                            style = Stroke(width = 8.dp.toPx())
                        )
                    }
                    
                    // Dibujar puntos animados
                    if (showPoints) {
                        points.forEachIndexed { index, point ->
                            val pointProgress = (index.toFloat() / (points.size - 1)) * animationProgress.value
                            if (pointProgress <= animationProgress.value) {
                                // Dibujar sombra del punto
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.2f),
                                    radius = 6.dp.toPx(),
                                    center = Offset(point.x + 1f, point.y + 1f)
                                )
                                
                                // Dibujar punto principal
                                drawCircle(
                                    color = primaryColor,
                                    radius = 5.dp.toPx(),
                                    center = point
                                )
                                
                                // Dibujar punto interior
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.dp.toPx(),
                                    center = point
                                )
                            }
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
                            text = "Promedio: ${formatCurrency(dailySales.map { it.sales }.average())}",
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
                            text = "Máximo: ${formatCurrency(dailySales.maxOfOrNull { it.sales } ?: 0.0)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
