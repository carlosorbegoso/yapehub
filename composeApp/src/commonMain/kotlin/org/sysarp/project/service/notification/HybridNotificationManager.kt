package org.sysarp.project.service.notification

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.service.websocket.WebSocketConnectionState

class HybridNotificationManager(
    private val webSocketService: PaymentWebSocketService
) {
    
    private val pollingService = NotificationPollingService()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    
    private var pollingJob: Job? = null
    private var lastMessageTime = 0L
    private var lastPollingTime = 0L
    private var isPollingActive = false
    
    // Callback para notificaciones nuevas
    private var onNewNotification: ((List<SellerPendingPayment>) -> Unit)? = null
    
    fun setOnNewNotificationCallback(callback: (List<SellerPendingPayment>) -> Unit) {
        onNewNotification = callback
    }
    
    fun start() {
        webSocketService.setOnMessageReceivedCallback {
            updateLastMessageTime()
        }

        pollingService.setOnNewNotificationCallback { payments ->
            onNewNotification?.invoke(payments)
        }
        
        // Iniciar WebSocket (ya está funcionando)
        webSocketService.startAutoConnect()
        
        // Iniciar polling como fallback
        startPollingFallback()
        
        // Monitorear actividad del WebSocket
        startWebSocketMonitoring()
    }
    
    fun stop() {
        println("[HYBRID_MANAGER] 🛑 Deteniendo Hybrid Notification Manager")
        
        pollingJob?.cancel()
        pollingJob = null
        isPollingActive = false
        
        pollingService.stop()
        webSocketService.stop()
    }
    
    private fun startPollingFallback() {
        pollingJob = coroutineScope.launch {
            try {
                while (currentCoroutineContext().isActive) {
                    delay(30000) // Verificar cada 30 segundos
                    
                    if (shouldUsePolling()) {
                        if (!isPollingActive) {
                            isPollingActive = true
                            pollingService.start()
                        }
                    } else {
                        if (isPollingActive) {
                            isPollingActive = false
                            pollingService.stop()
                        }
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                println("[HYBRID_MANAGER] 🛑 Polling fallback cancelado correctamente")
                throw e // Re-lanzar para manejo correcto de cancelación
            } catch (e: Exception) {
                println("[HYBRID_MANAGER] ❌ Error en polling fallback: ${e.message}")
            }
        }
    }
    
    private fun startWebSocketMonitoring() {
        coroutineScope.launch {
            // Monitorear el estado del WebSocket
            webSocketService.connectionState.collect { state ->
                when (state) {
                    WebSocketConnectionState.CONNECTED -> {
                        println("[HYBRID_MANAGER] ✅ WebSocket conectado")
                        lastMessageTime = getCurrentTimeMillis()
                    }
                    WebSocketConnectionState.DISCONNECTED -> {
                        println("[HYBRID_MANAGER] ❌ WebSocket desconectado")
                    }
                    WebSocketConnectionState.CONNECTING -> {
                        println("[HYBRID_MANAGER] 🔄 WebSocket conectando...")
                    }
                    WebSocketConnectionState.RECONNECTING -> {
                        println("[HYBRID_MANAGER] 🔄 WebSocket reconectando...")
                    }
                }
            }
        }
    }
    
    private fun shouldUsePolling(): Boolean {
        val timeSinceLastMessage = getCurrentTimeMillis() - lastMessageTime
        val timeSinceLastPolling = getCurrentTimeMillis() - lastPollingTime

        return timeSinceLastMessage > 120000 || timeSinceLastPolling > 300000
    }
    
    
    // Método para actualizar el tiempo del último mensaje (llamado desde WebSocket)
    fun updateLastMessageTime() {
        lastMessageTime = getCurrentTimeMillis()
    }
    
    fun getStatus(): String {
        val timeSinceLastMessage = (getCurrentTimeMillis() - lastMessageTime) / 1000
        val timeSinceLastPolling = (getCurrentTimeMillis() - lastPollingTime) / 1000
        
        return "WebSocket: ${timeSinceLastMessage}s, Polling: ${timeSinceLastPolling}s, Activo: $isPollingActive"
    }
}

/**
 * Función multiplataforma para obtener el tiempo actual en milisegundos
 */
internal expect fun getCurrentTimeMillis(): Long
