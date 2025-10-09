package org.sysarp.project.ui.screens.seller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.sysarp.project.data.AnalyticsData
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

/**
 * Funciones para cargar datos en SellerAnalyticsScreen
 */
@OptIn(ExperimentalTime::class)
class SellerAnalyticsDataLoader(
    private val authService: AuthService,
    private val statsService: StatsService,
    private val coroutineScope: CoroutineScope
) {
    
    /**
     * Carga los datos de analytics con filtros de fecha
     */
    fun loadAnalytics(
        startDate: String?,
        endDate: String?,
        onLoadingChange: (Boolean) -> Unit,
        onDataLoaded: (AnalyticsData) -> Unit,
        onError: (String) -> Unit
    ) {
        coroutineScope.launch {
            val userProfile = authService.userProfile.value
            val accessToken = authService.accessToken.value
            
            if (userProfile?.sellerId == null || accessToken == null) {
                onError("No se pudo obtener la información del usuario")
                return@launch
            }
            
            onLoadingChange(true)
            
            try {
                val sellerId = userProfile.sellerId.toIntOrNull()
                if (sellerId == null) {
                    onError("ID de vendedor inválido")
                    return@launch
                }
                
                statsService.getSellerQuickAnalytics(
                    sellerId = sellerId,
                    startDate = startDate,
                    endDate = endDate,
                    token = accessToken
                ).fold(
                    onSuccess = { response ->
                        onDataLoaded(response.data)
                        onLoadingChange(false)
                    },
                    onFailure = { error ->
                        onError(error.message ?: "Error cargando analytics")
                        onLoadingChange(false)
                    }
                )
            } catch (e: Exception) {
                onError(e.message ?: "Error inesperado")
                onLoadingChange(false)
            }
        }
    }
    
    /**
     * Carga los datos de analytics por defecto (últimos 7 días)
     */
    fun loadDefaultAnalytics(
        onLoadingChange: (Boolean) -> Unit,
        onDataLoaded: (AnalyticsData) -> Unit,
        onError: (String) -> Unit
    ) {
        // Usar fechas más amplias para incluir datos históricos
        val now = kotlin.time.Clock.System.now()
        val endDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        val startDate = now.minus(30.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        
        loadAnalytics(startDate, endDate, onLoadingChange, onDataLoaded, onError)
    }

}
