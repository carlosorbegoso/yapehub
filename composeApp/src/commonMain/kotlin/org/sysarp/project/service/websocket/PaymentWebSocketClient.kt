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
    
    // Configuración de reconexión
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private var currentSellerId: Long? = null
    
    /**
     * Conecta al WebSocket del vendedor
     */
    suspend fun connect(sellerId: Long) {
        if (_connectionState.value == WebSocketConnectionState.CONNECTED) {
            Logger.auth("WEBSOCKET", "Ya conectado")
            return
        }
        
        val token = authService.accessToken.value
        if (token.isNullOrBlank()) {
            Logger.auth("WEBSOCKET", "❌ Token no disponible")
            return
        }
        
        currentSellerId = sellerId
        _connectionState.value = WebSocketConnectionState.CONNECTING
        
        try {
            val url = "${Constants.WEBSOCKET_URL}/ws/payments/$sellerId?token=$token"
            Logger.auth("WEBSOCKET", "🔗 Conectando a: $url")
            Logger.auth("WEBSOCKET", "🔑 Token: ${token.take(20)}...")
            Logger.auth("WEBSOCKET", "👤 Seller ID: $sellerId")
            
            httpClient.webSocket(url) {
                webSocketSession = this
                
                // Escuchar mensajes entrantes
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            val message = frame.readText()
                            Logger.auth("WEBSOCKET", "📨 Mensaje recibido: $message")
                            processMessage(message)
                        }
                        is Frame.Close -> {
                            val reason = frame.readReason()
                            Logger.auth("WEBSOCKET", "🔌 Conexión cerrada: ${reason?.message}")
                            _connectionState.value = WebSocketConnectionState.DISCONNECTED
                            scheduleReconnect()
                        }
                        is Frame.Ping -> {
                            Logger.auth("WEBSOCKET", "🏓 Ping recibido")
                        }
                        is Frame.Pong -> {
                            Logger.auth("WEBSOCKET", "🏓 Pong recibido")
                        }
                        else -> {
                            Logger.auth("WEBSOCKET", "📋 Frame recibido: ${frame::class.simpleName}")
                        }
                    }
                }
            }
            
        } catch (e: Exception) {
            Logger.auth("WEBSOCKET", "❌ Error conectando: ${e.message}")
            Logger.auth("WEBSOCKET", "🔍 Tipo de error: ${e::class.simpleName}")
            Logger.auth("WEBSOCKET", "🔍 Stack trace: ${e.stackTraceToString()}")
            _connectionState.value = WebSocketConnectionState.DISCONNECTED
            
            // Solo reintentar si no es un error de autenticación
            val message = e.message ?: ""
            if (!message.contains("401") && !message.contains("403")) {
                scheduleReconnect()
            } else {
                Logger.auth("WEBSOCKET", "🚫 Error de autenticación, no reintentando")
            }
        }
    }
    
    /**
     * Procesa mensajes entrantes del WebSocket
     */
    private suspend fun processMessage(message: String) {
        try {
            Logger.auth("WEBSOCKET", "📨 Procesando mensaje: $message")
            
            // Manejar mensaje de conexión especial
            if (message.contains("\"type\":\"CONNECTED\"")) {
                Logger.auth("WEBSOCKET", "🎉 CONEXIÓN ESTABLECIDA")
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
                    Logger.auth("WEBSOCKET", "💰 NUEVO PAGO RECIBIDO: ${notificationData.paymentId} - S/ ${notificationData.amount}")
                    Logger.auth("WEBSOCKET", "👤 Cliente: ${notificationData.senderName}")
                    Logger.auth("WEBSOCKET", "🔢 Código Yape: ${notificationData.yapeCode}")
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
                    Logger.auth("WEBSOCKET", "✅ RESULTADO DE PAGO: ${resultData.paymentId} - ${resultData.status}")
                }
                
                else -> {
                    Logger.auth("WEBSOCKET", "⚠️ Tipo de mensaje desconocido: ${webSocketMessage.type}")
                }
            }
            
        } catch (e: Exception) {
            Logger.auth("WEBSOCKET", "❌ Error procesando mensaje: ${e.message}")
            Logger.auth("WEBSOCKET", "📄 Mensaje problemático: $message")
        }
    }
    
    /**
     * Inicia heartbeat para mantener conexión activa
     */
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (_connectionState.value == WebSocketConnectionState.CONNECTED) {
                delay(30000) // Heartbeat cada 30 segundos
                
                try {
                    Logger.auth("WEBSOCKET", "💓 Heartbeat WebSocket - Conexión activa")
                } catch (e: Exception) {
                    Logger.auth("WEBSOCKET", "❌ Error en heartbeat: ${e.message}")
                    _connectionState.value = WebSocketConnectionState.DISCONNECTED
                    scheduleReconnect()
                    break
                }
            }
        }
    }
    
    /**
     * Programa reconexión automática
     */
    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            reconnectAttempts++
            
            if (reconnectAttempts > maxReconnectAttempts) {
                Logger.auth("WEBSOCKET", "🚫 Máximo de intentos de reconexión alcanzado ($maxReconnectAttempts)")
                reconnectAttempts = 0
                return@launch
            }
            
            val delaySeconds = minOf(reconnectAttempts * 5, 30) // 5, 10, 15, 20, 30 segundos
            Logger.auth("WEBSOCKET", "🔄 Programando reconexión en $delaySeconds segundos (intento $reconnectAttempts/$maxReconnectAttempts)")
            
            delay(delaySeconds * 1000L)
            
            currentSellerId?.let { sellerId ->
                Logger.auth("WEBSOCKET", "🔄 Intentando reconexión automática...")
                connect(sellerId)
            }
        }
    }
    
    /**
     * Desconecta el WebSocket
     */
    fun disconnect() {
        Logger.auth("WEBSOCKET", "🔌 Desconectando WebSocket")
        
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
                Logger.auth("WEBSOCKET", "📤 Mensaje enviado: $message")
            } else {
                Logger.auth("WEBSOCKET", "❌ No se puede enviar mensaje: WebSocket no conectado")
            }
        } catch (e: Exception) {
            Logger.auth("WEBSOCKET", "❌ Error enviando mensaje: ${e.message}")
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