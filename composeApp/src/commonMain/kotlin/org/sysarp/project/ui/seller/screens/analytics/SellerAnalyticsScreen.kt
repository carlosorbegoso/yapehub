package org.sysarp.project.ui.screens.seller

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.ui.common.components.topbar.TopBarComponent

/**
 * Pantalla de analytics detallados para el vendedor - Refactorizada
 * Muestra gráficos, métricas y tendencias de rendimiento
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerAnalyticsScreen(
    authService: AuthService,
    statsService: StatsService,
    onNavigateBack: () -> Unit
) {
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Estado del componente
    val state = rememberSellerAnalyticsState()
    
    // Data loader
    val dataLoader = remember(authService, statsService, coroutineScope) {
        SellerAnalyticsDataLoader(authService, statsService, coroutineScope)
    }
    
    // Cargar datos por defecto al iniciar
    LaunchedEffect(Unit) {
        if (state.analyticsData == null && !state.isLoadingAnalytics) {
            dataLoader.loadDefaultAnalytics(
                onLoadingChange = { state.isLoadingAnalytics = it },
                onDataLoaded = { state.analyticsData = it },
                onError = { state.analyticsError = it }
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopBarComponent(
                title = "📊 Analytics",
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Controles de filtros y calendario
            item {
                SellerAnalyticsControls(
                    state = state,
                    onPeriodChange = { state.selectedPeriod = it },
                    onLoadAnalytics = { startDate, endDate ->
                        dataLoader.loadAnalytics(
                            startDate, endDate,
                            onLoadingChange = { state.isLoadingAnalytics = it },
                            onDataLoaded = { state.analyticsData = it },
                            onError = { state.analyticsError = it }
                        )
                    },
                    onShowFiltersDialog = { state.showFiltersDialog = true },
                    onShowFinancialFiltersDialog = { state.showFinancialFiltersDialog = true },
                    onShowPeriodMenu = { state.showPeriodMenu = true }
                )
            }
            
            // Estado vacío
            if (state.analyticsData == null && !state.isLoadingAnalytics && state.analyticsError.isEmpty()) {
                item {
                    SellerAnalyticsEmptyState(
                        onLoadDefaultAnalytics = {
                            dataLoader.loadDefaultAnalytics(
                                onLoadingChange = { state.isLoadingAnalytics = it },
                                onDataLoaded = { state.analyticsData = it },
                                onError = { state.analyticsError = it }
                            )
                        }
                    )
                }
            }
            
            
            // Estado de carga
            if (state.isLoadingAnalytics) {
                item {
                    SellerAnalyticsLoadingState()
                }
            }
            
            // Estado de error
            if (state.analyticsError.isNotEmpty()) {
                item {
                    SellerAnalyticsErrorState(
                        error = state.analyticsError,
                        onRetry = {
                            dataLoader.loadDefaultAnalytics(
                                onLoadingChange = { state.isLoadingAnalytics = it },
                                onDataLoaded = { state.analyticsData = it },
                                onError = { state.analyticsError = it }
                            )
                        }
                    )
                }
            }
            
            // Secciones de analytics
            state.analyticsData?.let { analyticsData ->
                item {
                    SellerAnalyticsSections(
                        analyticsData = analyticsData,
                        sellerFinancialData = state.sellerFinancialData,
                        showBasicCharts = state.showBasicCharts,
                        showAdvancedCharts = state.showAdvancedCharts,
                        showPredictiveCharts = state.showPredictiveCharts,
                        showAdditionalMetrics = state.showAdditionalMetrics
                    )
                }
            }
        }
        
        // Dialogs
        SellerAnalyticsDialogs(
            state = state,
            authService = authService,
            statsService = statsService,
            coroutineScope = coroutineScope,
            onDismissFiltersDialog = { state.showFiltersDialog = false },
            onDismissFinancialDialog = { state.showFinancialFiltersDialog = false },
            onShowBasicChartsChange = { state.showBasicCharts = it },
            onShowAdvancedChartsChange = { state.showAdvancedCharts = it },
            onShowPredictiveChartsChange = { state.showPredictiveCharts = it },
            onShowAdditionalMetricsChange = { state.showAdditionalMetrics = it },
            onFinancialDataLoaded = { state.sellerFinancialData = it },
            onFinancialError = { state.financialError = it }
        )
    }
}
