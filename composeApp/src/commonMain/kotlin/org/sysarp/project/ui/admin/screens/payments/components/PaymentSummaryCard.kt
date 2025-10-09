package org.sysarp.project.ui.admin.screens.payments.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.PaymentSummary
import org.sysarp.project.utils.formatCurrency

/**
 * Tarjeta de resumen de pagos administrativos
 * Refactorizada para ser más modular y mantenible
 */
@Composable
fun PaymentSummaryCard(summary: PaymentSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Resumen de Pagos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Estadísticas principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AdminStatItem(
                    value = "${summary.totalPayments}",
                    label = "Total",
                    icon = Icons.Filled.Payment,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                AdminStatItem(
                    value = "${summary.pendingCount}",
                    label = "Pendientes",
                    icon = Icons.Filled.Schedule,
                    color = MaterialTheme.colorScheme.error
                )
                
                AdminStatItem(
                    value = "${summary.confirmedCount}",
                    label = "Confirmados",
                    icon = Icons.Filled.CheckCircle,
                    color = MaterialTheme.colorScheme.primary
                )
                
                AdminStatItem(
                    value = "${summary.rejectedCount}",
                    label = "Rechazados",
                    icon = Icons.Filled.Cancel,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Montos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Monto Total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatCurrency(summary.totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Column {
                    Text(
                        text = "Pendiente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
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
}
