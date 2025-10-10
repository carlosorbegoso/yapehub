package org.sysarp.project.ui.admin.screens.payments.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.PaymentSummary
import org.sysarp.project.utils.formatCurrency

/**
 * Tarjeta de resumen de pagos administrativos mejorada
 * Incluye interactividad y mejor diseño visual
 */
@Composable
fun PaymentSummaryCard(
    summary: PaymentSummary,
    onStatusClick: (String?) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header compacto
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Payment,
                        contentDescription = "Icono de resumen de pagos",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Resumen de Pagos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Monto total destacado en el header
                Text(
                    text = formatCurrency(summary.totalAmount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Estadísticas compactas en dos filas
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primera fila: Total y Pendientes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CompactStatItem(
                        value = "${summary.totalPayments}",
                        label = "Total",
                        color = MaterialTheme.colorScheme.onSurface,
                        onClick = { onStatusClick(null) }
                    )
                    
                    CompactStatItem(
                        value = "${summary.pendingCount}",
                        label = "Pendientes",
                        color = MaterialTheme.colorScheme.error,
                        onClick = { onStatusClick("PENDING") }
                    )
                }
                
                // Segunda fila: Confirmados y Rechazados
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CompactStatItem(
                        value = "${summary.confirmedCount}",
                        label = "Confirmados",
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { onStatusClick("CONFIRMED") }
                    )
                    
                    CompactStatItem(
                        value = "${summary.rejectedCount}",
                        label = "Rechazados",
                        color = MaterialTheme.colorScheme.error,
                        onClick = { onStatusClick("REJECTED") }
                    )
                }
            }
            
            // Monto pendiente destacado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monto Pendiente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(summary.pendingAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun CompactStatItem(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.7f)
        )
    }
}
