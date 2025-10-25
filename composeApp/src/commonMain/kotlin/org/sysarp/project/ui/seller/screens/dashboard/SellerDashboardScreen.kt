package org.sysarp.project.ui.seller.screens.dashboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.notification.HybridNotificationManager
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.ui.seller.components.dashboard.SellerDashboardContent
import org.sysarp.project.ui.components.dashboard.SellerDashboardTopBar

/**
 * Pantalla principal del dashboard del vendedor
 * Responsabilidades:
 * - Mostrar top bar con información del usuario
 * - Delegar contenido principal a SellerDashboardContent
 * - Manejar token refresh periódico
 * - Contar notificaciones de WebSocket
 */
@Composable
fun SellerDashboardScreen(
    authService: AuthService,
    paymentService: PaymentService,
    statsService: StatsService,
    webSocketService: PaymentWebSocketService,
    hybridNotificationManager: HybridNotificationManager,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeactivationRequest: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    val userProfile by authService.userProfile.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // ===== Estados Locales =====
    var newPaymentsCount by remember { mutableStateOf(0) }

    // ===== Inicializaciones =====

    LaunchedEffect(Unit) {
        hybridNotificationManager.setOnNewNotificationCallback { _ ->

        }
    }

    LaunchedEffect(userProfile?.sellerId, authService.accessToken.value) {
        val sellerId = userProfile?.sellerId
        val accessToken = authService.accessToken.value

        if (sellerId != null && accessToken != null) {
            // Verificar estado de conexión del vendedor
            paymentService.getSellerConnectionStatus(
                sellerId = sellerId.toInt(),
                token = accessToken
            ).fold(
                onSuccess = { /* Conexión verificada */ },
                onFailure = { /* Manejar error */ }
            )
        }
    }

    // ===== WebSocket Notifications =====

    LaunchedEffect(Unit) {
        webSocketService.paymentNotifications.collect { _ ->
            newPaymentsCount++
        }
    }

    // ===== Token Refresh Periódico =====

    LaunchedEffect(Unit) {
        while (true) {
            delay(120_000) // 2 minutos
            try {
                val refreshSuccess = authService.checkAndRefreshTokenIfNeeded()
                if (!refreshSuccess) {

                    if (!authService.isSessionValid()) {
                        authService.logout()
                        onLogout()
                        break
                    }
                }
            } catch (e: Exception) {
                // Log del error pero continuar
            }
        }
    }

    // ===== UI =====

    Scaffold(
        topBar = {
            SellerDashboardTopBar(
                newPaymentsCount = newPaymentsCount,
                authService = authService,
                coroutineScope = coroutineScope,
                onNotificationsClick = {
                    newPaymentsCount = 0
                },
                onNavigateToAnalytics = onNavigateToAnalytics,
                onNavigateToSettings = onNavigateToSettings,
                onLogout = onLogout
            )
        }
    ) { _ ->
        SellerDashboardContent(
            accessToken = authService.accessToken.value ?: "",
            userProfile = userProfile,
            paymentService = paymentService,
            statsService = statsService,
            webSocketService = webSocketService,
            hybridNotificationManager = hybridNotificationManager,
            onNavigateToAnalytics = onNavigateToAnalytics,
            onNavigateToPendingPayments = onNavigateToPendingPayments,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToDeactivationRequest = onNavigateToDeactivationRequest,
            onNavigateToNotifications = onNavigateToNotifications,
            modifier = Modifier.fillMaxSize()
        )
    }
}