package org.sysarp.project.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sysarp.project.data.SellerComparisonsData
import org.sysarp.project.data.ComparisonData
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatPercentage
import kotlin.math.abs
import kotlin.math.max

/**
 * Gráfico de comparaciones temporales para mostrar rendimiento vs períodos anteriores
 * Compara ventas actuales con semana anterior, mes anterior, mejor personal y promedio
 */
@Composable
fun ComparisonsChart(
    sellerComparisons: SellerComparisonsData?,
    modifier: Modifier = Modifier
) {
    if (sellerComparisons == null) {
        EmptyChartCard(
            title = "📊 Comparaciones Temporales",
            subtitle = "No hay datos de comparaciones disponibles",
            modifier = modifier
        )
        return
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título y resumen
            ChartHeader(
                title = "📊 Comparaciones Temporales",
                subtitle = "Rendimiento vs períodos anteriores"
            )

            // Gráfico de barras comparativas
            ComparisonBarsChart(sellerComparisons = sellerComparisons)

            // Resumen de comparaciones
            ComparisonSummary(sellerComparisons = sellerComparisons)
        }
    }
}

@Composable
private fun ComparisonBarsChart(
    sellerComparisons: SellerComparisonsData
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(sellerComparisons) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📈 Comparación de Rendimiento",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            drawComparisonBars(
                sellerComparisons = sellerComparisons,
                animationProgress = animationProgress.value,
                canvasSize = size
            )
        }
    }
}

private fun DrawScope.drawComparisonBars(
    sellerComparisons: SellerComparisonsData,
    animationProgress: Float,
    canvasSize: Size
) {
    val comparisons = listOf(
        Triple("Semana Anterior", sellerComparisons.vsPreviousWeek, Color(0xFF4CAF50)),
        Triple("Mes Anterior", sellerComparisons.vsPreviousMonth, Color(0xFF2196F3)),
        Triple("Mejor Personal", sellerComparisons.vsPersonalBest, Color(0xFFFF9800)),
        Triple("Promedio", sellerComparisons.vsAverage, Color(0xFF9C27B0))
    )

    val maxValue = comparisons.maxOfOrNull { abs(it.second.percentageChange) } ?: 0.0
    val barWidth = canvasSize.width / comparisons.size
    val maxBarHeight = canvasSize.height * 0.7f
    val centerY = canvasSize.height / 2

    comparisons.forEachIndexed { index, (label, data, color) ->
        val x = index * barWidth + barWidth / 2
        val barHeight = if (maxValue > 0) {
            (abs(data.percentageChange) / maxValue * maxBarHeight * animationProgress).toFloat()
        } else 0f

        val y = if (data.percentageChange >= 0) {
            centerY - barHeight
        } else {
            centerY
        }

        // Dibujar barra
        drawRect(
            color = color,
            topLeft = Offset(x - barWidth * 0.3f, y),
            size = Size(barWidth * 0.6f, barHeight)
        )

        // Dibujar línea de referencia en el centro
        drawLine(
            color = Color.Gray.copy(alpha = 0.5f),
            start = Offset(0f, centerY),
            end = Offset(canvasSize.width, centerY),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
private fun ComparisonSummary(
    sellerComparisons: SellerComparisonsData
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📊 Resumen de Comparaciones",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        // Comparaciones individuales
        ComparisonItem(
            label = "vs Semana Anterior",
            comparison = sellerComparisons.vsPreviousWeek,
            icon = "📅"
        )
        ComparisonItem(
            label = "vs Mes Anterior",
            comparison = sellerComparisons.vsPreviousMonth,
            icon = "📆"
        )
        ComparisonItem(
            label = "vs Mejor Personal",
            comparison = sellerComparisons.vsPersonalBest,
            icon = "🏆"
        )
        ComparisonItem(
            label = "vs Promedio",
            comparison = sellerComparisons.vsAverage,
            icon = "📊"
        )
    }
}

@Composable
private fun ComparisonItem(
    label: String,
    comparison: ComparisonData,
    icon: String
) {
    val isPositive = comparison.percentageChange >= 0
    val color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
    val trendIcon = if (isPositive) "📈" else "📉"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = icon,
                    fontSize = 16.sp
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = formatCurrency(comparison.salesChange),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = formatPercentage(comparison.percentageChange),
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                }
                Text(
                    text = trendIcon,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ChartHeader(
    title: String,
    subtitle: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
private fun EmptyChartCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}
