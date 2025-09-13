package org.sysarp.project.data

import kotlinx.serialization.Serializable

// Modelos para transacciones
@Serializable
data class Transaction(
    val id: String,
    val amount: Double,
    val currency: String,
    val timestamp: String,
    val description: String? = null,
    val type: String,
    val businessName: String? = null,
    val branchId: String? = null,
    val branchName: String? = null,
    val sellerId: String? = null,
    val sellerName: String? = null,
    val isProcessed: Boolean,
    val paymentMethod: String? = null,
    val customerPhone: String? = null
)

@Serializable
data class TransactionsResponse(
    val success: Boolean,
    val data: TransactionsData? = null
)

@Serializable
data class TransactionsData(
    val transactions: List<Transaction>,
    val pagination: Pagination
)

@Serializable
data class Pagination(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val itemsPerPage: Int
)

// Modelos para confirmación de transacciones
@Serializable
data class ConfirmTransactionRequest(
    val transactionId: String,
    val action: String // "confirm" o "reject"
)

@Serializable
data class ConfirmTransactionResponse(
    val success: Boolean,
    val message: String,
    val data: ConfirmTransactionData? = null
)

@Serializable
data class ConfirmTransactionData(
    val transactionId: String,
    val status: String,
    val processedAt: String
)
