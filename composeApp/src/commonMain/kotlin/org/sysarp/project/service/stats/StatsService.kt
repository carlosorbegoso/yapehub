package org.sysarp.project.service.stats

import org.sysarp.project.data.AdminStatsResponse
import org.sysarp.project.data.AnalyticsConfigs
import org.sysarp.project.data.AnalyticsParams
import org.sysarp.project.data.AnalyticsResponse
import org.sysarp.project.data.FinancialAnalysisResponse
import org.sysarp.project.data.PaymentTransparencyResponse
import org.sysarp.project.data.QuickSummaryResponse
import org.sysarp.project.data.SellerFinancialAnalysisParams
import org.sysarp.project.data.SellerFinancialAnalysisResponse
import org.sysarp.project.data.SellerStatsResponse
import org.sysarp.project.service.http.StatsApiClient

class StatsService(
    private val statsApiClient: StatsApiClient
) {
    
    suspend fun getAdminStatsSummary(
        adminId: Int,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<AdminStatsResponse> {
        return try {

            val result = statsApiClient.getAdminStatsSummary(adminId, startDate, endDate, token)

            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSellerStatsSummary(
        sellerId: Int,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<SellerStatsResponse> {
        return try {

            val result = statsApiClient.getSellerStatsSummary(sellerId, startDate, endDate, token)

            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getAdminDashboard(
        adminId: Int,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<QuickSummaryResponse> {
        return try {

            val result = statsApiClient.getAdminDashboard(adminId, startDate, endDate, token)

            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAnalytics(
        adminId: Int,
        startDate: String? = null,
        endDate: String? = null,
        include: String? = null,
        period: String? = null,
        metric: String? = null,
        confidence: Double? = null,
        days: Int? = null,
        token: String
    ): Result<AnalyticsResponse> {
        return try {

            val result = statsApiClient.getAnalytics(adminId, startDate, endDate, include, period, metric, confidence, days, token)

            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSellerAnalytics(
        sellerId: Int,
        startDate: String? = null,
        endDate: String? = null,
        include: String? = null,
        period: String? = null,
        metric: String? = null,
        confidence: Double? = null,
        days: Int? = null,
        token: String
    ): Result<AnalyticsResponse> {
        return try {

            val result = statsApiClient.getSellerAnalytics(sellerId, startDate, endDate, include, period, metric, confidence, days, token)

            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAnalytics(
        adminId: Int,
        startDate: String? = null,
        endDate: String? = null,
        analyticsParams: AnalyticsParams,
        token: String
    ): Result<AnalyticsResponse> {
        return getAnalytics(
            adminId = adminId,
            startDate = startDate,
            endDate = endDate,
            include = analyticsParams.include,
            period = analyticsParams.period,
            metric = analyticsParams.metric,
            confidence = analyticsParams.confidence,
            days = analyticsParams.days,
            token = token
        )
    }

    suspend fun getSellerAnalytics(
        sellerId: Int,
        startDate: String? = null,
        endDate: String? = null,
        analyticsParams: AnalyticsParams,
        token: String
    ): Result<AnalyticsResponse> {
        return getSellerAnalytics(
            sellerId = sellerId,
            startDate = startDate,
            endDate = endDate,
            include = analyticsParams.include,
            period = analyticsParams.period,
            metric = analyticsParams.metric,
            confidence = analyticsParams.confidence,
            days = analyticsParams.days,
            token = token
        )
    }

    suspend fun getQuickAnalytics(
        adminId: Int,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<AnalyticsResponse> {
        return getAnalytics(
            adminId = adminId,
            startDate = startDate,
            endDate = endDate,
            analyticsParams = AnalyticsConfigs.QUICK_ANALYSIS.toParams(),
            token = token
        )
    }

    suspend fun getSellerQuickAnalytics(
        sellerId: Int,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<AnalyticsResponse> {
        return getSellerAnalytics(
            sellerId = sellerId,
            startDate = startDate,
            endDate = endDate,
            analyticsParams = AnalyticsConfigs.QUICK_ANALYSIS.toParams(),
            token = token
        )
    }

    suspend fun getSellerPerformanceAnalytics(
        sellerId: Int,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<AnalyticsResponse> {
        return getSellerAnalytics(
            sellerId = sellerId,
            startDate = startDate,
            endDate = endDate,
            analyticsParams = AnalyticsConfigs.PERFORMANCE_ANALYSIS.toParams(),
            token = token
        )
    }

    // API de Análisis Financiero
    suspend fun getFinancialAnalysis(
        adminId: Int,
        startDate: String? = null,
        endDate: String? = null,
        include: String? = null,
        currency: String? = null,
        taxRate: Double? = null,
        token: String
    ): Result<FinancialAnalysisResponse> {
        return try {

            val result = statsApiClient.getFinancialAnalysis(adminId, startDate, endDate, include, currency, taxRate, token)

            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // API de Transparencia de Pagos
    suspend fun getPaymentTransparency(
        adminId: Int,
        startDate: String? = null,
        endDate: String? = null,
        includeFees: Boolean? = null,
        includeTaxes: Boolean? = null,
        includeCommissions: Boolean? = null,
        token: String
    ): Result<PaymentTransparencyResponse> {
        return try {

            val result = statsApiClient.getPaymentTransparency(adminId, startDate, endDate, includeFees, includeTaxes, includeCommissions, token)

            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // API de Análisis Financiero de Vendedores
    suspend fun getSellerFinancialAnalysis(
        sellerId: Int,
        startDate: String? = null,
        endDate: String? = null,
        include: String? = null,
        currency: String? = null,
        commissionRate: Double? = null,
        token: String
    ): Result<SellerFinancialAnalysisResponse> {
        return try {

            val result = statsApiClient.getSellerFinancialAnalysis(sellerId, startDate, endDate, include, currency, commissionRate, token)

            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSellerFinancialAnalysis(
        sellerId: Int,
        startDate: String? = null,
        endDate: String? = null,
        sellerFinancialParams: SellerFinancialAnalysisParams,
        token: String
    ): Result<SellerFinancialAnalysisResponse> {
        return getSellerFinancialAnalysis(
            sellerId = sellerId,
            startDate = startDate,
            endDate = endDate,
            include = sellerFinancialParams.include,
            currency = sellerFinancialParams.currency,
            commissionRate = sellerFinancialParams.commissionRate,
            token = token
        )
    }

}
