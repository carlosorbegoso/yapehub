package org.sysarp.project.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class YapeTransaction(
    val id: Long = 0,
    val transactionId: String,
    val amount: Double,
    val currency: String = "PEN",
    val senderName: String? = null,
    val senderPhone: String? = null,
    val message: String? = null,
    val transactionType: TransactionType,
    val businessName: String? = null,
    @Contextual val createdAt: Instant,
    @Contextual val processedAt: Instant? = null,
    val isProcessed: Boolean = false,
    val rawNotification: String? = null,
    val securityCode: String? = null // Código de seguridad de Yape
)

@Serializable
enum class TransactionType {
    RECEIVED,
    SENT
}

@Serializable
data class BusinessReport(
    val businessName: String,
    val totalAmount: Double,
    val transactionCount: Int
)

@Serializable
data class DailyReport(
    val date: String,
    val businessName: String,
    val totalAmount: Double,
    val transactionCount: Int
)

