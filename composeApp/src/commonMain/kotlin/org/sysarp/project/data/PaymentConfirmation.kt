package org.sysarp.project.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PaymentConfirmation(
    val id: Long,
    val transactionId: String,
    val amount: Double,
    val currency: String,
    val confirmedBy: String, // ID del vendedor que confirmó
    val confirmedAt: Instant,
    val vendorStore: String, // Tienda del vendedor
    val isConfirmed: Boolean = true
)

@Serializable
data class PendingPayment(
    val id: Long,
    val transactionId: String,
    val amount: Double,
    val currency: String,
    val createdAt: Instant,
    val businessName: String, // Solo el nombre del negocio, no datos del cliente
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
    val timestamp: Long
)
