package org.sysarp.project.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.sysarp.project.data.YapeNotification
import org.sysarp.project.data.YapeNotificationRequest

class AndroidNotificationCaptureService : NotificationListenerService() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var notificationService: NotificationService
    
    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationCapture", "Servicio de captura de notificaciones creado")
        
        // Inicializar servicios
        try {
            notificationService = NotificationService()
            Log.d("NotificationCapture", "NotificationService inicializado correctamente")
        } catch (e: Exception) {
            Log.e("NotificationCapture", "Error inicializando NotificationService", e)
        }
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        sbn?.let { notification ->
            Log.d("NotificationCapture", "Nueva notificación recibida: ${notification.packageName}")
            
            // Aquí se puede procesar la notificación de Yape
            val packageName = notification.packageName
            if (packageName.contains("yape", ignoreCase = true)) {
                Log.d("NotificationCapture", "Notificación de Yape detectada")
                // Procesar notificación de Yape
                processYapeNotification(notification)
            }
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        Log.d("NotificationCapture", "Notificación removida: ${sbn?.packageName}")
    }
    
    private fun processYapeNotification(notification: StatusBarNotification) {
        try {
            val extras = notification.notification.extras
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            
            Log.d("NotificationCapture", "🔔 Notificación de Yape detectada")
            Log.d("NotificationCapture", "📱 Package: ${notification.packageName}")
            Log.d("NotificationCapture", "📝 Title: $title")
            Log.d("NotificationCapture", "📄 Text: $text")
            Log.d("NotificationCapture", "📄 BigText: $bigText")
            
            // Usar el parser existente para procesar la notificación
            val fullText = if (bigText.isNullOrEmpty()) text else bigText
            val yapeTransaction = YapeNotificationParser.parseYapeNotification(fullText)
            
            if (yapeTransaction == null) {
                Log.d("NotificationCapture", "❌ Parser no pudo procesar la notificación")
                return
            }
            
            Log.d("NotificationCapture", "✅ Transacción parseada: ${yapeTransaction.amount} PEN de ${yapeTransaction.senderName}")
            
            // Crear request para la API de notificaciones
            val notificationRequest = YapeNotificationRequest(
                adminId = 605, // TODO: Obtener del usuario autenticado
                encryptedNotification = fullText, // Texto original de la notificación
                deviceFingerprint = "a1b2c3d4e5f6789a", // TODO: Generar fingerprint único del dispositivo
                timestamp = notification.postTime
            )
            
            Log.d("NotificationCapture", "✅ YapeNotificationRequest creada")
            Log.d("NotificationCapture", "📤 Datos a enviar:")
            Log.d("NotificationCapture", "   - Admin ID: ${notificationRequest.adminId}")
            Log.d("NotificationCapture", "   - Encrypted Notification: ${notificationRequest.encryptedNotification}")
            Log.d("NotificationCapture", "   - Device Fingerprint: ${notificationRequest.deviceFingerprint}")
            Log.d("NotificationCapture", "   - Timestamp: ${notificationRequest.timestamp}")
            
            // Enviar a la API usando coroutines
            serviceScope.launch {
                try {
                    Log.d("NotificationCapture", "🚀 Enviando a API...")
                    
                    val result = notificationService.sendYapeNotification(
                        adminId = notificationRequest.adminId,
                        encryptedNotification = notificationRequest.encryptedNotification,
                        deviceFingerprint = notificationRequest.deviceFingerprint,
                        timestamp = notificationRequest.timestamp
                    )
                    
                    result.onSuccess { response ->
                        Log.d("NotificationCapture", "✅ Notificación enviada exitosamente: ${response.message}")
                    }
                    
                    result.onFailure { error ->
                        Log.e("NotificationCapture", "❌ Error enviando notificación: ${error.message}")
                    }
                    
                } catch (e: Exception) {
                    Log.e("NotificationCapture", "💥 Excepción enviando notificación: ${e.message}")
                }
            }
            
        } catch (e: Exception) {
            Log.e("NotificationCapture", "💥 Error procesando notificación de Yape: ${e.message}")
        }
    }
    
    /**
     * Verifica si una notificación es de Yape
     */
    private fun isYapeNotification(packageName: String, title: String, text: String): Boolean {
        // Verificar package name
        if (!packageName.contains("yape", ignoreCase = true)) {
            return false
        }
        
        // Verificar contenido típico de Yape
        val yapeKeywords = listOf(
            "yape", "pago", "recibiste", "envió", "soles", "s/", "código"
        )
        
        val fullText = "$title $text".lowercase()
        return yapeKeywords.any { keyword -> fullText.contains(keyword) }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d("NotificationCapture", "Servicio de captura de notificaciones destruido")
    }
}