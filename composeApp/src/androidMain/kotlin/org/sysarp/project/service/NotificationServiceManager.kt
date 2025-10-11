package org.sysarp.project.service

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Manages the lifecycle of AndroidNotificationCaptureService
 * Provides safe start/stop operations with proper error handling
 */
object NotificationServiceManager {

    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Safely starts the notification capture service
     * Only starts if notification listener permission is granted
     */
    fun startNotificationService(context: Context) {
        managerScope.launch {
            try {
                // Check if service is already enabled
                val isEnabled = AndroidNotificationCaptureService.isNotificationServiceEnabled(context)
                
                if (isEnabled) {
                    val serviceIntent = Intent(context, AndroidNotificationCaptureService::class.java)
                    context.startService(serviceIntent)
                    Timber.tag("NotificationServiceMana").d("Notification service started successfully")
                } else {
                    Timber.tag("NotificationServiceMana").w("Notification service not enabled, cannot start")
                }
            } catch (e: Exception) {
                Timber.tag("NotificationServiceMana").e(e, "Error starting notification service: ${e.message}")
            }
        }
    }

}
