package org.sysarp.project.data

import kotlinx.serialization.Serializable

@Serializable
data class NotificationData(
    val notificationId: String,
    val sentAt: String,
    val status: String
)

// Modelos para notificaciones de Yape (usar el de YapeNotification.kt)

@Serializable
data class YapeNotificationRequest(
    val adminId: Int,
    val encryptedNotification: String, // NOTA: Este campo contiene la notificación completa ENCRIPTADA (sin parsear)
    val deviceFingerprint: String,
    val timestamp: Long,
    val deduplicationHash: String // Hash para deduplicación en el backend
)

// Modelo alternativo más claro (opcional)
@Serializable
data class YapeNotificationRequestClear(
    val adminId: Int,
    val originalNotification: String, // Texto original de la notificación
    val deviceFingerprint: String,
    val timestamp: Long
)

// YapeNotificationResponse y YapeNotificationData están en YapeNotification.kt

@Serializable
data class PaymentNotification(
    val id: Long,
    val amount: Double,
    val currency: String,
    val sellerId: Int,
    val sender: String,
    val transactionId: String,
    val status: String,
    val timestamp: Long
)

// Modelos para notificaciones del vendedor
@Serializable
data class SellerNotificationsResponse(
    val success: Boolean,
    val message: String,
    val data: SellerNotificationsData? = null,
    val error: Boolean = false
)

@Serializable
data class SellerNotificationsData(
    val notifications: List<SellerNotification>,
    val pagination: PaginationInfo,
    val unreadCount: Int
)

@Serializable
data class SellerNotification(
    val id: Int,
    val type: String, // "PAYMENT", "SYSTEM", "ALERT"
    val title: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: String,
    val readAt: String? = null,
    val data: NotificationDataPayload? = null
)

@Serializable
data class NotificationDataPayload(
    val paymentId: Int? = null,
    val amount: Double? = null,
    val senderName: String? = null,
    val yapeCode: String? = null
)

// Modelos para marcar notificación como leída
@Serializable
data class MarkNotificationReadResponse(
    val success: Boolean,
    val message: String,
    val data: MarkNotificationReadData? = null,
    val error: Boolean = false
)

@Serializable
data class MarkNotificationReadData(
    val notificationId: Int,
    val readAt: String,
    val unreadCount: Int
)
