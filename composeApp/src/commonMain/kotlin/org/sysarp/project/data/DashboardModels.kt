package org.sysarp.project.data

import kotlinx.serialization.Serializable

@Serializable
data class DashboardData(
    val summary: DashboardSummary,
    val dailyStats: List<DailyStat>,
    val topSellers: List<TopSeller>,
    val branchStats: List<BranchStat>
)

@Serializable
data class DashboardSummary(
    val totalTransactions: Int,
    val totalAmount: Double,
    val activeSellers: Int,
    val pendingTransactions: Int
)

@Serializable
data class DailyStat(
    val date: String,
    val transactions: Int,
    val amount: Double
)

@Serializable
data class TopSeller(
    val sellerId: String,
    val sellerName: String,
    val transactions: Int,
    val totalAmount: Double
)

@Serializable
data class BranchStat(
    val branchId: String,
    val branchName: String,
    val transactions: Int,
    val totalAmount: Double
)
