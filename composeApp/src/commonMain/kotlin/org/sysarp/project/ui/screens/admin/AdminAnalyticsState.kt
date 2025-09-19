package org.sysarp.project.ui.screens.admin

import androidx.compose.runtime.*
import org.sysarp.project.data.AnalyticsData
import org.sysarp.project.data.FinancialAnalysisData
import org.sysarp.project.data.PaymentTransparencyData

/**
 * Estado para AdminAnalyticsScreen
 */
data class AdminAnalyticsState(
    // Estados para los datos de analytics
    var analyticsData: AnalyticsData? = null,
    var isLoadingAnalytics: Boolean = false,
    var analyticsError: String = "",
    var selectedPeriod: String = "📅 7 días",
    var showPeriodMenu: Boolean = false,
    
    // Estados para datos financieros
    var financialData: FinancialAnalysisData? = null,
    var isLoadingFinancial: Boolean = false,
    var financialError: String = "",
    var showFinancialFiltersDialog: Boolean = false,
    
    // Estados para transparencia de pagos
    var transparencyData: PaymentTransparencyData? = null,
    var isLoadingTransparency: Boolean = false,
    var transparencyError: String = "",
    var showTransparencyFiltersDialog: Boolean = false,
    
    // Estado para filtros de sección
    var showBasicCharts: Boolean = true,
    var showAdvancedCharts: Boolean = true,
    var showPredictiveCharts: Boolean = true,
    var showAdditionalMetrics: Boolean = true,
    var showFiltersDialog: Boolean = false
)

/**
 * Función para crear el estado inicial del AdminAnalyticsScreen
 */
@Composable
fun rememberAdminAnalyticsState(): AdminAnalyticsState {
    return remember {
        AdminAnalyticsState()
    }
}
