package org.sysarp.project.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.sysarp.project.repository.YapeTransactionRepository
import org.sysarp.project.repository.YapeTransactionRepositoryImpl

class YapeAccessibilityService : AccessibilityService() {
    
    private lateinit var repository: YapeTransactionRepository
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        repository = YapeTransactionRepositoryImpl()
        Log.d("YapeAccessibility", "Servicio de accesibilidad iniciado")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { accessibilityEvent ->
            when (accessibilityEvent.eventType) {
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                    val packageName = accessibilityEvent.packageName?.toString()
                    val text = accessibilityEvent.text?.joinToString(" ")
                    
                    Log.d("YapeAccessibility", "Notificación capturada: $packageName - $text")
                    
                    if (packageName?.contains("yape", ignoreCase = true) == true && 
                        text?.contains("te envió", ignoreCase = true) == true) {
                        processYapeNotification(text)
                    }
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    val text = accessibilityEvent.text?.joinToString(" ")
                    if (text?.contains("te envió", ignoreCase = true) == true &&
                        text.contains("Yape", ignoreCase = true)) {
                        processYapeNotification(text)
                    }
                }
            }
        }
    }
    
    private fun processYapeNotification(notificationText: String) {
        coroutineScope.launch {
            try {
                if (YapeNotificationParser.isYapeNotification(notificationText)) {
                    val transaction = YapeNotificationParser.parseYapeNotification(
                        notificationText = notificationText,
                        businessName = "Negocio Principal"
                    )
                    
                    transaction?.let {
                        repository.insertTransaction(it)
                        Log.d("YapeAccessibility", "Transacción guardada: ${it.amount}")
                    }
                }
            } catch (e: Exception) {
                Log.e("YapeAccessibility", "Error procesando notificación", e)
            }
        }
    }
    
    override fun onInterrupt() {
        Log.d("YapeAccessibility", "Servicio interrumpido")
    }
    
    companion object {
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            return enabledServices?.contains(context.packageName + "/" + YapeAccessibilityService::class.java.name) == true
        }
        
        fun requestAccessibilityPermission(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
}
