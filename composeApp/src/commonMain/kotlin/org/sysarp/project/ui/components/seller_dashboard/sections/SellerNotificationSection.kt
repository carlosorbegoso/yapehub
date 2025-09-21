package org.sysarp.project.ui.components.seller_dashboard.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.sysarp.project.data.PaymentNotificationData
import org.sysarp.project.ui.components.EnhancedPaymentNotificationCard
import org.sysarp.project.utils.Logger

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
    Logger.auth("NOTIFICATION_SECTION", "🔍 SellerNotificationSection renderizando - currentNotification: ${currentNotification?.paymentId}")
    
    currentNotification?.let { notification ->
        Logger.auth("NOTIFICATION_SECTION", "📱 Mostrando EnhancedPaymentNotificationCard para pago: ${notification.paymentId}")
        EnhancedPaymentNotificationCard(
            notification = notification,
            onDismiss = onDismissNotification,
            onClaim = onClaimNotification,
            onReject = onRejectNotification,
            modifier = modifier.fillMaxWidth()
        )
    } ?: run {
        Logger.auth("NOTIFICATION_SECTION", "❌ No hay notificación para mostrar")
    }
}
