package org.sysarp.project.service

/**
 * Interfaz común para verificar permisos en diferentes plataformas
 */
expect object PermissionChecker {
    /**
     * Verifica si el servicio de notificaciones está habilitado
     */
    fun isNotificationServiceEnabled(): Boolean
    
    /**
     * Verifica si el servicio de accesibilidad está habilitado
     */
    fun isAccessibilityServiceEnabled(): Boolean
}
