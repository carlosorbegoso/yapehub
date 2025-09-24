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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.sysarp.project.data.HourlySalesData
import org.sysarp.project.utils.formatCurrency

/**
 * Gráfico de barras para mostrar ventas por hora del día
 * Muestra un gráfico de calor con barras que representan las ventas por hora
 */
@Composable
fun HourlySalesChart(
    hourlySales: List<HourlySalesData>?,
    showCard: Boolean = true,
    modifier: Modifier = Modifier
) {
    var selectedHour by remember { mutableStateOf<HourlySalesData?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    val chartContent = @Composable {
        if (hourlySales.isNullOrEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (showCard) 16.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No hay datos disponibles",
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

            // Gráfico de barras interactivo con etiquetas
            Box {
                HourlyBarsChart(
                    hourlySales = hourlySales,
                    onHourSelected = { hourData ->
                        selectedHour = hourData
                        showDetailsDialog = true
                    }
                )
                
                // Etiquetas de horas superpuestas
                HourLabelsOverlay(hourlySales = hourlySales)
            }

                // Estadísticas resumidas
                HourlyStatsSummary(hourlySales = hourlySales)
            }
        }
    }

    // Renderizar con o sin Card según el parámetro
    if (showCard) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            chartContent()
        }
    } else {
        Box(modifier = modifier.fillMaxWidth()) {
            chartContent()
        }
    }

    // Diálogo de detalles
    if (showDetailsDialog && selectedHour != null) {
        HourDetailsDialog(
            hourData = selectedHour!!,
            onDismiss = {
                showDetailsDialog = false
                selectedHour = null
            }
        )
    }
}


@Composable
private fun HourlyBarsChart(
    hourlySales: List<HourlySalesData>,
    onHourSelected: (HourlySalesData) -> Unit
) {
    val maxSales = hourlySales.maxOfOrNull { it.sales } ?: 0.0
    val animationProgress = remember { Animatable(0f) }
    var selectedIndex by remember { mutableStateOf(-1) }
    
    // Obtener colores del tema fuera del Canvas
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val outlineColor = MaterialTheme.colorScheme.outline

    LaunchedEffect(hourlySales) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(hourlySales) {
                detectTapGestures { offset ->
                    val barWidth = size.width / 24f
                    val tappedIndex = (offset.x / barWidth).toInt().coerceIn(0, hourlySales.size - 1)
                    selectedIndex = tappedIndex
                    onHourSelected(hourlySales[tappedIndex])
                }
            }
    ) {
        drawHourlyBars(
            hourlySales = hourlySales,
            maxSales = maxSales,
            animationProgress = animationProgress.value,
            canvasSize = size,
            selectedIndex = selectedIndex,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            outlineColor = outlineColor
        )
    }
}

private fun DrawScope.drawHourlyBars(
    hourlySales: List<HourlySalesData>,
    maxSales: Double,
    animationProgress: Float,
    canvasSize: Size,
    selectedIndex: Int = -1,
    primaryColor: Color,
    secondaryColor: Color,
    outlineColor: Color
) {
    val barWidth = canvasSize.width / 24f
    val maxBarHeight = canvasSize.height * 0.7f // Reducido para espacio de etiquetas
    val startY = canvasSize.height * 0.1f
    val labelAreaHeight = canvasSize.height * 0.2f // Espacio para etiquetas

    // Dibujar líneas de referencia
    drawReferenceLines(maxSales, canvasSize, maxBarHeight, startY, primaryColor, secondaryColor)
    
    // Dibujar etiquetas de horas
    drawHourLabels(hourlySales, canvasSize, barWidth, labelAreaHeight, outlineColor)

    hourlySales.forEachIndexed { index, hourData ->
        val barHeight = if (maxSales > 0) {
            (hourData.sales / maxSales * maxBarHeight * animationProgress).toFloat()
        } else 0f

        val x = index * barWidth
        val y = startY + maxBarHeight - barHeight
        val isSelected = index == selectedIndex

        // Sistema de colores mejorado
        val baseColor = getBarColor(hourData.sales, maxSales, primaryColor)

        // Color final con efecto de selección
        val finalColor = if (isSelected) {
            baseColor.copy(alpha = 0.9f)
        } else {
            baseColor
        }

        // Dibujar barra con gradiente sutil
        drawRect(
            color = finalColor,
            topLeft = Offset(x + 2, y),
            size = Size(barWidth - 4, barHeight)
        )

        // Dibujar borde de selección mejorado
        if (isSelected) {
            drawRect(
                color = primaryColor,
                topLeft = Offset(x + 1, y - 2),
                size = Size(barWidth - 2, barHeight + 4),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }
    }
}

private fun DrawScope.drawReferenceLines(
    maxSales: Double,
    canvasSize: Size,
    maxBarHeight: Float,
    startY: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val averageSales = maxSales * 0.5 // Simulamos promedio como 50% del máximo
    
    // Línea de valor máximo
    val maxLineY = startY
    drawLine(
        color = primaryColor.copy(alpha = 0.3f),
        start = Offset(0f, maxLineY),
        end = Offset(canvasSize.width, maxLineY),
        strokeWidth = 1.dp.toPx()
    )
    
    // Línea de promedio
    val avgLineY = startY + maxBarHeight * 0.5f
    drawLine(
        color = secondaryColor.copy(alpha = 0.3f),
        start = Offset(0f, avgLineY),
        end = Offset(canvasSize.width, avgLineY),
        strokeWidth = 1.dp.toPx()
    )
}

private fun DrawScope.drawHourLabels(
    hourlySales: List<HourlySalesData>,
    canvasSize: Size,
    barWidth: Float,
    labelAreaHeight: Float,
    outlineColor: Color
) {
    // Dibujar etiquetas cada 3 horas para mejor legibilidad
    hourlySales.forEachIndexed { index, hourData ->
        if (index % 3 == 0) {
            val hourText = hourData.hour.substring(0, 2)
            val x = index * barWidth + barWidth / 2
            
            // Dibujar línea vertical de referencia
            drawLine(
                color = outlineColor.copy(alpha = 0.2f),
                start = Offset(x, canvasSize.height * 0.1f),
                end = Offset(x, canvasSize.height * 0.8f),
                strokeWidth = 0.5.dp.toPx()
            )
            
            // Nota: El texto se dibujará usando Text composables superpuestos
            // ya que drawIntoCanvas no está disponible en Compose multiplataforma
        }
    }
}

private fun getBarColor(sales: Double, maxSales: Double, primaryColor: Color): Color {
    val intensity = if (maxSales > 0) sales / maxSales else 0.0
    
    return when {
        intensity > 0.8 -> primaryColor // Color principal
        intensity > 0.6 -> Color(0xFF1976D2) // Azul medio
        intensity > 0.4 -> Color(0xFF42A5F5) // Azul claro
        intensity > 0.2 -> Color(0xFF81C784) // Verde claro
        intensity > 0.0 -> Color(0xFFFFB74D) // Naranja claro
        else -> Color(0xFFE0E0E0) // Gris para cero
    }
}

@Composable
private fun HourLabelsOverlay(
    hourlySales: List<HourlySalesData>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        hourlySales.forEachIndexed { index, hourData ->
            if (index % 3 == 0) { // Mostrar cada 3 horas
                Text(
                    text = hourData.hour.substring(0, 2),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            } else {
                // Espacio vacío para mantener alineación
                Spacer(modifier = Modifier.width(0.dp))
            }
        }
    }
}

@Composable
private fun HourlyStatsSummary(
    hourlySales: List<HourlySalesData>
) {
    val peakHour = hourlySales.maxByOrNull { it.sales }
    val totalSales = hourlySales.sumOf { it.sales }
    val activeHours = hourlySales.count { it.sales > 0 }
    val averageSales = if (activeHours > 0) totalSales / activeHours else 0.0
    val maxSales = hourlySales.maxOfOrNull { it.sales } ?: 0.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            label = "Hora Pico",
            value = peakHour?.hour ?: "N/A",
            color = MaterialTheme.colorScheme.primary
        )
        StatItem(
            label = "Promedio",
            value = formatCurrency(averageSales),
            color = MaterialTheme.colorScheme.secondary
        )
        StatItem(
            label = "Activas",
            value = "$activeHours/24",
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 10.sp
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun HourDetailsDialog(
    hourData: HourlySalesData,
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
                // Icono de hora
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = Color(0xFF2196F3).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🕐",
                        fontSize = 24.sp
                    )
                }

                // Título
                Text(
                    text = "Detalles de ${hourData.hour}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Información detallada
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailRow(
                        label = "Ventas",
                        value = formatCurrency(hourData.sales),
                        icon = "💰",
                        color = Color(0xFF4CAF50)
                    )
                    
                    DetailRow(
                        label = "Transacciones",
                        value = "${hourData.transactions}",
                        icon = "🛒",
                        color = Color(0xFF2196F3)
                    )
                    
                    DetailRow(
                        label = "Promedio por Transacción",
                        value = if (hourData.transactions > 0) {
                            formatCurrency(hourData.sales / hourData.transactions)
                        } else {
                            "N/A"
                        },
                        icon = "📊",
                        color = Color(0xFFFF9800)
                    )
                }

                // Botón de cerrar
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Cerrar",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
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
    icon: String,
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
                color = Color.Gray
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
