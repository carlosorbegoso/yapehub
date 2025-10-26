package org.sysarp.project.service

/**
 * Implementación de iOS para PermissionChecker
 * iOS no soporta captura de notificaciones de otras apps
 */
actual object PermissionChecker {
    
    actual fun isNotificationServiceEnabled(): Boolean {
        // iOS no soporta captura de notificaciones de otras apps
        return false
    }
    
    actual fun isAccessibilityServiceEnabled(): Boolean {
        // iOS no soporta captura de notificaciones de otras apps
        return false
    }
}
