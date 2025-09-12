package org.sysarp.project.service

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class AndroidNotificationCaptureService : NotificationListenerService() {
    
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
            
            Log.d("NotificationCapture", "Título: $title")
            Log.d("NotificationCapture", "Texto: $text")
            
            // Aquí se puede enviar la información a la aplicación principal
            // para procesar la transacción de Yape
            
        } catch (e: Exception) {
            Log.e("NotificationCapture", "Error procesando notificación de Yape", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("NotificationCapture", "Servicio de captura de notificaciones destruido")
    }
}