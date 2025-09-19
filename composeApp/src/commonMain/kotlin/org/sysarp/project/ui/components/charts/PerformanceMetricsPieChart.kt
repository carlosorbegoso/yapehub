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
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.PerformanceMetricsData
import org.sysarp.project.utils.formatOneDecimal
import org.sysarp.project.utils.formatPercentage

/**
 * Componente reutilizable para mostrar un gráfico circular de métricas de rendimiento
 * 
 * @param performanceMetrics Datos de métricas de rendimiento
 * @param title Título del gráfico (opcional)
 * @param showLegend Si mostrar la leyenda (por defecto true)
 * @param showStats Si mostrar estadísticas adicionales (por defecto true)
 * @param modifier Modificador para el componente
 */
@Composable
fun PerformanceMetricsPieChart(
    performanceMetrics: PerformanceMetricsData,
    title: String = "Estado de Pagos",
    showLegend: Boolean = true,
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
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Gráfico circular
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gráfico circular animado
                val totalPayments = performanceMetrics.confirmedPayments + 
                                  performanceMetrics.pendingPayments + 
                                  performanceMetrics.rejectedPayments
                val animationProgress = remember { Animatable(0f) }
                
                // Animar la entrada del gráfico
                LaunchedEffect(performanceMetrics) {
                    animationProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 1500, delayMillis = 300)
                    )
                }
                
                val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
                val surfaceColor = MaterialTheme.colorScheme.surface
                
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    surfaceVariantColor.copy(alpha = 0.1f),
                                    surfaceVariantColor.copy(alpha = 0.3f)
                                )
                            )
                        )
                        .padding(12.dp)
                ) {
                    if (totalPayments > 0) {
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val centerX = size.width / 2
                            val centerY = size.height / 2
                            val radius = minOf(centerX, centerY) - 15f
                            
                            var startAngle = -90f
                            
                            // Confirmados (Verde)
                            val confirmedAngle = (performanceMetrics.confirmedPayments.toFloat() / totalPayments) * 360f * animationProgress.value
                            drawArc(
                                color = Color(0xFF4CAF50),
                                startAngle = startAngle,
                                sweepAngle = confirmedAngle,
                                useCenter = true,
                                topLeft = Offset(centerX - radius, centerY - radius),
                                size = Size(radius * 2, radius * 2)
                            )
                            startAngle += confirmedAngle
                            
                            // Pendientes (Amarillo)
                            val pendingAngle = (performanceMetrics.pendingPayments.toFloat() / totalPayments) * 360f * animationProgress.value
                            drawArc(
                                color = Color(0xFFFFC107),
                                startAngle = startAngle,
                                sweepAngle = pendingAngle,
                                useCenter = true,
                                topLeft = Offset(centerX - radius, centerY - radius),
                                size = Size(radius * 2, radius * 2)
                            )
                            startAngle += pendingAngle
                            
                            // Rechazados (Rojo)
                            val rejectedAngle = (performanceMetrics.rejectedPayments.toFloat() / totalPayments) * 360f * animationProgress.value
                            drawArc(
                                color = Color(0xFFF44336),
                                startAngle = startAngle,
                                sweepAngle = rejectedAngle,
                                useCenter = true,
                                topLeft = Offset(centerX - radius, centerY - radius),
                                size = Size(radius * 2, radius * 2)
                            )
                            
                            // Dibujar círculo central
                            drawCircle(
                                color = surfaceColor,
                                radius = radius * 0.4f,
                                center = Offset(centerX, centerY)
                            )
                        }
                        
                        // Texto central animado
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$totalPayments",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Total",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // Sin datos
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sin datos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Leyenda (opcional)
                if (showLegend) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Confirmados
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        color = Color(0xFF4CAF50),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                            Column {
                                Text(
                                    text = "Confirmados",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${performanceMetrics.confirmedPayments} (${formatPercentage(performanceMetrics.claimRate)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Pendientes
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        color = Color(0xFFFFC107),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                            Column {
                                Text(
                                    text = "Pendientes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${performanceMetrics.pendingPayments}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Rechazados
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        color = Color(0xFFF44336),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                            Column {
                                Text(
                                    text = "Rechazados",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${performanceMetrics.rejectedPayments} (${formatPercentage(performanceMetrics.rejectionRate)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
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
                            text = "Tiempo promedio: ${formatOneDecimal(performanceMetrics.averageConfirmationTime)} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Tasa de éxito: ${formatPercentage(performanceMetrics.claimRate)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
