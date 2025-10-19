package org.sysarp.project.service.websocket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.sysarp.project.data.PaymentNotificationData
import org.sysarp.project.data.UserRole
import org.sysarp.project.service.auth.AuthService

/**
 * Servicio WebSocket que maneja la lógica de negocio para notificaciones de pagos
 */
class PaymentWebSocketService(
    private val authService: AuthService
) {
    
    /**
     * Logging para WebSocket Service - Solo errores críticos
     */
    private fun logInfo(service: String, message: String) {
        // Solo logs críticos de inicio/error
        if (message.contains("Iniciando servicio WebSocket") || 
            message.contains("Servicio WebSocket ya está iniciado")) {
            println("[$service] INFO: $message")
        }
    }
    
    private fun logError(service: String, message: String) {
        println("[$service] ERROR: $message")
    }
    
    private val webSocketClient = PaymentWebSocketClient(authService)
    
    // Callback para notificar cuando se recibe un mensaje
    private var onMessageReceivedCallback: (() -> Unit)? = null
    private var autoStartJob: Job? = null
    private var isAutoConnectStarted = false
    
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
    fun setOnMessageReceivedCallback(callback: () -> Unit) {
        onMessageReceivedCallback = callback
        webSocketClient.setOnMessageReceivedCallback(callback)
    }
    
    fun startAutoConnect() {
        if (isAutoConnectStarted) {
            logInfo("WEBSOCKET_SERVICE", "⚠️ Servicio WebSocket ya está iniciado, ignorando llamada duplicada")
            return
        }
        
        logInfo("WEBSOCKET_SERVICE", "🚀 Iniciando servicio WebSocket automático")
        isAutoConnectStarted = true
        
        autoStartJob = CoroutineScope(Dispatchers.IO).launch {
            // Observar cambios en el perfil de usuario y token
            combine(authService.userProfile, authService.accessToken) { userProfile, token ->
                // Verificar si es un seller con token válido
                val isSeller = userProfile?.role == UserRole.VENDOR
                val hasSellerId = userProfile?.sellerId != null
                val hasToken = !token.isNullOrBlank()
                
                if (isSeller && hasSellerId && hasToken) {
                    webSocketClient.connect(userProfile.sellerId.toLong())
                } else {
                    webSocketClient.disconnect()
                }
            }.collect { }
        }
        
        // Observar estado de conexión (sin reconexión automática aquí para evitar conflictos)
        CoroutineScope(Dispatchers.IO).launch {
            webSocketClient.connectionState.collect { state ->
                _connectionState.value = state
                _isConnected.value = state == WebSocketConnectionState.CONNECTED
            }
        }
    }
    
    /**
     * Detiene el servicio WebSocket
     */
    fun stop() {
        isAutoConnectStarted = false
        autoStartJob?.cancel()
        autoStartJob = null
        webSocketClient.disconnect()
    }
    
    
    /**
     * Envía mensaje al servidor
     */
    suspend fun sendMessage(message: String) {
        webSocketClient.sendMessage(message)
    }
    
    
}
