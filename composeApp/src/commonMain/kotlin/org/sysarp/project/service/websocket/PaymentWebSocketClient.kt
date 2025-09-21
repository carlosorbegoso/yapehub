package org.sysarp.project.service.websocket

import io.ktor.client.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import org.sysarp.project.data.*
import org.sysarp.project.utils.*

/**
 * Cliente WebSocket simplificado para notificaciones de pagos en tiempo real
 */
class PaymentWebSocketClient(
    private val authService: org.sysarp.project.service.auth.AuthService
) {
    
    private val httpClient = HttpClient(CIO) {
        install(WebSockets)
    }
    
    private var webSocketSession: DefaultWebSocketSession? = null
    private var reconnectJob: Job? = null
    private var heartbeatJob: Job? = null
    
    // Estados de conexión
    private val _connectionState = MutableStateFlow(WebSocketConnectionState.DISCONNECTED)
    val connectionState: StateFlow<WebSocketConnectionState> = _connectionState.asStateFlow()
    
    // Flujos de datos
    private val _paymentNotifications = MutableSharedFlow<PaymentNotificationData>()
    val paymentNotifications: SharedFlow<PaymentNotificationData> = _paymentNotifications.asSharedFlow()
    
    private val _paymentResults = MutableSharedFlow<PaymentResultData>()
    val paymentResults: SharedFlow<PaymentResultData> = _paymentResults.asSharedFlow()
    
    // Configuración de reconexión optimizada
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 3
    private var currentSellerId: Long? = null
    private var lastReconnectTime = 0L
    private val minTimeBetweenReconnects = 30000L // 30 segundos mínimo entre reconexiones
    
    /**
     * Conecta al WebSocket del vendedor
     */
    suspend fun connect(sellerId: Long) {
        if (_connectionState.value == WebSocketConnectionState.CONNECTED) {
            return
        }
        
        val token = authService.accessToken.value
        if (token.isNullOrBlank()) {
            return
        }
        
        currentSellerId = sellerId
        _connectionState.value = WebSocketConnectionState.CONNECTING
        
        try {
            val url = "${Constants.WEBSOCKET_URL}/ws/payments/$sellerId?token=$token"
            
            httpClient.webSocket(url) {
                webSocketSession = this
                
                // Escuchar mensajes entrantes
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val message = frame.readText()
                            processMessage(message)
                        }
                        is Frame.Close -> {
                            val reason = frame.readReason()
                            _connectionState.value = WebSocketConnectionState.DISCONNECTED
                            scheduleReconnect()
                        }
                        is Frame.Ping -> {
                        }
                        is Frame.Pong -> {
                        }
                        else -> {
                        }
                    }
                }
            }
            
        } catch (e: Exception) {
            _connectionState.value = WebSocketConnectionState.DISCONNECTED
            
            val message = e.message ?: ""
            if (!message.contains("401") && !message.contains("403")) {
                scheduleReconnect()
            } else {
            }
        }
    }
    
    /**
     * Procesa mensajes entrantes del WebSocket
     */
    private suspend fun processMessage(message: String) {
        try {
            
            // Manejar mensaje de conexión especial
            if (message.contains("\"type\":\"CONNECTED\"")) {
                _connectionState.value = WebSocketConnectionState.CONNECTED
                reconnectAttempts = 0
                startHeartbeat()
                return
            }
            
            val webSocketMessage = Json.decodeFromString<WebSocketMessage>(message)
            
            when (webSocketMessage.type) {
                "PAYMENT_NOTIFICATION" -> {
                    val notificationData = PaymentNotificationData(
                        paymentId = webSocketMessage.data.paymentId,
                        amount = webSocketMessage.data.amount,
                        senderName = webSocketMessage.data.senderName,
                        yapeCode = webSocketMessage.data.yapeCode,
                        status = webSocketMessage.data.status,
                        timestamp = webSocketMessage.data.timestamp,
                        message = webSocketMessage.data.message
                    )
                    _paymentNotifications.emit(notificationData)
                }
                
                "PAYMENT_RESULT" -> {
                    val resultData = PaymentResultData(
                        paymentId = webSocketMessage.data.paymentId,
                        status = webSocketMessage.data.status,
                        message = webSocketMessage.data.message,
                        sellerId = webSocketMessage.data.sellerId ?: 0,
                        sellerName = webSocketMessage.data.sellerName ?: ""
                    )
                    _paymentResults.emit(resultData)
                }
                
                else -> {
                }
            }
            
        } catch (e: Exception) {
        }
    }
    
    /**
     * Inicia heartbeat para mantener conexión activa (optimizado)
     */
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (_connectionState.value == WebSocketConnectionState.CONNECTED) {
                delay(60000) // Heartbeat cada 60 segundos (aumentado de 30 segundos)
                
                try {
                } catch (e: Exception) {
                    _connectionState.value = WebSocketConnectionState.DISCONNECTED
                    scheduleReconnect()
                    break
                }
            }
        }
    }
    
    /**
     * Programa reconexión automática con throttling
     */
    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - lastReconnectTime < minTimeBetweenReconnects) {
            return
        }
        
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            reconnectAttempts++
            
            if (reconnectAttempts > maxReconnectAttempts) {
                reconnectAttempts = 0
                return@launch
            }
            
            val delaySeconds = minOf(reconnectAttempts * 10, 60) // 10, 20, 30 segundos (aumentado)
            
            delay(delaySeconds * 1000L)
            
            currentSellerId?.let { sellerId ->
                lastReconnectTime = System.currentTimeMillis()
                connect(sellerId)
            }
        }
    }
    
    /**
     * Desconecta el WebSocket
     */
    fun disconnect() {
        
        reconnectJob?.cancel()
        reconnectJob = null
        heartbeatJob?.cancel()
        heartbeatJob = null
        
        _connectionState.value = WebSocketConnectionState.DISCONNECTED
        reconnectAttempts = 0
        currentSellerId = null
    }
    
    /**
     * Envía mensaje al servidor
     */
    suspend fun sendMessage(message: String) {
        try {
            val session = webSocketSession
            if (session != null && _connectionState.value == WebSocketConnectionState.CONNECTED) {
                session.send(Frame.Text(message))
            } else {
            }
        } catch (e: Exception) {
        }
    }
    
}

/**
 * Estados de conexión WebSocket
 */
enum class WebSocketConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING
}