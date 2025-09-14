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

// Modelos para notificaciones de Yape (usar el de YapeNotification.kt)

@Serializable
data class YapeNotificationRequest(
    val adminId: Int,
    val encryptedNotification: String, // NOTA: Este campo contiene el texto original sin encriptar
    val deviceFingerprint: String,
    val timestamp: Long
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
