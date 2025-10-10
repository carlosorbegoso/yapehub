package org.sysarp.project.ui.common.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
 * @param showCard Si mostrar el componente dentro de un Card
 * @param isLoading Si está en estado de carga (por defecto false)
 * @param modifier Modificador para el componente
 */
@Composable
fun PerformanceMetricsPieChart(
    performanceMetrics: PerformanceMetricsData,
    title: String = "Estado de Pagos",
    showLegend: Boolean = true,
    showStats: Boolean = true,
    showCard: Boolean = true,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val chartContent = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (showCard) 16.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título del gráfico (solo si no está dentro de ResponsiveChartRow)
            if (title != "Estado de Pagos") {
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
            }
            
            // Gráfico circular
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gráfico circular animado mejorado
                val totalPayments = performanceMetrics.confirmedPayments + 
                                  performanceMetrics.pendingPayments + 
                                  performanceMetrics.rejectedPayments
                val animationProgress = remember { Animatable(0f) }
                val scaleAnimation = remember { Animatable(1f) }
                val counterAnimation = remember { Animatable(0f) }
                val interactionSource = remember { MutableInteractionSource() }
                val isHovered by interactionSource.collectIsHoveredAsState()
                
                // Animar la entrada del gráfico con efecto mejorado
                LaunchedEffect(performanceMetrics) {
                    animationProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 1000,
                            delayMillis = 100,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
                
                // Animar el contador del número total
                LaunchedEffect(totalPayments) {
                    counterAnimation.animateTo(
                        targetValue = totalPayments.toFloat(),
                        animationSpec = tween(
                            durationMillis = 1500,
                            delayMillis = 200,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
                
                // Animar escala en hover
                LaunchedEffect(isHovered) {
                    scaleAnimation.animateTo(
                        targetValue = if (isHovered) 1.05f else 1f,
                        animationSpec = tween(200)
                    )
                }
                
                // Colores del tema Material
                val confirmedColor = MaterialTheme.colorScheme.primary
                val pendingColor = MaterialTheme.colorScheme.secondary
                val rejectedColor = MaterialTheme.colorScheme.error
                val surfaceColor = MaterialTheme.colorScheme.surface
                
                // Tamaño responsivo
                // Usar valores fijos para multiplataforma
                val screenWidth = 400.dp // Valor por defecto
                val isLargeScreen = screenWidth >= 840.dp
                val chartSize = if (isLargeScreen) 200.dp else 160.dp
                
                Box(
                    modifier = Modifier
                        .size(chartSize)
                        .scale(scaleAnimation.value)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                // Aquí se puede añadir lógica de click si es necesario
                            }
                        }
                        .semantics {
                            contentDescription = "Gráfico circular de métricas de rendimiento. " +
                                    "Confirmados: ${performanceMetrics.confirmedPayments}, " +
                                    "Pendientes: ${performanceMetrics.pendingPayments}, " +
                                    "Rechazados: ${performanceMetrics.rejectedPayments}"
                        }
                ) {
                    when {
                        isLoading -> {
                            LoadingState(chartSize = chartSize)
                        }
                        totalPayments > 0 -> {
                            Canvas(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                drawPieChart(
                                    confirmedPayments = performanceMetrics.confirmedPayments,
                                    pendingPayments = performanceMetrics.pendingPayments,
                                    rejectedPayments = performanceMetrics.rejectedPayments,
                                    totalPayments = totalPayments,
                                    animationProgress = animationProgress.value,
                                    confirmedColor = confirmedColor,
                                    pendingColor = pendingColor,
                                    rejectedColor = rejectedColor,
                                    surfaceColor = surfaceColor,
                                    isHovered = isHovered
                                )
                            }
                            
                            // Texto central animado con contador
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = formatAnimatedCounter(counterAnimation.value, totalPayments),
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
                        }
                        else -> {
                            EmptyState(chartSize = chartSize)
                        }
                    }
                }
                
                // Leyenda mejorada (opcional)
                if (showLegend) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Confirmados
                        LegendItem(
                            icon = Icons.Filled.CheckCircle,
                            label = "Confirmados",
                            value = "${performanceMetrics.confirmedPayments}",
                            percentage = formatPercentage(performanceMetrics.claimRate),
                            color = confirmedColor
                        )
                        
                        // Pendientes
                        LegendItem(
                            icon = Icons.Filled.Schedule,
                            label = "Pendientes",
                            value = "${performanceMetrics.pendingPayments}",
                            percentage = formatPercentage(performanceMetrics.pendingPayments.toDouble() / totalPayments),
                            color = pendingColor
                        )
                        
                        // Rechazados
                        LegendItem(
                            icon = Icons.Filled.Error,
                            label = "Rechazados",
                            value = "${performanceMetrics.rejectedPayments}",
                            percentage = formatPercentage(performanceMetrics.rejectionRate),
                            color = rejectedColor
                        )
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
 * Función helper para dibujar el gráfico circular con efectos mejorados
 */
private fun DrawScope.drawPieChart(
    confirmedPayments: Int,
    pendingPayments: Int,
    rejectedPayments: Int,
    totalPayments: Int,
    animationProgress: Float,
    confirmedColor: Color,
    pendingColor: Color,
    rejectedColor: Color,
    surfaceColor: Color,
    isHovered: Boolean
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val radius = minOf(centerX, centerY) - 30f // Aumentar margen para mejor forma circular
    val strokeWidth = if (isHovered) 1.5f else 1f
    
    // Dibujar sombra suave del círculo completo (solo si hay segmentos)
    val totalPayments = confirmedPayments + pendingPayments + rejectedPayments
    if (totalPayments > 0) {
        drawCircle(
            color = Color.Black.copy(alpha = if (isHovered) 0.15f else 0.08f),
            radius = radius + 2f,
            center = Offset(centerX + 1f, centerY + 1f)
        )
    }
    
    var startAngle = -90f
    
    // Crear lista de segmentos para renderizado más preciso
    val segments = mutableListOf<Triple<Float, Color, String>>()
    
    if (confirmedPayments > 0) {
        val confirmedAngle = (confirmedPayments.toFloat() / totalPayments) * 360f * animationProgress
        segments.add(Triple(confirmedAngle, confirmedColor, "confirmed"))
    }
    
    if (pendingPayments > 0) {
        val pendingAngle = (pendingPayments.toFloat() / totalPayments) * 360f * animationProgress
        segments.add(Triple(pendingAngle, pendingColor, "pending"))
    }
    
    if (rejectedPayments > 0) {
        val rejectedAngle = (rejectedPayments.toFloat() / totalPayments) * 360f * animationProgress
        segments.add(Triple(rejectedAngle, rejectedColor, "rejected"))
    }
    
    // Dibujar cada segmento con precisión mejorada
    segments.forEach { (angle, color, _) ->
        // Solo dibujar si el ángulo es significativo (evita renderizado de segmentos muy pequeños)
        if (angle > 0.5f) {
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = angle,
                useCenter = true,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2)
            )
        }
        startAngle += angle
    }
    
    // Dibujar borde exterior suave del círculo completo solo si hay segmentos
    if (segments.isNotEmpty()) {
        drawCircle(
            color = Color.Black.copy(alpha = 0.08f),
            radius = radius,
            center = Offset(centerX, centerY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
    }

    // Círculo central con renderizado mejorado para evitar puntas
    val innerRadius = radius * 0.35f
    

    drawCircle(
        color = surfaceColor.copy(alpha = 0.98f),
        radius = innerRadius + 1.5f,
        center = Offset(centerX, centerY)
    )

    // Círculo principal
    drawCircle(
        color = surfaceColor,
        radius = innerRadius,
        center = Offset(centerX, centerY)
    )



    // Borde exterior muy sutil
    drawCircle(
        color = Color.Black.copy(alpha = 0.05f),
        radius = innerRadius,
        center = Offset(centerX, centerY),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.2f)
    )
}

/**
 * Componente de item de leyenda mejorado
 */
@Composable
private fun LegendItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    percentage: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icono con fondo circular
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = color.copy(alpha = 0.15f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = percentage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Estado de carga con skeleton animado
 */
@Composable
private fun LoadingState(
    chartSize: androidx.compose.ui.unit.Dp
) {
    val pulseAnimation = remember { Animatable(0.3f) }
    
    LaunchedEffect(Unit) {
        pulseAnimation.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Círculo de carga con pulso
        Box(
            modifier = Modifier
                .size(chartSize * 0.8f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = pulseAnimation.value),
                    shape = CircleShape
                )
        )
        
        // Icono de carga
        Icon(
            imageVector = Icons.Filled.Analytics,
            contentDescription = "Cargando datos",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = pulseAnimation.value),
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * Estado vacío mejorado con animación
 */
@Composable
private fun EmptyState(
    chartSize: androidx.compose.ui.unit.Dp
) {
    val fadeAnimation = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        fadeAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(800)
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(fadeAnimation.value),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icono con fondo circular
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Texto principal
            Text(
                text = "Sin datos disponibles",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Texto secundario
            Text(
                text = "Los datos aparecerán aquí cuando estén disponibles",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Función helper para formatear el contador animado con saltos inteligentes
 */
private fun formatAnimatedCounter(currentValue: Float, totalValue: Int): String {
    val currentInt = currentValue.toInt()
    
    return when {
        totalValue < 10 -> currentInt.toString()
        totalValue < 100 -> {
            val step = 10
            val rounded = (currentInt / step) * step
            rounded.toString()
        }
        totalValue < 1000 -> {
            val step = 100
            val rounded = (currentInt / step) * step
            rounded.toString()
        }
        else -> {
            val step = 1000
            val rounded = (currentInt / step) * step
            when {
                rounded >= 1000000 -> "${rounded / 1000000}M"
                rounded >= 1000 -> "${rounded / 1000}K"
                else -> rounded.toString()
            }
        }
    }
}

