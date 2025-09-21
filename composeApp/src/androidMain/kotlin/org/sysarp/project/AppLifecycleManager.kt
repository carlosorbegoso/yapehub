package org.sysarp.project

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.sysarp.project.service.AndroidNotificationCaptureService

object AppLifecycleManager : DefaultLifecycleObserver {
    private val _appResumed = MutableSharedFlow<Unit>()
    val appResumed: SharedFlow<Unit> = _appResumed.asSharedFlow()
    
    private var application: Application? = null
    
    fun initialize(application: Application) {
        this.application = application
    }
    
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        
        // Emitir evento para otros componentes
        _appResumed.tryEmit(Unit)
        
        // Verificar permisos y limpiar notificaciones
        application?.let { app ->
            checkNotificationPermissionsAndCleanup(app)
        }
    }
    
    /**
     * Verifica permisos de notificaciones y limpia notificaciones antiguas
     */
    private fun checkNotificationPermissionsAndCleanup(context: Context) {
        try {
            
            // 1. Verificar si el servicio de notificaciones está habilitado
            val hasNotificationPermission = AndroidNotificationCaptureService.isNotificationServiceEnabled(context)
            
            if (hasNotificationPermission) {
                
                // 2. Limpiar notificaciones antiguas para evitar acumulación
                cleanupOldNotifications(context)
                
                // 3. Verificar que el servicio esté funcionando correctamente
                verifyNotificationServiceStatus(context)
                
            } else {
                
            }
            
        } catch (e: Exception) {
        }
    }
    
    /**
     * Limpia notificaciones antiguas para evitar acumulación
     */
    private fun cleanupOldNotifications(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            notificationManager.cancelAll()
            
            
            
        } catch (e: Exception) {
        }
    }
    
    /**
     * Verifica que el servicio de notificaciones esté funcionando correctamente
     */
    private fun verifyNotificationServiceStatus(context: Context) {
        try {
            // Verificar si el servicio está registrado en el manifest
            val packageManager = context.packageManager
            packageManager.getServiceInfo(
                android.content.ComponentName(context, AndroidNotificationCaptureService::class.java),
                0
            )
            
            
            
        } catch (e: Exception) {
        }
    }
}
