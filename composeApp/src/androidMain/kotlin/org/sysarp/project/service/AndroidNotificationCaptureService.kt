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
import kotlinx.coroutines.launch
import org.sysarp.project.repository.YapeTransactionRepository
import org.sysarp.project.repository.YapeTransactionRepositoryImpl
import org.sysarp.project.data.YapeNotification
import org.sysarp.project.service.DebugLogger
import org.sysarp.project.service.TimberLogger
import org.sysarp.project.RepositorySingleton
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class AndroidNotificationCaptureService : NotificationListenerService() {
    
    private lateinit var transactionRepository: YapeTransactionRepository
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    // Lista en memoria para notificaciones de Yape
    private val yapeNotifications = mutableListOf<YapeNotification>()
    
    override fun onCreate() {
        super.onCreate()
        // Usar el repositorio singleton que tiene contexto
        transactionRepository = RepositorySingleton.getRepository()
        DebugLogger.info("🔧 Servicio de notificaciones inicializado con repositorio")
        DebugLogger.info("🔍 Repositorio tiene contexto: ${(transactionRepository as? YapeTransactionRepositoryImpl)?.let { true } ?: false}")
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        coroutineScope.launch {
            processNotification(sbn)
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        DebugLogger.debug("🗑️ Notificación removida: ${sbn.packageName} - ${sbn.id}")
    }
    
    private suspend fun processNotification(sbn: StatusBarNotification) {
        try {
            TimberLogger.notification("🔍 Procesando notificación: ${sbn.packageName}")
            if (isYapePackage(sbn.packageName)) {
                val notificationText = extractNotificationText(sbn)
                TimberLogger.notification("📱 Notificación de Yape detectada: $notificationText")
                
                val yapeNotification = YapeNotification(
                    packageName = sbn.packageName,
                    notificationTitle = sbn.notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
                    notificationText = notificationText ?: "",
                    notificationBigText = sbn.notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString(),
                    notificationSubText = sbn.notification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString(),
                    notificationInfoText = sbn.notification.extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString(),
                    notificationSummaryText = sbn.notification.extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString(),
                    notificationTickerText = sbn.notification.extras.getCharSequence("android.tickerText")?.toString(),
                    notificationExtras = serializeExtras(sbn.notification.extras),
                    notificationId = sbn.id,
                    notificationTag = sbn.tag,
                    notificationKey = sbn.key,
                    notificationTimestamp = sbn.postTime,
                    isClearable = sbn.isClearable,
                    isOngoing = sbn.isOngoing,
                    userHandle = sbn.user.toString(),
                    createdAt = Clock.System.now(),
                    isProcessed = false,
                    processingError = null
                )
                
                yapeNotifications.add(yapeNotification)
                processYapeNotification(sbn, yapeNotification)
            }
        } catch (e: Exception) {
            DebugLogger.error("Error procesando notificación: ${e.message}")
        }
    }
    
    /**
     * Procesa una notificación de Yape específica
     */
    private suspend fun processYapeNotification(sbn: StatusBarNotification, yapeNotification: YapeNotification) {
        try {
            val notificationText = extractNotificationText(sbn)
            DebugLogger.info("🔍 Procesando notificación de Yape: $notificationText")
            
            if (notificationText != null && YapeNotificationParser.isYapeNotification(notificationText)) {
                DebugLogger.info("✅ Es notificación de Yape válida, parseando...")
                val transaction = YapeNotificationParser.parseYapeNotification(
                    notificationText = notificationText,
                    businessName = "Negocio Principal"
                )
                
                if (transaction != null) {
                    DebugLogger.info("✅ Transacción parseada exitosamente: ${transaction.amount} PEN de ${transaction.senderName}")
                    DebugLogger.info("💾 Guardando transacción en repositorio...")
                    transactionRepository.insertTransaction(transaction)
                    DebugLogger.info("✅ Transacción guardada exitosamente en repositorio")
                } else {
                    DebugLogger.warn("❌ No se pudo parsear la transacción")
                }
            } else {
                DebugLogger.warn("❌ No es notificación de Yape válida")
            }
        } catch (e: Exception) {
            DebugLogger.error("Error procesando notificación de Yape: ${e.message}")
        }
    }
    
    /**
     * Serializa los extras de la notificación a JSON
     */
    private fun serializeExtras(extras: android.os.Bundle): String? {
        return try {
            val extrasMap = mutableMapOf<String, String>()
            for (key in extras.keySet()) {
                val value = extras.get(key)
                extrasMap[key] = value?.toString() ?: "null"
            }
            Json.encodeToString(extrasMap)
        } catch (e: Exception) {
            DebugLogger.warn("⚠️ Error serializando extras: ${e.message}")
            null
        }
    }
    
    
    /**
     * Verifica si el package name corresponde a Yape
     */
    private fun isYapePackage(packageName: String): Boolean {
        val yapePackages = listOf(
            "com.bcp.innovacxion.yapeapp", // Package principal de Yape
            "com.bcp.yape", // Posible variación
            "pe.com.bcp.yape", // Otra posible variación
            "com.bcp.innovacxion.yape" // Otra posible variación
        )
        
        val isYape = yapePackages.contains(packageName)
        DebugLogger.debug("🔍 Verificando package Yape: $packageName -> $isYape")
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
    
    /**
     * Verifica si un texto es duplicado o está contenido en los textos ya agregados
     */
    private fun isDuplicateText(newText: String, existingTexts: List<String>): Boolean {
        // Verificar si el texto es exactamente igual a alguno existente
        if (existingTexts.contains(newText)) return true
        
        // Verificar si el texto está contenido en alguno existente
        if (existingTexts.any { it.contains(newText) }) return true
        
        // Verificar si algún texto existente está contenido en el nuevo texto
        if (existingTexts.any { newText.contains(it) }) return true
        
        return false
    }
}