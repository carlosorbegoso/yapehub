package org.sysarp.project

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
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

            KoinContext {
                YapeApp()
            }
        }
    }
}

@Composable
fun YapeApp() {
    // Koin will provide the dependencies. No more manual creation!
    val hybridNotificationManager: HybridNotificationManager = koinInject()
    val navigationManager = rememberNavigationManager()

    LaunchedEffect(Unit) {
        // Configure and start the notification manager
        hybridNotificationManager.setOnNewNotificationCallback { payments ->
            println("[APP] 🔔 Hybrid notifications received: ${payments.size} payments")
            payments.forEach { payment ->
                println("[APP] 💰 Payment: ${payment.paymentId} - S/ ${payment.amount} from ${payment.senderName}")
            }
        }
        hybridNotificationManager.start()
    }

    DisposableEffect(Unit) {
        onDispose {
            // Stop the manager when the app closes
            hybridNotificationManager.stop()
        }
    }

    // AppContent is now much cleaner!
    // Screens inside AppContent can now use koinInject() to get their own dependencies.
    AppContent(navigationManager = navigationManager)
}
