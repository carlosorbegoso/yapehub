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
import org.sysarp.project.service.DebugLogger
import org.sysarp.project.service.TimberLogger
import org.sysarp.project.RepositorySingleton
import kotlinx.datetime.Clock

class AndroidNotificationCaptureService : NotificationListenerService() {
    
    private lateinit var transactionRepository: YapeTransactionRepository
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        transactionRepository = RepositorySingleton.getRepository()
        DebugLogger.info("🔧 Servicio de notificaciones inicializado")
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
                
                processYapeNotification(sbn, notificationText)
            }
        } catch (e: Exception) {
            DebugLogger.error("Error procesando notificación: ${e.message}")
        }
    }
    
    private suspend fun processYapeNotification(sbn: StatusBarNotification, notificationText: String?) {
        try {
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
    
    private fun isYapePackage(packageName: String): Boolean {
        val yapePackages = listOf(
            "com.bcp.innovacxion.yapeapp",
            "com.bcp.yape",
            "pe.com.bcp.yape",
            "com.bcp.innovacxion.yape"
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
}