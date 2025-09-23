package org.sysarp.project.ui.components.seller_dashboard.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.ui.components.seller_dashboard.cards.SellerPaymentCard

/**
 * Sección de lista de pagos pendientes del vendedor
 * Maneja la visualización de la lista de pagos y estados vacíos/carga
 */
@Composable
fun SellerPaymentListSection(
    filteredPayments: List<SellerPendingPayment>,
    pendingPayments: List<SellerPendingPayment>,
    showAllPayments: Boolean,
    processingPayments: Set<Int>,
    isRefreshing: Boolean,
    onClaimPayment: (Int) -> Unit,
    onRejectPayment: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (filteredPayments.isNotEmpty()) {
        val paymentsToShow = if (showAllPayments) filteredPayments else filteredPayments.take(2)
        
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            paymentsToShow.forEach { payment ->
                SellerPaymentCard(
                    payment = payment,
                    onClaim = onClaimPayment,
                    onReject = onRejectPayment,
                    isProcessing = processingPayments.contains(payment.paymentId)
                )
            }
        }
    } else if (pendingPayments.isEmpty() && !isRefreshing) {
        // Estado vacío
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                text = "No hay pagos pendientes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Los nuevos pagos aparecerán aquí automáticamente",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    
    // Indicador de carga
    if (isRefreshing) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Actualizando datos...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
