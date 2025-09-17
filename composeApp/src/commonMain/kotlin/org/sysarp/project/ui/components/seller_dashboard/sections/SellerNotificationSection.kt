package org.sysarp.project.ui.components.seller_dashboard.sections

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.PaymentNotificationData
import org.sysarp.project.ui.components.PaymentNotificationCard

/**
 * Sección de notificaciones de pago del vendedor
 * Maneja la visualización de notificaciones de nuevos pagos
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
        PaymentNotificationCard(
            notification = notification,
            onDismiss = onDismissNotification,
            onClaim = onClaimNotification,
            onReject = onRejectNotification,
            modifier = modifier.fillMaxWidth()
        )
    }
}
