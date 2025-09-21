package org.sysarp.project.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.sysarp.project.data.ServiceStatus
import org.sysarp.project.data.YapeNotificationRequest
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.utils.AndroidDeviceUtils
import org.sysarp.project.utils.EncryptionUtils

class AndroidNotificationCaptureService : NotificationListenerService() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var notificationService: NotificationService? = null
    private var authService: AuthService? = null
    private var deviceFingerprint: String? = null
    private var audioService: AudioService? = null
    
    // Queue para notificaciones pendientes
    private val pendingNotifications = mutableListOf<YapeNotificationRequest>()
    private val maxRetries = 2 // Reducido de 3 a 2 para evitar duplicados
    private val retryDelayMs = 10000L // Aumentado de 5 a 10 segundos para dar más tiempo a la API
    
    // Sistema de deduplicación para evitar notificaciones duplicadas
    private val sentNotifications = mutableSetOf<String>() // Hash de notificaciones ya enviadas
    private val maxSentNotifications = 100 // Límite para evitar memory leak
    
    override fun onCreate() {
        super.onCreate()
        
        
                // Generar device fingerprint único usando identificadores reales del dispositivo
                serviceScope.launch {
                    deviceFingerprint = AndroidDeviceUtils.generateDeviceFingerprint(this@AndroidNotificationCaptureService)

                }
        
        // Inicializar AuthService con contexto disponible
        try {
            authService = org.sysarp.project.service.auth.AuthService.getInstance()
            // Inicializar NotificationService con dependencias reales
            val notificationApiClient = org.sysarp.project.service.http.NotificationApiClient()
            notificationService = org.sysarp.project.service.NotificationService(notificationApiClient, authService!!)
            
            // Inicializar AudioService
            audioService = AudioService()
            audioService?.initialize(this@AndroidNotificationCaptureService)
            
        } catch (e: Exception) {
            // Error handling removed for production
        }

        // Services initialized
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        serviceScope.launch {
            processNotification(sbn)
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
    }
    
    /**
     * Procesa notificaciones de Yape
     * Solo envía notificaciones de aplicaciones de Yape para evitar errores 400 del servidor
     */
    private suspend fun processNotification(sbn: StatusBarNotification) {
        try {
            
            
            if (isYapePackage(sbn.packageName)) {
                val notificationText = extractNotificationText(sbn)
                
                
                processYapeNotification(sbn, notificationText)
            } else {
            }
        } catch (e: Exception) {
            
        }
    }
    
    private suspend fun processYapeNotification(sbn: StatusBarNotification, notificationText: String?) {
        try {
            if (!isYapePackage(sbn.packageName)) {
                return
            }
            
            
            if (notificationText != null) {
                
                // Extraer datos de la notificación
                val extras = sbn.notification.extras
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

                
                
                notificationService?.let { _ ->
                    val fullText = if (bigText.isNullOrEmpty()) text else bigText
                    
                            // Crear request para la API de notificaciones con datos reales
                            val currentFingerprint = deviceFingerprint ?: AndroidDeviceUtils.generateSimpleFingerprint(this@AndroidNotificationCaptureService)
                    val adminId = authService?.userProfile?.value?.id?.toIntOrNull() ?: 605
                    
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

                        
                        encrypted
                    } catch (e: Exception) {
                        EncryptionUtils.encryptWithKey(fullText, currentFingerprint)
                    }
                    
                    val notificationRequest = YapeNotificationRequest(
                        adminId = adminId,
                        encryptedNotification = encryptedNotification,
                        deviceFingerprint = currentFingerprint,
                        timestamp = sbn.postTime,
                        deduplicationHash = generateNotificationHash(
                            YapeNotificationRequest(
                                adminId = adminId,
                                encryptedNotification = encryptedNotification,
                                deviceFingerprint = currentFingerprint,
                                timestamp = sbn.postTime,
                                deduplicationHash = ""
                            )
                        )
                    )

                    
                    // Reproducir sonido Yape realista (melodía ascendente)
                    audioService?.playCustomSound(NotificationSoundType.YAPE_REALISTIC)
                    
                    // Enviar a la API con retry automático
                    sendNotificationWithRetry(notificationRequest, 0)
                }
                
            } else {
            }
        } catch (e: Exception) {
        }
    }
    
    /**
     * Verificar si el package es de Yape
     */
    private fun isYapePackage(packageName: String): Boolean {
        val yapePackages = listOf(
            "com.bcp.innovacxion.yapeapp",
            "com.bcp.yape",
            "pe.com.bcp.yape",
            "com.bcp.innovacxion.yape"
        )
        
        val isYape = yapePackages.contains(packageName)
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
    
    /**
     * Genera un hash único para la notificación para deduplicación
     */
    private fun generateNotificationHash(notificationRequest: YapeNotificationRequest): String {
        return "${notificationRequest.adminId}_${notificationRequest.timestamp}_${notificationRequest.encryptedNotification.take(20)}"
    }
    
    /**
     * Verifica si la notificación ya fue enviada (deduplicación)
     */
    private fun isNotificationAlreadySent(notificationRequest: YapeNotificationRequest): Boolean {
        val hash = generateNotificationHash(notificationRequest)
        return sentNotifications.contains(hash)
    }
    
    /**
     * Marca la notificación como enviada
     */
    private fun markNotificationAsSent(notificationRequest: YapeNotificationRequest) {
        val hash = generateNotificationHash(notificationRequest)
        sentNotifications.add(hash)
        
        // Limpiar notificaciones antiguas para evitar memory leak
        if (sentNotifications.size > maxSentNotifications) {
            val toRemove = sentNotifications.take(sentNotifications.size - maxSentNotifications)
            sentNotifications.removeAll(toRemove)
        }
    }
    
    /**
     * Envía notificación con retry automático y deduplicación
     */
    private suspend fun sendNotificationWithRetry(notificationRequest: YapeNotificationRequest, attempt: Int) {
        try {
            // DEDUPLICACIÓN: Verificar si la notificación ya fue enviada
            if (isNotificationAlreadySent(notificationRequest)) {
                return
            }
            
            if (notificationRequest.adminId == 605) {
            }
            
            
            
            val result = notificationService?.sendYapeNotification(
                adminId = notificationRequest.adminId,
                encryptedNotification = notificationRequest.encryptedNotification,
                deviceFingerprint = notificationRequest.deviceFingerprint,
                timestamp = notificationRequest.timestamp
            )
            
            result?.onSuccess { _ ->
                
                // Marcar como enviada para deduplicación
                markNotificationAsSent(notificationRequest)
                
                
                // Remover de queue si estaba pendiente
                pendingNotifications.remove(notificationRequest)
                
            }?.onFailure { _ ->
                
                // Si no es el último intento, agregar a queue y reintentar
                if (attempt < maxRetries - 1) {
                    
                    // Agregar a queue si no está ya
                    if (!pendingNotifications.contains(notificationRequest)) {
                        pendingNotifications.add(notificationRequest)
                    }
                    
                    // Reintentar después del delay
                    kotlinx.coroutines.delay(retryDelayMs)
                    sendNotificationWithRetry(notificationRequest, attempt + 1)
                } else {
                    
                    if (!pendingNotifications.contains(notificationRequest)) {
                        pendingNotifications.add(notificationRequest)
                    }
                    
                }
            }
            
        } catch (e: Exception) {
            
            // Si no es el último intento, reintentar
            if (attempt < maxRetries - 1) {
                kotlinx.coroutines.delay(retryDelayMs)
                sendNotificationWithRetry(notificationRequest, attempt + 1)
            } else {
                // Agregar a queue para procesamiento manual
                if (!pendingNotifications.contains(notificationRequest)) {
                    pendingNotifications.add(notificationRequest)
                }
                
            }
        }
    }
    
    /**
     * Procesa notificaciones pendientes en la queue
     */
    private suspend fun processPendingNotifications() {
        if (pendingNotifications.isNotEmpty()) {
            
            val notificationsToProcess = pendingNotifications.toList()
            pendingNotifications.clear()
            
            for (notification in notificationsToProcess) {
                sendNotificationWithRetry(notification, 0)
                kotlinx.coroutines.delay(1000) // Delay entre notificaciones
            }
        }
    }
    
    /**
     * Obtiene el estado del servicio
     */
    fun getServiceStatus(): ServiceStatus {
        return ServiceStatus(
            isRunning = true,
            isCapturing = true,
            pendingNotificationsCount = pendingNotifications.size,
            deviceFingerprint = deviceFingerprint?.take(20) ?: "No disponible",
            lastError = null
        )
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
        
        /**
         * Limpia notificaciones antiguas del sistema para evitar acumulación
         * Solo funciona si el servicio de notificaciones está habilitado
         */
        fun cleanupSystemNotifications(context: Context) {
            try {
                if (isNotificationServiceEnabled(context)) {
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    
                    // Limpiar notificaciones activas de la app
                    notificationManager.cancelAll()
                    
                    
                } else {
                }
            } catch (e: Exception) {
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}