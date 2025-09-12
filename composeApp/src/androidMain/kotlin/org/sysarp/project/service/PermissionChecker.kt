package org.sysarp.project.service

import android.content.Context
import android.provider.Settings
import android.text.TextUtils

object PermissionChecker {
    
    fun isNotificationServiceEnabled(context: Context): Boolean {
        return try {
            val pkgName = context.packageName
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            android.util.Log.d("PermissionChecker", "Checking notification permission for package: $pkgName")

            if (!TextUtils.isEmpty(flat)) {
                val names = flat.split(":").toTypedArray()
                for (name in names) {
                    val componentName = android.content.ComponentName.unflattenFromString(name)
                    if (componentName != null) {
                        if (TextUtils.equals(pkgName, componentName.packageName)) {
                            android.util.Log.d("PermissionChecker", "Notification permission GRANTED")
                            return true
                        }
                    }
                }
            }
            android.util.Log.d("PermissionChecker", "Notification permission DENIED")
            false
        } catch (e: Exception) {
            android.util.Log.e("PermissionChecker", "Error checking notification permission", e)
            false
        }
    }
    
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        // SIMPLIFICADO: Solo verificar si la accesibilidad está habilitada en general
        // Ya que no tenemos un servicio de accesibilidad específico
        return try {
            val accessibilityEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED, 0
            )
            val isEnabled = accessibilityEnabled == 1
            android.util.Log.d("PermissionChecker", "Accessibility service enabled: $isEnabled")
            isEnabled
        } catch (e: Exception) {
            android.util.Log.e("PermissionChecker", "Error checking accessibility permission", e)
            false
        }
    }
    
    /**
     * SIMPLIFICADO: Ir directamente a la configuración general de notificaciones
     */
    fun requestNotificationPermission(context: Context) {
        try {
            android.util.Log.d("PermissionChecker", "Abriendo configuración de notificaciones...")
            val intent = android.content.Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            android.util.Log.d("PermissionChecker", "Intent de notificaciones enviado exitosamente")
        } catch (e: Exception) {
            android.util.Log.e("PermissionChecker", "Error requesting notification permission", e)
        }
    }
    
    /**
     * SIMPLIFICADO: Solo abrir configuración general de accesibilidad
     */
    fun requestAccessibilityPermission(context: Context) {
        try {
            android.util.Log.d("PermissionChecker", "Abriendo configuración general de accesibilidad")
            val intent = android.content.Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("PermissionChecker", "Error requesting accessibility permission", e)
        }
    }
}
