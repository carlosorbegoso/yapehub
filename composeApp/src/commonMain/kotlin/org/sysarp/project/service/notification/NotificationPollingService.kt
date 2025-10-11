package org.sysarp.project.service.notification

import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.http.PaymentApiClient
import org.sysarp.project.service.payment.PaymentService

class NotificationPollingService {
    
    private val authService = AuthService.getInstance()
    private val httpClient = HttpClient()
    private val paymentApiClient = PaymentApiClient(httpClient)
    private val paymentService = PaymentService(paymentApiClient)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    private var pollingJob: Job? = null
    private var isActive = false
    
    // Callback para notificaciones nuevas
    private var onNewNotification: ((List<SellerPendingPayment>) -> Unit)? = null
    
    fun setOnNewNotificationCallback(callback: (List<SellerPendingPayment>) -> Unit) {
        onNewNotification = callback
    }
    
    fun start() {
        if (isActive) {
            return
        }
        
        isActive = true
        pollingJob = coroutineScope.launch {
            while (isActive) {
                try {
                    delay(30000) // Polling cada 30 segundos
                    checkForNewNotifications()
                } catch (e: Exception) {
                    println("[POLLING_SERVICE] ❌ Error en polling: ${e.message}")
                    delay(60000) // Esperar más tiempo si hay error
                }
            }
        }
    }
    
    fun stop() {
        isActive = false
        pollingJob?.cancel()
        pollingJob = null
        println("[POLLING_SERVICE] 🛑 Polling detenido")
    }
    
    private suspend fun checkForNewNotifications() {
        try {
            val userProfile = authService.userProfile.value
            val accessToken = authService.accessToken.value
            
            if (accessToken.isNullOrBlank()) {
                println("[POLLING_SERVICE] ❌ No hay token disponible")
                return
            }
            
            // Verificar si es un seller o un admin
            val sellerId = userProfile?.sellerId
            val adminId = userProfile?.adminId
            
            when {
                sellerId != null -> {
                    checkSellerNotifications(sellerId.toInt(), accessToken)
                }
                else -> {
                    println("[POLLING_SERVICE] ❌ No hay sellerId ni adminId disponible")
                    return
                }
            }
            
        } catch (e: Exception) {
            println("[POLLING_SERVICE] ❌ Excepción en polling: ${e.message}")
        }
    }
    
    private suspend fun checkSellerNotifications(sellerId: Int, accessToken: String) {
        val response = paymentService.getPendingPayments(
            sellerId = sellerId,
            page = 0,
            limit = 50,
            startDate = null,
            endDate = null,
            token = accessToken
        )
        
        response.fold(
            onSuccess = { pendingPayments ->
                val payments = pendingPayments.data.payments
                println("[POLLING_SERVICE] 📨 Encontrados ${payments.size} pagos pendientes para seller $sellerId")
                
                if (payments.isNotEmpty()) {
                    showNewNotifications(payments)
                }
            },
            onFailure = { error ->
                println("[POLLING_SERVICE] ❌ Error en polling para seller $sellerId: ${error.message}")
            }
        )
    }

    private fun showNewNotifications(payments: List<SellerPendingPayment>) {
        try {
            onNewNotification?.invoke(payments)
            
        } catch (e: Exception) {
            println("[POLLING_SERVICE] ❌ Error mostrando notificaciones: ${e.message}")
        }
    }
}
