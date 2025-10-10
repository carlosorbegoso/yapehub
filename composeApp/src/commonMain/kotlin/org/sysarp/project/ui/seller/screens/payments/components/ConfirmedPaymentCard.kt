package org.sysarp.project.ui.seller.screens.payments.components

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
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.utils.formatCurrency

/**
 * Tarjeta de pago confirmado
 */
@Composable
fun ConfirmedPaymentCard(
    payment: SellerPendingPayment
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con estado
            PaymentCardHeader(
                paymentId = payment.paymentId,
                status = "Confirmado",
                statusColor = MaterialTheme.colorScheme.primaryContainer,
                statusTextColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Información del pago
            PaymentInfoSection(payment = payment)
        }
    }
}

@Composable
private fun PaymentCardHeader(
    paymentId: Int,
    status: String,
    statusColor: androidx.compose.ui.graphics.Color,
    statusTextColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Pago #$paymentId",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = statusColor
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = status,
                color = statusTextColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun PaymentInfoSection(
    payment: SellerPendingPayment
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PaymentInfoRow(
            label = "Monto",
            value = formatCurrency(payment.amount),
            icon = Icons.Filled.AttachMoney
        )
        
        PaymentInfoRow(
            label = "Cliente",
            value = payment.senderName,
            icon = Icons.Filled.Person
        )
        
        PaymentInfoRow(
            label = "Código",
            value = payment.yapeCode,
            icon = Icons.Filled.QrCode
        )
        
        PaymentInfoRow(
            label = "Fecha",
            value = payment.timestamp,
            icon = Icons.Filled.Schedule
        )
    }
}

@Composable
fun PaymentInfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
