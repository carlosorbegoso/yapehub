package org.sysarp.project

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.navigation.AppContent
import org.sysarp.project.navigation.rememberNavigationManager
import org.sysarp.project.service.notification.HybridNotificationManager
import org.sysarp.project.ui.theme.YapeHubTheme

@Composable
fun App() {
    YapeHubTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            YapeApp()
        }
    }
}

@Composable
fun YapeApp() {
    val hybridNotificationManager: HybridNotificationManager = koinInject()
    val navigationManager = rememberNavigationManager()

    InitializeNotificationManager(hybridNotificationManager)
    ManageNotificationLifecycle(hybridNotificationManager)

    AppContent(navigationManager = navigationManager)
}

@Composable
private fun InitializeNotificationManager(
    notificationManager: HybridNotificationManager
) {
    LaunchedEffect(Unit) {
        notificationManager.setOnNewNotificationCallback { payments ->
            handleIncomingPayments(payments)
        }
        notificationManager.start()
    }
}

@Composable
private fun ManageNotificationLifecycle(
    notificationManager: HybridNotificationManager
) {
    DisposableEffect(Unit) {
        onDispose {
            notificationManager.stop()
        }
    }
}

private fun handleIncomingPayments(payments: List<SellerPendingPayment>) {
    logPaymentNotification(payments.size)
    payments.forEach { payment ->
        logPaymentDetails(payment)
    }
}

private fun logPaymentNotification(paymentCount: Int) {
    println("[APP] 🔔 Hybrid notifications received: $paymentCount payments")
}

private fun logPaymentDetails(payment: SellerPendingPayment) {
    println(
        "[APP] 💰 Payment: ${payment.paymentId} - S/ ${payment.amount} from ${payment.senderName}"
    )
}