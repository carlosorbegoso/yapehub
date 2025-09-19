package org.sysarp.project.service.stats

import org.sysarp.project.data.AdminStatsResponse
import org.sysarp.project.data.AnalyticsResponse
import org.sysarp.project.data.QuickSummaryResponse
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
        token: String
    ): Result<AnalyticsResponse> {
        return try {
            Logger.auth("STATS_SERVICE", "Obteniendo analytics completos para admin: $adminId")

            val result = statsApiClient.getAnalytics(adminId, startDate, endDate, token)

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
        token: String
    ): Result<AnalyticsResponse> {
        return try {
            Logger.auth("STATS_SERVICE", "Obteniendo analytics completos para vendedor: $sellerId")

            val result = statsApiClient.getSellerAnalytics(sellerId, startDate, endDate, token)

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
}
