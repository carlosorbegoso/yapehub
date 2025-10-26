package org.sysarp.project.service

import android.annotation.SuppressLint
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
import org.sysarp.project.data.YapeNotificationRequest
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.utils.AndroidDeviceUtils
import org.sysarp.project.utils.EncryptionUtils
import timber.log.Timber

class AndroidNotificationCaptureService : NotificationListenerService() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var notificationService: NotificationService? = null
    private var authService: AuthService? = null
    private var deviceFingerprint: String? = null
    private var audioService: AudioService? = null
    
    // Service state management
    @Volatile
    private var isServiceBound = false
    @Volatile
    private var isServiceDestroyed = false
    
    // Queue para notificaciones pendientes
    private val pendingNotifications = mutableListOf<YapeNotificationRequest>()
    private val maxRetries = 2 // Reducido de 3 a 2 para evitar duplicados
    private val retryDelayMs = 10000L // Aumentado de 5 a 10 segundos para dar más tiempo a la API

    private val sentNotifications = mutableSetOf<String>() // Hash de notificaciones ya enviadas
    private val maxSentNotifications = 100 // Límite para evitar memory leak
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            isServiceBound = true
            isServiceDestroyed = false
            
            // Generar device fingerprint único usando identificadores reales del dispositivo
            serviceScope.launch {
                try {
                    deviceFingerprint = AndroidDeviceUtils.generateDeviceFingerprint(this@AndroidNotificationCaptureService)
                } catch (e: Exception) {
                    // Fallback to simple fingerprint if generation fails
                    deviceFingerprint = AndroidDeviceUtils.generateSimpleFingerprint(this@AndroidNotificationCaptureService)
                }
            }
            
            // Inicializar AuthService con contexto disponible
            authService = AuthService.getInstance()
            // Inicializar NotificationService con dependencias reales
            val notificationApiClient = org.sysarp.project.service.http.NotificationApiClient()
            notificationService = NotificationService(notificationApiClient, authService!!)
            
            // Inicializar AudioService
            audioService = AudioService()
            audioService?.initialize(this@AndroidNotificationCaptureService)
            
        } catch (e: Exception) {
            // Log error but don't crash the service
            Timber.tag("AndroidNotificationCapt").e(e, "Error in onCreate: ${e.message}")
        }
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        // Only process notifications if service is properly bound and not destroyed
        if (isServiceBound && !isServiceDestroyed) {
            serviceScope.launch {
                try {
                    processNotification(sbn)
                } catch (e: Exception) {
                    Timber.tag("AndroidNotificationCapt")
                        .e(e, "Error processing notification: ${e.message}")
                }
            }
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        
        // Only process removals if service is properly bound and not destroyed
        if (isServiceBound && !isServiceDestroyed) {
            // Handle notification removal if needed
        }
    }
    

    @SuppressLint("LogNotTimber")
    private suspend fun processNotification(sbn: StatusBarNotification) {
        try {
            if (!isServiceValid()) {
                return
            }
            
            if (isYapePackage(sbn.packageName)) {
                val notificationText = extractNotificationText(sbn)
                processYapeNotification(sbn, notificationText)
            }
        } catch (e: Exception) {
            Timber.tag("AndroidNotificationCapt")
                .e(e, "Error processing notification: ${e.message}")
        }
    }
    
    private suspend fun processYapeNotification(sbn: StatusBarNotification, notificationText: String?) {
        try {
            if (!isServiceValid()) {
                Timber.tag("AndroidNotificationCapt")
                    .w("Service not valid, skipping Yape notification processing")
                return
            }
            
            if (!isYapePackage(sbn.packageName)) {
                return
            }
            
            if (notificationText != null) {

                val extras = sbn.notification.extras
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

                
                
                notificationService?.let { _ ->
                    val fullText = if (bigText.isNullOrEmpty()) text else bigText

                            val currentFingerprint = deviceFingerprint ?: AndroidDeviceUtils.generateSimpleFingerprint(this@AndroidNotificationCaptureService)
                    val adminId = authService?.userProfile?.value?.id?.toIntOrNull() ?: 605

                    val encryptedNotification = try {

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
                
            }
        } catch (e: Exception) {
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
            if (combinedText.isEmpty()) null else combinedText
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
        try {
            // Mark service as destroyed first to prevent new operations
            isServiceDestroyed = true
            isServiceBound = false
            
            // Cancel all pending operations
            serviceScope.cancel()
            
            // Clean up resources
            notificationService = null
            authService = null
            audioService = null
            
            // Clear pending notifications to prevent memory leaks
            pendingNotifications.clear()
            sentNotifications.clear()
            
        } catch (e: Exception) {
            Timber.tag("AndroidNotificationCapt").e(e, "Error in onDestroy: ${e.message}")
        } finally {
            super.onDestroy()
        }
    }
    
    /**
     * Safely checks if the service is in a valid state for operations
     */
    private fun isServiceValid(): Boolean {
        return isServiceBound && !isServiceDestroyed
    }
    
    /**
     * Gracefully handles service binding state changes
     */
    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            isServiceBound = true
            Timber.tag("AndroidNotificationCapt").d("Notification listener connected")
        } catch (e: Exception) {
            Timber.tag("AndroidNotificationCapt")
                .e(e, "Error in onListenerConnected: ${e.message}")
        }
    }
    
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        try {
            isServiceBound = false
            Timber.tag("AndroidNotificationCapt").d("Notification listener disconnected")
        } catch (e: Exception) {
            Timber.tag("AndroidNotificationCapt")
                .e(e, "Error in onListenerDisconnected: ${e.message}")
        }
    }
}