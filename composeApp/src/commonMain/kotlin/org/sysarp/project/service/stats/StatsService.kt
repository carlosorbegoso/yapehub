package org.sysarp.project.service.stats

import org.sysarp.project.data.AdminStatsResponse
import org.sysarp.project.data.SellerStatsResponse
import org.sysarp.project.service.http.StatsApiClient
import org.sysarp.project.utils.Logger

class StatsService(
    private val statsApiClient: StatsApiClient
) {
    
    suspend fun getAdminStatsSummary(
        adminId: Int,
        token: String
    ): Result<AdminStatsResponse> {
        return try {
            Logger.auth("STATS_SERVICE", "Obteniendo resumen de estadísticas del admin: $adminId")

            val result = statsApiClient.getAdminStatsSummary(adminId, token)

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

    suspend fun getAdminStats(
        adminId: Int,
        startDate: String,
        endDate: String,
        token: String
    ): Result<AdminStatsResponse> {
        return try {
            Logger.auth("STATS_SERVICE", "Obteniendo estadísticas del admin: $adminId desde $startDate hasta $endDate")

            val result = statsApiClient.getAdminStats(adminId, startDate, endDate, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("STATS_SERVICE", "Estadísticas de admin obtenidas exitosamente")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("STATS_SERVICE", "Error obteniendo estadísticas de admin: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("STATS_SERVICE", "Error obteniendo estadísticas de admin: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getSellerStatsSummary(
        sellerId: Int,
        token: String
    ): Result<SellerStatsResponse> {
        return try {
            Logger.auth("STATS_SERVICE", "Obteniendo resumen de estadísticas del vendedor: $sellerId")

            val result = statsApiClient.getSellerStatsSummary(sellerId, token)

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
}
