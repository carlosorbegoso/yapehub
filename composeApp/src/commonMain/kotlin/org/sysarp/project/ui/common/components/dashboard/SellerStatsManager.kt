package org.sysarp.project.ui.components.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.sysarp.project.service.stats.StatsService

/**
 * Manager para manejar la lógica de estadísticas del vendedor
 * Actualizado para usar solo el endpoint unificado /api/stats/summary
 */
class SellerStatsManager(
    private val statsService: StatsService
) {
    
    /**
     * Cargar estadísticas del vendedor usando endpoint unificado
     */
    suspend fun loadSellerStats(
        accessToken: String,
        sellerId: Long,
        onSuccess: (Int, Double) -> Unit, // (confirmedPaymentsCount, totalAmountCollected)
        onError: (String) -> Unit
    ) {
        try {
            val response = statsService.getUnifiedStatsSummary(
                adminId = null,
                sellerId = sellerId.toInt(),
                startDate = null,
                endDate = null,
                token = accessToken
            )
            
            response.fold(
                onSuccess = { result ->
                    val confirmedCount = result.data.performanceMetrics.confirmedPayments
                    val totalAmount = result.data.overview.totalSales
                    
                    onSuccess(confirmedCount, totalAmount)
                },
                onFailure = { error ->
                    onError(error.message ?: "Error al cargar estadísticas")
                }
            )
        } catch (e: Exception) {
            onError("Error de conexión: ${e.message}")
        }
    }
    
    /**
     * Cargar analytics específicos usando URLs del endpoint unificado
     */
    suspend fun loadAnalyticsFromUrl(
        url: String,
        accessToken: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val response = statsService.getAnalyticsFromUrl(url, accessToken)
            
            response.fold(
                onSuccess = { result ->
                    onSuccess(result)
                },
                onFailure = { error ->
                    onError(error.message ?: "Error al cargar analytics")
                }
            )
        } catch (e: Exception) {
            onError("Error de conexión: ${e.message}")
        }
    }
}

/**
 * Composable para crear una instancia del SellerStatsManager
 */
@Composable
fun rememberSellerStatsManager(statsService: StatsService): SellerStatsManager {
    return remember { SellerStatsManager(statsService) }
}