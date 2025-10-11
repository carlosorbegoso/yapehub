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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.sysarp.project.data.SalesDistributionData
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatPercentage
import kotlin.math.pow

/**
 * Gráfico mejorado de distribución de ventas por tiempo del día y día de la semana
 * Incluye animaciones, interactividad y análisis detallado
 */
@Composable
fun SalesDistributionChart(
    salesDistribution: SalesDistributionData?,
    showCard: Boolean = true,
    modifier: Modifier = Modifier
) {
    val chartContent = @Composable {
        if (salesDistribution == null) {
            EmptyDistributionState(showPadding = showCard)
        } else {
            EnhancedDistributionContent(
                salesDistribution = salesDistribution,
                showPadding = showCard
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
private fun EmptyDistributionState(showPadding: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(if (showPadding) 16.dp else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Analytics,
            contentDescription = "Sin datos",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "No hay datos de distribución disponibles",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EnhancedDistributionContent(
    salesDistribution: SalesDistributionData,
    showPadding: Boolean
) {
    var selectedSegment by remember { mutableStateOf<String?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(if (showPadding) 16.dp else 0.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
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
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = "Distribución",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "📊 Distribución de Ventas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Indicador de total
            TotalIndicator(salesDistribution = salesDistribution)
        }
        
        // Gráfico circular mejorado con interactividad
        EnhancedDistributionPieChart(
            salesDistribution = salesDistribution,
            onSegmentSelected = { segment ->
                selectedSegment = segment
                showDetailsDialog = true
            }
        )
        
        // Gráfico de barras para distribución por tiempo
        TimeDistributionBars(salesDistribution = salesDistribution)
        
        // Gráfico de barras para distribución por día
        DayDistributionBars(salesDistribution = salesDistribution)
        
        // Análisis y insights
        DistributionInsights(salesDistribution = salesDistribution)
    }
    
    // Diálogo de detalles
    if (showDetailsDialog && selectedSegment != null) {
        DistributionDetailsDialog(
            salesDistribution = salesDistribution,
            selectedSegment = selectedSegment!!,
            onDismiss = {
                showDetailsDialog = false
                selectedSegment = null
            }
        )
    }
}

@Composable
private fun TotalIndicator(salesDistribution: SalesDistributionData) {
    val total = salesDistribution.morning + salesDistribution.afternoon + salesDistribution.evening
    
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = formatCurrency(total),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EnhancedDistributionPieChart(
    salesDistribution: SalesDistributionData,
    onSegmentSelected: (String) -> Unit
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(salesDistribution) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, delayMillis = 300)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🥧 Distribución por Tiempo del Día",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(salesDistribution) {
                        detectTapGestures { offset ->
                            val centerX = size.width / 2
                            val centerY = size.height / 2
                            val radius = minOf(centerX, centerY) - 20f
                            
                            // Calcular distancia desde el centro
                            val distance = kotlin.math.sqrt(
                                (offset.x - centerX).toDouble().pow(2) + 
                                (offset.y - centerY).toDouble().pow(2)
                            ).toFloat()
                            
                            if (distance <= radius) {
                                // Calcular ángulo del tap
                                val angle = kotlin.math.atan2(
                                    (offset.y - centerY).toDouble(),
                                    (offset.x - centerX).toDouble()
                                ) * 180 / kotlin.math.PI
                                
                                val normalizedAngle = (angle + 90 + 360) % 360
                                
                                val total = salesDistribution.morning + salesDistribution.afternoon + salesDistribution.evening
                                val morningAngle = (salesDistribution.morning / total * 360f)
                                val afternoonAngle = (salesDistribution.afternoon / total * 360f)
                                
                                val segment = when {
                                    normalizedAngle <= morningAngle -> "morning"
                                    normalizedAngle <= morningAngle + afternoonAngle -> "afternoon"
                                    else -> "evening"
                                }
                                onSegmentSelected(segment)
                            }
                        }
                    }
            ) {
                drawEnhancedDistributionPie(
                    salesDistribution = salesDistribution,
                    animationProgress = animationProgress.value,
                    size = size
                )
            }
        }

        // Leyenda mejorada del gráfico circular
        EnhancedDistributionLegend(salesDistribution = salesDistribution)
    }
}

@Composable
private fun TimeDistributionBars(salesDistribution: SalesDistributionData) {
    val total = salesDistribution.morning + salesDistribution.afternoon + salesDistribution.evening
    if (total <= 0) return
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "📈 Distribución por Horario",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        val timeData = listOf(
            Triple("🌅 Mañana", salesDistribution.morning, Color(0xFF4CAF50)),
            Triple("☀️ Tarde", salesDistribution.afternoon, Color(0xFFFF9800)),
            Triple("🌙 Noche", salesDistribution.evening, Color(0xFF2196F3))
        )
        
        timeData.forEach { (label, value, color) ->
            val percentage = (value / total).toFloat()
            val animatedProgress by animateFloatAsState(
                targetValue = percentage,
                animationSpec = tween(durationMillis = 1000, delayMillis = 500),
                label = "progress"
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.2f)
                )
                
                Text(
                    text = formatCurrency(value),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun DayDistributionBars(salesDistribution: SalesDistributionData) {
    val total = salesDistribution.weekday + salesDistribution.weekend
    if (total <= 0) return
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "📅 Distribución por Día",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        val dayData = listOf(
            Triple("📅 Días Laborales", salesDistribution.weekday, Color(0xFF9C27B0)),
            Triple("🎉 Fin de Semana", salesDistribution.weekend, Color(0xFFE91E63))
        )
        
        dayData.forEach { (label, value, color) ->
            val percentage = (value / total).toFloat()
            val animatedProgress by animateFloatAsState(
                targetValue = percentage,
                animationSpec = tween(durationMillis = 1000, delayMillis = 700),
                label = "progress"
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.2f)
                )
                
                Text(
                    text = formatCurrency(value),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun DistributionInsights(salesDistribution: SalesDistributionData) {
    val total = salesDistribution.morning + salesDistribution.afternoon + salesDistribution.evening
    val weekdayTotal = salesDistribution.weekday + salesDistribution.weekend
    
    val bestTime = when {
        salesDistribution.morning >= salesDistribution.afternoon && salesDistribution.morning >= salesDistribution.evening -> "Mañana"
        salesDistribution.afternoon >= salesDistribution.evening -> "Tarde"
        else -> "Noche"
    }
    
    val bestDay = if (salesDistribution.weekday >= salesDistribution.weekend) "Días Laborales" else "Fin de Semana"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "💡 Insights de Distribución",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Mejor Horario:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = bestTime,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Mejor Día:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = bestDay,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            
            Text(
                text = "📊 Total General: ${formatCurrency(total + weekdayTotal)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EnhancedDistributionLegend(salesDistribution: SalesDistributionData) {
    val total = salesDistribution.morning + salesDistribution.afternoon + salesDistribution.evening
    if (total <= 0) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(
            label = "Mañana",
            percentage = formatPercentage(salesDistribution.morning / total),
            color = Color(0xFF4CAF50),
            icon = "🌅"
        )
        LegendItem(
            label = "Tarde",
            percentage = formatPercentage(salesDistribution.afternoon / total),
            color = Color(0xFFFF9800),
            icon = "☀️"
        )
        LegendItem(
            label = "Noche",
            percentage = formatPercentage(salesDistribution.evening / total),
            color = Color(0xFF2196F3),
            icon = "🌙"
        )
    }
}

@Composable
private fun DistributionDetailsDialog(
    salesDistribution: SalesDistributionData,
    selectedSegment: String,
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
                    text = "📊 Detalles de Distribución",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                val segmentData = when (selectedSegment) {
                    "morning" -> Triple("🌅 Mañana", salesDistribution.morning, Color(0xFF4CAF50))
                    "afternoon" -> Triple("☀️ Tarde", salesDistribution.afternoon, Color(0xFFFF9800))
                    "evening" -> Triple("🌙 Noche", salesDistribution.evening, Color(0xFF2196F3))
                    else -> Triple("❓ Desconocido", 0.0, Color.Gray)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Período:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = segmentData.first,
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
                        text = "Ventas:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(segmentData.second),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = segmentData.third
                    )
                }
                
                val total = salesDistribution.morning + salesDistribution.afternoon + salesDistribution.evening
                val percentage = if (total > 0) (segmentData.second / total * 100) else 0.0
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Porcentaje:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatPercentage(percentage / 100),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = segmentData.third
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    label: String,
    percentage: String,
    color: Color,
    icon: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color = color, shape = CircleShape)
        )
        Text(
            text = icon,
            fontSize = 14.sp
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = percentage,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 12.sp
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

private fun DrawScope.drawEnhancedDistributionPie(
    salesDistribution: SalesDistributionData,
    animationProgress: Float,
    size: Size
) {
    val center = Offset(size.width / 2, size.height / 2)
    val radius = (size.minDimension - 40f) / 2
    
    // Datos para el gráfico circular con colores mejorados
    val data = listOf(
        Triple("Mañana", salesDistribution.morning, Color(0xFF4CAF50)),
        Triple("Tarde", salesDistribution.afternoon, Color(0xFFFF9800)),
        Triple("Noche", salesDistribution.evening, Color(0xFF2196F3))
    )
    
    val total = data.sumOf { it.second }
    if (total <= 0) return
    
    var startAngle = -90f
    
    data.forEach { (label, value, color) ->
        val sweepAngle = (value / total * 360f * animationProgress).toFloat()
        
        if (sweepAngle > 0) {
            // Crear gradiente para el segmento
            val gradient = Brush.sweepGradient(
                colors = listOf(
                    color.copy(alpha = 0.7f),
                    color,
                    color.copy(alpha = 0.8f)
                ),
                center = center
            )
            
            // Dibujar arco con gradiente
            drawArc(
                brush = gradient,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            
            // Dibujar borde con sombra
            drawArc(
                color = Color.White,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 3f)
            )
            
            startAngle += sweepAngle
        }
    }
    
    // Dibujar círculo central con información
    drawCircle(
        color = Color.White,
        radius = radius * 0.3f,
        center = center
    )
    
    drawCircle(
        color = Color.Gray.copy(alpha = 0.3f),
        radius = radius * 0.3f,
        center = center,
        style = Stroke(width = 2f)
    )
}