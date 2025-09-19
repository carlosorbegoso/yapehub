package org.sysarp.project.service.dashboard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sysarp.project.data.PaymentNotificationData
import org.sysarp.project.data.UserRole
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.utils.Logger

/**
 * Servicio para actualización automática de dashboards basado en eventos
 */
class DashboardAutoRefreshService(
    private val authService: AuthService,
    private val statsService: StatsService,
    val webSocketService: PaymentWebSocketService
) {
    
    private var refreshJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    // Estados para controlar las actualizaciones
    private val _isAutoRefreshEnabled = MutableStateFlow(true)
    val isAutoRefreshEnabled: StateFlow<Boolean> = _isAutoRefreshEnabled.asStateFlow()
    
    private val _lastRefreshTime = MutableStateFlow(System.currentTimeMillis())
    val lastRefreshTime: StateFlow<Long> = _lastRefreshTime.asStateFlow()
    
    // Callbacks para actualizar dashboards
    private var onSellerDashboardRefresh: (() -> Unit)? = null
    private var onAdminDashboardRefresh: (() -> Unit)? = null
    
    /**
     * Inicia el servicio de actualización automática
     */
    fun startAutoRefresh(
        onSellerRefresh: (() -> Unit)? = null,
        onAdminRefresh: (() -> Unit)? = null
    ) {
        Logger.auth("DASHBOARD_REFRESH", "🔄 Iniciando servicio de actualización automática")
        
        onSellerDashboardRefresh = onSellerRefresh
        onAdminDashboardRefresh = onAdminRefresh
        
        // Escuchar notificaciones de WebSocket
        coroutineScope.launch {
            webSocketService.paymentNotifications.collect { notification ->
                handlePaymentNotification(notification)
            }
        }
        
        // Actualización periódica cada 30 segundos
        refreshJob = coroutineScope.launch {
            while (true) {
                delay(30_000) // 30 segundos
                if (_isAutoRefreshEnabled.value) {
                    performPeriodicRefresh()
                }
            }
        }
        
        Logger.auth("DASHBOARD_REFRESH", "✅ Servicio de actualización automática iniciado")
    }
    
    /**
     * Detiene el servicio de actualización automática
     */
    fun stopAutoRefresh() {
        Logger.auth("DASHBOARD_REFRESH", "⏹️ Deteniendo servicio de actualización automática")
        
        refreshJob?.cancel()
        refreshJob = null
        onSellerDashboardRefresh = null
        onAdminDashboardRefresh = null
    }
    
    /**
     * Habilita o deshabilita la actualización automática
     */
    fun setAutoRefreshEnabled(enabled: Boolean) {
        _isAutoRefreshEnabled.value = enabled
        Logger.auth("DASHBOARD_REFRESH", "🔄 Actualización automática ${if (enabled) "habilitada" else "deshabilitada"}")
    }
    
    /**
     * Fuerza una actualización inmediata
     */
    fun forceRefresh() {
        Logger.auth("DASHBOARD_REFRESH", "🔄 Forzando actualización inmediata")
        coroutineScope.launch {
            performRefresh("manual")
        }
    }
    
    /**
     * Maneja las notificaciones de pago del WebSocket
     */
    private suspend fun handlePaymentNotification(notification: PaymentNotificationData) {
        Logger.auth("DASHBOARD_REFRESH", "📨 Notificación de pago recibida: ${notification.status}")
        
        when (notification.status) {
            "CONFIRMED", "REJECTED", "PENDING" -> {
                // Actualizar dashboards cuando hay cambios en pagos
                performRefresh("payment_notification")
            }
            else -> {
                Logger.auth("DASHBOARD_REFRESH", "ℹ️ Estado de notificación no requiere actualización: ${notification.status}")
            }
        }
    }
    
    /**
     * Realiza una actualización periódica
     */
    private suspend fun performPeriodicRefresh() {
        Logger.auth("DASHBOARD_REFRESH", "⏰ Actualización periódica")
        performRefresh("periodic")
    }
    
    /**
     * Ejecuta la actualización de dashboards
     */
    private suspend fun performRefresh(source: String) {
        try {
            Logger.auth("DASHBOARD_REFRESH", "🔄 Ejecutando actualización desde: $source")
            
            val userProfile = authService.userProfile.value
            val userRole = userProfile?.role
            
            when (userRole) {
                UserRole.VENDOR -> {
                    Logger.auth("DASHBOARD_REFRESH", "👤 Actualizando dashboard del vendedor")
                    onSellerDashboardRefresh?.invoke()
                }
                UserRole.ADMIN -> {
                    Logger.auth("DASHBOARD_REFRESH", "👑 Actualizando dashboard del administrador")
                    onAdminDashboardRefresh?.invoke()
                }
                else -> {
                    Logger.auth("DASHBOARD_REFRESH", "❓ Rol de usuario no reconocido: $userRole")
                }
            }
            
            _lastRefreshTime.value = System.currentTimeMillis()
            Logger.auth("DASHBOARD_REFRESH", "✅ Actualización completada desde: $source")
            
        } catch (e: Exception) {
            Logger.auth("DASHBOARD_REFRESH", "❌ Error en actualización: ${e.message}")
        }
    }
    
    /**
     * Obtiene estadísticas del servicio
     */
    fun getServiceStats(): DashboardRefreshStats {
        return DashboardRefreshStats(
            isEnabled = _isAutoRefreshEnabled.value,
            lastRefreshTime = _lastRefreshTime.value,
            isWebSocketConnected = webSocketService.isConnected.value
        )
    }
}

/**
 * Estadísticas del servicio de actualización automática
 */
data class DashboardRefreshStats(
    val isEnabled: Boolean,
    val lastRefreshTime: Long,
    val isWebSocketConnected: Boolean
)