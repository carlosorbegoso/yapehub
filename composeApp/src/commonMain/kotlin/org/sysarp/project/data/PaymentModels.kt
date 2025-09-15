package org.sysarp.project.data

import kotlinx.serialization.Serializable

@Serializable
data class PendingPaymentsResponse(
    val success: Boolean,
    val message: String,
    val data: PendingPaymentsData,
    val error: Boolean
)

@Serializable
data class PendingPaymentsData(
    val payments: List<SellerPendingPayment>,
    val pagination: PaymentPagination
)

@Serializable
data class PaymentPagination(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val itemsPerPage: Int
)

@Serializable
data class SellerPendingPayment(
    val paymentId: Int,
    val amount: Double,
    val senderName: String,
    val yapeCode: String,
    val status: String,
    val timestamp: String,
    val message: String
)

@Serializable
data class ClaimPaymentRequest(
    val sellerId: Int,
    val paymentId: Int
)

@Serializable
data class ClaimPaymentResponse(
    val success: Boolean,
    val message: String,
    val data: PaymentClaimData?,
    val error: Boolean
)

@Serializable
data class PaymentClaimData(
    val paymentId: Int,
    val sellerId: Int,
    val status: String,
    val claimedAt: String
)
