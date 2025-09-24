package org.sysarp.project.ui.common.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.DailySalesData
import org.sysarp.project.utils.formatCurrency

/**
 * Gráfico de barras mejorado para ventas diarias con estadísticas flotantes
 * 
 * @param dailySales Lista de datos de ventas diarias
 * @param title Título del gráfico (opcional)
 * @param showStats Si mostrar estadísticas adicionales (por defecto true)
 * @param modifier Modificador para el componente
 */
@Composable
fun DailySalesBarChart(
    dailySales: List<DailySalesData>,
    title: String = "Ventas Diarias",
    showStats: Boolean = true,
    showCard: Boolean = true,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember { mutableStateOf<DailySalesData?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showFloatingStats by remember { mutableStateOf(false) }

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
            
            // Etiquetas de resumen semanal en la parte superior (fuera del Box)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Semanal: ${formatCurrency(dailySales.sumOf { it.sales })}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Promedio Diario: ${formatCurrency(dailySales.map { it.sales }.average())}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Total Transacciones: ${dailySales.sumOf { it.transactions }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
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
                                    delayMillis = index * 100,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }

                        val pulseAnimation by rememberInfiniteTransition(label = "pulse").animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse"
                        )

                        val barColor = when (index % 5) {
                            0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            1 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                            2 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                            3 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                        }

                        val isSelected = selectedDay == dayData
                        val isHovered = false // Se puede implementar hover real si es necesario

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(35.dp)
                                    .height((animatedHeight.value * 200).dp)
                                    .shadow(
                                        elevation = if (isSelected) 12.dp else 6.dp,
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = 6.dp,
                                            bottomEnd = 6.dp
                                        ),
                                        ambientColor = barColor.copy(alpha = 0.3f),
                                        spotColor = barColor.copy(alpha = 0.5f)
                                    )
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                barColor.copy(alpha = 1.0f),
                                                barColor.copy(alpha = 0.9f),
                                                barColor.copy(alpha = 0.7f),
                                                barColor.copy(alpha = 0.5f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = 6.dp,
                                            bottomEnd = 6.dp
                                        )
                                    )
                                    .clickable {
                                        selectedDay = dayData
                                        showDetailsDialog = true
                                    }
                            )
                            
                            // Etiquetas informativas debajo de la barra
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Día de la semana
                                Text(
                                    text = dayData.dayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                // Monto de ventas (más compacto)
                                Text(
                                    text = formatCurrency(dayData.sales).replace("S/ ", ""),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                // Número de transacciones (más compacto)
                                Text(
                                    text = "${dayData.transactions} tx",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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