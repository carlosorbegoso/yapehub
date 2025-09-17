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
import org.sysarp.project.utils.DeviceUtils
import org.sysarp.project.utils.EncryptionUtils
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.http.NotificationApiClient

class AndroidNotificationCaptureService : NotificationListenerService() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var notificationService: NotificationService? = null
    private var safeNotificationService: SafeNotificationService? = null
    private var authService: AuthService? = null
    private var deviceFingerprint: String? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationCapture", "Servicio de captura de notificaciones creado")
        
        // Inicializar servicios de manera segura
        try {
            safeNotificationService = SafeNotificationService(this)
            if (safeNotificationService?.initializeNotificationService() == true) {
                // Generar device fingerprint único
                serviceScope.launch {
                    deviceFingerprint = DeviceUtils.generateDeviceFingerprint()
                    Log.d("NotificationCapture", "Device fingerprint generado: ${deviceFingerprint?.take(20)}...")
                }
                
                // TODO: Inicializar AuthService cuando esté disponible
                // authService = AuthService()
                
                // Inicializar NotificationService con dependencias reales
                // notificationService = NotificationService(NotificationApiClient(), authService)
                
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
            
            // SOLO procesar notificaciones de Yape
            if (isYapeNotification(notification.packageName)) {
                Log.d("NotificationCapture", "✅ Notificación de Yape detectada, procesando...")
                processYapeNotification(notification)
            } else {
                Log.d("NotificationCapture", "⏭️ Notificación ignorada (no es de Yape): ${notification.packageName}")
            }
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        Log.d("NotificationCapture", "Notificación removida: ${sbn?.packageName}")
    }
    
    private fun processYapeNotification(notification: StatusBarNotification) {
        try {
            // DOBLE VERIFICACIÓN: asegurar que es Yape
            if (!isYapeNotification(notification.packageName)) {
                Log.w("NotificationCapture", "⚠️ Intento de procesar notificación no-Yape: ${notification.packageName}")
                return
            }
            
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
                
                Log.d("NotificationCapture", "📄 Texto original de la notificación:")
                Log.d("NotificationCapture", "   Title: $title")
                Log.d("NotificationCapture", "   Text: $text")
                Log.d("NotificationCapture", "   BigText: $bigText")
                Log.d("NotificationCapture", "   FullText: $fullText")
                
                // Crear request para la API de notificaciones con datos reales
                val currentFingerprint = deviceFingerprint ?: DeviceUtils.generateSimpleFingerprint()
                val adminId = authService?.userProfile?.value?.id?.toIntOrNull() ?: 605 // Fallback temporal
                
                // ENCRIPTAR la notificación completa sin parsear
                val encryptedNotification = try {
                    // Crear JSON manualmente para evitar problemas de serialización
                    val jsonString = buildString {
                        append("{")
                        append("\"packageName\":\"${notification.packageName}\",")
                        append("\"title\":\"${title.replace("\"", "\\\"")}\",")
                        append("\"text\":\"${text.replace("\"", "\\\"")}\",")
                        append("\"bigText\":\"${bigText?.replace("\"", "\\\"") ?: ""}\",")
                        append("\"fullText\":\"${fullText.replace("\"", "\\\"")}\",")
                        append("\"timestamp\":${notification.postTime},")
                        append("\"notificationId\":${notification.id}")
                        append("}")
                    }
                    
                    // Encriptar usando XOR con deviceFingerprint como clave
                    val encrypted = EncryptionUtils.encryptWithKey(jsonString, currentFingerprint)
                    
                    Log.d("NotificationCapture", "🔐 Notificación encriptada exitosamente")
                    Log.d("NotificationCapture", "🔐 Tamaño original: ${jsonString.length} chars")
                    Log.d("NotificationCapture", "🔐 Tamaño encriptado: ${encrypted.length} chars")
                    
                    encrypted
                } catch (e: Exception) {
                    Log.e("NotificationCapture", "❌ Error encriptando notificación: ${e.message}")
                    // Fallback: encriptar solo el texto
                    EncryptionUtils.encryptWithKey(fullText, currentFingerprint)
                }
                
                val notificationRequest = YapeNotificationRequest(
                    adminId = adminId,
                    encryptedNotification = encryptedNotification,
                    deviceFingerprint = currentFingerprint,
                    timestamp = notification.postTime
                )
                
                Log.d("NotificationCapture", "📱 AdminId real: $adminId")
                Log.d("NotificationCapture", "🔑 DeviceFingerprint real: ${currentFingerprint.take(20)}...")
                Log.d("NotificationCapture", "🔐 EncryptedNotification: ${encryptedNotification.take(50)}...")
                
                Log.d("NotificationCapture", "✅ YapeNotificationRequest creada con notificación encriptada")
                
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
     * Verifica si una notificación es de Yape basándose SOLO en el package name
     */
    private fun isYapeNotification(packageName: String): Boolean {
        // Lista de package names válidos de Yape
        val validYapePackages = listOf(
            "com.yape.android",
            "com.yape",
            "pe.com.yape",
            "com.bcp.yape",
            "yape.android"
        )
        
        // Verificar si el package name coincide con algún Yape válido
        val isYapePackage = validYapePackages.any { validPackage ->
            packageName.equals(validPackage, ignoreCase = true) ||
            packageName.contains(validPackage, ignoreCase = true)
        }
        
        Log.d("NotificationCapture", "🔍 Verificando package: $packageName")
        Log.d("NotificationCapture", "🔍 Es Yape: $isYapePackage")
        
        return isYapePackage
    }
    
    /**
     * Verifica si una notificación es de Yape (método alternativo con contenido)
     * @deprecated Usar isYapeNotification(packageName) en su lugar
     */
    @Deprecated("Usar isYapeNotification(packageName) para mejor rendimiento")
    private fun isYapeNotification(packageName: String, title: String, text: String): Boolean {
        // Verificar package name primero
        if (!isYapeNotification(packageName)) {
            return false
        }
        
        // Verificar contenido típico de Yape (opcional)
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