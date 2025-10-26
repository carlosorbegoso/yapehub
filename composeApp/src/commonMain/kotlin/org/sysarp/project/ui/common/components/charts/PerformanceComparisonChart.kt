package org.sysarp.project.ui.common.components.charts

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compare
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.PerformanceMetricsData
import org.sysarp.project.utils.formatPercentage

/**
 * Componente reutilizable para mostrar un gráfico de comparación de rendimiento
 * 
 * @param currentMetrics Métricas actuales
 * @param previousMetrics Métricas del período anterior (opcional)
 * @param title Título del gráfico (opcional)
 * @param showCard Si mostrar el componente dentro de un Card
 * @param modifier Modificador para el componente
 */
@Composable
fun PerformanceComparisonChart(
    currentMetrics: PerformanceMetricsData,
    previousMetrics: PerformanceMetricsData? = null,
    title: String = "Comparación de Rendimiento",
    showCard: Boolean = true,
    modifier: Modifier = Modifier
) {
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
                    imageVector = Icons.Filled.Compare,
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
            
            // Gráfico de comparación
            val animationProgress = remember { Animatable(0f) }
            
            // Animar la entrada del gráfico
            LaunchedEffect(currentMetrics, previousMetrics) {
                animationProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1200, delayMillis = 200)
                )
            }
            
            val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val tertiaryColor = MaterialTheme.colorScheme.tertiary
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
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
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawComparisonChart(
                        currentMetrics = currentMetrics,
                        previousMetrics = previousMetrics,
                        animationProgress = animationProgress.value,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        tertiaryColor = tertiaryColor
                    )
                }
            }
            
            // Leyenda de comparación
            if (previousMetrics != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ComparisonLegendItem(
                        label = "Actual",
                        color = primaryColor
                    )
                    ComparisonLegendItem(
                        label = "Anterior",
                        color = secondaryColor
                    )
                }
            }
            
            // Estadísticas de comparación
            if (previousMetrics != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ComparisonStatRow(
                        label = "Tasa de éxito",
                        current = currentMetrics.claimRate,
                        previous = previousMetrics.claimRate
                    )
                    ComparisonStatRow(
                        label = "Tiempo promedio",
                        current = currentMetrics.averageConfirmationTime,
                        previous = previousMetrics.averageConfirmationTime,
                        isTime = true
                    )
                    ComparisonStatRow(
                        label = "Tasa de rechazo",
                        current = currentMetrics.rejectionRate,
                        previous = previousMetrics.rejectionRate
                    )
                }
            } else {
                Text(
                    text = "No hay datos de comparación disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
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
 * Función helper para dibujar el gráfico de comparación
 */
private fun DrawScope.drawComparisonChart(
    currentMetrics: PerformanceMetricsData,
    previousMetrics: PerformanceMetricsData?,
    animationProgress: Float,
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val padding = 30f
    val barWidth = 60f
    val spacing = 20f
    
    // Calcular métricas normalizadas (0-100)
    val currentSuccess = (currentMetrics.claimRate * 100).toFloat()
    val currentTime = (100f - (currentMetrics.averageConfirmationTime / 10.0).coerceIn(0.0, 100.0)).toFloat()
    val currentRejection = (currentMetrics.rejectionRate * 100).toFloat()
    
    val previousSuccess = previousMetrics?.let { (it.claimRate * 100).toFloat() } ?: 0f
    val previousTime = previousMetrics?.let { (100f - (it.averageConfirmationTime / 10.0).coerceIn(0.0, 100.0)).toFloat() } ?: 0f
    val previousRejection = previousMetrics?.let { (it.rejectionRate * 100).toFloat() } ?: 0f
    
    val metrics = listOf(
        Triple("Éxito", currentSuccess, previousSuccess),
        Triple("Tiempo", currentTime, previousTime),
        Triple("Rechazo", currentRejection, previousRejection)
    )
    
    metrics.forEachIndexed { index, (label, current, previous) ->
        val x = padding + index * (barWidth + spacing)
        val maxHeight = canvasHeight - 2 * padding
        
        // Dibujar barra actual
        val currentHeight = (current / 100f) * maxHeight * animationProgress
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor, primaryColor.copy(alpha = 0.8f))
            ),
            topLeft = Offset(x, canvasHeight - padding - currentHeight),
            size = Size(barWidth * 0.4f, currentHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        
        // Dibujar barra anterior si existe
        if (previousMetrics != null) {
            val previousHeight = (previous / 100f) * maxHeight * animationProgress
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(secondaryColor, secondaryColor.copy(alpha = 0.8f))
                ),
                topLeft = Offset(x + barWidth * 0.5f, canvasHeight - padding - previousHeight),
                size = Size(barWidth * 0.4f, previousHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }
        
        // Dibujar etiqueta
        val labelY = canvasHeight - padding + 15f
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = 20f,
            center = Offset(x + barWidth / 2, labelY)
        )
    }
}

@Composable
private fun ComparisonLegendItem(
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color = color, shape = CircleShape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ComparisonStatRow(
    label: String,
    current: Double,
    previous: Double,
    isTime: Boolean = false
) {
    val change = current - previous
    val changeColor = when {
        change > 0 && !isTime -> MaterialTheme.colorScheme.primary // Mejor rendimiento
        change < 0 && isTime -> MaterialTheme.colorScheme.primary // Menos tiempo es mejor
        else -> MaterialTheme.colorScheme.error
    }
    
    val changeIcon = when {
        change > 0 && !isTime -> "📈"
        change < 0 && isTime -> "📈"
        change < 0 && !isTime -> "📉"
        change > 0 && isTime -> "📉"
        else -> "➡️"
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = changeIcon,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (isTime) {
                    "${formatPercentage(change.toDouble())} min"
                } else {
                    formatPercentage(change.toDouble())
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = changeColor
            )
        }
    }
}
