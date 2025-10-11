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

        _appResumed.tryEmit(Unit)

        application?.let { app ->
            checkNotificationPermissionsAndCleanup(app)
        }
    }

    private fun checkNotificationPermissionsAndCleanup(context: Context) {
        try {
            

            val hasNotificationPermission = AndroidNotificationCaptureService.isNotificationServiceEnabled(context)
            
            if (hasNotificationPermission) {
                cleanupOldNotifications(context)

                verifyNotificationServiceStatus(context)
                
            }else{
                // TODO: Aquí podrías notificar al usuario que active el permiso
            }
            
        } catch (e: Exception) {


        }
    }

    private fun cleanupOldNotifications(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            notificationManager.cancelAll()
            
            
            
        } catch (e: Exception) {
        }
    }

    private fun verifyNotificationServiceStatus(context: Context) {
        try {
            val packageManager = context.packageManager
            packageManager.getServiceInfo(
                android.content.ComponentName(context, AndroidNotificationCaptureService::class.java),
                0
            )
            
            
            
        } catch (e: Exception) {

        }
    }
}
