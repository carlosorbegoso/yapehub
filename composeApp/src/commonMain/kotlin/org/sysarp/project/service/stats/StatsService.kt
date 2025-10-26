package org.sysarp.project.service.stats

import org.sysarp.project.data.UnifiedStatsResponse
import org.sysarp.project.service.http.StatsApiClient

/**
 * Servicio de estadísticas - Solo endpoint unificado
 * Backend ha deprecado todas las APIs antiguas, solo queda /api/stats/summary
 */
class StatsService(
    private val statsApiClient: StatsApiClient
) {
    
    /**
     * Método unificado para obtener estadísticas tanto para ADMIN como para SELLER
     * Usa el único endpoint disponible: /api/stats/summary
     */
    suspend fun getUnifiedStatsSummary(
        adminId: Int? = null,
        sellerId: Int? = null,
        startDate: String? = null,
        endDate: String? = null,
        token: String
    ): Result<UnifiedStatsResponse> {
        return try {
            val result = statsApiClient.getUnifiedStatsSummary(adminId, sellerId, startDate, endDate, token)

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
    
    /**
     * Obtiene datos de analytics específicos usando las URLs proporcionadas por el endpoint unificado
     */
    suspend fun getAnalyticsFromUrl(
        url: String,
        token: String
    ): Result<String> {
        return try {
            val result = statsApiClient.getAnalyticsFromUrl(url, token)

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
}