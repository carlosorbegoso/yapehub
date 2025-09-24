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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

            // Gráfico de barras interactivo
            HourlyBarsChart(
                hourlySales = hourlySales,
                onHourSelected = { hourData ->
                    selectedHour = hourData
                    showDetailsDialog = true
                }
            )

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
            selectedIndex = selectedIndex
        )
    }
}

private fun DrawScope.drawHourlyBars(
    hourlySales: List<HourlySalesData>,
    maxSales: Double,
    animationProgress: Float,
    canvasSize: Size,
    selectedIndex: Int = -1
) {
    val barWidth = canvasSize.width / 24f
    val maxBarHeight = canvasSize.height * 0.8f
    val startY = canvasSize.height * 0.1f

    hourlySales.forEachIndexed { index, hourData ->
        val barHeight = if (maxSales > 0) {
            (hourData.sales / maxSales * maxBarHeight * animationProgress).toFloat()
        } else 0f

        val x = index * barWidth
        val y = startY + maxBarHeight - barHeight
        val isSelected = index == selectedIndex

        // Color basado en la intensidad de ventas
        val baseColor = when {
            hourData.sales > maxSales * 0.7 -> Color(0xFF4CAF50) // Verde fuerte
            hourData.sales > maxSales * 0.4 -> Color(0xFF8BC34A) // Verde medio
            hourData.sales > maxSales * 0.1 -> Color(0xFFFFC107) // Amarillo
            hourData.sales > 0 -> Color(0xFFFF9800) // Naranja
            else -> Color(0xFFE0E0E0) // Gris claro
        }

        // Color final con efecto de selección
        val finalColor = if (isSelected) {
            baseColor.copy(alpha = 0.8f)
        } else {
            baseColor
        }

        // Dibujar barra
        drawRect(
            color = finalColor,
            topLeft = Offset(x + 2, y),
            size = Size(barWidth - 4, barHeight)
        )

        // Dibujar borde de selección
        if (isSelected) {
            drawRect(
                color = Color(0xFF2196F3),
                topLeft = Offset(x + 1, y - 2),
                size = Size(barWidth - 2, barHeight + 4),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )
        }

        // Dibujar etiqueta de hora cada 4 horas
        if (index % 4 == 0) {
            val hourText = hourData.hour.substring(0, 2)
            // Nota: En Compose multiplataforma, el dibujo de texto en Canvas es limitado
            // Se puede usar drawIntoCanvas para acceso nativo si es necesario
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            label = "Hora Pico",
            value = peakHour?.hour ?: "N/A",
            color = Color(0xFF4CAF50)
        )
        StatItem(
            label = "Total Ventas",
            value = formatCurrency(totalSales),
            color = Color(0xFF2196F3)
        )
        StatItem(
            label = "Horas Activas",
            value = "$activeHours/24",
            color = Color(0xFFFF9800)
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
