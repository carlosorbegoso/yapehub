package org.sysarp.project.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.ui.components.topbar.TopBarComponent

/**
 * Pantalla de analytics detallados para el administrador - Refactorizada
 * Muestra gráficos, métricas y tendencias de rendimiento administrativo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsScreen(
    authService: AuthService,
    statsService: StatsService,
    onNavigateBack: () -> Unit
) {
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Estado del componente
    val state = rememberAdminAnalyticsState()
    
    // Data loader
    val dataLoader = remember(authService, statsService, coroutineScope) {
        AdminAnalyticsDataLoader(authService, statsService, coroutineScope)
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
                title = "📊 Analytics Administrativos",
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
                AdminAnalyticsControls(
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
                    onShowTransparencyFiltersDialog = { state.showTransparencyFiltersDialog = true },
                    onShowPeriodMenu = { state.showPeriodMenu = true }
                )
            }
            
            // Estado vacío
            if (state.analyticsData == null && !state.isLoadingAnalytics && state.analyticsError.isEmpty()) {
                item {
                    AdminAnalyticsEmptyState(
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
                    AdminAnalyticsLoadingState()
                }
            }
            
            // Estado de error
            if (state.analyticsError.isNotEmpty()) {
                item {
                    AdminAnalyticsErrorState(
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
                    AdminAnalyticsSections(
                        analyticsData = analyticsData,
                        financialData = state.financialData,
                        transparencyData = state.transparencyData,
                        showBasicCharts = state.showBasicCharts,
                        showAdvancedCharts = state.showAdvancedCharts,
                        showPredictiveCharts = state.showPredictiveCharts,
                        showAdditionalMetrics = state.showAdditionalMetrics
                    )
                }
            }
        }
        
        // Dialogs
        AdminAnalyticsDialogs(
            state = state,
            authService = authService,
            statsService = statsService,
            coroutineScope = coroutineScope,
            onDismissFiltersDialog = { state.showFiltersDialog = false },
            onDismissFinancialDialog = { state.showFinancialFiltersDialog = false },
            onDismissTransparencyDialog = { state.showTransparencyFiltersDialog = false },
            onShowBasicChartsChange = { state.showBasicCharts = it },
            onShowAdvancedChartsChange = { state.showAdvancedCharts = it },
            onShowPredictiveChartsChange = { state.showPredictiveCharts = it },
            onShowAdditionalMetricsChange = { state.showAdditionalMetrics = it },
            onFinancialDataLoaded = { state.financialData = it },
            onFinancialError = { state.financialError = it },
            onTransparencyDataLoaded = { state.transparencyData = it },
            onTransparencyError = { state.transparencyError = it }
        )
    }
}
