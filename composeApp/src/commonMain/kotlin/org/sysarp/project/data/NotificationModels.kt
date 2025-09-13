package org.sysarp.project.data

import kotlinx.serialization.Serializable

// Modelos para notificaciones
@Serializable
data class NotificationRequest(
    val title: String,
    val message: String,
    val type: String,
    val userId: String? = null,
    val sellerId: String? = null,
    val adminId: String? = null
)

@Serializable
data class NotificationResponse(
    val success: Boolean,
    val message: String,
    val data: NotificationData? = null
)

@Serializable
data class NotificationData(
    val notificationId: String,
    val sentAt: String,
    val status: String
)
