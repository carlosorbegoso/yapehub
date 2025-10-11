package org.sysarp.project.ui.common.components.charts.predictions

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.sysarp.project.data.PredictedSalesData
import org.sysarp.project.data.TrendAnalysisData
import org.sysarp.project.utils.formatCurrency
import kotlin.math.pow


@Composable
fun PredictionsLineChart(
    predictedSales: List<PredictedSalesData>,
    trendAnalysis: TrendAnalysisData,
    showCard: Boolean = true,
    modifier: Modifier = Modifier
) {
    val chartContent = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (showCard) 16.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con título e icono
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = "Tendencia",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "📈 Proyección de Ventas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                

                TrendIndicator(trendAnalysis = trendAnalysis)
            }

            EnhancedPredictionsChart(
                predictedSales = predictedSales,
                trendAnalysis = trendAnalysis
            )
            
            // Métricas y análisis
            PredictionsMetricsSection(
                predictedSales = predictedSales,
                trendAnalysis = trendAnalysis
            )
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
private fun TrendIndicator(trendAnalysis: TrendAnalysisData) {
    val trendColor = when {
        trendAnalysis.slope > 0 -> Color(0xFF4CAF50) // Verde para tendencia positiva
        trendAnalysis.slope < 0 -> Color(0xFFF44336) // Rojo para tendencia negativa
        else -> Color(0xFFFF9800) // Naranja para tendencia neutral
    }
    
    val trendIcon = when {
        trendAnalysis.slope > 0 -> "📈"
        trendAnalysis.slope < 0 -> "📉"
        else -> "➡️"
    }
    
    Box(
        modifier = Modifier
            .background(
                color = trendColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$trendIcon ${String.format("%.1f", trendAnalysis.slope)}%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = trendColor
        )
    }
}

@Composable
private fun EnhancedPredictionsChart(
    predictedSales: List<PredictedSalesData>,
    trendAnalysis: TrendAnalysisData
) {
    var selectedPoint by remember { mutableStateOf<PredictedSalesData?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(predictedSales) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, delayMillis = 300)
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(predictedSales) {
                    detectTapGestures { offset ->
                        // Detectar punto más cercano al tap
                        val tappedPoint = findNearestPoint(offset, predictedSales, androidx.compose.ui.geometry.Size(size.width.toFloat(), size.height.toFloat()))
                        if (tappedPoint != null) {
                            selectedPoint = tappedPoint
                            showDetailsDialog = true
                        }
                    }
                }
        ) {
            drawEnhancedPredictionsChart(
                predictedSales = predictedSales,
                trendAnalysis = trendAnalysis,
                animationProgress = animationProgress.value,
                selectedPoint = selectedPoint
            )
        }
    }
    
    // Diálogo de detalles
    if (showDetailsDialog && selectedPoint != null) {
        PredictionDetailsDialog(
            predictedData = selectedPoint!!,
            onDismiss = {
                showDetailsDialog = false
                selectedPoint = null
            }
        )
    }
}

@Composable
private fun PredictionsMetricsSection(
    predictedSales: List<PredictedSalesData>,
    trendAnalysis: TrendAnalysisData
) {
    if (predictedSales.isEmpty()) return
    
    val maxValue = predictedSales.maxOfOrNull { it.predicted } ?: 0.0
    val minValue = predictedSales.minOfOrNull { it.predicted } ?: 0.0
    val avgValue = predictedSales.map { it.predicted }.average()
    val totalProjected = predictedSales.sumOf { it.predicted }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Métricas principales
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EnhancedStatItem(
                label = "Proyección Total",
                value = formatCurrency(totalProjected),
                icon = "💰",
                color = MaterialTheme.colorScheme.primary
            )
            EnhancedStatItem(
                label = "Promedio Diario",
                value = formatCurrency(avgValue),
                icon = "📊",
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        // Análisis de tendencia
        TrendAnalysisCard(trendAnalysis = trendAnalysis)
    }
}

@Composable
private fun EnhancedStatItem(
    label: String,
    value: String,
    icon: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TrendAnalysisCard(trendAnalysis: TrendAnalysisData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📈 Análisis de Tendencia",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dirección: ${if (trendAnalysis.slope > 0) "Alcista" else if (trendAnalysis.slope < 0) "Bajista" else "Estable"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Precisión: ${String.format("%.1f", trendAnalysis.forecastAccuracy)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PredictionDetailsDialog(
    predictedData: PredictedSalesData,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "📊 Detalles de Proyección",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Fecha:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = predictedData.date,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Proyección:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(predictedData.predicted),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (predictedData.confidence != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Confianza:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format("%.1f", predictedData.confidence)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun findNearestPoint(
    tapOffset: Offset,
    predictedSales: List<PredictedSalesData>,
    canvasSize: androidx.compose.ui.geometry.Size
): PredictedSalesData? {
    if (predictedSales.isEmpty()) return null
    
    val padding = 40f
    val chartWidth = canvasSize.width - padding * 2
    val chartHeight = canvasSize.height - padding * 2
    val startX = padding
    val startY = padding
    
    val maxValue = predictedSales.maxOfOrNull { it.predicted } ?: 0.0
    val minValue = predictedSales.minOfOrNull { it.predicted } ?: 0.0
    val valueRange = maxValue - minValue
    
    val stepX = chartWidth / (predictedSales.size - 1)
    val tapX = tapOffset.x
    
    // Encontrar el punto más cercano horizontalmente
    val nearestIndex = predictedSales.indices.minByOrNull { index ->
        val pointX = startX + index * stepX
        kotlin.math.abs(pointX - tapX)
    } ?: return null
    
    return predictedSales[nearestIndex]
}

private fun DrawScope.drawEnhancedPredictionsChart(
    predictedSales: List<PredictedSalesData>,
    trendAnalysis: TrendAnalysisData,
    animationProgress: Float,
    selectedPoint: PredictedSalesData?
) {
    if (predictedSales.isEmpty()) return
    
    val padding = 40f
    val chartWidth = size.width - padding * 2
    val chartHeight = size.height - padding * 2
    val startX = padding
    val startY = padding
    
    val maxValue = predictedSales.maxOfOrNull { it.predicted } ?: 0.0
    val minValue = predictedSales.minOfOrNull { it.predicted } ?: 0.0
    val valueRange = maxValue - minValue
    
    if (predictedSales.size < 2) return
    
    // Dibujar área de fondo con gradiente
    val areaPath = Path()
    val linePath = Path()
    val stepX = chartWidth / (predictedSales.size - 1)
    
    predictedSales.forEachIndexed { index, data ->
        val x = startX + index * stepX
        val normalizedValue = if (valueRange > 0) {
            (data.predicted - minValue) / valueRange
        } else 0.5
        val y = startY + chartHeight - (normalizedValue * chartHeight).toFloat()
        
        if (index == 0) {
            areaPath.moveTo(x, startY + chartHeight)
            areaPath.lineTo(x, y)
            linePath.moveTo(x, y)
        } else {
            areaPath.lineTo(x, y)
            linePath.lineTo(x, y)
        }
    }
    
    // Cerrar el área
    areaPath.lineTo(startX + chartWidth, startY + chartHeight)
    areaPath.close()
    
    // Dibujar área con gradiente usando colores fijos
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2196F3).copy(alpha = 0.1f),
            Color(0xFF2196F3).copy(alpha = 0.05f)
        ),
        startY = startY,
        endY = startY + chartHeight
    )
    
    drawPath(
        path = areaPath,
        brush = gradient
    )
    
    // Dibujar línea principal con animación
    val animatedPath = Path()
    val animatedPoints = (predictedSales.size * animationProgress).toInt()
    
    predictedSales.take(animatedPoints).forEachIndexed { index, data ->
        val x = startX + index * stepX
        val normalizedValue = if (valueRange > 0) {
            (data.predicted - minValue) / valueRange
        } else 0.5
        val y = startY + chartHeight - (normalizedValue * chartHeight).toFloat()
        
        if (index == 0) {
            animatedPath.moveTo(x, y)
        } else {
            animatedPath.lineTo(x, y)
        }
    }
    
    drawPath(
        path = animatedPath,
        color = Color(0xFF2196F3),
        style = Stroke(width = 3f, cap = StrokeCap.Round)
    )
    
    // Dibujar puntos de datos
    predictedSales.forEachIndexed { index, data ->
        val x = startX + index * stepX
        val normalizedValue = if (valueRange > 0) {
            (data.predicted - minValue) / valueRange
        } else 0.5
        val y = startY + chartHeight - (normalizedValue * chartHeight).toFloat()
        
        val isSelected = selectedPoint == data
        val pointColor = if (isSelected) {
            Color(0xFFFF9800)
        } else {
            Color(0xFF2196F3)
        }
        
        val pointSize = if (isSelected) 8f else 4f
        
        drawCircle(
            color = pointColor,
            radius = pointSize,
            center = Offset(x, y)
        )
        
        // Dibujar borde blanco para mejor visibilidad
        drawCircle(
            color = Color.White,
            radius = pointSize + 1f,
            center = Offset(x, y),
            style = Stroke(width = 1f)
        )
    }
    
    // Dibujar líneas de referencia
    drawLine(
        color = Color.Gray.copy(alpha = 0.3f),
        start = Offset(startX, startY + chartHeight / 2),
        end = Offset(startX + chartWidth, startY + chartHeight / 2),
        strokeWidth = 1f
    )
}
