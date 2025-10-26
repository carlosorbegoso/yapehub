package org.sysarp.project.data

import kotlinx.serialization.Serializable



@Serializable
data class AdminStatsData(
    val overview: AdminOverviewData,
    val urls: StatsUrlsData,
    val topSellers: List<TopSellerData>,
    val performanceMetrics: PerformanceMetricsData,
    val userType: String,
    val userId: Int
)



@Serializable
data class SellerStatsData(
    val sellerId: Int? = null, // Campo opcional ya que no viene en la respuesta real
    val sellerName: String? = null, // Campo opcional ya que no viene en la respuesta real
    val period: StatsPeriod? = null, // Campo opcional ya que no viene en la respuesta real
    val summary: SellerSummary? = null, // Campo opcional ya que no viene en la respuesta real
    val dailyStats: List<SellerDailyStats>? = null, // Campo opcional ya que no viene en la respuesta real
    val performanceMetrics: PerformanceMetricsData, // ✅ Campo agregado para la respuesta real de la API
    val dailySales: List<DailySalesData>, // ✅ Campo agregado para la respuesta real de la API
    val overview: SellerOverviewSummaryData // ✅ Campo corregido para usar SellerOverviewSummaryData que coincide con la API
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
data class SellerDailyStats(
    val date: String,
    val totalSales: Double,
    val transactionCount: Int,
    val averageValue: Double,
    val pendingCount: Int,
    val confirmedCount: Int
)


@Serializable
data class QuickSummaryData(
    val confirmedSales: Double,     // Ventas confirmadas
    val allSales: Double,           // Todas las ventas (para admin)
    val totalTransactions: Int,
    val averageTransactionValue: Double,
    val salesGrowth: Double,        // +12.5%
    val transactionGrowth: Double,  // +8.2%
    val averageGrowth: Double,      // +3.1%
    val pendingPayments: Int,
    val confirmedPayments: Int,
    val rejectedPayments: Int,
    val claimRate: Double,          // 6.35%
    val averageConfirmationTime: Double,  // 2.3 minutos
    // Nuevos campos para mayor detalle
    val confirmedTransactions: Int,
    val pendingTransactions: Int,
    val rejectedTransactions: Int
) {

}



@Serializable
data class AnalyticsData(
    val overview: AnalyticsOverview,
    val dailySales: List<DailySalesData>,
    val topSellers: List<TopSellerData>? = null, // Hacer opcional ya que no está en la respuesta del vendedor
    val performanceMetrics: PerformanceMetricsData,
    // Datos avanzados para gráficos
    val hourlySales: List<HourlySalesData> = emptyList(),
    val weeklySales: List<WeeklySalesData>? = null,
    val monthlySales: List<MonthlySalesData>? = null,
    val sellerGoals: SellerGoalsData? = null,
    val sellerPerformance: SellerPerformanceData? = null,
    val sellerComparisons: SellerComparisonsData? = null,
    val sellerTrends: SellerTrendsData? = null,
    val sellerAchievements: SellerAchievementsData? = null,
    val sellerInsights: SellerInsightsData? = null,
    val sellerForecasting: SellerForecastingData? = null,
    val sellerAnalytics: SellerAnalyticsData? = null,
    // Nuevos datos administrativos
    val branchAnalytics: BranchAnalyticsData? = null,
    val sellerManagement: SellerManagementData? = null,
    val systemMetrics: SystemMetricsData? = null,
    val administrativeInsights: AdministrativeInsightsData? = null,
    val financialOverview: FinancialOverviewData? = null,
    val complianceAndSecurity: ComplianceAndSecurityData? = null
)

@Serializable
data class AnalyticsOverview(
    val totalSales: Double,
    val totalTransactions: Int,
    val averageTransactionValue: Double,
    val salesGrowth: Double,
    val transactionGrowth: Double,
    val averageGrowth: Double
)

@Serializable
data class DailySalesData(
    val date: String,
    val dayName: String,  // "MONDAY", "TUESDAY", etc. (en inglés)
    val sales: Double,
    val transactions: Int
)

@Serializable
data class TopSellerData(
    val rank: Int?,
    val sellerId: Int,
    val sellerName: String,
    val branchName: String,
    val totalSales: Double,
    val transactionCount: Int
)

@Serializable
data class PerformanceMetricsData(
    val averageConfirmationTime: Double,
    val claimRate: Double,
    val rejectionRate: Double,
    val pendingPayments: Int,
    val confirmedPayments: Int,
    val rejectedPayments: Int
)

// Modelos avanzados para gráficos
@Serializable
data class HourlySalesData(
    val hour: String,
    val sales: Double,
    val transactions: Int = 0  // Valor por defecto
)

@Serializable
data class WeeklySalesData(
    val week: String, // "2025-04-07" formato de fecha
    val sales: Double,
    val transactions: Int
)

@Serializable
data class MonthlySalesData(
    val month: String, // "2025-04" formato YYYY-MM
    val sales: Double,
    val transactions: Int
)

@Serializable
data class SellerGoalsData(
    val dailyTarget: Double,
    val weeklyTarget: Double,
    val monthlyTarget: Double,
    val yearlyTarget: Double,
    val achievementRate: Double,
    val dailyProgress: Double,
    val weeklyProgress: Double,
    val monthlyProgress: Double
)

@Serializable
data class SellerPerformanceData(
    val bestDay: String? = null,
    val worstDay: String? = null,
    val averageDailySales: Double,
    val consistencyScore: Double,
    val peakPerformanceHours: List<String>,
    val productivityScore: Double,
    val efficiencyRate: Double,
    val responseTime: Double
)

@Serializable
data class SellerComparisonsData(
    val vsPreviousWeek: ComparisonData,
    val vsPreviousMonth: ComparisonData,
    val vsPersonalBest: ComparisonData,
    val vsAverage: ComparisonData
)

@Serializable
data class ComparisonData(
    val salesChange: Double,
    val transactionChange: Int,
    val percentageChange: Double
)

@Serializable
data class SellerTrendsData(
    val salesTrend: String,
    val transactionTrend: String,
    val growthRate: Double,
    val momentum: String,
    val trendDirection: String,
    val volatility: Double,
    val seasonality: String
)

@Serializable
data class SellerAchievementsData(
    val streakDays: Int,
    val bestStreak: Int,
    val totalStreaks: Int,
    val milestones: List<MilestoneData>,
    val badges: List<BadgeData>
)

@Serializable
data class MilestoneData(
    val type: String,
    val date: String,
    val achieved: Boolean,
    val value: Double
)

@Serializable
data class BadgeData(
    val name: String,
    val icon: String,
    val description: String,
    val earned: Boolean,
    val date: String
)

@Serializable
data class SellerInsightsData(
    val peakPerformanceDay: String? = null,
    val peakPerformanceHour: String? = null,
    val averageTransactionValue: Double,
    val customerRetentionRate: Double,
    val repeatCustomerRate: Double,
    val newCustomerRate: Double,
    val conversionRate: Double,
    val satisfactionScore: Double
)

@Serializable
data class SellerForecastingData(
    val predictedSales: List<PredictedSalesData>,
    val trendAnalysis: TrendAnalysisData,
    val recommendations: List<String>
)

@Serializable
data class PredictedSalesData(
    val date: String,
    val predicted: Double,
    val confidence: Double
)

@Serializable
data class TrendAnalysisData(
    val trend: String,
    val slope: Double,
    val r2: Double,
    val forecastAccuracy: Double
)

@Serializable
data class SellerAnalyticsData(
    val salesDistribution: SalesDistributionData,
    val transactionPatterns: TransactionPatternsData,
    val performanceIndicators: PerformanceIndicatorsData
)

@Serializable
data class SalesDistributionData(
    val weekday: Double,
    val weekend: Double,
    val morning: Double,
    val afternoon: Double,
    val evening: Double
)

@Serializable
data class TransactionPatternsData(
    val averageTransactionsPerDay: Double,
    val mostActiveDay: String,
    val mostActiveHour: String,
    val transactionFrequency: String
)

@Serializable
data class PerformanceIndicatorsData(
    val salesVelocity: Double,
    val transactionVelocity: Double,
    val efficiencyIndex: Double,
    val consistencyIndex: Double
)

// Nuevos modelos para Analytics Administrativos
@Serializable
data class BranchAnalyticsData(
    val branchPerformance: List<BranchPerformanceData>,
    val branchComparison: BranchComparisonData
)

@Serializable
data class BranchPerformanceData(
    val branchId: Int,
    val branchName: String,
    val branchLocation: String? = null, // Nuevo campo observado en la respuesta
    val totalSales: Double,
    val totalTransactions: Int,
    val activeSellers: Int,
    val inactiveSellers: Int,
    val averageSalesPerSeller: Double,
    val performanceScore: Double,
    val lastActivity: String // ISO formato "2025-10-03T01:34:45.819403"
)

@Serializable
data class BranchComparisonData(
    val topPerformingBranch: BranchSummaryData,
    val lowestPerformingBranch: BranchSummaryData,
    val averagePerformance: BranchAverageData // Cambio de nombre según respuesta real
)

@Serializable
data class BranchSummaryData(
    val branchName: String, // Sin branchId en la respuesta real
    val totalSales: Double, // Renombrado desde 'sales'
    val totalTransactions: Int, // Nuevo campo
    val performanceScore: Double // Nuevo campo
)

@Serializable
data class BranchAverageData(
    val averageSales: Double, // Renombrado según respuesta real
    val averageTransactions: Double, // Renombrado y tipo cambiado
    val averagePerformanceScore: Double // Nuevo campo
)

@Serializable
data class SellerManagementData(
    val sellerOverview: SellerOverviewData,
    val sellerPerformanceDistribution: SellerPerformanceDistributionData,
    val sellerActivity: SellerActivityData
)

@Serializable
data class AdminOverviewData(
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

@Serializable
data class StatsUrlsData(
    val monthlySales: String,
    val hourlySales: String,
    val topSellers: String,
    val completeAnalytics: String,
    val weeklySales: String,
    val dailySales: String,
    val performanceDetails: String
)

@Serializable
data class SellerOverviewSummaryData(
    val totalSales: Double,
    val totalTransactions: Int,
    val averageTransactionValue: Double,
    val salesGrowth: Double,
    val transactionGrowth: Double,
    val averageGrowth: Double
)

@Serializable
data class SellerOverviewData(
    val totalSellers: Int,
    val activeSellers: Int,
    val inactiveSellers: Int,
    val newSellersThisMonth: Int,
    val sellersWithZeroSales: Int,
    val topPerformers: Int,
    val underPerformers: Int
)

@Serializable
data class SellerPerformanceDistributionData(
    val excellent: Int,
    val good: Int,
    val average: Int,
    val poor: Int
)

@Serializable
data class SellerActivityData(
    val dailyActiveSellers: Int,
    val weeklyActiveSellers: Int,
    val monthlyActiveSellers: Int,
    val averageSessionDuration: Double,
    val averageTransactionsPerSeller: Double
)

@Serializable
data class SystemMetricsData(
    val overallSystemHealth: OverallSystemHealthData,
    val paymentSystemMetrics: PaymentSystemMetricsData,
    val userEngagement: UserEngagementData
)

@Serializable
data class OverallSystemHealthData(
    val totalSystemSales: Double,
    val totalSystemTransactions: Int,
    val systemUptime: Double,
    val averageResponseTime: Double,
    val errorRate: Double,
    val activeUsers: Int
)

@Serializable
data class PaymentSystemMetricsData(
    val totalPaymentsProcessed: Int,
    val pendingPayments: Int,
    val confirmedPayments: Int,
    val rejectedPayments: Int,
    val averageConfirmationTime: Double,
    val paymentSuccessRate: Double
)

@Serializable
data class UserEngagementData(
    val dailyActiveUsers: Int,
    val weeklyActiveUsers: Int,
    val monthlyActiveUsers: Int,
    val averageSessionDuration: Double,
    val featureUsage: FeatureUsageData
)

@Serializable
data class FeatureUsageData(
    val qrScannerUsage: Double,
    val paymentManagementUsage: Double,
    val analyticsUsage: Double,
    val notificationsUsage: Double
)

@Serializable
data class AdministrativeInsightsData(
    val managementAlerts: List<ManagementAlertData>,
    val recommendations: List<String>,
    val growthOpportunities: GrowthOpportunitiesData
)

@Serializable
data class ManagementAlertData(
    val type: String,
    val severity: String, // Cambiado: puede ser "Meta semanal alcanzada", "Alto volumen de transacciones"
    val message: String,
    val affectedBranch: String,
    val affectedSellers: List<String>,
    val recommendation: String // Puede ser fecha o texto
)

@Serializable
data class GrowthOpportunitiesData(
    val potentialNewBranches: Int,
    val marketExpansion: String,
    val sellerRecruitment: Int,
    val revenueProjection: Double
)

@Serializable
data class FinancialOverviewData(
    val revenueBreakdown: RevenueBreakdownData,
    val costAnalysis: CostAnalysisData
)

@Serializable
data class RevenueBreakdownData(
    val totalRevenue: Double,
    val revenueByBranch: List<RevenueByBranchData>,
    val revenueGrowth: RevenueGrowthData
)

@Serializable
data class RevenueByBranchData(
    val branchId: Int,
    val branchName: String,
    val revenue: Double, // Mantener revenue según respuesta real
    val percentage: Double
)

@Serializable
data class RevenueGrowthData(
    val daily: Double,
    val weekly: Double,
    val monthly: Double,
    val yearly: Double
)

@Serializable
data class CostAnalysisData(
    val operationalCosts: Double,
    val sellerCommissions: Double,
    val systemMaintenance: Double,
    val netProfit: Double,
    val profitMargin: Double
)

@Serializable
data class ComplianceAndSecurityData(
    val securityMetrics: SecurityMetricsData,
    val complianceStatus: ComplianceStatusData
)

@Serializable
data class SecurityMetricsData(
    val failedLoginAttempts: Int,
    val suspiciousActivities: Int,
    val dataBreaches: Int,
    val securityScore: Double
)

@Serializable
data class ComplianceStatusData(
    val dataProtection: String,
    val auditTrail: String,
    val backupStatus: String,
    val lastAudit: String
)




