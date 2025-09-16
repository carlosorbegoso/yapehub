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
    val amount: Double,
    val senderName: String,
    val yapeCode: String,
    val status: String,
    val timestamp: String,
    val message: String,
    val confirmedBy: Int,
    val confirmedAt: String
)

@Serializable
data class RejectPaymentRequest(
    val sellerId: Int,
    val paymentId: Int,
    val reason: String
)

@Serializable
data class RejectPaymentResponse(
    val success: Boolean,
    val message: String,
    val data: PaymentRejectData?,
    val error: Boolean
)

@Serializable
data class PaymentRejectData(
    val paymentId: Int,
    val amount: Double,
    val senderName: String,
    val yapeCode: String,
    val status: String,
    val timestamp: String,
    val message: String,
    val rejectedBy: Int,
    val rejectedAt: String,
    val rejectionReason: String
)

// Modelos para gestión de pagos del administrador
@Serializable
data class AdminPaymentManagementResponse(
    val success: Boolean,
    val message: String,
    val data: AdminPaymentManagementData,
    val error: Boolean
)

@Serializable
data class AdminPaymentManagementData(
    val payments: List<AdminPayment>,
    val summary: PaymentSummary,
    val pagination: PaymentPagination
)

@Serializable
data class AdminPayment(
    val paymentId: Int,
    val amount: Double,
    val senderName: String,
    val yapeCode: String,
    val status: String,
    val createdAt: String,
    val confirmedBy: Int?,
    val confirmedAt: String?,
    val rejectedBy: Int?,
    val rejectedAt: String?,
    val rejectionReason: String?,
    val sellerName: String,
    val branchName: String
)

@Serializable
data class PaymentSummary(
    val totalPayments: Int,
    val pendingCount: Int,
    val confirmedCount: Int,
    val rejectedCount: Int,
    val totalAmount: Double,
    val confirmedAmount: Double,
    val pendingAmount: Double
)

// Modelos para WebSocket
@Serializable
data class WebSocketMessage(
    val type: String,
    val data: WebSocketData
)

@Serializable
data class WebSocketData(
    val paymentId: Int,
    val amount: Double,
    val senderName: String,
    val yapeCode: String,
    val status: String,
    val timestamp: String,
    val message: String,
    val sellerId: Int? = null,
    val sellerName: String? = null
)

@Serializable
data class PaymentNotificationData(
    val paymentId: Int,
    val amount: Double,
    val senderName: String,
    val yapeCode: String,
    val status: String,
    val timestamp: String,
    val message: String
)

@Serializable
data class PaymentResultData(
    val paymentId: Int,
    val status: String,
    val message: String,
    val sellerId: Int,
    val sellerName: String
)

// Modelos para manejo de errores del servidor
@Serializable
data class ServerErrorResponse(
    val message: String,
    val code: String,
    val details: ErrorDetails,
    val timestamp: String
)
