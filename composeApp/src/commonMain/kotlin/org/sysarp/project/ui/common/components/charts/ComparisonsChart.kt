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
import org.sysarp.project.data.ComparisonData
import org.sysarp.project.data.SellerComparisonsData
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatPercentage
import kotlin.math.abs

/**
 * Gráfico mejorado de comparaciones temporales para mostrar rendimiento vs períodos anteriores
 * Incluye animaciones, interactividad y análisis detallado de comparaciones
 */
@Composable
fun ComparisonsChart(
    sellerComparisons: SellerComparisonsData?,
    showCard: Boolean = true,
    modifier: Modifier = Modifier
) {
    val chartContent = @Composable {
        if (sellerComparisons == null) {
            EmptyComparisonsState(showPadding = showCard)
        } else {
            EnhancedComparisonsContent(
                sellerComparisons = sellerComparisons,
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
private fun EmptyComparisonsState(showPadding: Boolean) {
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
            text = "No hay datos de comparaciones disponibles",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EnhancedComparisonsContent(
    sellerComparisons: SellerComparisonsData,
    showPadding: Boolean
) {
    var selectedComparison by remember { mutableStateOf<String?>(null) }
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
                    imageVector = Icons.Filled.Compare,
                    contentDescription = "Comparaciones",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "📊 Comparaciones de Rendimiento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Indicador de rendimiento general
            PerformanceIndicator(sellerComparisons = sellerComparisons)
        }
        
        // Gráfico de barras mejorado con interactividad
        EnhancedComparisonBarsChart(
            sellerComparisons = sellerComparisons,
            onComparisonSelected = { comparison ->
                selectedComparison = comparison
                showDetailsDialog = true
            }
        )
        
        // Comparaciones detalladas con barras de progreso
        DetailedComparisonsSection(sellerComparisons = sellerComparisons)
        
        // Análisis y insights
        ComparisonInsights(sellerComparisons = sellerComparisons)
    }
    
    // Diálogo de detalles
    if (showDetailsDialog && selectedComparison != null) {
        ComparisonDetailsDialog(
            sellerComparisons = sellerComparisons,
            selectedComparison = selectedComparison!!,
            onDismiss = {
                showDetailsDialog = false
                selectedComparison = null
            }
        )
    }
}

@Composable
private fun PerformanceIndicator(sellerComparisons: SellerComparisonsData) {
    val comparisons = listOf(
        sellerComparisons.vsPreviousWeek,
        sellerComparisons.vsPreviousMonth,
        sellerComparisons.vsPersonalBest,
        sellerComparisons.vsAverage
    )
    
    val positiveCount = comparisons.count { it.percentageChange >= 0 }
    val totalCount = comparisons.size
    val performanceRatio = positiveCount.toFloat() / totalCount
    
    val indicatorColor = when {
        performanceRatio >= 0.75f -> Color(0xFF4CAF50) // Verde para excelente
        performanceRatio >= 0.5f -> Color(0xFFFF9800) // Naranja para bueno
        else -> Color(0xFFF44336) // Rojo para mejorar
    }
    
    Box(
        modifier = Modifier
            .background(
                color = indicatorColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "${(performanceRatio * 100).toInt()}% Positivo",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = indicatorColor
        )
    }
}

@Composable
private fun EnhancedComparisonBarsChart(
    sellerComparisons: SellerComparisonsData,
    onComparisonSelected: (String) -> Unit
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(sellerComparisons) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, delayMillis = 300)
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "📈 Comparación de Rendimiento",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

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
                    .pointerInput(sellerComparisons) {
                        detectTapGestures { offset ->
                            val comparisons = listOf(
                                "vsPreviousWeek" to sellerComparisons.vsPreviousWeek,
                                "vsPreviousMonth" to sellerComparisons.vsPreviousMonth,
                                "vsPersonalBest" to sellerComparisons.vsPersonalBest,
                                "vsAverage" to sellerComparisons.vsAverage
                            )
                            
                            val barWidth = size.width / comparisons.size
                            val tappedIndex = (offset.x / barWidth).toInt()
                            
                            if (tappedIndex in comparisons.indices) {
                                onComparisonSelected(comparisons[tappedIndex].first)
                            }
                        }
                    }
            ) {
                drawEnhancedComparisonBars(
                    sellerComparisons = sellerComparisons,
                    animationProgress = animationProgress.value,
                    size = size
                )
            }
        }
    }
}

@Composable
private fun DetailedComparisonsSection(sellerComparisons: SellerComparisonsData) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "📊 Comparaciones Detalladas",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        val comparisons = listOf(
            Triple("📅 Semana Anterior", sellerComparisons.vsPreviousWeek, Color(0xFF4CAF50)),
            Triple("📆 Mes Anterior", sellerComparisons.vsPreviousMonth, Color(0xFF2196F3)),
            Triple("🏆 Mejor Personal", sellerComparisons.vsPersonalBest, Color(0xFFFF9800)),
            Triple("📊 Promedio", sellerComparisons.vsAverage, Color(0xFF9C27B0))
        )
        
        comparisons.forEach { (label, comparison, color) ->
            val percentage = abs(comparison.percentageChange.toFloat()) / 100f
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (comparison.percentageChange >= 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (comparison.percentageChange >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = formatPercentage(comparison.percentageChange),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (comparison.percentageChange >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
                
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (comparison.percentageChange >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    trackColor = color.copy(alpha = 0.2f)
                )
                
                Text(
                    text = formatCurrency(comparison.salesChange),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ComparisonInsights(sellerComparisons: SellerComparisonsData) {
    val comparisons = listOf(
        sellerComparisons.vsPreviousWeek,
        sellerComparisons.vsPreviousMonth,
        sellerComparisons.vsPersonalBest,
        sellerComparisons.vsAverage
    )
    
    val bestComparison = comparisons.maxByOrNull { it.percentageChange }
    val worstComparison = comparisons.minByOrNull { it.percentageChange }
    val positiveCount = comparisons.count { it.percentageChange >= 0 }
    
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
                text = "💡 Insights de Comparación",
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
                        text = "Mejor Rendimiento:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatPercentage(bestComparison?.percentageChange ?: 0.0),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Comparaciones Positivas:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$positiveCount/4",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            
            Text(
                text = "📈 Rendimiento General: ${if (positiveCount >= 3) "Excelente" else if (positiveCount >= 2) "Bueno" else "Necesita Mejora"}",
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
private fun ComparisonDetailsDialog(
    sellerComparisons: SellerComparisonsData,
    selectedComparison: String,
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
                    text = "📊 Detalles de Comparación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                val comparisonData = when (selectedComparison) {
                    "vsPreviousWeek" -> Triple("📅 Semana Anterior", sellerComparisons.vsPreviousWeek, Color(0xFF4CAF50))
                    "vsPreviousMonth" -> Triple("📆 Mes Anterior", sellerComparisons.vsPreviousMonth, Color(0xFF2196F3))
                    "vsPersonalBest" -> Triple("🏆 Mejor Personal", sellerComparisons.vsPersonalBest, Color(0xFFFF9800))
                    "vsAverage" -> Triple("📊 Promedio", sellerComparisons.vsAverage, Color(0xFF9C27B0))
                    else -> Triple("❓ Desconocido", ComparisonData(0.0, 0, 0.0), Color.Gray)
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Comparación:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = comparisonData.first,
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
                        text = "Cambio en Ventas:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(comparisonData.second.salesChange),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = comparisonData.third
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cambio Porcentual:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatPercentage(comparisonData.second.percentageChange),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = comparisonData.third
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawEnhancedComparisonBars(
    sellerComparisons: SellerComparisonsData,
    animationProgress: Float,
    size: Size
) {
    val comparisons = listOf(
        Triple("Semana Anterior", sellerComparisons.vsPreviousWeek, Color(0xFF4CAF50)),
        Triple("Mes Anterior", sellerComparisons.vsPreviousMonth, Color(0xFF2196F3)),
        Triple("Mejor Personal", sellerComparisons.vsPersonalBest, Color(0xFFFF9800)),
        Triple("Promedio", sellerComparisons.vsAverage, Color(0xFF9C27B0))
    )

    val maxValue = comparisons.maxOfOrNull { abs(it.second.percentageChange) } ?: 0.0
    val barWidth = size.width / comparisons.size
    val maxBarHeight = size.height * 0.6f
    val centerY = size.height / 2

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

        // Crear gradiente mejorado
        val gradient = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = 0.9f),
                color.copy(alpha = 0.6f),
                color.copy(alpha = 0.3f)
            ),
            startY = y,
            endY = y + barHeight
        )

        // Dibujar barra con gradiente
        drawRect(
            brush = gradient,
            topLeft = Offset(x - barWidth * 0.3f, y),
            size = Size(barWidth * 0.6f, barHeight)
        )

        // Dibujar borde de la barra
        drawRect(
            color = color,
            topLeft = Offset(x - barWidth * 0.3f, y),
            size = Size(barWidth * 0.6f, barHeight),
            style = Stroke(width = 2f)
        )

        // Dibujar indicador de valor en la barra
        if (barHeight > 20f) {
            val indicatorY = if (data.percentageChange >= 0) {
                y - 10f
            } else {
                y + barHeight + 10f
            }
            drawCircle(
                color = color,
                radius = 6f,
                center = Offset(x, indicatorY)
            )
            
            // Dibujar borde blanco para mejor visibilidad
            drawCircle(
                color = Color.White,
                radius = 7f,
                center = Offset(x, indicatorY),
                style = Stroke(width = 1f)
            )
        }
    }

    // Dibujar línea de referencia en el centro
    drawLine(
        color = Color.Gray.copy(alpha = 0.5f),
        start = Offset(0f, centerY),
        end = Offset(size.width, centerY),
        strokeWidth = 2f
    )
}


