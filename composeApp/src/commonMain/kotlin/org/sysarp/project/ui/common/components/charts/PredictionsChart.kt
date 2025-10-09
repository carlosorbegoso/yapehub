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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sysarp.project.data.PredictedSalesData
import org.sysarp.project.data.SellerForecastingData
import org.sysarp.project.data.TrendAnalysisData
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatPercentage

/**
 * Gráfico de predicciones para mostrar ventas futuras y tendencias
 * Combina datos históricos con predicciones y análisis de tendencias
 */
@Composable
fun PredictionsChart(
    sellerForecasting: SellerForecastingData?,
    showCard: Boolean = true,
    modifier: Modifier = Modifier
) {
    val chartContent = @Composable {
        if (sellerForecasting == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (showCard) 16.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No hay datos de predicciones disponibles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (showCard) 16.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

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
private fun PredictionsLineChart(
    predictedSales: List<PredictedSalesData>,
    trendAnalysis: TrendAnalysisData
) {
    val animationProgress = remember { Animatable(0f) }
    val maxValue = predictedSales.maxOfOrNull { it.predicted } ?: 0.0
    val minValue = predictedSales.minOfOrNull { it.predicted } ?: 0.0
    
    // Mejorar el rango para valores pequeños
    val adjustedMinValue = if (minValue == maxValue) 0.0 else minValue
    val adjustedMaxValue = if (maxValue == 0.0) 0.1 else maxValue
    val valueRange = adjustedMaxValue - adjustedMinValue
    
    // Debug: Imprimir valores para verificar
    println("DEBUG PredictionsChart - MaxValue: $maxValue, MinValue: $minValue, ValueRange: $valueRange")
    println("DEBUG PredictionsChart - AdjustedMaxValue: $adjustedMaxValue, AdjustedMinValue: $adjustedMinValue")
    predictedSales.forEach { data ->
        println("DEBUG PredictionsChart - Date: ${data.date}, Predicted: ${data.predicted}, Confidence: ${data.confidence}")
    }

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
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Información sobre la escalabilidad
        if (maxValue <= 0.1) {
            Text(
                text = "💡 Los valores pueden crecer con el tiempo según el desarrollo del negocio",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }

        val primaryColor = MaterialTheme.colorScheme.primary
        val tertiaryColor = MaterialTheme.colorScheme.tertiary
        val errorColor = MaterialTheme.colorScheme.error
        val textColor = MaterialTheme.colorScheme.onSurface
        
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            drawPredictionsLine(
                predictedSales = predictedSales,
                maxValue = adjustedMaxValue,
                minValue = adjustedMinValue,
                valueRange = valueRange,
                animationProgress = animationProgress.value,
                canvasSize = size,
                primaryColor = primaryColor,
                tertiaryColor = tertiaryColor,
                errorColor = errorColor,
                textColor = textColor
            )
        }

        // Resumen de valores
        PredictedValuesSummary(predictedSales = predictedSales)
        
        // Leyenda de confianza
        ConfidenceLegend()
    }
}

private fun DrawScope.drawPredictionsLine(
    predictedSales: List<PredictedSalesData>,
    maxValue: Double,
    minValue: Double,
    valueRange: Double,
    animationProgress: Float,
    canvasSize: Size,
    primaryColor: Color,
    tertiaryColor: Color,
    errorColor: Color,
    textColor: Color
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

    // Dibujar área bajo la curva
    drawAreaUnderCurve(
        predictedSales = predictedSales,
        maxValue = maxValue,
        minValue = minValue,
        valueRange = valueRange,
        startX = startX,
        startY = startY,
        chartWidth = chartWidth,
        chartHeight = chartHeight,
        animationProgress = animationProgress,
        primaryColor = primaryColor
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
        animationProgress = animationProgress,
        lineColor = primaryColor
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
        animationProgress = animationProgress,
        primaryColor = primaryColor,
        tertiaryColor = tertiaryColor,
        errorColor = errorColor
    )
    
    // Dibujar etiquetas de valores
    drawValueLabels(
        predictedSales = predictedSales,
        maxValue = maxValue,
        minValue = minValue,
        valueRange = valueRange,
        startX = startX,
        startY = startY,
        chartWidth = chartWidth,
        chartHeight = chartHeight,
        animationProgress = animationProgress,
        textColor = textColor
    )

    // Dibujar etiquetas de ejes
    drawAxisLabels(
        predictedSales = predictedSales,
        maxValue = maxValue,
        minValue = minValue,
        startX = startX,
        startY = startY,
        chartWidth = chartWidth,
        chartHeight = chartHeight,
        textColor = textColor
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
    animationProgress: Float,
    lineColor: Color
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
        color = lineColor,
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
    animationProgress: Float,
    primaryColor: Color,
    tertiaryColor: Color,
    errorColor: Color
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
            data.confidence >= 0.8 -> primaryColor // Alta confianza
            data.confidence >= 0.6 -> tertiaryColor // Media confianza
            else -> errorColor // Baja confianza
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

private fun DrawScope.drawValueLabels(
    predictedSales: List<PredictedSalesData>,
    maxValue: Double,
    minValue: Double,
    valueRange: Double,
    startX: Float,
    startY: Float,
    chartWidth: Float,
    chartHeight: Float,
    animationProgress: Float,
    textColor: Color
) {
    val stepX = chartWidth / (predictedSales.size - 1)

    predictedSales.forEachIndexed { index, data ->
        val x = startX + index * stepX
        val normalizedValue = if (valueRange > 0) {
            (data.predicted - minValue) / valueRange
        } else 0.5
        val y = startY + chartHeight - (normalizedValue * chartHeight * animationProgress).toFloat()

        // Dibujar etiqueta de valor solo si es significativo
        if (data.predicted > 0) {
            // Dibujar un pequeño círculo como indicador de valor
            drawCircle(
                color = textColor,
                radius = 3.dp.toPx(),
                center = Offset(x, y - 15.dp.toPx())
            )
        }
    }
}

@Composable
private fun PredictedValuesSummary(
    predictedSales: List<PredictedSalesData>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "📊 Valores Predichos",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(predictedSales.take(5)) { data ->
                PredictedValueCard(data = data)
            }
        }
    }
}

@Composable
private fun PredictedValueCard(
    data: PredictedSalesData
) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(60.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = data.date.substring(5), // Solo mes-día
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 10.sp
            )
            Text(
                text = formatCurrency(data.predicted),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp
            )
            Text(
                text = "${(data.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun ConfidenceLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(
            color = MaterialTheme.colorScheme.primary,
            label = "Alta Confianza",
            description = "≥80%"
        )
        LegendItem(
            color = MaterialTheme.colorScheme.tertiary,
            label = "Media Confianza",
            description = "60-79%"
        )
        LegendItem(
            color = MaterialTheme.colorScheme.error,
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
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 10.sp
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            color = MaterialTheme.colorScheme.onSurface
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Primera fila: Tendencia y Precisión
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TrendItem(
                    label = "Tendencia",
                    value = trendAnalysis.trend,
                    color = when (trendAnalysis.trend.lowercase()) {
                        "up", "rising", "creciente" -> MaterialTheme.colorScheme.primary
                        "down", "falling", "decreciente" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.tertiary
                    },
                    icon = when (trendAnalysis.trend.lowercase()) {
                        "up", "rising", "creciente" -> "📈"
                        "down", "falling", "decreciente" -> "📉"
                        else -> "➡️"
                    },
                    animationDelay = 0
                )
                TrendItem(
                    label = "Precisión",
                    value = formatPercentage(trendAnalysis.forecastAccuracy * 100),
                    color = MaterialTheme.colorScheme.secondary,
                    icon = "🎯",
                    animationDelay = 200
                )
            }
            
            // Segunda fila: R² (centrado)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TrendItem(
                    label = "R²",
                    value = String.format("%.3f", trendAnalysis.r2),
                    color = MaterialTheme.colorScheme.tertiary,
                    icon = "📐",
                    animationDelay = 400
                )
            }
        }
    }
}

@Composable
private fun TrendItem(
    label: String,
    value: String,
    color: Color,
    icon: String,
    animationDelay: Int = 0
) {
    val scaleAnimation = remember { Animatable(0.8f) }
    
    LaunchedEffect(Unit) {
        scaleAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, delayMillis = animationDelay)
        )
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer {
                    scaleX = scaleAnimation.value
                    scaleY = scaleAnimation.value
                }
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            color = MaterialTheme.colorScheme.onSurface
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
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(80.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "💡",
                fontSize = 18.sp
            )
            Text(
                text = recommendation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp,
                maxLines = 3,
                lineHeight = 14.sp
            )
        }
    }
}

private fun DrawScope.drawAreaUnderCurve(
    predictedSales: List<PredictedSalesData>,
    maxValue: Double,
    minValue: Double,
    valueRange: Double,
    startX: Float,
    startY: Float,
    chartWidth: Float,
    chartHeight: Float,
    animationProgress: Float,
    primaryColor: Color
) {
    if (predictedSales.size < 2) return

    val path = Path()
    val stepX = chartWidth / (predictedSales.size - 1)

    // Crear área bajo la curva
    predictedSales.forEachIndexed { index, data ->
        val x = startX + index * stepX
        val normalizedValue = if (valueRange > 0) {
            (data.predicted - minValue) / valueRange
        } else 0.5
        val y = startY + chartHeight - (normalizedValue * chartHeight * animationProgress).toFloat()

        if (index == 0) {
            path.moveTo(x, startY + chartHeight) // Empezar desde la base
            path.lineTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    // Cerrar el área volviendo a la base
    path.lineTo(startX + chartWidth, startY + chartHeight)
    path.lineTo(startX, startY + chartHeight)
    path.close()

    // Dibujar área con gradiente
    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.3f),
                primaryColor.copy(alpha = 0.1f)
            ),
            startY = startY,
            endY = startY + chartHeight
        )
    )
}

private fun DrawScope.drawAxisLabels(
    predictedSales: List<PredictedSalesData>,
    maxValue: Double,
    minValue: Double,
    startX: Float,
    startY: Float,
    chartWidth: Float,
    chartHeight: Float,
    textColor: Color
) {
    val stepX = chartWidth / (predictedSales.size - 1)

    // Etiquetas del eje X (fechas) - usando círculos como marcadores
    predictedSales.forEachIndexed { index, data ->
        if (index % 2 == 0) { // Mostrar cada dos fechas para evitar saturación
            val x = startX + index * stepX
            // Dibujar un pequeño círculo como marcador de fecha
            drawCircle(
                color = textColor.copy(alpha = 0.6f),
                radius = 2.dp.toPx(),
                center = Offset(x, startY + chartHeight + 15.dp.toPx())
            )
        }
    }

    // Etiquetas del eje Y (valores) - usando líneas como marcadores
    val yLabels = listOf(minValue, (minValue + maxValue) / 2, maxValue)
    yLabels.forEach { value ->
        val normalizedValue = if (maxValue > minValue) {
            (value - minValue) / (maxValue - minValue)
        } else 0.5
        val y = startY + chartHeight - (normalizedValue * chartHeight)
        
        // Dibujar una pequeña línea como marcador de valor
        drawLine(
            color = textColor.copy(alpha = 0.6f),
            start = Offset(startX - 8.dp.toPx(), y.toFloat()),
            end = Offset(startX - 3.dp.toPx(), y.toFloat()),
            strokeWidth = 2.dp.toPx()
        )
    }
}

