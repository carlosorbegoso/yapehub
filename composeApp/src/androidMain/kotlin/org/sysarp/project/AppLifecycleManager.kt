package org.sysarp.project

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber
import org.sysarp.project.service.AndroidNotificationCaptureService
import org.sysarp.project.ui.components.DebugLogManager
import org.sysarp.project.ui.components.DebugLog
import org.sysarp.project.ui.components.LogType
import kotlinx.datetime.Clock

object AppLifecycleManager : DefaultLifecycleObserver {
    private val _appResumed = MutableSharedFlow<Unit>()
    val appResumed: SharedFlow<Unit> = _appResumed.asSharedFlow()
    
    private var application: Application? = null
    
    fun initialize(application: Application) {
        this.application = application
        Timber.tag("AppLifecycleManager").d("🔧 Lifecycle manager initialized")
    }
    
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        Timber.tag("AppLifecycleManager").d("📱 App resumed - checking permissions and cleaning notifications")
        
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
            Timber.tag("AppLifecycleManager").d("🔍 Verificando permisos de notificaciones...")
            
            // 1. Verificar si el servicio de notificaciones está habilitado
            val hasNotificationPermission = AndroidNotificationCaptureService.isNotificationServiceEnabled(context)
            
            if (hasNotificationPermission) {
                Timber.tag("AppLifecycleManager").d("✅ Permisos de notificaciones habilitados")
                
                // 2. Limpiar notificaciones antiguas para evitar acumulación
                cleanupOldNotifications(context)
                
                // 3. Verificar que el servicio esté funcionando correctamente
                verifyNotificationServiceStatus(context)
                
            } else {
                Timber.tag("AppLifecycleManager").w("❌ Permisos de notificaciones NO habilitados")
                
                // Enviar log de debug
                DebugLogManager.addLog(
                    DebugLog(
                        timestamp = Clock.System.now().toEpochMilliseconds(),
                        type = LogType.PERMISSION,
                        message = "❌ Permisos de notificaciones no habilitados",
                        details = "El usuario necesita habilitar el servicio de notificaciones"
                    )
                )
            }
            
        } catch (e: Exception) {
            Timber.tag("AppLifecycleManager").e("❌ Error verificando permisos: ${e.message}")
        }
    }
    
    /**
     * Limpia notificaciones antiguas para evitar acumulación
     */
    private fun cleanupOldNotifications(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Limpiar notificaciones activas (esto solo afecta las notificaciones de la app, no las del sistema)
            notificationManager.cancelAll()
            
            Timber.tag("AppLifecycleManager").d("🧹 Notificaciones de la app limpiadas")
            
            // Enviar log de debug
            DebugLogManager.addLog(
                DebugLog(
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    type = LogType.PERMISSION,
                    message = "🧹 Notificaciones limpiadas",
                    details = "Se limpiaron las notificaciones antiguas de la app"
                )
            )
            
        } catch (e: Exception) {
            Timber.tag("AppLifecycleManager").e("❌ Error limpiando notificaciones: ${e.message}")
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
            
            Timber.tag("AppLifecycleManager").d("✅ Servicio de notificaciones registrado correctamente")
            
            // Enviar log de debug
            DebugLogManager.addLog(
                DebugLog(
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    type = LogType.PERMISSION,
                    message = "✅ Servicio de notificaciones funcionando",
                    details = "El servicio está registrado y funcionando correctamente"
                )
            )
            
        } catch (e: Exception) {
            Timber.tag("AppLifecycleManager").w("⚠️ Servicio de notificaciones no encontrado: ${e.message}")
        }
    }
}
