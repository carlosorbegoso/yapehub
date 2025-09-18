package org.sysarp.project.ui.components.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.utils.Logger

/**
 * Manager para manejar la lógica de estadísticas del vendedor
 */
class SellerStatsManager(
    private val statsService: StatsService
) {
    
    /**
     * Cargar estadísticas del vendedor
     */
    suspend fun loadSellerStats(
        accessToken: String,
        sellerId: Long,
        onSuccess: (Int, Double) -> Unit, // (confirmedPaymentsCount, totalAmountCollected)
        onError: (String) -> Unit
    ) {
        try {
            Logger.auth("STATS_MANAGER", "Cargando estadísticas para vendedor $sellerId")
            val response = statsService.getSellerStatsSummary(sellerId.toInt(), accessToken)
            
            response.fold(
                onSuccess = { result ->
                    val summary = result.data.summary
                    val confirmedCount = summary.confirmedPayments
                    val totalAmount = summary.totalSales
                    
                    Logger.auth("STATS_MANAGER", "Estadísticas cargadas: $confirmedCount pagos confirmados, S/ $totalAmount")
                    onSuccess(confirmedCount, totalAmount)
                },
                onFailure = { error ->
                    Logger.auth("STATS_MANAGER", "Error al cargar estadísticas: ${error.message}")
                    onError(error.message ?: "Error al cargar estadísticas")
                }
            )
        } catch (e: Exception) {
            Logger.auth("STATS_MANAGER", "Excepción al cargar estadísticas: ${e.message}")
            onError("Error de conexión: ${e.message}")
        }
    }
    
    /**
     * Cargar analytics detallados del vendedor
     */
    suspend fun loadSellerAnalytics(
        accessToken: String,
        sellerId: Long,
        onSuccess: (org.sysarp.project.data.AnalyticsResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            Logger.auth("STATS_MANAGER", "Cargando analytics detallados para vendedor $sellerId")
            val response = statsService.getSellerAnalytics(
                sellerId = sellerId.toInt(),
                startDate = null, // Usar fechas por defecto
                endDate = null,
                token = accessToken
            )
            
            response.fold(
                onSuccess = { result ->
                    Logger.auth("STATS_MANAGER", "Analytics detallados cargados exitosamente")
                    onSuccess(result)
                },
                onFailure = { error ->
                    Logger.auth("STATS_MANAGER", "Error al cargar analytics: ${error.message}")
                    onError(error.message ?: "Error al cargar analytics")
                }
            )
        } catch (e: Exception) {
            Logger.auth("STATS_MANAGER", "Excepción al cargar analytics: ${e.message}")
            onError("Error de conexión: ${e.message}")
        }
    }
}

/**
 * Composable para crear el manager de estadísticas
 */
@Composable
fun rememberSellerStatsManager(
    statsService: StatsService
): SellerStatsManager {
    return remember(statsService) {
        SellerStatsManager(statsService)
    }
}
