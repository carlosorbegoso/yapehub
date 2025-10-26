package org.sysarp.project.ui.screens.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.sysarp.project.data.AnalyticsData
import org.sysarp.project.data.FinancialAnalysisData
import org.sysarp.project.data.PaymentTransparencyData

/**
 * Estado para AdminAnalyticsScreen
 */
class AdminAnalyticsState {
    // Estados para los datos de analytics
    var analyticsData by mutableStateOf<AnalyticsData?>(null)
    var isLoadingAnalytics by mutableStateOf(false)
    var analyticsError by mutableStateOf("")
    var selectedPeriod by mutableStateOf("📅 7 días")
    var showPeriodMenu by mutableStateOf(false)
    
    // Estados para datos financieros
    var financialData by mutableStateOf<FinancialAnalysisData?>(null)
    var isLoadingFinancial by mutableStateOf(false)
    var financialError by mutableStateOf("")
    var showFinancialFiltersDialog by mutableStateOf(false)
    
    // Estados para transparencia de pagos
    var transparencyData by mutableStateOf<PaymentTransparencyData?>(null)
    var isLoadingTransparency by mutableStateOf(false)
    var transparencyError by mutableStateOf("")
    var showTransparencyFiltersDialog by mutableStateOf(false)
    
    // Estado para filtros de sección
    var showBasicCharts by mutableStateOf(true)
    var showAdvancedCharts by mutableStateOf(true)
    var showPredictiveCharts by mutableStateOf(true)
    var showAdditionalMetrics by mutableStateOf(true)
    var showFiltersDialog by mutableStateOf(false)
}

/**
 * Función para crear el estado inicial del AdminAnalyticsScreen
 */
@Composable
fun rememberAdminAnalyticsState(): AdminAnalyticsState {
    return remember {
        AdminAnalyticsState()
    }
}
