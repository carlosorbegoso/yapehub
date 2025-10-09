package org.sysarp.project.ui.screens.admin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.sysarp.project.data.AnalyticsData
import org.sysarp.project.data.FinancialAnalysisData
import org.sysarp.project.data.PaymentTransparencyData
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

/**
 * Funciones para cargar datos en AdminAnalyticsScreen
 */@OptIn(ExperimentalTime::class)
class AdminAnalyticsDataLoader(
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
            
            if (userProfile?.adminId == null || accessToken == null) {
                onError("No se pudo obtener la información del administrador")
                return@launch
            }
            
            onLoadingChange(true)
            
            try {
                val adminId = userProfile.adminId.toIntOrNull()
                if (adminId == null) {
                    onError("ID de administrador inválido")
                    return@launch
                }
                
                statsService.getQuickAnalytics(
                    adminId = adminId,
                    startDate = startDate,
                    endDate = endDate,
                    token = accessToken
                ).fold(
                    onSuccess = { response ->
                        onDataLoaded(response.data)
                        onLoadingChange(false)
                    },
                    onFailure = { error ->
                        onError(error.message ?: "Error cargando analytics administrativos")
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
    
    /**
     * Carga los datos financieros del administrador
     */
    fun loadFinancialData(
        params: org.sysarp.project.data.FinancialAnalysisParams,
        onLoadingChange: (Boolean) -> Unit,
        onDataLoaded: (FinancialAnalysisData) -> Unit,
        onError: (String) -> Unit
    ) {
        coroutineScope.launch {
            val userProfile = authService.userProfile.value
            val accessToken = authService.accessToken.value
            
            if (userProfile?.adminId == null || accessToken == null) {
                onError("No se pudo obtener la información del administrador")
                return@launch
            }
            
            onLoadingChange(true)
            
            try {
                val adminId = userProfile.adminId.toIntOrNull()
                if (adminId == null) {
                    onError("ID de administrador inválido")
                    return@launch
                }
                
                statsService.getFinancialAnalysis(
                    adminId = adminId,
                    startDate = null,
                    endDate = null,
                    include = params.include,
                    currency = params.currency,
                    taxRate = params.taxRate,
                    token = accessToken
                ).fold(
                    onSuccess = { response ->
                        onDataLoaded(response.data)
                        onLoadingChange(false)
                    },
                    onFailure = { error ->
                        onError(error.message ?: "Error cargando datos financieros")
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
     * Carga los datos de transparencia de pagos
     */
    fun loadTransparencyData(
        params: org.sysarp.project.data.PaymentTransparencyParams,
        onLoadingChange: (Boolean) -> Unit,
        onDataLoaded: (PaymentTransparencyData) -> Unit,
        onError: (String) -> Unit
    ) {
        coroutineScope.launch {
            val userProfile = authService.userProfile.value
            val accessToken = authService.accessToken.value
            
            if (userProfile?.adminId == null || accessToken == null) {
                onError("No se pudo obtener la información del administrador")
                return@launch
            }
            
            onLoadingChange(true)
            
            try {
                val adminId = userProfile.adminId.toIntOrNull()
                if (adminId == null) {
                    onError("ID de administrador inválido")
                    return@launch
                }
                
                statsService.getPaymentTransparency(
                    adminId = adminId,
                    startDate = null,
                    endDate = null,
                    includeFees = params.includeFees,
                    includeTaxes = params.includeTaxes,
                    includeCommissions = params.includeCommissions,
                    token = accessToken
                ).fold(
                    onSuccess = { response ->
                        onDataLoaded(response.data)
                        onLoadingChange(false)
                    },
                    onFailure = { error ->
                        onError(error.message ?: "Error cargando datos de transparencia")
                        onLoadingChange(false)
                    }
                )
            } catch (e: Exception) {
                onError(e.message ?: "Error inesperado")
                onLoadingChange(false)
            }
        }
    }
}
