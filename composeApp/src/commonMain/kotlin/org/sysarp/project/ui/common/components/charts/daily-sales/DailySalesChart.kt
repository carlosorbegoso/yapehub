package org.sysarp.project.ui.common.components.charts.daily.sales

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
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
import org.sysarp.project.data.DailySalesData
import org.sysarp.project.ui.common.components.charts.daily.sales.components.AnalysisCard
import org.sysarp.project.ui.common.components.charts.daily.sales.components.EnhancedMetricCard
import org.sysarp.project.ui.common.components.charts.daily.sales.components.FloatingTooltip
import org.sysarp.project.ui.common.components.charts.daily.sales.components.TrendIndicator
import org.sysarp.project.ui.common.components.charts.daily.sales.data.analyzeSalesData
import org.sysarp.project.ui.common.components.charts.daily.sales.dialogs.DayDetailsDialog
import org.sysarp.project.utils.formatCurrency

/**
 * Gráfico de barras mejorado para ventas diarias
 * 
 * Características principales:
 * - Barras dinámicas que se adaptan al número de días
 * - Animaciones suaves y escalonadas
 * - Efectos de hover con tooltips informativos
 * - Análisis inteligente de tendencias y métricas
 * - Diálogo de detalles al hacer clic en una barra
 * - Diseño responsivo y adaptable
 * 
 * @param dailySales Lista de datos de ventas diarias
 * @param title Título del gráfico (opcional)
 * @param showCard Si mostrar el componente dentro de un Card
 * @param modifier Modificador para el componente
 */
@Composable
fun DailySalesChart(
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

            // Análisis inteligente de datos
            val maxValue = dailySales.maxOfOrNull { it.sales } ?: 1.0
            val minValue = dailySales.minOfOrNull { it.sales } ?: 0.0
            val valueRange = maxValue - minValue
            val totalDays = dailySales.size
            val analysis = remember(dailySales) { analyzeSalesData(dailySales) }
            
            val periodLabel = when {
                totalDays <= 7 -> "Semanal"
                totalDays <= 14 -> "Quincenal"
                totalDays <= 30 -> "Mensual"
                totalDays <= 60 -> "Bimestral"
                else -> "Período Extendido"
            }
            
            // Sección de métricas principales
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
                
                // Análisis inteligente de días destacados
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
            
            // Área principal del gráfico
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Contenedor del gráfico con fondo suave
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
                    // Calcular métricas para colores inteligentes (fuera del Row)
                    val averageSales = dailySales.map { it.sales }.average()
                    val maxSales = dailySales.maxOfOrNull { it.sales } ?: 0.0
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Calcular ancho dinámico de barras según cantidad de días
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
                        
                        // Renderizar barras con animaciones y colores inteligentes
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
                                        delayMillis = index * 50,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                            }

                            // Colores inteligentes basados en rendimiento
                            val barColor = calculateBarColor(
                                dayData.sales,
                                averageSales,
                                maxSales,
                                index,
                                MaterialTheme.colorScheme
                            )

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
                                        
                                        // Monto de ventas con mejor formato e indicadores
                                        if (barWidth >= 12.dp) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                // Monto principal
                                                Text(
                                                    text = formatCurrency(dayData.sales).replace("S/ ", ""),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = barColor,
                                                    fontSize = if (barWidth < 20.dp) 9.sp else 11.sp
                                                )
                                                
                                                // Indicador de rendimiento
                                                val performanceIndicator = when {
                                                    dayData.sales >= averageSales * 1.2 -> "🔥"
                                                    dayData.sales >= averageSales -> "📈"
                                                    dayData.sales >= averageSales * 0.8 -> "📊"
                                                    else -> "📉"
                                                }
                                                
                                                Text(
                                                    text = performanceIndicator,
                                                    fontSize = if (barWidth < 20.dp) 8.sp else 10.sp
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
                    }
                    
                    // Líneas de referencia mejoradas
                    ReferenceLines(
                        averageSales = averageSales,
                        maxSales = maxSales,
                        minValue = minValue,
                        valueRange = valueRange,
                        chartHeight = 180.dp
                    )
                }
                
                // Leyenda de colores y símbolos
                ChartLegend(
                    modifier = Modifier.padding(top = 8.dp)
                )
                
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

/**
 * Calcula el color de la barra basado en el rendimiento
 */
private fun calculateBarColor(
    sales: Double,
    averageSales: Double,
    maxSales: Double,
    index: Int,
    colorScheme: androidx.compose.material3.ColorScheme
): Color {
    return when {
        // Día con ventas excepcionales (top 10%)
        sales >= maxSales * 0.9 -> colorScheme.primary.copy(alpha = 0.9f)
        
        // Día con ventas por encima del promedio
        sales > averageSales * 1.2 -> colorScheme.primary.copy(alpha = 0.8f)
        
        // Día con ventas promedio
        sales >= averageSales * 0.8 -> colorScheme.secondary.copy(alpha = 0.8f)
        
        // Día con ventas por debajo del promedio
        sales >= averageSales * 0.5 -> colorScheme.tertiary.copy(alpha = 0.8f)
        
        // Día con ventas muy bajas
        else -> colorScheme.error.copy(alpha = 0.7f)
    }
}

/**
 * Componente para líneas de referencia en el gráfico
 */
@Composable
private fun ReferenceLines(
    averageSales: Double,
    maxSales: Double,
    minValue: Double,
    valueRange: Double,
    chartHeight: androidx.compose.ui.unit.Dp
) {
    val averageHeight = if (valueRange > 0) {
        ((averageSales - minValue) / valueRange * 0.9).coerceAtLeast(0.1)
    } else 0.15
    
    val maxHeight = if (valueRange > 0) {
        ((maxSales - minValue) / valueRange * 0.9).coerceAtLeast(0.1)
    } else 0.15
    
    // Línea de promedio
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(0.5.dp)
            )
            .offset(y = (-averageHeight * chartHeight.value).dp)
    )
    
    // Línea de máximo (solo si es significativamente diferente del promedio)
    if (maxSales > averageSales * 1.5) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(0.5.dp)
                )
                .offset(y = (-maxHeight * chartHeight.value).dp)
        )
    }
    
    // Línea base
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

/**
 * Leyenda para explicar los colores y símbolos del gráfico
 */
@Composable
private fun ChartLegend(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Título de la leyenda
        Text(
            text = "📊 Leyenda del Gráfico",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
        
        // Colores de rendimiento
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(
                color = MaterialTheme.colorScheme.primary,
                label = "Excelente",
                icon = "🔥"
            )
            LegendItem(
                color = MaterialTheme.colorScheme.secondary,
                label = "Bueno",
                icon = "📈"
            )
            LegendItem(
                color = MaterialTheme.colorScheme.tertiary,
                label = "Regular",
                icon = "📊"
            )
            LegendItem(
                color = MaterialTheme.colorScheme.error,
                label = "Bajo",
                icon = "📉"
            )
        }
        
        // Símbolos explicativos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SymbolItem(
                symbol = "🔥",
                description = "Top 10%"
            )
            SymbolItem(
                symbol = "📈",
                description = "Sobre promedio"
            )
            SymbolItem(
                symbol = "📊",
                description = "Promedio"
            )
            SymbolItem(
                symbol = "📉",
                description = "Bajo promedio"
            )
        }
    }
}

/**
 * Item individual de la leyenda de colores
 */
@Composable
private fun LegendItem(
    color: Color,
    label: String,
    icon: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = color.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(2.dp)
                )
        )
        Text(
            text = icon,
            fontSize = 10.sp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
    }
}

/**
 * Item individual de la leyenda de símbolos
 */
@Composable
private fun SymbolItem(
    symbol: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = symbol,
            fontSize = 12.sp
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp
        )
    }
}
