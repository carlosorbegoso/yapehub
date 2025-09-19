package org.sysarp.project.ui.screens.seller

import androidx.compose.runtime.*
import org.sysarp.project.data.AnalyticsData
import org.sysarp.project.data.SellerFinancialAnalysisData

/**
 * Estado para SellerAnalyticsScreen
 */
data class SellerAnalyticsState(
    // Estados para los datos de analytics
    var analyticsData: AnalyticsData? = null,
    var isLoadingAnalytics: Boolean = false,
    var analyticsError: String = "",
    var selectedPeriod: String = "📅 7 días",
    var showPeriodMenu: Boolean = false,
    
    // Estados para datos financieros
    var sellerFinancialData: SellerFinancialAnalysisData? = null,
    var isLoadingFinancial: Boolean = false,
    var financialError: String = "",
    var showFinancialFiltersDialog: Boolean = false,
    
    // Estado para filtros de sección
    var showBasicCharts: Boolean = true,
    var showAdvancedCharts: Boolean = true,
    var showPredictiveCharts: Boolean = true,
    var showAdditionalMetrics: Boolean = true,
    var showFiltersDialog: Boolean = false
)

/**
 * Función para crear el estado inicial del SellerAnalyticsScreen
 */
@Composable
fun rememberSellerAnalyticsState(): SellerAnalyticsState {
    return remember {
        SellerAnalyticsState()
    }
}
