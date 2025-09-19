package org.sysarp.project.ui.screens

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
import org.sysarp.project.service.notifications.PaymentNotificationService
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.ui.components.dashboard.SellerDashboardContent
import org.sysarp.project.ui.components.dashboard.SellerDashboardTopBar
import org.sysarp.project.ui.components.dashboard.rememberSellerPaymentManager
import org.sysarp.project.ui.components.dashboard.rememberSellerStatsManager

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
    notificationService: PaymentNotificationService,
    onNavigateToHistory: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeactivationRequest: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToQRScanner: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onLogout: () -> Unit
) {
    val userProfile by authService.userProfile.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Estados de notificaciones
    var newPaymentsCount by remember { mutableStateOf(0) }
    
    // Estados de WebSocket
    val isConnected by webSocketService.isConnected.collectAsState()
    val connectionState by webSocketService.connectionState.collectAsState()
    
    // Managers para manejar la lógica de negocio
    val paymentManager = rememberSellerPaymentManager(paymentService, authService)
    val statsManager = rememberSellerStatsManager(statsService)
    
    // Iniciar WebSocket cuando el componente se monta
    LaunchedEffect(userProfile?.sellerId, authService.accessToken.value) {
        val sellerId = userProfile?.sellerId
        val accessToken = authService.accessToken.value
        
        if (sellerId != null && accessToken != null) {
            webSocketService.startAutoConnect()
            
            // Verificar estado de conexión del vendedor
            paymentService.getSellerConnectionStatus(
                sellerId = sellerId.toInt(),
                token = accessToken
            ).fold(
                onSuccess = { response ->
                    println("🔗 [SELLER_DASHBOARD] Estado de conexión: ${response.data?.isConnected}")
                },
                onFailure = { error ->
                    println("❌ [SELLER_DASHBOARD] Error verificando conexión: ${error.message}")
                }
            )
        }
    }
    
    // Escuchar notificaciones de WebSocket
    LaunchedEffect(Unit) {
        webSocketService.paymentNotifications.collect { notification ->
            newPaymentsCount++
        }
    }
    
    // Verificación periódica de tokens (cada 2 minutos)
    LaunchedEffect(Unit) {
        while (true) {
            delay(120_000) // 2 minutos
            try {
                val refreshSuccess = authService.checkAndRefreshTokenIfNeeded()
                if (!refreshSuccess) {
                    println("⏰ [SELLER_DASHBOARD] Error en refresh periódico, verificando sesión...")
                    if (!authService.isSessionValid()) {
                        println("⏰ [SELLER_DASHBOARD] Sesión inválida después de refresh fallido, cerrando sesión...")
                        authService.logout()
                        onLogout()
                        break
                    }
                }
            } catch (e: Exception) {
                println("⏰ [SELLER_DASHBOARD] Error en verificación periódica: ${e.message}")
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
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToSettings = onNavigateToSettings,
                onLogout = onLogout
            )
        }
    ) { paddingValues ->
        SellerDashboardContent(
            accessToken = authService.accessToken.value ?: "",
            userProfile = userProfile,
            paymentService = paymentService,
            statsService = statsService,
            webSocketService = webSocketService,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToPendingPayments = onNavigateToPendingPayments,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToDeactivationRequest = onNavigateToDeactivationRequest,
            onNavigateToNotifications = onNavigateToNotifications,
            onNavigateToQRScanner = onNavigateToQRScanner,
            onNavigateToAnalytics = onNavigateToAnalytics,
            modifier = Modifier.fillMaxSize()
        )
    }
}