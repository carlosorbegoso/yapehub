package org.sysarp.project.ui.admin.screens.payments.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.AdminPayment
import org.sysarp.project.utils.extractShortYapeCode
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatDateTime

/**
 * Tarjeta individual de pago administrativo
 * Refactorizada para ser más modular y mantenible
 */
@Composable
fun AdminPaymentCard(
    payment: AdminPayment,
    onAction: (String) -> Unit = {}
) {
    val statusColor = when (payment.status) {
        "PENDING" -> MaterialTheme.colorScheme.error
        "CONFIRMED" -> MaterialTheme.colorScheme.primary
        "REJECTED_BY_SELLER" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val statusIcon = when (payment.status) {
        "PENDING" -> Icons.Filled.Schedule
        "CONFIRMED" -> Icons.Filled.CheckCircle
        "REJECTED_BY_SELLER" -> Icons.Filled.Cancel
        else -> Icons.Filled.Payment
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header con estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pago #${payment.paymentId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = statusColor.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = payment.status,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Información del pago
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(
                    label = "Monto",
                    value = formatCurrency(payment.amount),
                    icon = Icons.Filled.AttachMoney,
                    valueColor = MaterialTheme.colorScheme.primary
                )
                
                InfoRow(
                    label = "De",
                    value = payment.senderName,
                    icon = Icons.Filled.Person
                )
                
                InfoRow(
                    label = "Código",
                    value = extractShortYapeCode(payment.yapeCode),
                    icon = Icons.Filled.QrCode,
                    valueColor = MaterialTheme.colorScheme.primary
                )
                
                InfoRow(
                    label = "Vendedor",
                    value = payment.sellerName,
                    icon = Icons.Filled.Store
                )
                
                InfoRow(
                    label = "Sucursal",
                    value = payment.branchName,
                    icon = Icons.Filled.Business
                )
                
                InfoRow(
                    label = "Fecha",
                    value = formatDateTime(payment.createdAt),
                    icon = Icons.Filled.Schedule
                )
            }
            
            // Botones de acción (si el pago está pendiente)
            if (payment.status == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onAction("CONFIRM") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Confirmar")
                    }
                    
                    OutlinedButton(
                        onClick = { onAction("REJECT") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar")
                    }
                }
            }
        }
    }
}
