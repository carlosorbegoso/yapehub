package org.sysarp.project.service.websocket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.sysarp.project.data.PaymentNotificationData
import org.sysarp.project.service.auth.AuthService

/**
 * Servicio WebSocket que maneja la lógica de negocio para notificaciones de pagos
 */
class PaymentWebSocketService(
    private val authService: AuthService
) {
    
    /**
     * Logging para WebSocket Service
     */
    private fun logInfo(service: String, message: String) {
        println("[$service] INFO: $message")
    }
    
    private fun logError(service: String, message: String) {
        println("[$service] ERROR: $message")
    }
    
    private val webSocketClient = PaymentWebSocketClient(authService)
    private var autoStartJob: Job? = null
    
    // Estados del servicio
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _connectionState = MutableStateFlow(WebSocketConnectionState.DISCONNECTED)
    val connectionState: StateFlow<WebSocketConnectionState> = _connectionState.asStateFlow()
    
    // Flujos de notificaciones
    val paymentNotifications: SharedFlow<PaymentNotificationData> = webSocketClient.paymentNotifications
    
    /**
     * Inicia el servicio WebSocket con auto-conexión
     */
    fun startAutoConnect() {
        logInfo("WEBSOCKET_SERVICE", "🚀 Iniciando servicio WebSocket automático")
        
        autoStartJob = CoroutineScope(Dispatchers.IO).launch {
            // Observar cambios en el perfil de usuario y token
            combine(authService.userProfile, authService.accessToken) { userProfile, token ->
                logInfo("WEBSOCKET_SERVICE", "👤 Cambio detectado en perfil de usuario o token")
                logInfo("WEBSOCKET_SERVICE", "UserProfile: ${userProfile?.sellerId}, Token: ${if (token.isNullOrBlank()) "vacío" else "presente"}")
                
                if (userProfile?.sellerId != null && !token.isNullOrBlank()) {
                    logInfo("WEBSOCKET_SERVICE", "✅ Condiciones cumplidas, conectando WebSocket para sellerId: ${userProfile.sellerId}")
                    webSocketClient.connect(userProfile.sellerId.toLong())
                } else {
                    logInfo("WEBSOCKET_SERVICE", "❌ Condiciones no cumplidas, desconectando WebSocket")
                    logInfo("WEBSOCKET_SERVICE", "Razón: ${if (userProfile?.sellerId == null) "sellerId nulo" else "token vacío"}")
                    webSocketClient.disconnect()
                }
            }.collect { }
        }
        
        // Observar estado de conexión (sin reconexión automática aquí para evitar conflictos)
        CoroutineScope(Dispatchers.IO).launch {
            webSocketClient.connectionState.collect { state ->
                logInfo("WEBSOCKET_SERVICE", "🔄 Estado de conexión cambiado: $state")
                _connectionState.value = state
                _isConnected.value = state == WebSocketConnectionState.CONNECTED
                
                // Solo loggear el estado, la reconexión la maneja el cliente
                when (state) {
                    WebSocketConnectionState.CONNECTED -> {
                        logInfo("WEBSOCKET_SERVICE", "✅ WebSocket conectado y funcionando")
                    }
                    WebSocketConnectionState.DISCONNECTED -> {
                        logInfo("WEBSOCKET_SERVICE", "❌ WebSocket desconectado - el cliente manejará la reconexión")
                    }
                    WebSocketConnectionState.CONNECTING -> {
                        logInfo("WEBSOCKET_SERVICE", "🔄 WebSocket conectando...")
                    }
                    WebSocketConnectionState.RECONNECTING -> {
                        logInfo("WEBSOCKET_SERVICE", "🔄 WebSocket reconectando...")
                    }
                }
            }
        }
    }
    
    /**
     * Detiene el servicio WebSocket
     */
    fun stop() {
        logInfo("WEBSOCKET_SERVICE", "🛑 Deteniendo servicio WebSocket")
        
        if (autoStartJob != null) {
            logInfo("WEBSOCKET_SERVICE", "🔄 Cancelando trabajo de auto-conexión")
            autoStartJob?.cancel()
            autoStartJob = null
        }
        
        logInfo("WEBSOCKET_SERVICE", "🔌 Desconectando cliente WebSocket")
        webSocketClient.disconnect()
        
        logInfo("WEBSOCKET_SERVICE", "✅ Servicio WebSocket detenido completamente")
    }
    
    
    /**
     * Envía mensaje al servidor
     */
    suspend fun sendMessage(message: String) {
        logInfo("WEBSOCKET_SERVICE", "📤 Enviando mensaje al servidor: ${message.take(100)}${if (message.length > 100) "..." else ""}")
        webSocketClient.sendMessage(message)
    }
    
    
}
