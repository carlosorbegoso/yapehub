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
import kotlinx.datetime.Clock
import org.sysarp.project.data.ServiceStatus
import org.sysarp.project.data.YapeNotificationRequest
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.ui.components.DebugLog
import org.sysarp.project.ui.components.DebugLogManager
import org.sysarp.project.ui.components.LogType
import org.sysarp.project.utils.AndroidDeviceUtils
import org.sysarp.project.utils.EncryptionUtils
import timber.log.Timber

class AndroidNotificationCaptureService : NotificationListenerService() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var notificationService: NotificationService? = null
    private var authService: AuthService? = null
    private var deviceFingerprint: String? = null
    
    // Queue para notificaciones pendientes
    private val pendingNotifications = mutableListOf<YapeNotificationRequest>()
    private val maxRetries = 2 // Reducido de 3 a 2 para evitar duplicados
    private val retryDelayMs = 10000L // Aumentado de 5 a 10 segundos para dar más tiempo a la API
    
    // Sistema de deduplicación para evitar notificaciones duplicadas
    private val sentNotifications = mutableSetOf<String>() // Hash de notificaciones ya enviadas
    private val maxSentNotifications = 100 // Límite para evitar memory leak
    
    override fun onCreate() {
        super.onCreate()
        Timber.tag("NotificationCapture").d("🔧 Servicio de notificaciones inicializado")
        
        // Enviar log de inicialización
        DebugLogManager.addLog(
            DebugLog(
                timestamp = Clock.System.now().toEpochMilliseconds(),
                type = LogType.PERMISSION,
                message = "🔧 Servicio de notificaciones inicializado",
                details = "AndroidNotificationCaptureService onCreate()"
            )
        )
        
                // Generar device fingerprint único usando identificadores reales del dispositivo
                serviceScope.launch {
                    deviceFingerprint = AndroidDeviceUtils.generateDeviceFingerprint(this@AndroidNotificationCaptureService)
                    Timber.tag("NotificationCapture")
                        .d("Device fingerprint generado: ${deviceFingerprint?.take(20)}...")

                    // Enviar log de device fingerprint
                    DebugLogManager.addLog(
                        DebugLog(
                            timestamp = Clock.System.now().toEpochMilliseconds(),
                            type = LogType.PERMISSION,
                            message = "🔑 Device fingerprint generado",
                            details = "Fingerprint: ${deviceFingerprint?.take(20)}... (usando Android ID)"
                        )
                    )
                }
        
        // Inicializar AuthService con contexto disponible
        try {
            authService = org.sysarp.project.service.auth.AuthService.getInstance()
            Timber.tag("NotificationCapture").d("✅ AuthService singleton inicializado")
            
            // Verificar si hay token disponible
            val currentToken = authService?.accessToken?.value
            Timber.tag("NotificationCapture").d("🔑 Token disponible: ${if (currentToken != null) "SÍ (${currentToken.take(20)}...)" else "NO"}")
            
            // Inicializar NotificationService con dependencias reales
            val notificationApiClient = org.sysarp.project.service.http.NotificationApiClient()
            notificationService = org.sysarp.project.service.NotificationService(notificationApiClient, authService!!)
            Timber.tag("NotificationCapture").d("✅ NotificationService inicializado")
            
        } catch (e: Exception) {
            Timber.tag("NotificationCapture").e("❌ Error inicializando servicios: ${e.message}")
            
            // Enviar log de error
            DebugLogManager.addLog(
                DebugLog(
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    type = LogType.ERROR,
                    message = "❌ Error inicializando servicios",
                    details = "Exception: ${e.message}"
                )
            )
        }

        Timber.tag("NotificationCapture").d("Servicios inicializados correctamente")
        
        // Enviar log de servicios inicializados
        DebugLogManager.addLog(
            DebugLog(
                timestamp = Clock.System.now().toEpochMilliseconds(),
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
        Timber.tag("NotificationCapture")
            .d("🗑️ Notificación removida: ${sbn.packageName} - ${sbn.id}")
    }
    
    /**
     * Procesa notificaciones de Yape
     * Solo envía notificaciones de aplicaciones de Yape para evitar errores 400 del servidor
     */
    private suspend fun processNotification(sbn: StatusBarNotification) {
        try {
            Timber.tag("NotificationCapture").d("🔍 Procesando notificación: ${sbn.packageName}")
            
            // Enviar log de notificación recibida
            DebugLogManager.addLog(
                DebugLog(
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    type = LogType.NOTIFICATION,
                    message = "📨 Notificación recibida",
                    details = "Package: ${sbn.packageName}, ID: ${sbn.id}"
                )
            )
            
            // TEMPORAL: Restaurar validación de Yape para evitar errores 400 del servidor
            if (isYapePackage(sbn.packageName)) {
                val notificationText = extractNotificationText(sbn)
                Timber.tag("NotificationCapture")
                    .d("📱 Notificación de Yape detectada: $notificationText")
                
                // Enviar log de Yape detectado
                DebugLogManager.addLog(
                    DebugLog(
                        timestamp = Clock.System.now().toEpochMilliseconds(),
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
                        timestamp = Clock.System.now().toEpochMilliseconds(),
                        type = LogType.NOTIFICATION,
                        message = "❌ No es Yape",
                        details = "Package: ${sbn.packageName} - ignorando"
                    )
                )
            }
        } catch (e: Exception) {
            Timber.tag("NotificationCapture").e("Error procesando notificación: ${e.message}")
            
            // Enviar log de error
            DebugLogManager.addLog(
                DebugLog(
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    type = LogType.ERROR,
                    message = "❌ Error procesando notificación",
                    details = "Exception: ${e.message}"
                )
            )
        }
    }
    
    private suspend fun processYapeNotification(sbn: StatusBarNotification, notificationText: String?) {
        try {
            // VALIDACIÓN DE SEGURIDAD: Verificar que realmente sea Yape
            if (!isYapePackage(sbn.packageName)) {
                Timber.tag("NotificationCapture").w("🚫 SEGURIDAD: Intento de procesar notificación no-Yape bloqueado: ${sbn.packageName}")
                DebugLogManager.addLog(
                    DebugLog(
                        timestamp = Clock.System.now().toEpochMilliseconds(),
                        type = LogType.ERROR,
                        message = "🚫 SEGURIDAD: Bloqueado",
                        details = "Intento de procesar notificación no-Yape: ${sbn.packageName}"
                    )
                )
                return
            }
            
            Timber.tag("NotificationCapture")
                .d("🔍 Procesando notificación de Yape: $notificationText")
            
            if (notificationText != null) {
                Timber.tag("NotificationCapture")
                    .d("✅ Es notificación de Yape válida, enviando a API...")
                
                // Extraer datos de la notificación
                val extras = sbn.notification.extras
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

                Timber.tag("NotificationCapture").d("📱 Package: ${sbn.packageName}")
                Timber.tag("NotificationCapture").d("📝 Title: $title")
                Timber.tag("NotificationCapture").d("📄 Text: $text")
                
                // Enviar log de datos extraídos
                DebugLogManager.addLog(
                    DebugLog(
                        timestamp = Clock.System.now().toEpochMilliseconds(),
                        type = LogType.NOTIFICATION,
                        message = "📄 Datos extraídos",
                        details = "Title: $title, Text: ${text.take(30)}..."
                    )
                )
                
                // Solo procesar si el servicio está disponible
                notificationService?.let { _ ->
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

                        Timber.tag("NotificationCapture")
                            .d("🔐 Notificación encriptada exitosamente")
                        Timber.tag("NotificationCapture")
                            .d("🔐 Tamaño original: ${jsonString.length} chars")
                        Timber.tag("NotificationCapture")
                            .d("🔐 Tamaño encriptado: ${encrypted.length} chars")
                        
                        encrypted
                    } catch (e: Exception) {
                        Timber.tag("NotificationCapture")
                            .e("❌ Error encriptando notificación: ${e.message}")
                        // Fallback: encriptar solo el texto
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
                                deduplicationHash = "" // Temporal para generar hash
                            )
                        )
                    )

                    Timber.tag("NotificationCapture").d("📱 AdminId real: $adminId")
                    Timber.tag("NotificationCapture")
                        .d("🔑 DeviceFingerprint real: ${currentFingerprint.take(20)}...")
                    Timber.tag("NotificationCapture")
                        .d("🔐 EncryptedNotification: ${encryptedNotification.take(50)}...")
                    Timber.tag("NotificationCapture")
                        .d("🔗 DeduplicationHash: ${notificationRequest.deduplicationHash}")

                    Timber.tag("NotificationCapture")
                        .d("✅ YapeNotificationRequest creada con notificación encriptada y hash de deduplicación")
                    
                    // Enviar a la API con retry automático
                    sendNotificationWithRetry(notificationRequest, 0)
                } ?: Timber.tag("NotificationCapture").w("Servicio de notificaciones no disponible")
                
            } else {
                Timber.tag("NotificationCapture").w("❌ No es notificación de Yape válida")
            }
        } catch (e: Exception) {
            Timber.tag("NotificationCapture")
                .e("Error procesando notificación de Yape: ${e.message}")
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
        Timber.tag("NotificationCapture").d("🔍 Verificando package Yape: $packageName -> $isYape")
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
                Timber.tag("NotificationCapture").d("🔄 Notificación duplicada detectada - omitiendo envío")
                DebugLogManager.addLog(
                    DebugLog(
                        timestamp = Clock.System.now().toEpochMilliseconds(),
                        type = LogType.NOTIFICATION,
                        message = "🔄 Notificación duplicada omitida",
                        details = "Hash: ${generateNotificationHash(notificationRequest)}"
                    )
                )
                return
            }
            
            // VALIDACIÓN DE SEGURIDAD: Verificar que el adminId sea válido (no sea fallback)
            if (notificationRequest.adminId == 605) {
                Timber.tag("NotificationCapture").w("⚠️ Usando adminId fallback (605) - verificar autenticación")
            }
            
            Timber.tag("NotificationCapture").d("🚀 Enviando a API (intento ${attempt + 1}/$maxRetries)...")
            Timber.tag("NotificationCapture").d("🔒 SEGURIDAD: Solo datos de Yape autorizados")
            
            // Enviar log de envío a API
            DebugLogManager.addLog(
                DebugLog(
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    type = LogType.API,
                    message = "🚀 Enviando a API (intento ${attempt + 1})",
                    details = "POST /api/notifications/yape-notifications - Solo Yape autorizado"
                )
            )
            
            val result = notificationService?.sendYapeNotification(
                adminId = notificationRequest.adminId,
                encryptedNotification = notificationRequest.encryptedNotification,
                deviceFingerprint = notificationRequest.deviceFingerprint,
                timestamp = notificationRequest.timestamp
            )
            
            result?.onSuccess { response ->
                Timber.tag("NotificationCapture")
                    .d("✅ Notificación enviada exitosamente: ${response.message}")
                
                // Marcar como enviada para deduplicación
                markNotificationAsSent(notificationRequest)
                
                // Enviar log de éxito
                DebugLogManager.addLog(
                    DebugLog(
                        timestamp = Clock.System.now().toEpochMilliseconds(),
                        type = LogType.API,
                        message = "✅ Enviado exitosamente",
                        details = "Response: ${response.message}"
                    )
                )
                
                // Remover de queue si estaba pendiente
                pendingNotifications.remove(notificationRequest)
                
            }?.onFailure { error ->
                Timber.tag("NotificationCapture")
                    .e("❌ Error enviando notificación (intento ${attempt + 1}): ${error.message}")
                
                // Si no es el último intento, agregar a queue y reintentar
                if (attempt < maxRetries - 1) {
                    Timber.tag("NotificationCapture").d("🔄 Reintentando en ${retryDelayMs}ms...")
                    
                    // Agregar a queue si no está ya
                    if (!pendingNotifications.contains(notificationRequest)) {
                        pendingNotifications.add(notificationRequest)
                    }
                    
                    // Reintentar después del delay
                    kotlinx.coroutines.delay(retryDelayMs)
                    sendNotificationWithRetry(notificationRequest, attempt + 1)
                } else {
                    // Último intento fallido, mantener en queue para procesamiento manual
                    Timber.tag("NotificationCapture").e("💥 Fallo definitivo después de $maxRetries intentos")
                    
                    if (!pendingNotifications.contains(notificationRequest)) {
                        pendingNotifications.add(notificationRequest)
                    }
                    
                    // Enviar log de error final
                    DebugLogManager.addLog(
                        DebugLog(
                            timestamp = Clock.System.now().toEpochMilliseconds(),
                            type = LogType.ERROR,
                            message = "💥 Fallo definitivo",
                            details = "Error después de $maxRetries intentos: ${error.message}"
                        )
                    )
                }
            }
            
        } catch (e: Exception) {
            Timber.tag("NotificationCapture")
                .e("💥 Excepción enviando notificación (intento ${attempt + 1}): ${e.message}")
            
            // Si no es el último intento, reintentar
            if (attempt < maxRetries - 1) {
                kotlinx.coroutines.delay(retryDelayMs)
                sendNotificationWithRetry(notificationRequest, attempt + 1)
            } else {
                // Agregar a queue para procesamiento manual
                if (!pendingNotifications.contains(notificationRequest)) {
                    pendingNotifications.add(notificationRequest)
                }
                
                // Enviar log de excepción final
                DebugLogManager.addLog(
                    DebugLog(
                        timestamp = Clock.System.now().toEpochMilliseconds(),
                        type = LogType.ERROR,
                        message = "💥 Excepción definitiva",
                        details = "Exception después de $maxRetries intentos: ${e.message}"
                    )
                )
            }
        }
    }
    
    /**
     * Procesa notificaciones pendientes en la queue
     */
    private suspend fun processPendingNotifications() {
        if (pendingNotifications.isNotEmpty()) {
            Timber.tag("NotificationCapture").d("🔄 Procesando ${pendingNotifications.size} notificaciones pendientes...")
            
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
                    
                    Timber.tag("NotificationCapture").d("🧹 Notificaciones del sistema limpiadas")
                    
                    // Enviar log de debug
                    org.sysarp.project.ui.components.DebugLogManager.addLog(
                        org.sysarp.project.ui.components.DebugLog(
                            timestamp = Clock.System.now().toEpochMilliseconds(),
                            type = org.sysarp.project.ui.components.LogType.PERMISSION,
                            message = "🧹 Sistema limpiado",
                            details = "Se limpiaron las notificaciones del sistema"
                        )
                    )
                } else {
                    Timber.tag("NotificationCapture").w("⚠️ No se puede limpiar notificaciones - servicio no habilitado")
                }
            } catch (e: Exception) {
                Timber.tag("NotificationCapture").e("❌ Error limpiando notificaciones del sistema: ${e.message}")
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Timber.tag("NotificationCapture").d("Servicio de captura de notificaciones destruido")
    }
}