package org.sysarp.project.data

import kotlinx.serialization.Serializable

// Modelos para estadísticas de Admin
@Serializable
data class AdminStatsResponse(
    val success: Boolean,
    val message: String,
    val data: AdminStatsData,
    val error: Boolean
)

@Serializable
data class AdminStatsData(
    val period: StatsPeriod,
    val summary: AdminSummary,
    val dailyStats: List<DailyStats>,
    val sellerStats: List<SellerStats>
)

@Serializable
data class AdminSummary(
    val totalSales: Double,
    val totalTransactions: Int,
    val averageTransactionValue: Double,
    val pendingPayments: Int,
    val confirmedPayments: Int,
    val rejectedPayments: Int
)

// Modelos para estadísticas de Vendedor
@Serializable
data class SellerStatsResponse(
    val success: Boolean,
    val message: String,
    val data: SellerStatsData,
    val error: Boolean
)

@Serializable
data class SellerStatsData(
    val sellerId: Int,
    val sellerName: String,
    val period: StatsPeriod,
    val summary: SellerSummary,
    val dailyStats: List<SellerDailyStats>
)

@Serializable
data class SellerSummary(
    val totalSales: Double,
    val totalTransactions: Int,
    val averageTransactionValue: Double,
    val pendingPayments: Int,
    val confirmedPayments: Int,
    val rejectedPayments: Int,
    val claimRate: Double
)

// Modelos comunes
@Serializable
data class StatsPeriod(
    val startDate: String,
    val endDate: String,
    val totalDays: Int
)

@Serializable
data class DailyStats(
    val date: String,
    val totalSales: Double,
    val transactionCount: Int,
    val averageValue: Double
)

@Serializable
data class SellerDailyStats(
    val date: String,
    val totalSales: Double,
    val transactionCount: Int,
    val averageValue: Double,
    val pendingCount: Int,
    val confirmedCount: Int
)

@Serializable
data class SellerStats(
    val sellerId: Int,
    val sellerName: String,
    val totalSales: Double,
    val transactionCount: Int,
    val averageValue: Double,
    val pendingCount: Int
)
