package org.sysarp.project.viewmodel.admin

/**
 * Data class para las estadísticas del dashboard
 */
data class DashboardStats(
    val totalSellers: Int,
    val activeSellers: Int,
    val totalBranches: Int,
    val totalTransactions: Int,
    val totalRevenue: Double,
    val pendingPayments: Int
)
