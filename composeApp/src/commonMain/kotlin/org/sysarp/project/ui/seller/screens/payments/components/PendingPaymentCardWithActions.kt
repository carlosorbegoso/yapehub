package org.sysarp.project.ui.seller.screens.payments.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.utils.extractShortYapeCode
import org.sysarp.project.utils.formatCurrency

/**
 * Tarjeta de pago pendiente con acciones
 */
@Composable
fun PendingPaymentCardWithActions(
    payment: SellerPendingPayment,
    sellerId: Int?,
    accessToken: String?,
    paymentService: PaymentService,
    onClaimPayment: () -> Unit,
    onError: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con monto y estado
            PendingPaymentHeader(payment = payment)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Información del pago
            PendingPaymentInfo(payment = payment)
            
            // Mensaje si existe
            if (payment.message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = payment.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de confirmación
            ConfirmPaymentButton(
                sellerId = sellerId,
                accessToken = accessToken,
                payment = payment,
                paymentService = paymentService,
                coroutineScope = coroutineScope,
                onClaimPayment = onClaimPayment,
                onError = onError
            )
        }
    }
}

@Composable
private fun PendingPaymentHeader(
    payment: SellerPendingPayment
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatCurrency(payment.amount),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = payment.status,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun PendingPaymentInfo(
    payment: SellerPendingPayment
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Información del remitente
        PaymentInfoRow(
            icon = Icons.Filled.Person,
            label = "Remitente",
            value = payment.senderName
        )
        
        // Código Yape
        PaymentInfoRow(
            icon = Icons.Filled.QrCode,
            label = "Código",
            value = extractShortYapeCode(payment.yapeCode),
            valueFontFamily = FontFamily.Monospace
        )
        
        // Fecha
        PaymentInfoRow(
            icon = Icons.Filled.Schedule,
            label = "Fecha",
            value = formatPaymentTimestamp(payment.timestamp)
        )
    }
}

@Composable
private fun PaymentInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueFontFamily: FontFamily? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = valueFontFamily
        )
    }
}

@Composable
private fun ConfirmPaymentButton(
    sellerId: Int?,
    accessToken: String?,
    payment: SellerPendingPayment,
    paymentService: PaymentService,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onClaimPayment: () -> Unit,
    onError: (String) -> Unit
) {
    Button(
        onClick = {
            if (sellerId != null && accessToken != null) {
                coroutineScope.launch {
                    paymentService.claimPayment(
                        sellerId = sellerId,
                        paymentId = payment.paymentId,
                        token = accessToken
                    ).fold(
                        onSuccess = { response ->
                            onClaimPayment()
                        },
                        onFailure = { error ->
                            onError("Error confirmando pago: ${error.message}")
                        }
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Confirmar pago",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Confirmar Pago",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun formatPaymentTimestamp(timestamp: String): String {
    return formatTimestamp(timestamp)
}

/**
 * Función multiplataforma para formatear timestamps
 */
internal expect fun formatTimestamp(timestamp: String): String
