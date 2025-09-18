package org.sysarp.project.data

/**
 * Estado del servicio de notificaciones
 */
data class ServiceStatus(
    val isRunning: Boolean,
    val isCapturing: Boolean,
    val pendingNotificationsCount: Int,
    val deviceFingerprint: String,
    val lastError: String? = null,
    val lastNotificationTime: Long? = null,
    val totalNotificationsProcessed: Int = 0,
    val totalNotificationsFailed: Int = 0
)
