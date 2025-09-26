package org.sysarp.project.ui.common.components.charts.hourly.sales.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.sysarp.project.data.HourlySalesData
import org.sysarp.project.ui.common.components.charts.hourly.sales.data.getTimeOfDay
import org.sysarp.project.utils.formatCurrency

/**
 * Diálogo de detalles para una hora específica
 * Reutiliza DetailRow del DayDetailsDialog
 */
@Composable
fun HourDetailsDialog(
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
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
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
                // Icono de hora con período del día
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getHourEmoji(hourData.hour),
                        fontSize = 24.sp
                    )
                }

                // Título con período del día
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Detalles de ${hourData.hour}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Período: ${getTimeOfDay(hourData.hour)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Información detallada reutilizando DetailRow
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailRow(
                        label = "Ventas",
                        value = formatCurrency(hourData.sales),
                        icon = "💰",
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    DetailRow(
                        label = "Transacciones",
                        value = "${hourData.transactions}",
                        icon = "🛒",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    DetailRow(
                        label = "Promedio por Transacción",
                        value = if (hourData.transactions > 0) {
                            formatCurrency(hourData.sales / hourData.transactions)
                        } else {
                            "N/A"
                        },
                        icon = "📊",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    
                    // Información adicional del período
                    DetailRow(
                        label = "Período del Día",
                        value = getTimeOfDay(hourData.hour),
                        icon = "🌅",
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                // Botón de cerrar
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cerrar",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Componente reutilizable para mostrar una fila de detalles
 */
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

/**
 * Obtiene el emoji apropiado para la hora
 */
private fun getHourEmoji(hour: String): String {
    val hourInt = hour.substring(0, 2).toIntOrNull() ?: 0
    return when (hourInt) {
        in 6..11 -> "🌅"  // Mañana
        in 12..17 -> "☀️" // Tarde
        in 18..23 -> "🌆" // Noche
        else -> "🌙"      // Madrugada
    }
}
