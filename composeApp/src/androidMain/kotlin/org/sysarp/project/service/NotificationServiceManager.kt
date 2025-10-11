package org.sysarp.project.service

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Manages the lifecycle of AndroidNotificationCaptureService
 * Provides safe start/stop operations with proper error handling
 */
object NotificationServiceManager {
    private const val TAG = "NotificationServiceManager"
    
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
                    Log.d(TAG, "Notification service started successfully")
                } else {
                    Log.w(TAG, "Notification service not enabled, cannot start")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting notification service: ${e.message}", e)
            }
        }
    }
    
    /**
     * Safely stops the notification capture service
     * Handles potential binding issues gracefully
     */
    fun stopNotificationService(context: Context) {
        managerScope.launch {
            try {
                val serviceIntent = Intent(context, AndroidNotificationCaptureService::class.java)
                context.stopService(serviceIntent)
                Log.d(TAG, "Notification service stopped successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping notification service: ${e.message}", e)
            }
        }
    }
    
    /**
     * Restarts the notification service
     * Useful for handling permission changes or service binding issues
     */
    fun restartNotificationService(context: Context) {
        managerScope.launch {
            try {
                // Stop first
                stopNotificationService(context)
                
                // Wait a bit for cleanup
                delay(1000)
                
                // Start again
                startNotificationService(context)
                
                Log.d(TAG, "Notification service restarted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error restarting notification service: ${e.message}", e)
            }
        }
    }
    
    /**
     * Checks if the notification service is running and properly bound
     */
    fun isServiceRunning(context: Context): Boolean {
        return try {
            AndroidNotificationCaptureService.isNotificationServiceEnabled(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking service status: ${e.message}", e)
            false
        }
    }
    
    /**
     * Handles service binding errors gracefully
     * Called when the system reports binding issues
     */
    fun handleServiceBindingError(context: Context) {
        managerScope.launch {
            try {
                Log.w(TAG, "Service binding error detected, attempting recovery")
                
                // Clean up any existing service
                stopNotificationService(context)
                
                // Wait for cleanup
                delay(2000)
                
                // Restart if permission is still granted
                if (AndroidNotificationCaptureService.isNotificationServiceEnabled(context)) {
                    startNotificationService(context)
                    Log.d(TAG, "Service recovery completed")
                } else {
                    Log.w(TAG, "Service recovery skipped - permission not granted")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during service recovery: ${e.message}", e)
            }
        }
    }
}
