package org.sysarp.project.ui.screens.admin

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sysarp.project.data.FinancialAnalysisParams
import org.sysarp.project.data.PaymentTransparencyParams
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.utils.Logger

/**
 * Componente simplificado para los dialogs del AdminAnalyticsScreen
 */
@Composable
fun AdminAnalyticsDialogs(
    state: AdminAnalyticsState,
    authService: AuthService,
    statsService: StatsService,
    coroutineScope: CoroutineScope,
    onDismissFiltersDialog: () -> Unit,
    onDismissFinancialDialog: () -> Unit,
    onDismissTransparencyDialog: () -> Unit,
    onShowBasicChartsChange: (Boolean) -> Unit,
    onShowAdvancedChartsChange: (Boolean) -> Unit,
    onShowPredictiveChartsChange: (Boolean) -> Unit,
    onShowAdditionalMetricsChange: (Boolean) -> Unit,
    onFinancialDataLoaded: (org.sysarp.project.data.FinancialAnalysisData) -> Unit,
    onFinancialError: (String) -> Unit,
    onTransparencyDataLoaded: (org.sysarp.project.data.PaymentTransparencyData) -> Unit,
    onTransparencyError: (String) -> Unit
) {
    // Diálogo de filtros de sección
    if (state.showFiltersDialog) {
        AdminSectionFiltersDialog(
            showBasicCharts = state.showBasicCharts,
            showAdvancedCharts = state.showAdvancedCharts,
            showPredictiveCharts = state.showPredictiveCharts,
            showAdditionalMetrics = state.showAdditionalMetrics,
            onShowBasicChartsChange = onShowBasicChartsChange,
            onShowAdvancedChartsChange = onShowAdvancedChartsChange,
            onShowPredictiveChartsChange = onShowPredictiveChartsChange,
            onShowAdditionalMetricsChange = onShowAdditionalMetricsChange,
            onDismiss = onDismissFiltersDialog
        )
    }
    
    // Diálogo de filtros financieros específico
    if (state.showFinancialFiltersDialog) {
        AdminFinancialFilterDialog(
            isVisible = state.showFinancialFiltersDialog,
            onDismiss = onDismissFinancialDialog,
            onApply = { params ->
                onDismissFinancialDialog()
                // Cargar análisis financiero con parámetros seleccionados
                val userProfile = authService.userProfile.value
                val accessToken = authService.accessToken.value
                val adminId = userProfile?.adminId?.toIntOrNull()
                
                if (adminId != null && accessToken != null) {
                    coroutineScope.launch {
                        try {
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
                                    onFinancialDataLoaded(response.data)
                                    Logger.auth("ADMIN_FINANCIAL", "💰 Análisis financiero cargado")
                                },
                                onFailure = { error ->
                                    onFinancialError(error.message ?: "Error cargando análisis financiero")
                                    Logger.auth("ADMIN_FINANCIAL", "❌ Error cargando análisis financiero: ${error.message}")
                                }
                            )
                        } catch (e: Exception) {
                            onFinancialError(e.message ?: "Error inesperado")
                            Logger.auth("ADMIN_FINANCIAL", "❌ Error inesperado: ${e.message}")
                        }
                    }
                }
            }
        )
    }
    
    // Diálogo de filtros de transparencia
    if (state.showTransparencyFiltersDialog) {
        AdminTransparencyFilterDialog(
            isVisible = state.showTransparencyFiltersDialog,
            onDismiss = onDismissTransparencyDialog,
            onApply = { params ->
                onDismissTransparencyDialog()
                // Cargar datos de transparencia con parámetros por defecto
                val userProfile = authService.userProfile.value
                val accessToken = authService.accessToken.value
                val adminId = userProfile?.adminId?.toIntOrNull()
                
                if (adminId != null && accessToken != null) {
                    coroutineScope.launch {
                        try {
                            statsService.getPaymentTransparency(
                                adminId = adminId,
                                startDate = null,
                                endDate = null,
                                includeFees = true,
                                includeTaxes = true,
                                includeCommissions = true,
                                token = accessToken
                            ).fold(
                                onSuccess = { response ->
                                    onTransparencyDataLoaded(response.data)
                                    Logger.auth("ADMIN_TRANSPARENCY", "🔍 Datos de transparencia cargados")
                                },
                                onFailure = { error ->
                                    onTransparencyError(error.message ?: "Error cargando datos de transparencia")
                                    Logger.auth("ADMIN_TRANSPARENCY", "❌ Error cargando datos de transparencia: ${error.message}")
                                }
                            )
                        } catch (e: Exception) {
                            onTransparencyError(e.message ?: "Error inesperado")
                            Logger.auth("ADMIN_TRANSPARENCY", "❌ Error inesperado: ${e.message}")
                        }
                    }
                }
            }
        )
    }
}
