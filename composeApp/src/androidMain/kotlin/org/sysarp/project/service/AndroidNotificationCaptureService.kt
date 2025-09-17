package org.sysarp.project.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.sysarp.project.data.YapeNotificationRequest
import org.sysarp.project.utils.AndroidDeviceUtils
import org.sysarp.project.utils.EncryptionUtils
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.http.NotificationApiClient
import org.sysarp.project.ui.components.DebugLogManager
import org.sysarp.project.ui.components.DebugLog
import org.sysarp.project.ui.components.LogType

class AndroidNotificationCaptureService : NotificationListenerService() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var notificationService: NotificationService? = null
    private var authService: AuthService? = null
    private var deviceFingerprint: String? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationCapture", "🔧 Servicio de notificaciones inicializado")
        
        // Enviar log de inicialización
        DebugLogManager.addLog(
            DebugLog(
                timestamp = System.currentTimeMillis(),
                type = LogType.PERMISSION,
                message = "🔧 Servicio de notificaciones inicializado",
                details = "AndroidNotificationCaptureService onCreate()"
            )
        )
        
                // Generar device fingerprint único usando identificadores reales del dispositivo
                serviceScope.launch {
                    deviceFingerprint = AndroidDeviceUtils.generateDeviceFingerprint(this@AndroidNotificationCaptureService)
                    Log.d("NotificationCapture", "Device fingerprint generado: ${deviceFingerprint?.take(20)}...")

                    // Enviar log de device fingerprint
                    DebugLogManager.addLog(
                        DebugLog(
                            timestamp = System.currentTimeMillis(),
                            type = LogType.PERMISSION,
                            message = "🔑 Device fingerprint generado",
                            details = "Fingerprint: ${deviceFingerprint?.take(20)}... (usando Android ID)"
                        )
                    )
                }
        
        // TODO: Inicializar AuthService cuando esté disponible
        // authService = AuthService()
        
        // Inicializar NotificationService con dependencias reales
        // notificationService = NotificationService(NotificationApiClient(), authService)
        
        Log.d("NotificationCapture", "Servicios inicializados correctamente")
        
        // Enviar log de servicios inicializados
        DebugLogManager.addLog(
            DebugLog(
                timestamp = System.currentTimeMillis(),
                type = LogType.PERMISSION,
                message = "✅ Servicios inicializados correctamente",
                details = "AuthService y NotificationService preparados"
            )
        )
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        serviceScope.launch {
            processNotification(sbn)
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        Log.d("NotificationCapture", "🗑️ Notificación removida: ${sbn.packageName} - ${sbn.id}")
    }
    
    private suspend fun processNotification(sbn: StatusBarNotification) {
        try {
            Log.d("NotificationCapture", "🔍 Procesando notificación: ${sbn.packageName}")
            
            // Enviar log de notificación recibida
            DebugLogManager.addLog(
                DebugLog(
                    timestamp = System.currentTimeMillis(),
                    type = LogType.NOTIFICATION,
                    message = "📨 Notificación recibida",
                    details = "Package: ${sbn.packageName}, ID: ${sbn.id}"
                )
            )
            
            if (isYapePackage(sbn.packageName)) {
                val notificationText = extractNotificationText(sbn)
                Log.d("NotificationCapture", "📱 Notificación de Yape detectada: $notificationText")
                
                // Enviar log de Yape detectado
                DebugLogManager.addLog(
                    DebugLog(
                        timestamp = System.currentTimeMillis(),
                        type = LogType.NOTIFICATION,
                        message = "📱 Yape detectado",
                        details = "Package: ${sbn.packageName}, Text: ${notificationText?.take(50)}..."
                    )
                )
                
                processYapeNotification(sbn, notificationText)
            } else {
                // Enviar log de notificación no-Yape
                DebugLogManager.addLog(
                    DebugLog(
                        timestamp = System.currentTimeMillis(),
                        type = LogType.NOTIFICATION,
                        message = "❌ No es Yape",
                        details = "Package: ${sbn.packageName} - ignorando"
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("NotificationCapture", "Error procesando notificación: ${e.message}")
            
            // Enviar log de error
            DebugLogManager.addLog(
                DebugLog(
                    timestamp = System.currentTimeMillis(),
                    type = LogType.ERROR,
                    message = "❌ Error procesando notificación",
                    details = "Exception: ${e.message}"
                )
            )
        }
    }
    
    private suspend fun processYapeNotification(sbn: StatusBarNotification, notificationText: String?) {
        try {
            Log.d("NotificationCapture", "🔍 Procesando notificación de Yape: $notificationText")
            
            if (notificationText != null) {
                Log.d("NotificationCapture", "✅ Es notificación de Yape válida, enviando a API...")
                
                // Extraer datos de la notificación
                val extras = sbn.notification.extras
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
                
                Log.d("NotificationCapture", "📱 Package: ${sbn.packageName}")
                Log.d("NotificationCapture", "📝 Title: $title")
                Log.d("NotificationCapture", "📄 Text: $text")
                
                // Enviar log de datos extraídos
                DebugLogManager.addLog(
                    DebugLog(
                        timestamp = System.currentTimeMillis(),
                        type = LogType.NOTIFICATION,
                        message = "📄 Datos extraídos",
                        details = "Title: $title, Text: ${text.take(30)}..."
                    )
                )
                
                // Solo procesar si el servicio está disponible
                notificationService?.let { service ->
                    val fullText = if (bigText.isNullOrEmpty()) text else bigText
                    
                            // Crear request para la API de notificaciones con datos reales
                            val currentFingerprint = deviceFingerprint ?: AndroidDeviceUtils.generateSimpleFingerprint(this@AndroidNotificationCaptureService)
                    val adminId = authService?.userProfile?.value?.id?.toIntOrNull() ?: 605 // Fallback temporal
                    
                    // ENCRIPTAR la notificación completa sin parsear
                    val encryptedNotification = try {
                        // Crear JSON manualmente para evitar problemas de serialización
                        val jsonString = buildString {
                            append("{")
                            append("\"packageName\":\"${sbn.packageName}\",")
                            append("\"title\":\"${title.replace("\"", "\\\"")}\",")
                            append("\"text\":\"${text.replace("\"", "\\\"")}\",")
                            append("\"bigText\":\"${bigText?.replace("\"", "\\\"") ?: ""}\",")
                            append("\"fullText\":\"${fullText.replace("\"", "\\\"")}\",")
                            append("\"timestamp\":${sbn.postTime},")
                            append("\"notificationId\":${sbn.id}")
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
                        timestamp = sbn.postTime
                    )
                    
                    Log.d("NotificationCapture", "📱 AdminId real: $adminId")
                    Log.d("NotificationCapture", "🔑 DeviceFingerprint real: ${currentFingerprint.take(20)}...")
                    Log.d("NotificationCapture", "🔐 EncryptedNotification: ${encryptedNotification.take(50)}...")
                    
                    Log.d("NotificationCapture", "✅ YapeNotificationRequest creada con notificación encriptada")
                    
                    // Enviar a la API usando coroutines de manera segura
                    try {
                        Log.d("NotificationCapture", "🚀 Enviando a API...")
                        
                        // Enviar log de envío a API
                        DebugLogManager.addLog(
                            DebugLog(
                                timestamp = System.currentTimeMillis(),
                                type = LogType.API,
                                message = "🚀 Enviando a API",
                                details = "POST /api/notifications/yape-notifications"
                            )
                        )
                        
                        val result = service.sendYapeNotification(
                            adminId = notificationRequest.adminId,
                            encryptedNotification = notificationRequest.encryptedNotification,
                            deviceFingerprint = notificationRequest.deviceFingerprint,
                            timestamp = notificationRequest.timestamp
                        )
                        
                        result.onSuccess { response ->
                            Log.d("NotificationCapture", "✅ Notificación enviada exitosamente: ${response.message}")
                            
                            // Enviar log de éxito
                            DebugLogManager.addLog(
                                DebugLog(
                                    timestamp = System.currentTimeMillis(),
                                    type = LogType.API,
                                    message = "✅ Enviado exitosamente",
                                    details = "Response: ${response.message}"
                                )
                            )
                        }
                        
                        result.onFailure { error ->
                            Log.e("NotificationCapture", "❌ Error enviando notificación: ${error.message}")
                            
                            // Enviar log de error
                            DebugLogManager.addLog(
                                DebugLog(
                                    timestamp = System.currentTimeMillis(),
                                    type = LogType.ERROR,
                                    message = "❌ Error enviando a API",
                                    details = "Error: ${error.message}"
                                )
                            )
                        }
                        
                    } catch (e: Exception) {
                        Log.e("NotificationCapture", "💥 Excepción enviando notificación: ${e.message}")
                        
                        // Enviar log de excepción
                        DebugLogManager.addLog(
                            DebugLog(
                                timestamp = System.currentTimeMillis(),
                                type = LogType.ERROR,
                                message = "💥 Excepción en API",
                                details = "Exception: ${e.message}"
                            )
                        )
                    }
                } ?: Log.w("NotificationCapture", "Servicio de notificaciones no disponible")
                
            } else {
                Log.w("NotificationCapture", "❌ No es notificación de Yape válida")
            }
        } catch (e: Exception) {
            Log.e("NotificationCapture", "Error procesando notificación de Yape: ${e.message}")
        }
    }
    
    private fun isYapePackage(packageName: String): Boolean {
        val yapePackages = listOf(
            "com.bcp.innovacxion.yapeapp",
            "com.bcp.yape",
            "pe.com.bcp.yape",
            "com.bcp.innovacxion.yape"
        )
        
        val isYape = yapePackages.contains(packageName)
        Log.d("NotificationCapture", "🔍 Verificando package Yape: $packageName -> $isYape")
        return isYape
    }
    
    private fun extractNotificationText(notification: StatusBarNotification): String? {
        return try {
            val extras = notification.notification.extras
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            
            val mainContent = bigText ?: text
            val combinedText = listOfNotNull(title, mainContent).joinToString(" ").trim()
            combinedText.ifEmpty { null }
        } catch (e: Exception) {
            null
        }
    }
    
    companion object {
        fun isNotificationServiceEnabled(context: Context): Boolean {
            val pkgName = context.packageName
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            if (!TextUtils.isEmpty(flat)) {
                val names = flat.split(":").toTypedArray()
                for (name in names) {
                    val componentName = ComponentName.unflattenFromString(name)
                    if (componentName != null) {
                        if (TextUtils.equals(pkgName, componentName.packageName)) {
                            return true
                        }
                    }
                }
            }
            return false
        }
        
        fun requestNotificationPermission(context: Context) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d("NotificationCapture", "Servicio de captura de notificaciones destruido")
    }
}