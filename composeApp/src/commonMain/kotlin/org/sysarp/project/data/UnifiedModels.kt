package org.sysarp.project.data

import kotlinx.serialization.Serializable


@Serializable
data class UnifiedStatsResponse(
    val success: Boolean,
    val message: String,
    val data: UnifiedStatsData,
    val error: Boolean = false
)

@Serializable
data class UnifiedStatsData(
    val overview: UnifiedOverviewData,
    val urls: UnifiedAnalyticsUrls,
    val performanceMetrics: UnifiedPerformanceMetricsData,
    val userType: String,
    val userId: Int,
    val topSellers: List<UnifiedTopSellerData>? = null, // Campo opcional para admin
    val dailySales: List<UnifiedDailySalesData>? = null // Campo opcional que puede venir en la respuesta
)

/**
 * Datos de resumen unificado - Updated to match new API response
 */
@Serializable
data class UnifiedOverviewData(
    val confirmedSales: Double,
    val totalTransactions: Int,
    val averageTransactionValue: Double,
    val salesGrowth: Double,
    val transactionGrowth: Double,
    val averageGrowth: Double,
    val allSales: Double,
    val confirmedTransactions: Int,
    val pendingTransactions: Int,
    val rejectedTransactions: Int
)

/**
 * URLs de analytics unificadas - Updated to match actual API response
 * topSellers is NOT a URL, it comes as direct data in the response
 */
@Serializable
data class UnifiedAnalyticsUrls(
    val hourlySales: String? = null,
    val weeklySales: String? = null,
    val dailySales: String? = null,
    val completeAnalytics: String? = null,
    val performanceDetails: String? = null,
    val monthlySales: String? = null
)

/**
 * Métricas de rendimiento unificadas
 */
@Serializable
data class UnifiedPerformanceMetricsData(
    val averageConfirmationTime: Double,
    val claimRate: Double,
    val rejectionRate: Double,
    val pendingPayments: Int,
    val confirmedPayments: Int,
    val rejectedPayments: Int
)

/**
 * Datos de ventas diarias unificadas
 */
@Serializable
data class UnifiedDailySalesData(
    val date: String,
    val dayName: String,
    val sales: Double,
    val transactions: Int
)

/**
 * Datos de top sellers unificado
 */
@Serializable
data class UnifiedTopSellerData(
    val rank: Int,
    val sellerId: Int,
    val sellerName: String,
    val branchName: String,
    val totalSales: Double,
    val transactionCount: Int
)


