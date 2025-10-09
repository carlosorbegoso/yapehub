package org.sysarp.project.service.websocket

import io.ktor.client.*
import io.ktor.client.engine.cio.*
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
    
    /**
     * Logging para WebSocket
     */
    private fun logInfo(service: String, message: String) {
        println("[$service] INFO: $message")
    }
    
    private fun logError(service: String, message: String) {
        println("[$service] ERROR: $message")
    }
    
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
    
    // Configuración de reconexión automática
    private var reconnectAttempts = 0
    private var currentSellerId: Long? = null
    private var lastReconnectTime = 0L
    private val minTimeBetweenReconnects = 3000L // 3 segundos entre reconexiones
    private var lastHeartbeatTime = 0L
    private var lastMessageTime = 0L
    private var connectionCheckJob: Job? = null
    
    // Callback para notificar cuando se recibe un mensaje
    private var onMessageReceivedCallback: (() -> Unit)? = null
    
    /**
     * Conecta al WebSocket del vendedor
     */
    suspend fun connect(sellerId: Long) {
        if (_connectionState.value == WebSocketConnectionState.CONNECTED && currentSellerId == sellerId) {
            logInfo("WEBSOCKET", "Ya conectado para sellerId: $sellerId, ignorando intento de conexión")
            return
        }
        
        // Si hay una conexión diferente, desconectar primero
        if (_connectionState.value == WebSocketConnectionState.CONNECTED && currentSellerId != sellerId) {
            logInfo("WEBSOCKET", "Desconectando conexión anterior para sellerId: $currentSellerId")
            disconnect()
        }
        
        val token = authService.accessToken.value
        if (token.isNullOrBlank()) {
            logError("WEBSOCKET", "Token de acceso vacío, no se puede conectar para sellerId: $sellerId")
            return
        }
        
        currentSellerId = sellerId
        _connectionState.value = WebSocketConnectionState.CONNECTING
        
        logInfo("WEBSOCKET", "Iniciando conexión WebSocket para sellerId: $sellerId")
        logInfo("WEBSOCKET", "Estado de conexión: CONNECTING")
        
        try {
            val url = "${Constants.WEBSOCKET_URL}/ws/payments/$sellerId?token=$token"
            logInfo("WEBSOCKET", "URL de conexión: $url")
            
            httpClient.webSocket(url) {
                webSocketSession = this
                _connectionState.value = WebSocketConnectionState.CONNECTED
                reconnectAttempts = 0 // Resetear intentos de reconexión
                
                logInfo("WEBSOCKET", "✅ WebSocket conectado exitosamente para sellerId: $sellerId")
                logInfo("WEBSOCKET", "Estado de conexión: CONNECTED")
                logInfo("WEBSOCKET", "🔄 Intentos de reconexión reseteados a 0")
                
                lastHeartbeatTime = System.currentTimeMillis()
                lastMessageTime = System.currentTimeMillis()
                startHeartbeat()
                startConnectionCheck()
                
                // Escuchar mensajes entrantes con loop robusto
                try {
                    for (frame in incoming) {
                        try {
                            when (frame) {
                        is Frame.Text -> {
                            val message = frame.readText()
                            lastMessageTime = System.currentTimeMillis()
                            logInfo("WEBSOCKET", "📨 Mensaje recibido: ${message.take(100)}${if (message.length > 100) "..." else ""}")
                            processMessage(message)
                            
                            // Notificar al HybridNotificationManager que recibimos un mensaje
                            notifyMessageReceived()
                        }
                                is Frame.Close -> {
                                    val reason = frame.readReason()
                                    logInfo("WEBSOCKET", "🔌 WebSocket cerrado por el servidor. Razón: ${reason?.message ?: "No especificada"}")
                                    _connectionState.value = WebSocketConnectionState.DISCONNECTED
                                    logInfo("WEBSOCKET", "🔄 Programando reconexión automática...")
                                    scheduleReconnect()
                                    break
                                }
                                is Frame.Ping -> {
                                    logInfo("WEBSOCKET", "🏓 Ping recibido del servidor")
                                    lastMessageTime = System.currentTimeMillis()
                                }
                                is Frame.Pong -> {
                                    logInfo("WEBSOCKET", "🏓 Pong recibido del servidor")
                                    lastMessageTime = System.currentTimeMillis()
                                }
                                else -> {
                                    logInfo("WEBSOCKET", "📦 Frame recibido: ${frame::class.simpleName}")
                                    lastMessageTime = System.currentTimeMillis()
                                }
                            }
                        } catch (frameException: Exception) {
                            logError("WEBSOCKET", "❌ Error procesando frame: ${frameException.message}")
                            // Continuar con el siguiente frame
                        }
                    }
                } catch (loopException: Exception) {
                    logError("WEBSOCKET", "❌ Error en loop de mensajes: ${loopException.message}")
                    _connectionState.value = WebSocketConnectionState.DISCONNECTED
                    logInfo("WEBSOCKET", "🔄 Programando reconexión por error en loop...")
                    scheduleReconnect()
                }
            }
            
        } catch (e: Exception) {
            _connectionState.value = WebSocketConnectionState.DISCONNECTED
            logError("WEBSOCKET", "❌ Error al conectar WebSocket para sellerId: $sellerId")
            logError("WEBSOCKET", "Error: ${e.message}")
            logError("WEBSOCKET", "Estado de conexión: DISCONNECTED")
            
            val message = e.message ?: ""
            if (!message.contains("401") && !message.contains("403")) {
                logInfo("WEBSOCKET", "🔄 Programando reconexión automática...")
                scheduleReconnect()
            } else {
                logError("WEBSOCKET", "🚫 Error de autenticación (401/403), no se intentará reconectar")
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
        logInfo("WEBSOCKET", "💓 Iniciando heartbeat para mantener conexión activa")
        heartbeatJob?.cancel()
        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (_connectionState.value == WebSocketConnectionState.CONNECTED) {
                delay(60000) // Heartbeat cada 60 segundos (aumentado de 30 segundos)
                
                try {
                    logInfo("WEBSOCKET", "💓 Heartbeat - conexión activa")
                    // Enviar ping para mantener conexión
                    webSocketSession?.send(Frame.Ping(ByteArray(0)))
                    lastHeartbeatTime = System.currentTimeMillis()
                } catch (e: Exception) {
                    logError("WEBSOCKET", "❌ Error en heartbeat: ${e.message}")
                    _connectionState.value = WebSocketConnectionState.DISCONNECTED
                    logInfo("WEBSOCKET", "🔄 Error en heartbeat, programando reconexión...")
                    scheduleReconnect()
                    break
                }
            }
            logInfo("WEBSOCKET", "💓 Heartbeat detenido - conexión no activa")
        }
    }
    
    /**
     * Inicia verificación de conexión para detectar conexiones muertas
     */
    private fun startConnectionCheck() {
        logInfo("WEBSOCKET", "🔍 Iniciando verificación de conexión")
        connectionCheckJob?.cancel()
        connectionCheckJob = CoroutineScope(Dispatchers.IO).launch {
            while (_connectionState.value == WebSocketConnectionState.CONNECTED) {
                delay(30000) // Verificar cada 30 segundos
                
                val currentTime = System.currentTimeMillis()
                val timeSinceLastHeartbeat = currentTime - lastHeartbeatTime
                val timeSinceLastMessage = currentTime - lastMessageTime
                
                if (timeSinceLastHeartbeat > 120000) { // 2 minutos sin heartbeat
                    logError("WEBSOCKET", "🚨 Conexión muerta detectada - sin heartbeat por ${timeSinceLastHeartbeat/1000}s")
                    _connectionState.value = WebSocketConnectionState.DISCONNECTED
                    logInfo("WEBSOCKET", "🔄 Forzando reconexión por conexión muerta...")
                    scheduleReconnect()
                    break
                } else if (timeSinceLastMessage > 300000) { // 5 minutos sin mensajes del servidor
                    logError("WEBSOCKET", "🚨 Sin mensajes del servidor por ${timeSinceLastMessage/1000}s - posible conexión muerta")
                    logError("WEBSOCKET", "🚨 El loop de mensajes puede estar atascado - forzando reconexión")
                    _connectionState.value = WebSocketConnectionState.DISCONNECTED
                    logInfo("WEBSOCKET", "🔄 Forzando reconexión por falta de mensajes...")
                    scheduleReconnect()
                    break
                } else if (timeSinceLastHeartbeat > 90000) { // 1.5 minutos sin heartbeat - advertencia
                    logError("WEBSOCKET", "⚠️ Conexión lenta - sin heartbeat por ${timeSinceLastHeartbeat/1000}s")
                } else {
                    logInfo("WEBSOCKET", "✅ Conexión activa - último heartbeat hace ${timeSinceLastHeartbeat/1000}s, último mensaje hace ${timeSinceLastMessage/1000}s")
                }
            }
            logInfo("WEBSOCKET", "🔍 Verificación de conexión detenida")
        }
    }
    
    /**
     * Establece callback para notificar cuando se recibe un mensaje
     */
    fun setOnMessageReceivedCallback(callback: () -> Unit) {
        onMessageReceivedCallback = callback
    }
    
    /**
     * Notifica que se recibió un mensaje
     */
    private fun notifyMessageReceived() {
        onMessageReceivedCallback?.invoke()
    }
    
    /**
     * Programa reconexión automática con throttling
     */
    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) {
            logInfo("WEBSOCKET", "🔄 Ya existe un trabajo de reconexión activo, ignorando")
            return
        }
        
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - lastReconnectTime < minTimeBetweenReconnects) {
            val remainingTime = (minTimeBetweenReconnects - (currentTime - lastReconnectTime)) / 1000
            logInfo("WEBSOCKET", "⏰ Esperando ${remainingTime}s antes de la próxima reconexión (mínimo entre reconexiones)")
            return
        }
        
        logInfo("WEBSOCKET", "🔄 Programando reconexión automática...")
        logInfo("WEBSOCKET", "Intentos actuales: $reconnectAttempts")
        
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            reconnectAttempts++
            
            // Usar backoff exponencial pero con límite máximo
            val delaySeconds = minOf(reconnectAttempts * 3, 15) // 3, 6, 9, 12, 15 segundos
            logInfo("WEBSOCKET", "⏳ Esperando ${delaySeconds}s antes del intento de reconexión #$reconnectAttempts")
            
            delay(delaySeconds * 1000L)
            
            currentSellerId?.let { sellerId ->
                logInfo("WEBSOCKET", "🔄 Intentando reconexión #$reconnectAttempts para sellerId: $sellerId")
                lastReconnectTime = System.currentTimeMillis()
                connect(sellerId)
            } ?: run {
                logError("WEBSOCKET", "❌ No hay sellerId disponible para reconexión")
                // Si no hay sellerId, resetear intentos y esperar
                reconnectAttempts = 0
            }
        }
    }
    
    /**
     * Desconecta el WebSocket
     */
    fun disconnect() {
        logInfo("WEBSOCKET", "🔌 Iniciando desconexión del WebSocket")
        logInfo("WEBSOCKET", "Estado actual: ${_connectionState.value}")
        logInfo("WEBSOCKET", "SellerId actual: $currentSellerId")
        
        // Cancelar trabajos de reconexión
        if (reconnectJob != null) {
            logInfo("WEBSOCKET", "🔄 Cancelando trabajo de reconexión")
            reconnectJob?.cancel()
            reconnectJob = null
        }
        
        // Cancelar heartbeat
        if (heartbeatJob != null) {
            logInfo("WEBSOCKET", "💓 Cancelando heartbeat")
            heartbeatJob?.cancel()
            heartbeatJob = null
        }
        
        // Cancelar verificación de conexión
        if (connectionCheckJob != null) {
            logInfo("WEBSOCKET", "🔍 Cancelando verificación de conexión")
            connectionCheckJob?.cancel()
            connectionCheckJob = null
        }
        
        // Cerrar sesión WebSocket si está abierta
        if (webSocketSession != null) {
            logInfo("WEBSOCKET", "🔌 Cerrando sesión WebSocket")
            try {
                runBlocking {
                    webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "Desconexión solicitada por el cliente"))
                }
            } catch (e: Exception) {
                logError("WEBSOCKET", "Error al cerrar sesión WebSocket: ${e.message}")
            }
            webSocketSession = null
        }
        
        _connectionState.value = WebSocketConnectionState.DISCONNECTED
        reconnectAttempts = 0
        currentSellerId = null
        
        logInfo("WEBSOCKET", "✅ WebSocket desconectado completamente")
        logInfo("WEBSOCKET", "Estado final: DISCONNECTED")
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