package org.sysarp.project.ui.components.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.dashboard.DashboardAutoRefreshService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.utils.Logger

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
        Logger.auth("DASHBOARD_REFRESH_HANDLER", "🚀 Iniciando handler de actualización automática")
        
        refreshService.setAutoRefreshEnabled(autoRefreshEnabled)
        refreshService.startAutoRefresh(
            onSellerRefresh = {
                Logger.auth("DASHBOARD_REFRESH_HANDLER", "🔄 Actualizando dashboard del vendedor")
                onRefreshSellerDashboard()
            },
            onAdminRefresh = {
                Logger.auth("DASHBOARD_REFRESH_HANDLER", "🔄 Actualizando dashboard del administrador")
                onRefreshAdminDashboard()
            }
        )
    }
    
    // Cleanup cuando el composable se desmonta
    DisposableEffect(refreshService) {
        onDispose {
            Logger.auth("DASHBOARD_REFRESH_HANDLER", "🛑 Deteniendo handler de actualización automática")
            refreshService.stopAutoRefresh()
        }
    }
}

