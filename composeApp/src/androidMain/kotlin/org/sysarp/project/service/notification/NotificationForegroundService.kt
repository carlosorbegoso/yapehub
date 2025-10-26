package org.sysarp.project.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.sysarp.project.MainActivity


class NotificationForegroundService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "yape_notification_channel"
        private const val WORK_NAME = "yape_notification_polling"

    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startPolling()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        println("[FOREGROUND_SERVICE] 🚀 Servicio iniciado - Polling activo")
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
        println("[FOREGROUND_SERVICE] 🛑 Servicio detenido")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notificaciones de Yape",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene activo el polling de notificaciones de Yape"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("YapeHub - Notificaciones Activas")
            .setContentText("Monitoreando pagos de Yape en segundo plano")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun startPolling() {
        try {
            // Por ahora solo loggear - el polling se maneja desde HybridNotificationManager
            println("[FOREGROUND_SERVICE] ✅ Servicio iniciado - Polling manejado por HybridNotificationManager")
            
        } catch (e: Exception) {
            println("[FOREGROUND_SERVICE] ❌ Error iniciando servicio: ${e.message}")
        }
    }

    private fun stopPolling() {
        try {
            println("[FOREGROUND_SERVICE] 🛑 Servicio detenido")
        } catch (e: Exception) {
            println("[FOREGROUND_SERVICE] ❌ Error deteniendo servicio: ${e.message}")
        }
    }
}
