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
import org.sysarp.project.utils.Logger

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
            Logger.auth("STATS_SERVICE", "Obteniendo resumen de estadísticas del admin: $adminId")

            val result = statsApiClient.getAdminStatsSummary(adminId, startDate, endDate, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("STATS_SERVICE", "Resumen de estadísticas de admin obtenido exitosamente")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("STATS_SERVICE", "Error obteniendo resumen de estadísticas de admin: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("STATS_SERVICE", "Error obteniendo resumen de estadísticas de admin: ${e.message}")
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
            Logger.auth("STATS_SERVICE", "Obteniendo resumen de estadísticas del vendedor: $sellerId")

            val result = statsApiClient.getSellerStatsSummary(sellerId, startDate, endDate, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("STATS_SERVICE", "Resumen de estadísticas de vendedor obtenido exitosamente")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("STATS_SERVICE", "Error obteniendo resumen de estadísticas de vendedor: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("STATS_SERVICE", "Error obteniendo resumen de estadísticas de vendedor: ${e.message}")
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
            Logger.auth("STATS_SERVICE", "Obteniendo dashboard para admin: $adminId")

            val result = statsApiClient.getAdminDashboard(adminId, startDate, endDate, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("STATS_SERVICE", "Dashboard de admin obtenido exitosamente")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("STATS_SERVICE", "Error obteniendo dashboard de admin: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("STATS_SERVICE", "Error obteniendo dashboard de admin: ${e.message}")
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
            Logger.auth("STATS_SERVICE", "Obteniendo analytics completos para admin: $adminId con parámetros avanzados")

            val result = statsApiClient.getAnalytics(adminId, startDate, endDate, include, period, metric, confidence, days, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("STATS_SERVICE", "Analytics obtenidos exitosamente")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("STATS_SERVICE", "Error obteniendo analytics: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("STATS_SERVICE", "Error obteniendo analytics: ${e.message}")
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
            Logger.auth("STATS_SERVICE", "Obteniendo analytics completos para vendedor: $sellerId con parámetros avanzados")

            val result = statsApiClient.getSellerAnalytics(sellerId, startDate, endDate, include, period, metric, confidence, days, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("STATS_SERVICE", "Analytics de vendedor obtenidos exitosamente")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("STATS_SERVICE", "Error obteniendo analytics de vendedor: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("STATS_SERVICE", "Error obteniendo analytics de vendedor: ${e.message}")
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
            Logger.auth("STATS_SERVICE", "Obteniendo análisis financiero para admin: $adminId")

            val result = statsApiClient.getFinancialAnalysis(adminId, startDate, endDate, include, currency, taxRate, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("STATS_SERVICE", "Análisis financiero obtenido exitosamente")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("STATS_SERVICE", "Error obteniendo análisis financiero: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("STATS_SERVICE", "Error obteniendo análisis financiero: ${e.message}")
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
            Logger.auth("STATS_SERVICE", "Obteniendo transparencia de pagos para admin: $adminId")

            val result = statsApiClient.getPaymentTransparency(adminId, startDate, endDate, includeFees, includeTaxes, includeCommissions, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("STATS_SERVICE", "Transparencia de pagos obtenida exitosamente")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("STATS_SERVICE", "Error obteniendo transparencia de pagos: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("STATS_SERVICE", "Error obteniendo transparencia de pagos: ${e.message}")
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
            Logger.auth("STATS_SERVICE", "Obteniendo análisis financiero para vendedor: $sellerId")

            val result = statsApiClient.getSellerFinancialAnalysis(sellerId, startDate, endDate, include, currency, commissionRate, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("STATS_SERVICE", "Análisis financiero de vendedor obtenido exitosamente")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("STATS_SERVICE", "Error obteniendo análisis financiero de vendedor: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("STATS_SERVICE", "Error obteniendo análisis financiero de vendedor: ${e.message}")
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
