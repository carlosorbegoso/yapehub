package org.sysarp.project.data

import kotlinx.datetime.Instant

/**
 * Representa una notificación de Yape capturada antes de ser procesada
 */
data class YapeNotification(
    val id: Long = 0,
    val packageName: String,
    val notificationTitle: String?,
    val notificationText: String,
    val notificationBigText: String?,
    val notificationSubText: String?,
    val notificationInfoText: String?,
    val notificationSummaryText: String?,
    val notificationTickerText: String?,
    val notificationExtras: String?, // JSON string de todos los extras
    val notificationId: Int,
    val notificationTag: String?,
    val notificationKey: String?,
    val notificationTimestamp: Long,
    val isClearable: Boolean = true,
    val isOngoing: Boolean = false,
    val userHandle: String?,
    val createdAt: Instant,
    val isProcessed: Boolean = false,
    val processingError: String? = null
)

/**
 * Estadísticas de notificaciones por paquete
 */
data class NotificationStats(
    val packageName: String,
    val totalCount: Long,
    val yapeCount: Long,
    val processedCount: Long
)
