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
    
    private val webSocketClient = PaymentWebSocketClient()
    private var serviceJob: Job? = null
    
    // Estados del servicio
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _connectionState = MutableStateFlow(WebSocketConnectionState.DISCONNECTED)
    val connectionState: StateFlow<WebSocketConnectionState> = _connectionState.asStateFlow()
    
    // Flujos de notificaciones
    val paymentNotifications: SharedFlow<PaymentNotificationData> = webSocketClient.paymentNotifications
    val paymentResults: SharedFlow<PaymentResultData> = webSocketClient.paymentResults
    
    // Estado del vendedor
    private var currentSellerId: Int? = null
    private var currentToken: String? = null
    
    /**
     * Inicia el servicio WebSocket
     */
    fun start() {
        Logger.auth("WEBSOCKET_SERVICE", "Iniciando servicio WebSocket")
        
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            // Observar cambios en el perfil del usuario
            authService.userProfile.collect { userProfile ->
                if (userProfile?.sellerId != null) {
                    currentSellerId = userProfile.sellerId!!.toInt()
                    Logger.auth("WEBSOCKET_SERVICE", "Vendedor detectado: $currentSellerId")
                    
                    // Conectar si tenemos token
                    authService.accessToken.collect { token ->
                        if (token != null && currentSellerId != null) {
                            currentToken = token
                            connectToWebSocket()
                        } else {
                            disconnectFromWebSocket()
                        }
                    }
                } else {
                    Logger.auth("WEBSOCKET_SERVICE", "No hay vendedor logueado")
                    disconnectFromWebSocket()
                }
            }
        }
        
        // Observar estado de conexión
        CoroutineScope(Dispatchers.IO).launch {
            webSocketClient.connectionState.collect { state ->
                _connectionState.value = state
                _isConnected.value = state == WebSocketConnectionState.CONNECTED
                Logger.auth("WEBSOCKET_SERVICE", "Estado de conexión: $state")
            }
        }
    }
    
    /**
     * Detiene el servicio WebSocket
     */
    fun stop() {
        Logger.auth("WEBSOCKET_SERVICE", "Deteniendo servicio WebSocket")
        
        serviceJob?.cancel()
        disconnectFromWebSocket()
    }
    
    /**
     * Conecta al WebSocket
     */
    private fun connectToWebSocket() {
        if (currentSellerId != null && currentToken != null) {
            Logger.auth("WEBSOCKET_SERVICE", "Conectando WebSocket para vendedor: $currentSellerId")
            
            webSocketClient.connect(
                sellerId = currentSellerId!!,
                token = currentToken!!,
                baseUrl = Constants.WEBSOCKET_URL
            )
        }
    }
    
    /**
     * Desconecta del WebSocket
     */
    private fun disconnectFromWebSocket() {
        Logger.auth("WEBSOCKET_SERVICE", "Desconectando WebSocket")
        
        webSocketClient.disconnect()
        currentSellerId = null
        currentToken = null
    }
    
    /**
     * Reconecta manualmente
     */
    fun reconnect() {
        Logger.auth("WEBSOCKET_SERVICE", "Reconexión manual solicitada")
        
        if (currentSellerId != null && currentToken != null) {
            connectToWebSocket()
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
        return ConnectionInfo(
            sellerId = currentSellerId,
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
