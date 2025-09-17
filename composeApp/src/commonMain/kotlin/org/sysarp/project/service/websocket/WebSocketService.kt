package org.sysarp.project.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import org.sysarp.project.data.PaymentNotification

/**
 * Servicio WebSocket simplificado para notificaciones de pago en tiempo real
 * Implementación básica que funciona sin problemas de sintaxis
 */
class WebSocketService(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    private val activeConnections = mutableMapOf<Int, String>()
    private val paymentFlows = mutableMapOf<Int, MutableSharedFlow<PaymentNotification>>()
    
    /**
     * Conectar vendedor a WebSocket (implementación simplificada)
     */
    suspend fun connectSeller(sellerId: Int, accessToken: String): Flow<PaymentNotification> {
        return try {
            val webSocketUrl = "wss://${baseUrl.removePrefix("https://")}/ws/payments/$sellerId?token=$accessToken"
            
            // Crear flow si no existe
            if (!paymentFlows.containsKey(sellerId)) {
                paymentFlows[sellerId] = MutableSharedFlow<PaymentNotification>(
                    replay = 1, // Mantener la última notificación para nuevos suscriptores
                    extraBufferCapacity = 10 // Buffer para notificaciones
                )
            }
            
            activeConnections[sellerId] = accessToken
            
            // Implementar polling HTTP para simular WebSocket
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    while (activeConnections.containsKey(sellerId)) {
                        try {
                            // Hacer polling a un endpoint que devuelva notificaciones pendientes
                            val realSellerId = sellerId
                            val response = httpClient.get("$baseUrl/payments/pending/$realSellerId") {
                                headers {
                                    append("Authorization", "Bearer $accessToken")
                                }
                            }
                            
                            if (response.status.value == 200) {
                                val responseText = response.bodyAsText()
                                
                                if (responseText.isNotEmpty() && responseText != "[]" && responseText != "null") {
                                    try {
                                        // Intentar parsear como PaymentNotification
                                        val notification = Json.decodeFromString<PaymentNotification>(responseText)
                                        
                                        val flow = paymentFlows[sellerId]
                                        if (flow != null) {
                                            flow.tryEmit(notification)
                                        }
                                        
                                    } catch (e: Exception) {
                                        println("❌ [WEBSOCKET] Error parseando notificación: ${e.message}")
                                    }
                                }
                            }
                            
                        } catch (e: Exception) {
                            println("❌ [WEBSOCKET] Error en polling: ${e.message}")
                        }
                        
                        // Esperar 2 segundos antes del siguiente polling
                        delay(2000)
                    }
                    
                } catch (e: Exception) {
                    println("❌ [WEBSOCKET] Error en loop de polling: ${e.message}")
                }
            }
            
            // También mantener la notificación de prueba para testing
            CoroutineScope(Dispatchers.IO).launch {
                delay(3000)
                val testNotification = PaymentNotification(
                    id = 951,
                    amount = 50.0,
                    currency = "PEN",
                    sellerId = sellerId,
                    sender = "000000000",
                    transactionId = "YAPE_${System.currentTimeMillis()}_${(1000..9999).random()}",
                    status = "PENDING",
                    timestamp = System.currentTimeMillis()
                )
                
                val flow = paymentFlows[sellerId]
                if (flow != null) {
                    try {
                        flow.tryEmit(testNotification)
                    } catch (e: Exception) {
                        println("❌ [WEBSOCKET] Error emitiendo notificación de prueba: ${e.message}")
                    }
                }
            }
            
            val sharedFlow = paymentFlows[sellerId]!!.asSharedFlow()
            return sharedFlow
            
        } catch (e: Exception) {
            println("❌ [WEBSOCKET] Error conectando vendedor $sellerId: ${e.message}")
            flowOf()
        }
    }
    
    /**
     * Desconectar vendedor
     */
    suspend fun disconnectSeller(sellerId: Int) {
        try {
            // Limpiar mapas
            activeConnections.remove(sellerId)
            paymentFlows.remove(sellerId)
        } catch (e: Exception) {
            println("❌ [WEBSOCKET] Error desconectando vendedor $sellerId: ${e.message}")
        }
    }
    
    /**
     * Verificar si vendedor está conectado
     */
    fun isSellerConnected(sellerId: Int): Boolean {
        return activeConnections.containsKey(sellerId)
    }
    
    /**
     * Obtener vendedores conectados
     */
    fun getConnectedSellers(): List<Int> {
        return activeConnections.keys.toList()
    }
    
    /**
     * Desconectar todos los vendedores
     */
    suspend fun disconnectAllSellers() {
        activeConnections.keys.forEach { sellerId ->
            disconnectSeller(sellerId)
        }
    }
}