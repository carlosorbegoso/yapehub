package org.sysarp.project.service

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import org.sysarp.project.ContextProvider

actual object PermissionChecker {
    
    private val context: Context
        get() = ContextProvider.getContext() ?: throw IllegalStateException("Context not available")
    
    actual fun isNotificationServiceEnabled(): Boolean {
        return try {
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
            false
        } catch (e: Exception) {
            false
        }
    }
    
    actual fun isAccessibilityServiceEnabled(): Boolean {
        return try {
            // Para Android, verificamos si el servicio de accesibilidad está habilitado
            val accessibilityEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED,
                0
            ) == 1
            
            if (accessibilityEnabled) {
                val accessibilityServices = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                val pkgName = context.packageName
                return !TextUtils.isEmpty(accessibilityServices) && 
                       accessibilityServices.contains(pkgName)
            }
            false
        } catch (e: Exception) {
            false
        }
    }
}
