package org.sysarp.project.ui.screens.seller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.sysarp.project.data.AnalyticsData
import org.sysarp.project.data.SellerFinancialAnalysisData

/**
 * Estado para SellerAnalyticsScreen
 */
class SellerAnalyticsState {
    // Estados para los datos de analytics
    var analyticsData by mutableStateOf<AnalyticsData?>(null)
    var isLoadingAnalytics by mutableStateOf(false)
    var analyticsError by mutableStateOf("")
    var selectedPeriod by mutableStateOf("📅 7 días")
    var showPeriodMenu by mutableStateOf(false)
    
    // Estados para datos financieros
    var sellerFinancialData by mutableStateOf<SellerFinancialAnalysisData?>(null)
    var isLoadingFinancial by mutableStateOf(false)
    var financialError by mutableStateOf("")
    var showFinancialFiltersDialog by mutableStateOf(false)
    
    // Estado para filtros de sección
    var showBasicCharts by mutableStateOf(true)
    var showAdvancedCharts by mutableStateOf(true)
    var showPredictiveCharts by mutableStateOf(true)
    var showAdditionalMetrics by mutableStateOf(true)
    var showFiltersDialog by mutableStateOf(false)
}

/**
 * Función para crear el estado inicial del SellerAnalyticsScreen
 */
@Composable
fun rememberSellerAnalyticsState(): SellerAnalyticsState {
    return remember {
        SellerAnalyticsState()
    }
}
