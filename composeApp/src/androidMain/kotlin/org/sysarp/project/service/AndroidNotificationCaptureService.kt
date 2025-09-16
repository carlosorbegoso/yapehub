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
    private var notificationService: NotificationService? = null
    private var safeNotificationService: SafeNotificationService? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationCapture", "Servicio de captura de notificaciones creado")
        
        // Inicializar servicios de manera segura
        try {
            safeNotificationService = SafeNotificationService(this)
            if (safeNotificationService?.initializeNotificationService() == true) {
                notificationService = NotificationService()
                Log.d("NotificationCapture", "Servicios inicializados correctamente")
            } else {
                Log.w("NotificationCapture", "Servicio de notificaciones no disponible")
            }
        } catch (e: Exception) {
            Log.e("NotificationCapture", "Error inicializando servicios", e)
        }
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        sbn?.let { notification ->
            Log.d("NotificationCapture", "Nueva notificación recibida: ${notification.packageName}")
            
            // Procesar notificación de manera segura
            try {
                val packageName = notification.packageName
                val extras = notification.notification.extras
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                
                // Usar el servicio seguro para procesar la notificación
                safeNotificationService?.processNotificationSafely(packageName, title, text)
                
            } catch (e: Exception) {
                Log.e("NotificationCapture", "Error procesando notificación: ${e.message}")
            }
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        Log.d("NotificationCapture", "Notificación removida: ${sbn?.packageName}")
    }
    
    private fun processYapeNotification(notification: StatusBarNotification) {
        try {
            Log.d("NotificationCapture", "🔔 Procesando notificación de Yape de manera segura")
            
            val extras = notification.notification.extras
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            
            Log.d("NotificationCapture", "📱 Package: ${notification.packageName}")
            Log.d("NotificationCapture", "📝 Title: $title")
            Log.d("NotificationCapture", "📄 Text: $text")
            
            // Solo procesar si el servicio está disponible
            notificationService?.let { service ->
                val fullText = if (bigText.isNullOrEmpty()) text else bigText
                
                // Crear request para la API de notificaciones
                val notificationRequest = YapeNotificationRequest(
                    adminId = 605, // TODO: Obtener del usuario autenticado
                    encryptedNotification = fullText,
                    deviceFingerprint = "a1b2c3d4e5f6789a", // TODO: Generar fingerprint único del dispositivo
                    timestamp = notification.postTime
                )
                
                Log.d("NotificationCapture", "✅ YapeNotificationRequest creada")
                
                // Enviar a la API usando coroutines de manera segura
                serviceScope.launch {
                    try {
                        Log.d("NotificationCapture", "🚀 Enviando a API...")
                        
                        val result = service.sendYapeNotification(
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
            } ?: Log.w("NotificationCapture", "Servicio de notificaciones no disponible")
            
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