package org.sysarp.project.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import org.sysarp.project.data.AnalyticsOverview
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatPercentage

/**
 * Gráfico combinado que muestra múltiples métricas en un solo componente
 * Incluye animaciones dinámicas y efectos visuales
 */
@Composable
fun CombinedMetricsChart(
    overview: AnalyticsOverview,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        delay(300)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200, easing = EaseOutCubic)
        )
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📊 Métricas Combinadas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            // Gráfico principal combinado
            CombinedChart(
                overview = overview,
                animationProgress = animationProgress.value
            )
            
            // Métricas adicionales en filas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricIndicator(
                    label = "Crecimiento",
                    value = formatPercentage(overview.averageGrowth),
                    color = if (overview.averageGrowth >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    icon = if (overview.averageGrowth >= 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown
                )
                
                MetricIndicator(
                    label = "Promedio",
                    value = formatCurrency(overview.averageTransactionValue),
                    color = Color(0xFF2196F3),
                    icon = Icons.AutoMirrored.Filled.TrendingUp
                )
                
                MetricIndicator(
                    label = "Transacciones",
                    value = "${overview.totalTransactions}",
                    color = Color(0xFF9C27B0),
                    icon = Icons.Filled.TrendingUp
                )
            }
        }
    }
}

@Composable
private fun CombinedChart(
    overview: AnalyticsOverview,
    animationProgress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD).copy(alpha = 0.3f),
                        Color(0xFFBBDEFB).copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawCombinedChart(
                overview = overview,
                animationProgress = animationProgress,
                canvasSize = size
            )
        }
    }
}

private fun DrawScope.drawCombinedChart(
    overview: AnalyticsOverview,
    animationProgress: Float,
    canvasSize: Size
) {
    val centerX = canvasSize.width / 2f
    val centerY = canvasSize.height / 2f
    val baseRadius = minOf(canvasSize.width, canvasSize.height) * 0.3f
    val animatedRadius = maxOf(baseRadius * animationProgress, 1f) // Asegurar que siempre sea > 0
    
    // Círculo principal con gradiente
    val gradient = Brush.radialGradient(
        colors = listOf(
            Color(0xFF2196F3).copy(alpha = 0.8f),
            Color(0xFF1976D2).copy(alpha = 0.6f),
            Color(0xFF0D47A1).copy(alpha = 0.4f)
        ),
        radius = animatedRadius
    )
    
    drawCircle(
        brush = gradient,
        radius = animatedRadius,
        center = Offset(centerX, centerY)
    )
    
    // Anillos concéntricos animados
    val ringColors = listOf(
        Color(0xFF4CAF50),
        Color(0xFFFF9800),
        Color(0xFF9C27B0),
        Color(0xFFF44336)
    )
    
    ringColors.forEachIndexed { index, color ->
        val ringRadius = maxOf(animatedRadius * (0.6f + index * 0.1f), 1f)
        val ringWidth = 8.dp.toPx()
        
        drawCircle(
            color = color.copy(alpha = 0.7f),
            radius = ringRadius,
            center = Offset(centerX, centerY),
            style = Stroke(width = ringWidth)
        )
    }
    
    // Líneas radiales animadas
    val lineCount = 8
    for (i in 0 until lineCount) {
        val angle = (i * 360f / lineCount) * (PI / 180f)
        val startRadius = maxOf(animatedRadius * 0.3f, 1f)
        val endRadius = maxOf(animatedRadius * 0.9f, 1f)
        
        val startX = centerX + (startRadius * cos(angle)).toFloat()
        val startY = centerY + (startRadius * sin(angle)).toFloat()
        val endX = centerX + (endRadius * cos(angle)).toFloat()
        val endY = centerY + (endRadius * sin(angle)).toFloat()
        
        drawLine(
            color = Color.White.copy(alpha = 0.6f),
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 2.dp.toPx()
        )
    }
    
    // Puntos de datos animados
    val dataPoints = listOf(
        overview.totalSales to Color(0xFF4CAF50),
        overview.averageTransactionValue to Color(0xFFFF9800),
        overview.totalTransactions.toDouble() to Color(0xFF9C27B0),
        overview.averageGrowth to Color(0xFFF44336)
    )
    
    dataPoints.forEachIndexed { index, (value, color) ->
        val angle = (index * 90f) * (PI / 180f)
        val pointRadius = maxOf(animatedRadius * 0.8f, 1f)
        val pointX = centerX + (pointRadius * cos(angle)).toFloat()
        val pointY = centerY + (pointRadius * sin(angle)).toFloat()
        
        drawCircle(
            color = color,
            radius = 6.dp.toPx(),
            center = Offset(pointX, pointY)
        )
        
        // Efecto de pulso
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = maxOf(12.dp.toPx() * animationProgress, 1f),
            center = Offset(pointX, pointY)
        )
    }
}

@Composable
private fun MetricIndicator(
    label: String,
    value: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontSize = 10.sp
        )
    }
}
