package org.sysarp.project.ui.screens.seller

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sysarp.project.data.SellerFinancialAnalysisParams
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService
// Import removed - using local SellerSectionFiltersDialog
import org.sysarp.project.ui.components.financial.SellerFinancialFilterDialog
import org.sysarp.project.utils.Logger

/**
 * Componente para los dialogs del SellerAnalyticsScreen
 */
@Composable
fun SellerAnalyticsDialogs(
    state: SellerAnalyticsState,
    authService: AuthService,
    statsService: StatsService,
    coroutineScope: CoroutineScope,
    onDismissFiltersDialog: () -> Unit,
    onDismissFinancialDialog: () -> Unit,
    onShowBasicChartsChange: (Boolean) -> Unit,
    onShowAdvancedChartsChange: (Boolean) -> Unit,
    onShowPredictiveChartsChange: (Boolean) -> Unit,
    onShowAdditionalMetricsChange: (Boolean) -> Unit,
    onFinancialDataLoaded: (org.sysarp.project.data.SellerFinancialAnalysisData) -> Unit,
    onFinancialError: (String) -> Unit
) {
    // Diálogo de filtros de sección
    if (state.showFiltersDialog) {
        SellerSectionFiltersDialog(
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
    
    // Diálogo de filtros financieros
    if (state.showFinancialFiltersDialog) {
        SellerFinancialFilterDialog(
            isVisible = state.showFinancialFiltersDialog,
            onDismiss = onDismissFinancialDialog,
            onApply = { params ->
                onDismissFinancialDialog()
                // Cargar análisis financiero con los nuevos parámetros
                val userProfile = authService.userProfile.value
                val accessToken = authService.accessToken.value
                val sellerId = userProfile?.sellerId?.toIntOrNull()
                
                if (sellerId != null && accessToken != null) {
                    coroutineScope.launch {
                        try {
                            statsService.getSellerFinancialAnalysis(
                                sellerId = sellerId,
                                startDate = null, // Usar fechas por defecto
                                endDate = null,
                                include = params.include,
                                currency = params.currency,
                                commissionRate = params.commissionRate,
                                token = accessToken
                            ).fold(
                                onSuccess = { response ->
                                    onFinancialDataLoaded(response.data)
                                    Logger.auth("SELLER_FINANCIAL", "💰 Análisis financiero cargado: ${response.data.netEarnings}")
                                },
                                onFailure = { error ->
                                    onFinancialError(error.message ?: "Error cargando análisis financiero")
                                    Logger.auth("SELLER_FINANCIAL", "❌ Error cargando análisis financiero: ${error.message}")
                                }
                            )
                        } catch (e: Exception) {
                            onFinancialError(e.message ?: "Error inesperado")
                            Logger.auth("SELLER_FINANCIAL", "❌ Error inesperado: ${e.message}")
                        }
                    }
                }
            }
        )
    }
}
