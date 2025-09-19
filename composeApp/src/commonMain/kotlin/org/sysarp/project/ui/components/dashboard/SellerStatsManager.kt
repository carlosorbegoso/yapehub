package org.sysarp.project.ui.components.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.data.AnalyticsConfigs
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
            val response = statsService.getSellerStatsSummary(sellerId.toInt(), null, null, accessToken)
            
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

    /**
     * Cargar analytics detallados con filtros de fecha específicos
     */
    suspend fun loadSellerAnalyticsWithDates(
        accessToken: String,
        sellerId: Long,
        startDate: String?,
        endDate: String?,
        onSuccess: (org.sysarp.project.data.AnalyticsResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            Logger.auth("STATS_MANAGER", "Cargando analytics detallados para vendedor $sellerId con fechas: $startDate - $endDate")
            val response = statsService.getSellerQuickAnalytics(
                sellerId = sellerId.toInt(),
                startDate = startDate,
                endDate = endDate,
                token = accessToken
            )
            
            response.fold(
                onSuccess = { result ->
                    Logger.auth("STATS_MANAGER", "Analytics detallados con filtros cargados exitosamente")
                    onSuccess(result)
                },
                onFailure = { error ->
                    Logger.auth("STATS_MANAGER", "Error al cargar analytics con filtros: ${error.message}")
                    onError(error.message ?: "Error al cargar analytics")
                }
            )
        } catch (e: Exception) {
            Logger.auth("STATS_MANAGER", "Excepción al cargar analytics con filtros: ${e.message}")
            onError("Error de conexión: ${e.message}")
        }
    }

    /**
     * Cargar analytics de rendimiento del vendedor
     */
    suspend fun loadSellerPerformanceAnalytics(
        accessToken: String,
        sellerId: Long,
        startDate: String?,
        endDate: String?,
        onSuccess: (org.sysarp.project.data.AnalyticsResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            Logger.auth("STATS_MANAGER", "Cargando analytics de rendimiento para vendedor $sellerId")
            val response = statsService.getSellerPerformanceAnalytics(
                sellerId = sellerId.toInt(),
                startDate = startDate,
                endDate = endDate,
                token = accessToken
            )
            
            response.fold(
                onSuccess = { result ->
                    Logger.auth("STATS_MANAGER", "Analytics de rendimiento cargados exitosamente")
                    onSuccess(result)
                },
                onFailure = { error ->
                    Logger.auth("STATS_MANAGER", "Error al cargar analytics de rendimiento: ${error.message}")
                    onError(error.message ?: "Error al cargar analytics de rendimiento")
                }
            )
        } catch (e: Exception) {
            Logger.auth("STATS_MANAGER", "Excepción al cargar analytics de rendimiento: ${e.message}")
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
