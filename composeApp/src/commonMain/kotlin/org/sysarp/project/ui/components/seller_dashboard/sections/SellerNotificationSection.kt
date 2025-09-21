package org.sysarp.project.ui.components.seller_dashboard.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.sysarp.project.data.PaymentNotificationData
import org.sysarp.project.ui.components.EnhancedPaymentNotificationCard

/**
 * Sección de notificaciones de pago del vendedor mejorada
 * Maneja la visualización de notificaciones de nuevos pagos con diseño moderno
 */
@Composable
fun SellerNotificationSection(
    currentNotification: PaymentNotificationData?,
    onDismissNotification: () -> Unit,
    onClaimNotification: () -> Unit,
    onRejectNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    
    currentNotification?.let { notification ->
        EnhancedPaymentNotificationCard(
            notification = notification,
            onDismiss = onDismissNotification,
            onClaim = onClaimNotification,
            onReject = onRejectNotification,
            modifier = modifier.fillMaxWidth()
        )
    } ?: run {
    }
}
