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
    val dailySales: List<UnifiedDailySalesData>? = null, // Campo opcional
    val userType: String,
    val userId: Int,
    val topSellers: List<UnifiedTopSellerData>? = null // Campo opcional para admin
)

/**
 * Datos de resumen unificado
 */
@Serializable
data class UnifiedOverviewData(
    val totalSales: Double,
    val totalTransactions: Int,
    val averageTransactionValue: Double,
    val salesGrowth: Double,
    val transactionGrowth: Double,
    val averageGrowth: Double
)

/**
 * URLs de analytics unificadas
 */
@Serializable
data class UnifiedAnalyticsUrls(
    val performanceDetails: String? = null, // Campo opcional
    val dailySales: String,
    val monthlySales: String,
    val topSellers: String? = null, // Campo opcional para admin
    val weeklySales: String? = null, // Campo opcional para admin
    val hourlySales: String? = null, // Campo opcional para admin
    val completeAnalytics: String? = null // Campo opcional para admin
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


