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

// Modelos para Quick Summary endpoint
@Serializable
data class QuickSummaryResponse(
    val success: Boolean,
    val message: String,
    val data: QuickSummaryData,
    val error: Boolean
)

@Serializable
data class QuickSummaryData(
    val totalSales: Double,
    val totalTransactions: Int,
    val averageTransactionValue: Double,
    val salesGrowth: Double,        // +12.5%
    val transactionGrowth: Double,  // +8.2%
    val averageGrowth: Double,      // +3.1%
    val pendingPayments: Int,
    val confirmedPayments: Int,
    val rejectedPayments: Int,
    val claimRate: Double,          // 6.35%
    val averageConfirmationTime: Double  // 2.3 minutos
)

// Modelos para Analytics endpoint
@Serializable
data class AnalyticsResponse(
    val success: Boolean,
    val message: String,
    val data: AnalyticsData,
    val error: Boolean
)

@Serializable
data class AnalyticsData(
    val overview: AnalyticsOverview,
    val dailySales: List<DailySalesData>,
    val topSellers: List<TopSellerData>? = null, // Hacer opcional ya que no está en la respuesta del vendedor
    val performanceMetrics: PerformanceMetricsData,
    // Datos avanzados para gráficos
    val hourlySales: List<HourlySalesData>? = null,
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
    val dayName: String,  // "Lun", "Mar", "Mié", etc.
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
    val transactions: Int
)

@Serializable
data class WeeklySalesData(
    val week: String,
    val sales: Double,
    val transactions: Int
)

@Serializable
data class MonthlySalesData(
    val month: String,
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
    val branchCode: String,
    val totalSales: Double,
    val totalTransactions: Int,
    val activeSellers: Int,
    val inactiveSellers: Int,
    val averageSalesPerSeller: Double,
    val performanceScore: Double,
    val growthRate: Double,
    val lastActivity: String
)

@Serializable
data class BranchComparisonData(
    val topPerformingBranch: BranchSummaryData,
    val lowestPerformingBranch: BranchSummaryData,
    val averageBranchPerformance: BranchAverageData
)

@Serializable
data class BranchSummaryData(
    val branchId: Int,
    val branchName: String,
    val sales: Double,
    val growth: Double
)

@Serializable
data class BranchAverageData(
    val sales: Double,
    val transactions: Int,
    val sellers: Int
)

@Serializable
data class SellerManagementData(
    val sellerOverview: SellerOverviewData,
    val sellerPerformanceDistribution: SellerPerformanceDistributionData,
    val sellerActivity: SellerActivityData
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
    val qrScanner: Double,
    val paymentManagement: Double,
    val analytics: Double,
    val notifications: Double
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
    val severity: String,
    val message: String,
    val affectedBranch: String,
    val affectedSellers: List<String>,
    val recommendation: String
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
    val revenue: Double,
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
