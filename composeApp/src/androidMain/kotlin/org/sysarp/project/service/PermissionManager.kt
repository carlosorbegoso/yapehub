package org.sysarp.project.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Manager mejorado de permisos para YapeHub
 * Usa Accompanist Permissions para manejo robusto
 */
class PermissionManager(private val context: Context) {
    
    fun hasNotificationPermission(): Boolean {
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )?.contains(context.packageName) == true
    }
    
    fun hasAccessibilityPermission(): Boolean {
        val accessibilityEnabled = android.provider.Settings.Secure.getInt(
            context.contentResolver,
            android.provider.Settings.Secure.ACCESSIBILITY_ENABLED, 0
        )
        
        if (accessibilityEnabled == 1) {
            val services = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            return services?.contains(context.packageName) == true
        }
        return false
    }
    
    fun requestNotificationPermission() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            TimberLogger.permission("🔔 Abriendo configuración de notificaciones")
        } catch (e: Exception) {
            TimberLogger.error(e, "❌ Error abriendo configuración de notificaciones")
        }
    }
    
    fun requestAccessibilityPermission() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            TimberLogger.permission("♿ Abriendo configuración de accesibilidad")
        } catch (e: Exception) {
            TimberLogger.error(e, "❌ Error abriendo configuración de accesibilidad")
        }
    }
    
    fun requestOverlayPermission() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:${context.packageName}")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            TimberLogger.permission("🪟 Abriendo configuración de ventanas superpuestas")
        } catch (e: Exception) {
            TimberLogger.error(e, "❌ Error abriendo configuración de ventanas superpuestas")
        }
    }
    
    fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }
    
    fun getPermissionStatus(): PermissionStatus {
        return PermissionStatus(
            notification = hasNotificationPermission(),
            accessibility = hasAccessibilityPermission(),
            overlay = hasOverlayPermission()
        )
    }
}

data class PermissionStatus(
    val notification: Boolean,
    val accessibility: Boolean,
    val overlay: Boolean
) {
    val allGranted: Boolean get() = notification && accessibility && overlay
    val criticalGranted: Boolean get() = notification && accessibility
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberPermissionState() = rememberMultiplePermissionsState(
    permissions = listOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.SYSTEM_ALERT_WINDOW
    )
)

val LocalPermissionManager = staticCompositionLocalOf<PermissionManager?> { null }
