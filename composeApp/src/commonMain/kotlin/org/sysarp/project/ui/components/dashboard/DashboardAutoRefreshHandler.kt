package org.sysarp.project.ui.components.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.dashboard.DashboardAutoRefreshService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.service.websocket.PaymentWebSocketService

/**
 * Composable para manejar la actualización automática de dashboards
 */
@Composable
fun DashboardAutoRefreshHandler(
    authService: AuthService,
    statsService: StatsService,
    webSocketService: PaymentWebSocketService,
    onRefreshSellerDashboard: () -> Unit = {},
    onRefreshAdminDashboard: () -> Unit = {},
    autoRefreshEnabled: Boolean = true
) {
    // Crear el servicio de actualización automática
    val refreshService = remember(authService, statsService, webSocketService) {
        DashboardAutoRefreshService(authService, statsService, webSocketService)
    }
    
    // Iniciar el servicio cuando el composable se monta
    LaunchedEffect(refreshService, autoRefreshEnabled) {
        
        refreshService.setAutoRefreshEnabled(autoRefreshEnabled)
        refreshService.startAutoRefresh(
            onSellerRefresh = {
                onRefreshSellerDashboard()
            },
            onAdminRefresh = {
                onRefreshAdminDashboard()
            }
        )
    }
    
    // Cleanup cuando el composable se desmonta
    DisposableEffect(refreshService) {
        onDispose {
            refreshService.stopAutoRefresh()
        }
    }
}
