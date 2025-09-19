package org.sysarp.project.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.sysarp.project.data.PerformanceMetricsData
import org.sysarp.project.utils.formatOneDecimal
import org.sysarp.project.utils.formatPercentage
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

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
    var selectedSegment by remember { mutableStateOf<String?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }
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
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(performanceMetrics) {
                                    detectTapGestures { offset ->
                                        val centerX = size.width / 2
                                        val centerY = size.height / 2
                                        val radius = minOf(centerX, centerY) - 15f
                                        
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
                                            
                                            val totalPayments = performanceMetrics.confirmedPayments + 
                                                              performanceMetrics.pendingPayments + 
                                                              performanceMetrics.rejectedPayments
                                            
                                            val confirmedAngle = (performanceMetrics.confirmedPayments.toFloat() / totalPayments) * 360f
                                            val pendingAngle = (performanceMetrics.pendingPayments.toFloat() / totalPayments) * 360f
                                            
                                            selectedSegment = when {
                                                normalizedAngle <= confirmedAngle -> "confirmed"
                                                normalizedAngle <= confirmedAngle + pendingAngle -> "pending"
                                                else -> "rejected"
                                            }
                                            showDetailsDialog = true
                                        }
                                    }
                                }
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
    
    // Diálogo de detalles del segmento seleccionado
    selectedSegment?.let { segment ->
        if (showDetailsDialog) {
            SegmentDetailsDialog(
                segment = segment,
                performanceMetrics = performanceMetrics,
                onDismiss = { showDetailsDialog = false }
            )
        }
    }
}

@Composable
private fun SegmentDetailsDialog(
    segment: String,
    performanceMetrics: PerformanceMetricsData,
    onDismiss: () -> Unit
) {
    val (title, description, color, icon, value, percentage) = when (segment) {
        "confirmed" -> {
            val total = performanceMetrics.confirmedPayments + performanceMetrics.pendingPayments + performanceMetrics.rejectedPayments
            val pct = if (total > 0) (performanceMetrics.confirmedPayments.toFloat() / total * 100) else 0f
            Tuple6(
                "✅ Pagos Confirmados",
                "Pagos procesados exitosamente",
                Color(0xFF4CAF50),
                "✅",
                performanceMetrics.confirmedPayments,
                pct
            )
        }
        "pending" -> {
            val total = performanceMetrics.confirmedPayments + performanceMetrics.pendingPayments + performanceMetrics.rejectedPayments
            val pct = if (total > 0) (performanceMetrics.pendingPayments.toFloat() / total * 100) else 0f
            Tuple6(
                "⏳ Pagos Pendientes",
                "Pagos en proceso de verificación",
                Color(0xFFFF9800),
                "⏳",
                performanceMetrics.pendingPayments,
                pct
            )
        }
        "rejected" -> {
            val total = performanceMetrics.confirmedPayments + performanceMetrics.pendingPayments + performanceMetrics.rejectedPayments
            val pct = if (total > 0) (performanceMetrics.rejectedPayments.toFloat() / total * 100) else 0f
            Tuple6(
                "❌ Pagos Rechazados",
                "Pagos que fueron rechazados",
                Color(0xFFF44336),
                "❌",
                performanceMetrics.rejectedPayments,
                pct
            )
        }
        else -> Tuple6("", "", Color.Gray, "", 0, 0f)
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icono y título
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = color.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                // Información detallada
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailRow(
                        label = "Cantidad",
                        value = "$value pagos",
                        icon = "📊"
                    )
                    
                    DetailRow(
                        label = "Porcentaje",
                        value = formatPercentage(percentage),
                        icon = "📈"
                    )
                    
                    DetailRow(
                        label = "Tiempo promedio",
                        value = "${formatOneDecimal(performanceMetrics.averageConfirmationTime)} seg",
                        icon = "⏱️"
                    )
                }
                
                // Botón de cerrar
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = color
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Cerrar",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    icon: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

// Helper data class para múltiples valores
private data class Tuple6<T1, T2, T3, T4, T5, T6>(
    val first: T1,
    val second: T2,
    val third: T3,
    val fourth: T4,
    val fifth: T5,
    val sixth: T6
)
