package org.sysarp.project.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sysarp.project.data.PredictedSalesData
import org.sysarp.project.data.SellerForecastingData
import org.sysarp.project.data.TrendAnalysisData
import org.sysarp.project.utils.formatPercentage

/**
 * Gráfico de predicciones para mostrar ventas futuras y tendencias
 * Combina datos históricos con predicciones y análisis de tendencias
 */
@Composable
fun PredictionsChart(
    sellerForecasting: SellerForecastingData?,
    modifier: Modifier = Modifier
) {
    if (sellerForecasting == null) {
        EmptyChartCard(
            title = "🔮 Predicciones de Ventas",
            subtitle = "No hay datos de predicciones disponibles",
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
                title = "🔮 Predicciones de Ventas",
                subtitle = "Tendencias y proyecciones futuras"
            )

            // Gráfico de líneas con predicciones
            PredictionsLineChart(
                predictedSales = sellerForecasting.predictedSales,
                trendAnalysis = sellerForecasting.trendAnalysis
            )

            // Análisis de tendencias
            TrendAnalysisSection(trendAnalysis = sellerForecasting.trendAnalysis)

            // Recomendaciones
            if (sellerForecasting.recommendations.isNotEmpty()) {
                RecommendationsSection(recommendations = sellerForecasting.recommendations)
            }
        }
    }
}

@Composable
private fun PredictionsLineChart(
    predictedSales: List<PredictedSalesData>,
    trendAnalysis: TrendAnalysisData
) {
    val animationProgress = remember { Animatable(0f) }
    val maxValue = predictedSales.maxOfOrNull { it.predicted } ?: 0.0
    val minValue = predictedSales.minOfOrNull { it.predicted } ?: 0.0
    val valueRange = maxValue - minValue

    LaunchedEffect(predictedSales) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500)
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Título del gráfico
        Text(
            text = "📈 Proyección de Ventas",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            drawPredictionsLine(
                predictedSales = predictedSales,
                maxValue = maxValue,
                minValue = minValue,
                valueRange = valueRange,
                animationProgress = animationProgress.value,
                canvasSize = size
            )
        }

        // Leyenda de confianza
        ConfidenceLegend(predictedSales = predictedSales)
    }
}

private fun DrawScope.drawPredictionsLine(
    predictedSales: List<PredictedSalesData>,
    maxValue: Double,
    minValue: Double,
    valueRange: Double,
    animationProgress: Float,
    canvasSize: Size
) {
    if (predictedSales.isEmpty()) return

    val padding = 40.dp.toPx()
    val chartWidth = canvasSize.width - padding * 2
    val chartHeight = canvasSize.height - padding * 2
    val startX = padding
    val startY = padding

    // Dibujar líneas de referencia
    drawReferenceLines(
        maxValue = maxValue,
        minValue = minValue,
        startX = startX,
        startY = startY,
        chartWidth = chartWidth,
        chartHeight = chartHeight
    )

    // Dibujar línea de predicciones
    drawPredictionLine(
        predictedSales = predictedSales,
        maxValue = maxValue,
        minValue = minValue,
        valueRange = valueRange,
        startX = startX,
        startY = startY,
        chartWidth = chartWidth,
        chartHeight = chartHeight,
        animationProgress = animationProgress
    )

    // Dibujar puntos de datos
    drawDataPoints(
        predictedSales = predictedSales,
        maxValue = maxValue,
        minValue = minValue,
        valueRange = valueRange,
        startX = startX,
        startY = startY,
        chartWidth = chartWidth,
        chartHeight = chartHeight,
        animationProgress = animationProgress
    )
}

private fun DrawScope.drawReferenceLines(
    maxValue: Double,
    minValue: Double,
    startX: Float,
    startY: Float,
    chartWidth: Float,
    chartHeight: Float
) {
    // Línea de referencia máxima
    drawLine(
        color = Color.Gray.copy(alpha = 0.3f),
        start = Offset(startX, startY),
        end = Offset(startX + chartWidth, startY),
        strokeWidth = 1.dp.toPx()
    )

    // Línea de referencia mínima
    drawLine(
        color = Color.Gray.copy(alpha = 0.3f),
        start = Offset(startX, startY + chartHeight),
        end = Offset(startX + chartWidth, startY + chartHeight),
        strokeWidth = 1.dp.toPx()
    )

    // Línea media
    drawLine(
        color = Color.Gray.copy(alpha = 0.2f),
        start = Offset(startX, startY + chartHeight / 2),
        end = Offset(startX + chartWidth, startY + chartHeight / 2),
        strokeWidth = 1.dp.toPx()
    )
}

private fun DrawScope.drawPredictionLine(
    predictedSales: List<PredictedSalesData>,
    maxValue: Double,
    minValue: Double,
    valueRange: Double,
    startX: Float,
    startY: Float,
    chartWidth: Float,
    chartHeight: Float,
    animationProgress: Float
) {
    if (predictedSales.size < 2) return

    val path = Path()
    val stepX = chartWidth / (predictedSales.size - 1)

    predictedSales.forEachIndexed { index, data ->
        val x = startX + index * stepX
        val normalizedValue = if (valueRange > 0) {
            (data.predicted - minValue) / valueRange
        } else 0.5
        val y = startY + chartHeight - (normalizedValue * chartHeight * animationProgress).toFloat()

        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    // Dibujar línea principal
    drawPath(
        path = path,
        color = Color(0xFF2196F3),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawDataPoints(
    predictedSales: List<PredictedSalesData>,
    maxValue: Double,
    minValue: Double,
    valueRange: Double,
    startX: Float,
    startY: Float,
    chartWidth: Float,
    chartHeight: Float,
    animationProgress: Float
) {
    val stepX = chartWidth / (predictedSales.size - 1)

    predictedSales.forEachIndexed { index, data ->
        val x = startX + index * stepX
        val normalizedValue = if (valueRange > 0) {
            (data.predicted - minValue) / valueRange
        } else 0.5
        val y = startY + chartHeight - (normalizedValue * chartHeight * animationProgress).toFloat()

        // Color basado en confianza
        val color = when {
            data.confidence >= 0.8 -> Color(0xFF4CAF50) // Verde - Alta confianza
            data.confidence >= 0.6 -> Color(0xFFFF9800) // Naranja - Media confianza
            else -> Color(0xFFF44336) // Rojo - Baja confianza
        }

        // Dibujar punto
        drawCircle(
            color = color,
            radius = 6.dp.toPx(),
            center = Offset(x, y)
        )

        // Dibujar círculo exterior para confianza
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = (6 + data.confidence * 4).dp.toPx(),
            center = Offset(x, y)
        )
    }
}

@Composable
private fun ConfidenceLegend(
    predictedSales: List<PredictedSalesData>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(
            color = Color(0xFF4CAF50),
            label = "Alta Confianza",
            description = "≥80%"
        )
        LegendItem(
            color = Color(0xFFFF9800),
            label = "Media Confianza",
            description = "60-79%"
        )
        LegendItem(
            color = Color(0xFFF44336),
            label = "Baja Confianza",
            description = "<60%"
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color = color, shape = CircleShape)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 10.sp
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun TrendAnalysisSection(
    trendAnalysis: TrendAnalysisData
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📊 Análisis de Tendencias",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TrendItem(
                label = "Tendencia",
                value = trendAnalysis.trend,
                color = when (trendAnalysis.trend.lowercase()) {
                    "up", "rising", "creciente" -> Color(0xFF4CAF50)
                    "down", "falling", "decreciente" -> Color(0xFFF44336)
                    else -> Color(0xFFFF9800)
                },
                icon = when (trendAnalysis.trend.lowercase()) {
                    "up", "rising", "creciente" -> "📈"
                    "down", "falling", "decreciente" -> "📉"
                    else -> "➡️"
                }
            )
            TrendItem(
                label = "Precisión",
                value = formatPercentage(trendAnalysis.forecastAccuracy * 100),
                color = Color(0xFF2196F3),
                icon = "🎯"
            )
            TrendItem(
                label = "R²",
                value = String.format("%.3f", trendAnalysis.r2),
                color = Color(0xFF9C27B0),
                icon = "📐"
            )
        }
    }
}

@Composable
private fun TrendItem(
    label: String,
    value: String,
    color: Color,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 16.sp
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
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
}

@Composable
private fun RecommendationsSection(
    recommendations: List<String>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "💡 Recomendaciones",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(recommendations.take(3)) { recommendation ->
                RecommendationCard(recommendation = recommendation)
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: String
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "💡",
                fontSize = 16.sp
            )
            Text(
                text = recommendation,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black,
                fontSize = 10.sp,
                maxLines = 3
            )
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
