package org.sysarp.project.service.websocket

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import org.sysarp.project.data.WebSocketMessage
import org.sysarp.project.data.PaymentNotificationData
import org.sysarp.project.data.PaymentResultData
import org.sysarp.project.utils.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach

/**
 * Cliente WebSocket para recibir notificaciones de pagos en tiempo real
 */
class PaymentWebSocketClient {
    
    private var webSocketJob: Job? = null
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
    
    // Canal para mensajes entrantes
    private val messageChannel = Channel<String>(Channel.UNLIMITED)
    
    // Configuración
    private var sellerId: Int? = null
    private var token: String? = null
    private var baseUrl: String = "ws://localhost:8080"
    
    // Configuración de reconexión
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 10
    private val reconnectDelayMs = 5000L
    
    /**
     * Conecta al WebSocket del vendedor
     */
    fun connect(sellerId: Int, token: String, baseUrl: String = "ws://localhost:8080") {
        this.sellerId = sellerId
        this.token = token
        this.baseUrl = baseUrl
        
        Logger.auth("WEBSOCKET", "Conectando WebSocket para vendedor: $sellerId")
        
        // Cancelar conexiones anteriores
        disconnect()
        
        // Iniciar nueva conexión
        webSocketJob = CoroutineScope(Dispatchers.IO).launch {
            connectWebSocket()
        }
        
        // Iniciar procesamiento de mensajes
        CoroutineScope(Dispatchers.IO).launch {
            processMessages()
        }
    }
    
    /**
     * Desconecta el WebSocket
     */
    fun disconnect() {
        Logger.auth("WEBSOCKET", "Desconectando WebSocket")
        
        webSocketJob?.cancel()
        reconnectJob?.cancel()
        heartbeatJob?.cancel()
        
        _connectionState.value = WebSocketConnectionState.DISCONNECTED
        reconnectAttempts = 0
    }
    
    /**
     * Conecta al WebSocket
     */
    private suspend fun connectWebSocket() {
        try {
            _connectionState.value = WebSocketConnectionState.CONNECTING
            
            val url = "$baseUrl/ws/payments/$sellerId"
            Logger.auth("WEBSOCKET", "Conectando a: $url")
            
            // TODO: Implementar conexión WebSocket real usando Ktor
            // Por ahora simulamos la conexión
            simulateWebSocketConnection()
            
        } catch (e: Exception) {
            Logger.auth("WEBSOCKET", "Error conectando WebSocket: ${e.message}")
            _connectionState.value = WebSocketConnectionState.DISCONNECTED
            scheduleReconnect()
        }
    }
    
    /**
     * Simula la conexión WebSocket (para desarrollo)
     * TODO: Reemplazar con implementación real de Ktor WebSocket
     */
    private suspend fun simulateWebSocketConnection() {
        Logger.auth("WEBSOCKET", "Simulando conexión WebSocket")
        
        // Simular conexión exitosa
        delay(1000)
        _connectionState.value = WebSocketConnectionState.CONNECTED
        reconnectAttempts = 0
        
        // Iniciar heartbeat
        startHeartbeat()
        
        // Simular mensaje de prueba (para desarrollo)
        simulateTestMessage()
    }
    
    /**
     * Simula un mensaje de prueba (para desarrollo)
     */
    private suspend fun simulateTestMessage() {
        delay(5000) // Esperar 5 segundos antes de enviar mensaje de prueba
        
        val testMessage = """
        {
            "type": "PAYMENT_NOTIFICATION",
            "data": {
                "paymentId": 9999,
                "amount": 50.0,
                "senderName": "987654321",
                "yapeCode": "YAPE_1757840358050_907349_68",
                "status": "PENDING",
                "timestamp": "2025-09-15T22:26:52.09808",
                "message": "Pago pendiente de confirmación"
            }
        }
        """.trimIndent()
        
        Logger.auth("WEBSOCKET", "Enviando mensaje de prueba")
        messageChannel.send(testMessage)
    }
    
    /**
     * Procesa mensajes entrantes
     */
    private suspend fun processMessages() {
        messageChannel.consumeEach { message ->
            try {
                Logger.auth("WEBSOCKET", "Procesando mensaje: $message")
                
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
                        Logger.auth("WEBSOCKET", "Notificación de pago recibida: ${notificationData.paymentId}")
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
                        Logger.auth("WEBSOCKET", "Resultado de pago recibido: ${resultData.paymentId}")
                    }
                    
                    else -> {
                        Logger.auth("WEBSOCKET", "Tipo de mensaje desconocido: ${webSocketMessage.type}")
                    }
                }
                
            } catch (e: Exception) {
                Logger.auth("WEBSOCKET", "Error procesando mensaje: ${e.message}")
            }
        }
    }
    
    /**
     * Programa reconexión automática
     */
    private fun scheduleReconnect() {
        if (reconnectAttempts >= maxReconnectAttempts) {
            Logger.auth("WEBSOCKET", "Máximo número de intentos de reconexión alcanzado")
            return
        }
        
        reconnectJob?.cancel()
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            val delay = reconnectDelayMs * (reconnectAttempts + 1)
            Logger.auth("WEBSOCKET", "Reconectando en ${delay}ms (intento ${reconnectAttempts + 1})")
            
            delay(delay)
            reconnectAttempts++
            
            if (sellerId != null && token != null) {
                connectWebSocket()
            }
        }
    }
    
    /**
     * Inicia heartbeat para mantener conexión activa
     */
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && _connectionState.value == WebSocketConnectionState.CONNECTED) {
                delay(30000) // Heartbeat cada 30 segundos
                
                try {
                    // TODO: Enviar ping al servidor
                    Logger.auth("WEBSOCKET", "Enviando heartbeat")
                } catch (e: Exception) {
                    Logger.auth("WEBSOCKET", "Error en heartbeat: ${e.message}")
                    _connectionState.value = WebSocketConnectionState.DISCONNECTED
                    scheduleReconnect()
                    break
                }
            }
        }
    }
    
    /**
     * Envía mensaje al servidor
     */
    suspend fun sendMessage(message: String) {
        try {
            // TODO: Implementar envío real de mensajes
            Logger.auth("WEBSOCKET", "Enviando mensaje: $message")
        } catch (e: Exception) {
            Logger.auth("WEBSOCKET", "Error enviando mensaje: ${e.message}")
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
