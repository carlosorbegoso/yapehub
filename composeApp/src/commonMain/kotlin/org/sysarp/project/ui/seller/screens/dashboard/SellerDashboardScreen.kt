package org.sysarp.project.ui.screens.seller

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
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.ui.components.dashboard.SellerDashboardContent
import org.sysarp.project.ui.components.dashboard.SellerDashboardTopBar

/**
 * Pantalla principal del dashboard del vendedor
 * Refactorizada para usar componentes más pequeños y manejables
 */
@Composable
fun SellerDashboardScreen(
    authService: AuthService,
    paymentService: PaymentService,
    statsService: org.sysarp.project.service.stats.StatsService,
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
    
    var newPaymentsCount by remember { mutableStateOf(0) }
    var hybridPaymentsCount by remember { mutableStateOf(0) }
    
    // Conectar con el sistema híbrido para recibir notificaciones
    LaunchedEffect(Unit) {
        hybridNotificationManager.setOnNewNotificationCallback { payments ->
            println("[SELLER_DASHBOARD] 🔔 Notificaciones híbridas recibidas: ${payments.size} pagos")
            hybridPaymentsCount += payments.size
            // Aquí se podría actualizar la UI directamente
        }
    }
    
    LaunchedEffect(userProfile?.sellerId, authService.accessToken.value) {
        val sellerId = userProfile?.sellerId
        val accessToken = authService.accessToken.value
        
        if (sellerId != null && accessToken != null) {
            webSocketService.startAutoConnect()
            
            paymentService.getSellerConnectionStatus(
                sellerId = sellerId.toInt(),
                token = accessToken
            ).fold(
                onSuccess = { response ->
                },
                onFailure = { error ->
                }
            )
        }
    }
    
    LaunchedEffect(Unit) {
        webSocketService.paymentNotifications.collect { _ ->
            newPaymentsCount++
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(120_000) // 2 minutos
            try {
                val refreshSuccess = authService.checkAndRefreshTokenIfNeeded()
                if (!refreshSuccess) {
                    // Error en refresh periódico, verificando sesión
                    if (!authService.isSessionValid()) {
                        // Sesión inválida después de refresh fallido, cerrando sesión
                        authService.logout()
                        onLogout()
                        break
                    }
                }
            } catch (e: Exception) {
                // Error en verificación periódica
            }
        }
    }
    
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