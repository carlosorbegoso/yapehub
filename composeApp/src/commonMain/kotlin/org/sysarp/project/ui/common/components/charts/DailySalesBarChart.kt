package org.sysarp.project.ui.common.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.sysarp.project.data.DailySalesData
import org.sysarp.project.utils.formatCurrency
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Gráfico de barras mejorado para ventas diarias
 * 
 * @param dailySales Lista de datos de ventas diarias
 * @param title Título del gráfico (opcional)
 * @param showCard Si mostrar el componente dentro de un Card
 * @param modifier Modificador para el componente
 */
@Composable
fun DailySalesBarChart(
    dailySales: List<DailySalesData>,
    title: String = "Ventas Diarias",
    showCard: Boolean = true,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf<DailySalesData?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    val chartContent = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (showCard) 16.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título del gráfico (solo si no está dentro de ResponsiveChartRow)
            if (title != "Ventas Diarias") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
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

            // Gráfico de barras con animaciones y efectos
            val maxValue = dailySales.maxOfOrNull { it.sales } ?: 1.0
            val minValue = dailySales.minOfOrNull { it.sales } ?: 0.0
            val valueRange = maxValue - minValue
            
            // Análisis inteligente de datos
            val totalDays = dailySales.size
            val analysis = remember(dailySales) { analyzeSalesData(dailySales) }
            
            val periodLabel = when {
                totalDays <= 7 -> "Semanal"
                totalDays <= 14 -> "Quincenal"
                totalDays <= 30 -> "Mensual"
                totalDays <= 60 -> "Bimestral"
                else -> "Período Extendido"
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título principal con indicador de tendencia
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Total $periodLabel: ${formatCurrency(dailySales.sumOf { it.sales })}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Indicador de tendencia
                    TrendIndicator(
                        trend = analysis.trend,
                        percentage = analysis.trendPercentage
                    )
                }
                
                // Métricas principales mejoradas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EnhancedMetricCard(
                        icon = "📊",
                        label = "Promedio Diario",
                        value = formatCurrency(dailySales.map { it.sales }.average()),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    EnhancedMetricCard(
                        icon = "🔄",
                        label = "Total Transacciones",
                        value = "${dailySales.sumOf { it.transactions }}",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                
                // Análisis inteligente
                if (analysis.peakDay != null || analysis.valleyDay != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (analysis.peakDay != null) {
                            AnalysisCard(
                                icon = "📈",
                                label = "Mejor Día",
                                value = analysis.peakDay.dayName,
                                detail = formatCurrency(analysis.peakDay.sales),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        if (analysis.valleyDay != null) {
                            AnalysisCard(
                                icon = "📉",
                                label = "Día Bajo",
                                value = analysis.valleyDay.dayName,
                                detail = formatCurrency(analysis.valleyDay.sales),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                // Información adicional para períodos largos
                if (totalDays > 7) {
                    Text(
                        text = "Período: ${totalDays} días",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Contenedor principal del gráfico mejorado
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Área del gráfico con mejor espaciado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Calcular ancho dinámico de barras según cantidad de días
                        val totalDays = dailySales.size
                        val barWidth = when {
                            totalDays <= 7 -> 35.dp
                            totalDays <= 14 -> 25.dp
                            totalDays <= 30 -> 18.dp
                            totalDays <= 60 -> 12.dp
                            else -> 8.dp
                        }
                        
                        // Determinar cada cuántos días mostrar etiquetas
                        val labelInterval = when {
                            totalDays <= 7 -> 1  // Todos los días
                            totalDays <= 14 -> 2  // Cada 2 días
                            totalDays <= 30 -> 3  // Cada 3 días
                            totalDays <= 60 -> 5  // Cada 5 días
                            else -> 7  // Cada semana
                        }
                        
                        dailySales.forEachIndexed { index, dayData ->
                            val barHeight = if (valueRange > 0) {
                                ((dayData.sales - minValue) / valueRange * 0.9).coerceAtLeast(0.1)
                            } else {
                                0.15
                            }

                            val animatedHeight = remember {
                                Animatable(0f)
                            }

                            LaunchedEffect(dayData) {
                                animatedHeight.animateTo(
                                    targetValue = barHeight.toFloat(),
                                    animationSpec = tween(
                                        durationMillis = 800,
                                        delayMillis = index * 50, // Reducido para mejor rendimiento
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            }

                            val barColor = when (index % 5) {
                                0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                1 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                                2 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                                3 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                            }

                            val isSelected = selectedDay == dayData
                            val shouldShowLabel = index % labelInterval == 0 || index == totalDays - 1
                            val interactionSource = remember { MutableInteractionSource() }
                            val isHovered by interactionSource.collectIsHoveredAsState()
                            
                            // Calcular tendencia individual
                            val previousDaySales = if (index > 0) dailySales[index - 1].sales else dayData.sales
                            val trendDirection = when {
                                dayData.sales > previousDaySales -> "up"
                                dayData.sales < previousDaySales -> "down"
                                else -> "stable"
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            ) {
                                // Tooltip flotante mejorado
                                if (isHovered && barWidth >= 12.dp) {
                                    FloatingTooltip(
                                        dayData = dayData,
                                        trendDirection = trendDirection,
                                        modifier = Modifier.offset(y = (-80).dp)
                                    )
                                }
                                
                                // Barra principal con diseño mejorado
                                Box(
                                    modifier = Modifier
                                        .width(barWidth)
                                        .height((animatedHeight.value * 180).dp)
                                        .scale(if (isHovered) 1.08f else 1f)
                                        .shadow(
                                            elevation = when {
                                                isSelected -> 12.dp
                                                isHovered -> 8.dp
                                                else -> 4.dp
                                            },
                                            shape = RoundedCornerShape(
                                                topStart = 8.dp,
                                                topEnd = 8.dp,
                                                bottomStart = 4.dp,
                                                bottomEnd = 4.dp
                                            ),
                                            ambientColor = barColor.copy(alpha = 0.2f),
                                            spotColor = barColor.copy(alpha = 0.4f)
                                        )
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    barColor.copy(alpha = if (isHovered) 1.0f else 0.9f),
                                                    barColor.copy(alpha = if (isHovered) 0.9f else 0.8f),
                                                    barColor.copy(alpha = if (isHovered) 0.7f else 0.6f),
                                                    barColor.copy(alpha = if (isHovered) 0.5f else 0.4f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(
                                                topStart = 8.dp,
                                                topEnd = 8.dp,
                                                bottomStart = 4.dp,
                                                bottomEnd = 4.dp
                                            )
                                        )
                                        .hoverable(interactionSource)
                                        .clickable {
                                            selectedDay = dayData
                                            showDetailsDialog = true
                                        }
                                )
                                
                                // Etiquetas mejoradas debajo de la barra
                                if (shouldShowLabel) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(top = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Día de la semana con fondo para mejor legibilidad
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (totalDays <= 7) {
                                                    dayData.dayName
                                                } else {
                                                    dayData.dayName.take(3).uppercase()
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = if (barWidth < 15.dp) 9.sp else 11.sp
                                            )
                                        }
                                        
                                        // Monto de ventas con mejor formato
                                        if (barWidth >= 12.dp) {
                                            Text(
                                                text = formatCurrency(dayData.sales).replace("S/ ", ""),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = barColor,
                                                fontSize = if (barWidth < 20.dp) 9.sp else 11.sp
                                            )
                                            
                                            // Número de transacciones con icono
                                            if (barWidth >= 18.dp) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Text(
                                                        text = "🔄",
                                                        fontSize = 8.sp
                                                    )
                                                    Text(
                                                        text = "${dayData.transactions}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 9.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Línea de referencia horizontal para mejor visualización
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                            .offset(y = (-20).dp)
                    )
                }
                
                // Información adicional del gráfico
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡 Toca una barra para ver detalles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    
                    Text(
                        text = "📊 ${dailySales.size} días",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
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
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            chartContent()
        }
    } else {
        Box(modifier = modifier.fillMaxWidth()) {
            chartContent()
        }
    }

    // Diálogo de detalles
    if (showDetailsDialog && selectedDay != null) {
        DayDetailsDialog(
            dayData = selectedDay!!,
            onDismiss = {
                showDetailsDialog = false
                selectedDay = null
            }
        )
    }
}

@Composable
private fun DayDetailsDialog(
    dayData: DailySalesData,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icono del día
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📅",
                        fontSize = 24.sp
                    )
                }

                // Título
                Text(
                    text = "Detalles de ${dayData.dayName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Información detallada
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailRow(
                        icon = "💰",
                        label = "Ventas Totales",
                        value = formatCurrency(dayData.sales),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    DetailRow(
                        icon = "🔄",
                        label = "Transacciones",
                        value = "${dayData.transactions}",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    DetailRow(
                        icon = "📊",
                        label = "Promedio por Transacción",
                        value = formatCurrency(dayData.sales / dayData.transactions.coerceAtLeast(1)),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                // Botón de cerrar
                androidx.compose.material3.Button(
                    onClick = onDismiss,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Cerrar",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: String,
    label: String,
    value: String,
    color: Color
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
                fontSize = 16.sp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// ===== FUNCIONES DE ANÁLISIS INTELIGENTE =====

data class SalesAnalysis(
    val trend: String, // "up", "down", "stable"
    val trendPercentage: Double,
    val peakDay: DailySalesData?,
    val valleyDay: DailySalesData?,
    val averageGrowth: Double,
    val volatility: Double
)

private fun analyzeSalesData(dailySales: List<DailySalesData>): SalesAnalysis {
    if (dailySales.isEmpty()) {
        return SalesAnalysis("stable", 0.0, null, null, 0.0, 0.0)
    }
    
    val sales = dailySales.map { it.sales }
    val peakDay = dailySales.maxByOrNull { it.sales }
    val valleyDay = dailySales.minByOrNull { it.sales }
    
    // Calcular tendencia general
    val firstHalf = sales.take(sales.size / 2).average()
    val secondHalf = sales.drop(sales.size / 2).average()
    val trendPercentage = if (firstHalf > 0) {
        ((secondHalf - firstHalf) / firstHalf) * 100
    } else 0.0
    
    val trend = when {
        trendPercentage > 5 -> "up"
        trendPercentage < -5 -> "down"
        else -> "stable"
    }
    
    // Calcular crecimiento promedio
    val growthRates = mutableListOf<Double>()
    for (i in 1 until sales.size) {
        if (sales[i-1] > 0) {
            growthRates.add(((sales[i] - sales[i-1]) / sales[i-1]) * 100)
        }
    }
    val averageGrowth = growthRates.average()
    
    // Calcular volatilidad
    val mean = sales.average()
    val variance = sales.map { (it - mean).pow(2) }.average()
    val volatility = sqrt(variance)
    
    return SalesAnalysis(
        trend = trend,
        trendPercentage = abs(trendPercentage),
        peakDay = peakDay,
        valleyDay = valleyDay,
        averageGrowth = averageGrowth,
        volatility = volatility
    )
}

// ===== COMPONENTES AUXILIARES =====

@Composable
private fun TrendIndicator(
    trend: String,
    percentage: Double
) {
    val (icon, color) = when (trend) {
        "up" -> Icons.AutoMirrored.Filled.TrendingUp to MaterialTheme.colorScheme.primary
        "down" -> Icons.AutoMirrored.Filled.TrendingDown to MaterialTheme.colorScheme.error
        else -> Icons.Filled.Analytics to MaterialTheme.colorScheme.secondary
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "${percentage.toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun EnhancedMetricCard(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.08f),
                        color.copy(alpha = 0.03f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icono con fondo circular suave
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 18.sp
                )
            }
            
            // Valor principal
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            // Etiqueta
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AnalysisCard(
    icon: String,
    label: String,
    value: String,
    detail: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.08f),
                        color.copy(alpha = 0.03f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Icono con fondo circular suave
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
                    text = icon,
                    fontSize = 14.sp
                )
            }
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun FloatingTooltip(
    dayData: DailySalesData,
    trendDirection: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = dayData.dayName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatCurrency(dayData.sales),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${dayData.transactions} tx",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Indicador de tendencia
            val trendIcon = when (trendDirection) {
                "up" -> "↗️"
                "down" -> "↘️"
                else -> "→"
            }
            Text(
                text = trendIcon,
                fontSize = 12.sp
            )
        }
    }
}