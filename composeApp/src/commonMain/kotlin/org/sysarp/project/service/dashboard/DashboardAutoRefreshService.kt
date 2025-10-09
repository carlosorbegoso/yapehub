package org.sysarp.project.service.dashboard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import org.sysarp.project.utils.getCurrentTimeMillis

/**
 * Servicio para actualización automática de dashboards basado en eventos
 */
class DashboardAutoRefreshService(
    private val authService: AuthService,
    private val statsService: StatsService,
    val webSocketService: PaymentWebSocketService
) {
    
    private var refreshJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    
    // Estados para controlar las actualizaciones
    private val _isAutoRefreshEnabled = MutableStateFlow(true)
    val isAutoRefreshEnabled: StateFlow<Boolean> = _isAutoRefreshEnabled.asStateFlow()
    
    private val _lastRefreshTime = MutableStateFlow(getCurrentTimeMillis())
    val lastRefreshTime: StateFlow<Long> = _lastRefreshTime.asStateFlow()
    
    // Callbacks para actualizar dashboards
    private var onSellerDashboardRefresh: (() -> Unit)? = null
    private var onAdminDashboardRefresh: (() -> Unit)? = null
    
    // Control de throttling para evitar refrescos excesivos
    private var lastNotificationTime = 0L
    private val minTimeBetweenNotifications = 5000L // 5 segundos mínimo entre notificaciones
    private var lastPeriodicRefreshTime = 0L
    private val minTimeBetweenPeriodicRefresh = 120_000L // 2 minutos mínimo entre refrescos periódicos
    
    /**
     * Inicia el servicio de actualización automática
     */
    fun startAutoRefresh(
        onSellerRefresh: (() -> Unit)? = null,
        onAdminRefresh: (() -> Unit)? = null
    ) {
        
        onSellerDashboardRefresh = onSellerRefresh
        onAdminDashboardRefresh = onAdminRefresh
        
        // Escuchar notificaciones de WebSocket
        coroutineScope.launch {
            webSocketService.paymentNotifications.collect { notification ->
                handlePaymentNotification(notification)
            }
        }
        
        refreshJob = coroutineScope.launch {
            while (true) {
                delay(120_000) // 2 minutos en lugar de 30 segundos
                if (_isAutoRefreshEnabled.value) {
                    performPeriodicRefresh()
                }
            }
        }
        
    }
    
    /**
     * Detiene el servicio de actualización automática
     */
    fun stopAutoRefresh() {
        
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
    }
    
    /**
     * Fuerza una actualización inmediata
     */
    fun forceRefresh() {
        coroutineScope.launch {
            performRefresh("manual")
        }
    }
    
    /**
     * Maneja las notificaciones de pago del WebSocket
     */
    private suspend fun handlePaymentNotification(notification: PaymentNotificationData) {
        val currentTime = getCurrentTimeMillis()
        
        if (currentTime - lastNotificationTime < minTimeBetweenNotifications) {
            return
        }
        
        lastNotificationTime = currentTime
        
        when (notification.status) {
            "CONFIRMED", "REJECTED", "PENDING" -> {
                // Actualizar dashboards cuando hay cambios en pagos
                performRefresh("payment_notification")
            }
            else -> {
            }
        }
    }
    
    /**
     * Realiza una actualización periódica
     */
    private suspend fun performPeriodicRefresh() {
        val currentTime = getCurrentTimeMillis()
        
        if (currentTime - lastPeriodicRefreshTime < minTimeBetweenPeriodicRefresh) {
            return
        }
        
        lastPeriodicRefreshTime = currentTime
        performRefresh("periodic")
    }
    
    /**
     * Ejecuta la actualización de dashboards
     */
    private suspend fun performRefresh(source: String) {
        try {
            
            val userProfile = authService.userProfile.value
            val userRole = userProfile?.role
            
            when (userRole) {
                UserRole.VENDOR -> {
                    onSellerDashboardRefresh?.invoke()
                }
                UserRole.ADMIN -> {
                    onAdminDashboardRefresh?.invoke()
                }
                else -> {
                }
            }
            
            _lastRefreshTime.value = getCurrentTimeMillis()
            
        } catch (e: Exception) {
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