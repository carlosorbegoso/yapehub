package org.sysarp.project.ui.common.components.charts.daily.sales.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
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
import org.sysarp.project.utils.formatCurrency

/**
 * Área principal del gráfico de ventas diarias
 */
@Composable
fun DailySalesChartArea(
    dailySales: List<DailySalesData>,
    onDaySelected: (DailySalesData) -> Unit
) {
    val scrollState = rememberScrollState()
    val shouldShowScroll = dailySales.size > 7 // Mostrar scroll si hay más de 7 días
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header con controles de scroll
        if (shouldShowScroll) {
            DailySalesScrollHeader(
                totalDays = dailySales.size,
                scrollState = scrollState
            )
        }
        
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
            // Gráfico de barras con scroll horizontal
            if (shouldShowScroll) {
                DailySalesBarsChartWithScroll(
                    dailySales = dailySales,
                    onDaySelected = onDaySelected,
                    scrollState = scrollState
                )
            } else {
                DailySalesBarsChart(
                    dailySales = dailySales,
                    onDaySelected = onDaySelected
                )
            }
            
            // Líneas de referencia
            DailySalesReferenceLines(
                dailySales = dailySales
            )
        }
        
        // Leyenda de colores y símbolos
        DailySalesChartLegend(
            modifier = Modifier.padding(top = 8.dp)
        )
        
        // Información adicional del gráfico
        DailySalesChartInfo(
            dailySales = dailySales
        )
    }
}

@Composable
private fun DailySalesChartInfo(
    dailySales: List<DailySalesData>
) {
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

@Composable
private fun DailySalesScrollHeader(
    totalDays: Int,
    scrollState: androidx.compose.foundation.ScrollState
) {
    val coroutineScope = rememberCoroutineScope()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "📈 Ventas Diarias - $totalDays días",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(scrollState.value - 200)
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowLeft,
                    contentDescription = "Desplazar izquierda",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        scrollState.animateScrollTo(scrollState.value + 200)
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = "Desplazar derecha",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun DailySalesBarsChartWithScroll(
    dailySales: List<DailySalesData>,
    onDaySelected: (DailySalesData) -> Unit,
    scrollState: androidx.compose.foundation.ScrollState
) {
    val maxValue = dailySales.maxOfOrNull { it.sales } ?: 1.0
    val minValue = dailySales.minOfOrNull { it.sales } ?: 0.0
    val valueRange = maxValue - minValue
    val totalDays = dailySales.size
    val averageSales = dailySales.map { it.sales }.average()
    val maxSales = dailySales.maxOfOrNull { it.sales } ?: 0.0
    
    // Ancho fijo para cada barra cuando hay scroll
    val barWidth = 45.dp
    val spacing = 8.dp
    val totalWidth = (barWidth + spacing) * totalDays
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Row(
            modifier = Modifier
                .width(totalWidth)
                .height(200.dp)
                .horizontalScroll(scrollState)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.Bottom
        ) {
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

                val shouldShowLabel = index % 3 == 0 || index == totalDays - 1
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
                    if (isHovered) {
                        FloatingTooltip(
                            dayData = dayData,
                            trendDirection = trendDirection,
                            modifier = Modifier.offset(y = (-80).dp)
                        )
                    }
                    
                    // Barra principal con diseño mejorado
                    DailySalesBar(
                        barWidth = barWidth,
                        barHeight = animatedHeight.value,
                        barColor = barColor,
                        isHovered = isHovered,
                        interactionSource = interactionSource,
                        onClick = { onDaySelected(dayData) }
                    )
                    
                    // Etiquetas mejoradas debajo de la barra
                    if (shouldShowLabel) {
                        DailySalesBarLabel(
                            dayData = dayData,
                            barWidth = barWidth,
                            barColor = barColor,
                            averageSales = averageSales,
                            totalDays = totalDays
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailySalesBar(
    barWidth: androidx.compose.ui.unit.Dp,
    barHeight: Float,
    barColor: Color,
    isHovered: Boolean,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(barWidth)
            .height((barHeight * 180).dp)
            .scale(if (isHovered) 1.08f else 1f)
            .shadow(
                elevation = when {
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
            .clickable { onClick() }
    )
}

@Composable
private fun DailySalesBarLabel(
    dayData: DailySalesData,
    barWidth: androidx.compose.ui.unit.Dp,
    barColor: Color,
    averageSales: Double,
    totalDays: Int
) {
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
