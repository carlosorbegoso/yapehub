package org.sysarp.project.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.service.notifications.PaymentNotificationService
import org.sysarp.project.ui.components.dashboard.SellerDashboardTopBar
import org.sysarp.project.ui.components.dashboard.SellerDashboardContent
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
    onLogout: () -> Unit
) {
    val userProfile by authService.userProfile.collectAsState()
    
    // Estados de notificaciones
    var newPaymentsCount by remember { mutableStateOf(0) }
    
    // Managers para manejar la lógica de negocio
    val paymentManager = rememberSellerPaymentManager(paymentService, authService)
    val statsManager = rememberSellerStatsManager(statsService)
    
    Scaffold(
        topBar = {
            SellerDashboardTopBar(
                newPaymentsCount = newPaymentsCount,
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
            authService = authService,
            webSocketService = webSocketService,
            notificationService = notificationService,
            paymentManager = paymentManager,
            statsManager = statsManager,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToPendingPayments = onNavigateToPendingPayments,
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToDeactivationRequest = onNavigateToDeactivationRequest,
            onLogout = onLogout,
            modifier = Modifier.fillMaxSize()
        )
    }
}