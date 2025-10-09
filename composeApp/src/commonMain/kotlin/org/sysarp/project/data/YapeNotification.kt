package org.sysarp.project.data

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


data class YapeNotification @OptIn(ExperimentalTime::class) constructor(
    val id: Long = 0,
    val packageName: String,
    val notificationTitle: String?,
    val notificationText: String,
    val notificationBigText: String?,
    val notificationSubText: String?,
    val notificationInfoText: String?,
    val notificationSummaryText: String?,
    val notificationTickerText: String?,
    val notificationExtras: String?,
    val notificationId: Int,
    val notificationTag: String?,
    val notificationKey: String?,
    val notificationTimestamp: Long,
    val isClearable: Boolean = true,
    val isOngoing: Boolean = false,
    val userHandle: String?,
    val createdAt: Instant, // Changed from Instant to String
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

@Serializable
data class YapeNotificationResponse(
    val success: Boolean,
    val message: String,
    val data: YapeNotificationData? = null
)

@Serializable
data class YapeNotificationData(
    val id: Long,
    val transactionId: String,
    val amount: Double,
    val currency: String,
    val sellerId: Int,
    val sender: String,
    val status: String,
    val timestamp: Long
)

@Serializable
data class YapeNotificationApiResponse(
    val success: Boolean,
    val message: String,
    val data: YapeNotificationData? = null
)
