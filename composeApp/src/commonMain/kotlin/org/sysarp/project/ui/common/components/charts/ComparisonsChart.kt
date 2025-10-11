package org.sysarp.project.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sysarp.project.data.ComparisonData
import org.sysarp.project.data.SellerComparisonsData
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatPercentage
import kotlin.math.abs

/**
 * Gráfico de comparaciones temporales para mostrar rendimiento vs períodos anteriores
 * Compara ventas actuales con semana anterior, mes anterior, mejor personal y promedio
 */
@Composable
fun ComparisonsChart(
    sellerComparisons: SellerComparisonsData?,
    showCard: Boolean = true,
    modifier: Modifier = Modifier
) {
    val chartContent = @Composable {
        if (sellerComparisons == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (showCard) 20.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No hay datos de comparaciones disponibles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (showCard) 20.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

            // Gráfico de barras comparativas
            ComparisonBarsChart(sellerComparisons = sellerComparisons)

                // Resumen de comparaciones
                ComparisonSummary(sellerComparisons = sellerComparisons)
            }
        }
    }

    // Renderizar con o sin Card según el parámetro
    if (showCard) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            chartContent()
        }
    } else {
        Box(modifier = modifier.fillMaxWidth()) {
            chartContent()
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
            color = MaterialTheme.colorScheme.onSurface
        )

        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.secondary
        val tertiaryColor = MaterialTheme.colorScheme.tertiary
        val errorColor = MaterialTheme.colorScheme.error
        val outlineColor = MaterialTheme.colorScheme.outline

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            drawComparisonBars(
                sellerComparisons = sellerComparisons,
                animationProgress = animationProgress.value,
                canvasSize = size,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                tertiaryColor = tertiaryColor,
                errorColor = errorColor,
                outlineColor = outlineColor
            )
        }
    }
}

private fun DrawScope.drawComparisonBars(
    sellerComparisons: SellerComparisonsData,
    animationProgress: Float,
    canvasSize: Size,
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    errorColor: Color,
    outlineColor: Color
) {
    val comparisons = listOf(
        Triple("Semana Anterior", sellerComparisons.vsPreviousWeek, primaryColor),
        Triple("Mes Anterior", sellerComparisons.vsPreviousMonth, secondaryColor),
        Triple("Mejor Personal", sellerComparisons.vsPersonalBest, tertiaryColor),
        Triple("Promedio", sellerComparisons.vsAverage, errorColor)
    )

    val maxValue = comparisons.maxOfOrNull { abs(it.second.percentageChange) } ?: 0.0
    val barWidth = canvasSize.width / comparisons.size
    val maxBarHeight = canvasSize.height * 0.7f
    val centerY = canvasSize.height / 2

    comparisons.forEachIndexed { index, (_, data, color) ->
        val x = index * barWidth + barWidth / 2
        val barHeight = if (maxValue > 0) {
            (abs(data.percentageChange) / maxValue * maxBarHeight * animationProgress).toFloat()
        } else 0f

        val y = if (data.percentageChange >= 0) {
            centerY - barHeight
        } else {
            centerY
        }

        // Dibujar barra con gradiente
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    color.copy(alpha = 0.8f),
                    color.copy(alpha = 0.4f)
                ),
                startY = y,
                endY = y + barHeight
            ),
            topLeft = Offset(x - barWidth * 0.3f, y),
            size = Size(barWidth * 0.6f, barHeight)
        )

        // Dibujar borde de la barra
        drawRect(
            color = color,
            topLeft = Offset(x - barWidth * 0.3f, y),
            size = Size(barWidth * 0.6f, barHeight),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )

        // Dibujar indicador de valor en la barra (círculo pequeño)
        if (barHeight > 20.dp.toPx()) {
            val indicatorY = if (data.percentageChange >= 0) {
                y - 8.dp.toPx()
            } else {
                y + barHeight + 8.dp.toPx()
            }
            drawCircle(
                color = color,
                radius = 4.dp.toPx(),
                center = Offset(x, indicatorY)
            )
        }
    }

    // Dibujar línea de referencia en el centro
    drawLine(
        color = outlineColor.copy(alpha = 0.5f),
        start = Offset(0f, centerY),
        end = Offset(canvasSize.width, centerY),
        strokeWidth = 2.dp.toPx()
    )
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
            color = MaterialTheme.colorScheme.onSurface
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
    val color = if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val trendIcon = if (isPositive) "📈" else "📉"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = color.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icono con fondo circular suave
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = color.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 18.sp
                    )
                }
                
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
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
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Icono de tendencia con fondo circular
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = color.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = trendIcon,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

