package org.sysarp.project.dtos

import kotlinx.serialization.Serializable

// DTOs para el sistema de administradores

@Serializable
data class AdminRegistrationData(
    val adminId: Int,
    val businessId: Int,
    val email: String,
    val businessName: String,
    val verificationRequired: Boolean
)

@Serializable
data class AdminDashboardData(
    val totalSellers: Int,
    val activeSellers: Int,
    val totalTransactions: Int,
    val totalAmount: Double,
    val pendingPayments: Int,
    val confirmedPayments: Int,
    val recentTransactions: List<TransactionSummary>,
    val topSellers: List<SellerPerformance>
)

@Serializable
data class TransactionSummary(
    val id: String,
    val amount: Double,
    val sellerName: String,
    val timestamp: String,
    val status: String
)

@Serializable
data class SellerPerformance(
    val sellerId: Int,
    val sellerName: String,
    val totalTransactions: Int,
    val totalAmount: Double,
    val lastActivity: String
)

@Serializable
data class AnalyticsData(
    val period: String,
    val totalTransactions: Int,
    val totalAmount: Double,
    val averageTransaction: Double,
    val growthRate: Double,
    val topSellers: List<SellerPerformance>,
    val transactionTrends: List<TransactionTrend>
)

@Serializable
data class TransactionTrend(
    val date: String,
    val count: Int,
    val amount: Double
)
