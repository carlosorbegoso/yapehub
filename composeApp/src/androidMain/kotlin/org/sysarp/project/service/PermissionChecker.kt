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
            android.util.Log.d("PermissionChecker", "Enabled listeners: $flat")
            
            if (!TextUtils.isEmpty(flat)) {
                val names = flat.split(":").toTypedArray()
                for (name in names) {
                    val componentName = android.content.ComponentName.unflattenFromString(name)
                    if (componentName != null) {
                        android.util.Log.d("PermissionChecker", "Checking component: ${componentName.packageName}")
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
        return try {
            val accessibilityEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED, 0
            )
            android.util.Log.d("PermissionChecker", "Accessibility enabled: $accessibilityEnabled")
            
            if (accessibilityEnabled == 1) {
                val settingValue = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                android.util.Log.d("PermissionChecker", "Enabled accessibility services: $settingValue")
                
                if (settingValue != null) {
                    val packageName = context.packageName
                    val serviceName = "$packageName/${YapeAccessibilityService::class.java.name}"
                    android.util.Log.d("PermissionChecker", "Looking for service: $serviceName")
                    
                    val splitter = TextUtils.SimpleStringSplitter(':')
                    splitter.setString(settingValue)
                    val isEnabled = splitter.any { it.equals(serviceName, ignoreCase = true) }
                    
                    android.util.Log.d("PermissionChecker", "Accessibility permission: ${if (isEnabled) "GRANTED" else "DENIED"}")
                    isEnabled
                } else {
                    android.util.Log.d("PermissionChecker", "Accessibility permission DENIED - no services")
                    false
                }
            } else {
                android.util.Log.d("PermissionChecker", "Accessibility permission DENIED - accessibility disabled")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("PermissionChecker", "Error checking accessibility permission", e)
            false
        }
    }
    
    fun requestNotificationPermission(context: Context) {
        try {
            val intent = android.content.Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("PermissionChecker", "Error requesting notification permission", e)
        }
    }
    
    fun requestAccessibilityPermission(context: Context) {
        try {
            val intent = android.content.Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("PermissionChecker", "Error requesting accessibility permission", e)
        }
    }
}
