package org.sysarp.project.service.notification

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.websocket.PaymentWebSocketService

class HybridNotificationManager(
    private val authService: AuthService,
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
        println("[HYBRID_MANAGER] 🚀 Iniciando Hybrid Notification Manager")
        
        // Configurar callback para mensajes del WebSocket
        webSocketService.setOnMessageReceivedCallback {
            updateLastMessageTime()
        }
        
        // Configurar callback para notificaciones del polling
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
            while (true) {
                try {
                    delay(30000) // Verificar cada 30 segundos
                    
                    if (shouldUsePolling()) {
                        if (!isPollingActive) {
                            println("[HYBRID_MANAGER] 🔄 Activando polling - WebSocket inactivo")
                            isPollingActive = true
                            pollingService.start()
                        }
                    } else {
                        if (isPollingActive) {
                            println("[HYBRID_MANAGER] ✅ Desactivando polling - WebSocket activo")
                            isPollingActive = false
                            pollingService.stop()
                        }
                    }
                    
                } catch (e: Exception) {
                    println("[HYBRID_MANAGER] ❌ Error en polling fallback: ${e.message}")
                }
            }
        }
    }
    
    private fun startWebSocketMonitoring() {
        coroutineScope.launch {
            // Monitorear el estado del WebSocket
            webSocketService.connectionState.collect { state ->
                when (state) {
                    org.sysarp.project.service.websocket.WebSocketConnectionState.CONNECTED -> {
                        println("[HYBRID_MANAGER] ✅ WebSocket conectado")
                        lastMessageTime = System.currentTimeMillis()
                    }
                    org.sysarp.project.service.websocket.WebSocketConnectionState.DISCONNECTED -> {
                        println("[HYBRID_MANAGER] ❌ WebSocket desconectado")
                    }
                    org.sysarp.project.service.websocket.WebSocketConnectionState.CONNECTING -> {
                        println("[HYBRID_MANAGER] 🔄 WebSocket conectando...")
                    }
                    org.sysarp.project.service.websocket.WebSocketConnectionState.RECONNECTING -> {
                        println("[HYBRID_MANAGER] 🔄 WebSocket reconectando...")
                    }
                }
            }
        }
    }
    
    private fun shouldUsePolling(): Boolean {
        val timeSinceLastMessage = System.currentTimeMillis() - lastMessageTime
        val timeSinceLastPolling = System.currentTimeMillis() - lastPollingTime
        
        // Usar polling si:
        // 1. No hay mensajes del WebSocket en 2 minutos
        // 2. O no hemos hecho polling en 5 minutos
        return timeSinceLastMessage > 120000 || timeSinceLastPolling > 300000
    }
    
    
    // Método para actualizar el tiempo del último mensaje (llamado desde WebSocket)
    fun updateLastMessageTime() {
        lastMessageTime = System.currentTimeMillis()
        println("[HYBRID_MANAGER] 📨 Mensaje recibido via WebSocket - actualizando timestamp")
    }
    
    fun getStatus(): String {
        val timeSinceLastMessage = (System.currentTimeMillis() - lastMessageTime) / 1000
        val timeSinceLastPolling = (System.currentTimeMillis() - lastPollingTime) / 1000
        
        return "WebSocket: ${timeSinceLastMessage}s, Polling: ${timeSinceLastPolling}s, Activo: $isPollingActive"
    }
}
