package org.sysarp.project.service.websocket

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.sysarp.project.data.PaymentNotificationData
import org.sysarp.project.data.PaymentResultData
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.utils.Logger
import org.sysarp.project.utils.Constants

/**
 * Servicio WebSocket que maneja la lógica de negocio para notificaciones de pagos
 */
class PaymentWebSocketService(
    private val authService: AuthService
) {
    
    private val webSocketClient = PaymentWebSocketClient(authService)
    private var autoStartJob: Job? = null
    
    // Estados del servicio
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _connectionState = MutableStateFlow(WebSocketConnectionState.DISCONNECTED)
    val connectionState: StateFlow<WebSocketConnectionState> = _connectionState.asStateFlow()
    
    // Flujos de notificaciones
    val paymentNotifications: SharedFlow<PaymentNotificationData> = webSocketClient.paymentNotifications
    val paymentResults: SharedFlow<PaymentResultData> = webSocketClient.paymentResults
    
    /**
     * Inicia el servicio WebSocket con auto-conexión
     */
    fun startAutoConnect() {
        Logger.auth("WEBSOCKET_SERVICE", "Iniciando auto-conexión WebSocket")
        
        autoStartJob = CoroutineScope(Dispatchers.IO).launch {
            // Observar cambios en el perfil de usuario y token
            combine(authService.userProfile, authService.accessToken) { userProfile, token ->
                Logger.auth("WEBSOCKET_SERVICE", "👤 UserProfile: ${userProfile?.sellerId}, Token: ${token?.take(20)}...")
                
                if (userProfile?.sellerId != null && !token.isNullOrBlank()) {
                    Logger.auth("WEBSOCKET_SERVICE", "🔗 Usuario y token disponibles, conectando WebSocket para seller: ${userProfile.sellerId}")
                    webSocketClient.connect(userProfile.sellerId.toLong())
                } else {
                    Logger.auth("WEBSOCKET_SERVICE", "❌ Usuario no autenticado, sin sellerId o sin token")
                    webSocketClient.disconnect()
                }
            }.collect { }
        }
        
        // Observar estado de conexión
        CoroutineScope(Dispatchers.IO).launch {
            webSocketClient.connectionState.collect { state ->
                _connectionState.value = state
                _isConnected.value = state == WebSocketConnectionState.CONNECTED
                Logger.auth("WEBSOCKET_SERVICE", "Estado de conexión: $state")
                
                // Reconexión automática si se desconecta
                if (state == WebSocketConnectionState.DISCONNECTED) {
                    val userProfile = authService.userProfile.value
                    val token = authService.accessToken.value
                    if (userProfile?.sellerId != null && !token.isNullOrBlank()) {
                        delay(2000) // Esperar 2 segundos antes de reconectar
                        Logger.auth("WEBSOCKET_SERVICE", "🔄 Intentando reconexión automática...")
                        webSocketClient.connect(userProfile.sellerId.toLong())
                    }
                }
            }
        }
    }
    
    /**
     * Detiene el servicio WebSocket
     */
    fun stop() {
        Logger.auth("WEBSOCKET_SERVICE", "Deteniendo servicio WebSocket")
        
        autoStartJob?.cancel()
        autoStartJob = null
        webSocketClient.disconnect()
    }
    
    /**
     * Reconecta manualmente
     */
    suspend fun reconnect() {
        Logger.auth("WEBSOCKET_SERVICE", "Reconexión manual solicitada")
        
        val userProfile = authService.userProfile.value
        val token = authService.accessToken.value
        
        if (userProfile?.sellerId != null && !token.isNullOrBlank()) {
            Logger.auth("WEBSOCKET_SERVICE", "🔄 Reconectando WebSocket para seller: ${userProfile.sellerId}")
            webSocketClient.connect(userProfile.sellerId.toLong())
        } else {
            Logger.auth("WEBSOCKET_SERVICE", "❌ No se puede reconectar: usuario no autenticado")
        }
    }
    
    /**
     * Envía mensaje al servidor
     */
    suspend fun sendMessage(message: String) {
        webSocketClient.sendMessage(message)
    }
    
    /**
     * Obtiene información de conexión
     */
    fun getConnectionInfo(): ConnectionInfo {
        val userProfile = authService.userProfile.value
        return ConnectionInfo(
            sellerId = userProfile?.sellerId?.toInt(),
            isConnected = _isConnected.value,
            connectionState = _connectionState.value,
            websocketUrl = Constants.WEBSOCKET_URL
        )
    }
}

/**
 * Información de conexión
 */
data class ConnectionInfo(
    val sellerId: Int?,
    val isConnected: Boolean,
    val connectionState: WebSocketConnectionState,
    val websocketUrl: String
)
