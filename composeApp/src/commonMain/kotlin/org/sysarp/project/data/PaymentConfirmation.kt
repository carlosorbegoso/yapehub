package org.sysarp.project.data

import kotlinx.serialization.Serializable

// Using a typealias for clarity. This represents a timestamp as an ISO 8601 string.
// This avoids the need for @OptIn(ExperimentalTime::class)
typealias TimestampString = String

@Serializable
data class PaymentConfirmation(
    val id: Long,
    val transactionId: String,
    val amount: Double,
    val currency: String,
    val confirmedBy: String, // ID del vendedor que confirmó
    val confirmedAt: TimestampString, // Changed from Instant to String
    val vendorStore: String, // Tienda del vendedor
    val isConfirmed: Boolean = true
)

@Serializable
data class PendingPayment(
    val id: Long,
    val transactionId: String,
    val amount: Double,
    val currency: String,
    val createdAt: TimestampString, // Changed from Instant to String
    val businessName: String,
    val message: String? = null, // Mensaje del pago (sin datos personales)
    val isConfirmed: Boolean = false,
    val confirmedBy: String? = null,
    val securityCode: String? = null // Código de seguridad de Yape
)

@Serializable
data class YapePaymentResponse(
    val success: Boolean,
    val message: String,
    val data: YapePaymentData? = null
)

@Serializable
data class YapePaymentData(
    val paymentId: Int,
    val transactionId: String,
    val amount: Double,
    val currency: String,
    val sellerId: Int,
    val status: String,
    // This uses Long, which is also a valid alternative.
    // For consistency across the app, you could consider changing this to a String as well.
    val timestamp: Long 
)
