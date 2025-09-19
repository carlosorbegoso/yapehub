package org.sysarp.project.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.sysarp.project.data.AnalyticsData
import org.sysarp.project.data.AnalyticsConfigs
import org.sysarp.project.data.FinancialAnalysisData
import org.sysarp.project.data.PaymentTransparencyData
import org.sysarp.project.data.FinancialAnalysisParams
import org.sysarp.project.data.PaymentTransparencyParams
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.ui.components.admin.ResponsiveChartRow
import org.sysarp.project.ui.components.admin.SectionFiltersDialog
import org.sysarp.project.ui.components.admin.ChartItem
import org.sysarp.project.ui.components.analytics.AnalyticsFilterDialog
import org.sysarp.project.ui.components.financial.FinancialAnalysisCard
import org.sysarp.project.ui.components.financial.PaymentTransparencyCard
import org.sysarp.project.ui.components.financial.FinancialFilterDialog
import org.sysarp.project.ui.components.charts.DailySalesBarChart
import org.sysarp.project.ui.components.charts.PerformanceMetricsPieChart
import org.sysarp.project.ui.components.charts.SalesTrendLineChart
import org.sysarp.project.ui.components.charts.HourlySalesChart
import org.sysarp.project.ui.components.charts.AchievementsChart
import org.sysarp.project.ui.components.charts.PredictionsChart
import org.sysarp.project.ui.components.charts.SalesDistributionChart
import org.sysarp.project.ui.components.charts.ComparisonsChart
import org.sysarp.project.ui.components.calendar.SmartCalendar
import org.sysarp.project.ui.components.topbar.TopBarComponent
import org.sysarp.project.utils.Logger
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatPercentage
import kotlin.time.Duration.Companion.days

/**
 * Pantalla de analytics detallados para el administrador
 * Muestra gráficos, métricas y tendencias del sistema completo
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
    
    // Estados para los datos de analytics
    var analyticsData by remember { mutableStateOf<AnalyticsData?>(null) }
    var isLoadingAnalytics by remember { mutableStateOf(false) }
    var analyticsError by remember { mutableStateOf("") }
    
    // Estados para datos financieros
    var financialData by remember { mutableStateOf<FinancialAnalysisData?>(null) }
    var transparencyData by remember { mutableStateOf<PaymentTransparencyData?>(null) }
    var isLoadingFinancial by remember { mutableStateOf(false) }
    var financialError by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("📅 7 días") }
    var showPeriodMenu by remember { mutableStateOf(false) }
    
    // Estado para filtros de sección
    var showBasicCharts by remember { mutableStateOf(true) }
    var showAdvancedCharts by remember { mutableStateOf(true) }
    var showPredictiveCharts by remember { mutableStateOf(true) }
    var showAdditionalMetrics by remember { mutableStateOf(true) }
    var showFiltersDialog by remember { mutableStateOf(false) }
    var showAdvancedFiltersDialog by remember { mutableStateOf(false) }
    var showFinancialFiltersDialog by remember { mutableStateOf(false) }
    
    // Cargar datos de analytics con filtros de fecha
    val loadAnalytics: (String?, String?) -> Unit = { startDate, endDate ->
        coroutineScope.launch {
            val adminId = userProfile?.adminId?.toIntOrNull()
            if (adminId != null && accessToken != null) {
            isLoadingAnalytics = true
            analyticsError = ""
            
                statsService.getDetailedAnalytics(
                    adminId = adminId,
                    startDate = startDate ?: Clock.System.now().minus(7.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString(),
                    endDate = endDate ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString(),
                token = accessToken!!
            ).fold(
                onSuccess = { response ->
                        analyticsData = response.data
                    isLoadingAnalytics = false
                        Logger.auth("ADMIN_ANALYTICS", "📊 Analytics del admin cargados: ${response.data.overview.totalSales}")
                },
                onFailure = { error ->
                    analyticsError = error.message ?: "Error cargando analytics"
                    isLoadingAnalytics = false
                        Logger.auth("ADMIN_ANALYTICS", "❌ Error cargando analytics: ${error.message}")
                    }
                )
            }
        }
    }
    
    // Cargar datos al iniciar - Últimos 7 días por defecto
    LaunchedEffect(userProfile?.adminId, accessToken) {
        val now = Clock.System.now()
        val endDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        val startDate = now.minus(7.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        selectedPeriod = "📅 7 días"
        loadAnalytics(startDate, endDate)
    }
    
    Scaffold(
        topBar = {
            TopBarComponent(
                title = "Analytics Admin",
                subtitle = "Análisis completo del sistema",
                onNavigateBack = onNavigateBack,
                onRefresh = {
                    val now = Clock.System.now()
                    val endDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                    val startDate = now.minus(7.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                    loadAnalytics(startDate, endDate)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header con período seleccionado - Elegante con fondo blanco
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Período de análisis",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedPeriod,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botón de filtros con icono centrado
                            IconButton(
                                onClick = { showFiltersDialog = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                        Icon(
                                    imageVector = Icons.Filled.FilterList,
                                    contentDescription = "Filtros",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // Botón de calendario profesional con icono centrado
                            IconButton(
                                onClick = { showPeriodMenu = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                        Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = "Seleccionar período",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // Botón de filtros avanzados
                            IconButton(
                                onClick = { showAdvancedFiltersDialog = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                        Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "Filtros Avanzados",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // Botón de filtros financieros
                            IconButton(
                                onClick = { showFinancialFiltersDialog = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AttachMoney,
                                    contentDescription = "Filtros Financieros",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Calendario inteligente reutilizable
                        SmartCalendar(
                            selectedPeriod = selectedPeriod,
                            onPeriodSelected = { period ->
                                selectedPeriod = period
                                showPeriodMenu = false
                                // El calendario inteligente ya maneja las fechas directamente
                                // Solo recargamos con el período por defecto (7 días)
                                val now = Clock.System.now()
                                val endDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                val startDate = now.minus(7.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                loadAnalytics(startDate, endDate)
                            },
                            expanded = showPeriodMenu,
                            onDismiss = { showPeriodMenu = false }
                        )
                    }
                }
            }
            
            // Estado inicial - Sin datos cargados
            if (!isLoadingAnalytics && analyticsData == null && analyticsError.isEmpty()) {
            item {
                Card(
                        modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                        shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                                text = "Selecciona un período para ver los analytics del sistema",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                                text = "Usa el botón 'Seleccionar' arriba para elegir un período o rango de fechas personalizado",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        }
                    }
                }
            }
            
            // Loading state
            if (isLoadingAnalytics) {
                item {
                    Box(
                                modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                text = "Cargando analytics del sistema...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Error state
            if (analyticsError.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                    Text(
                                text = "Error cargando analytics",
                                style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                text = analyticsError,
                                        style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Button(
                                onClick = { 
                                    val now = Clock.System.now()
                                    val endDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                    val startDate = now.minus(7.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                    loadAnalytics(startDate, endDate)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Reintentar")
                                }
                            }
                        }
                    }
                }
            
            // Analytics data - Diseño compacto y responsivo
            analyticsData?.let { data ->
                // Métricas principales destacadas
                item {
                    PrimaryMetricsSection(data = data.overview)
                }
                
                // Sección de gráficos básicos - Layout horizontal para pantallas grandes
                if (showBasicCharts) {
                item {
                    AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(600, easing = EaseOutCubic)
                            ) + fadeIn(animationSpec = tween(600))
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "📊 Gráficos Principales",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                
                                // Gráficos básicos en layout responsivo
                                ResponsiveChartRow(
                                    charts = listOf(
                                        ChartItem("Ventas Diarias", { DailySalesBarChart(dailySales = data.dailySales) }),
                                        ChartItem("Métricas de Rendimiento", { PerformanceMetricsPieChart(performanceMetrics = data.performanceMetrics) }),
                                        ChartItem("Tendencias", { SalesTrendLineChart(dailySales = data.dailySales) })
                                    )
                                )
                            }
                        }
                    }
                }

                // Sección de gráficos avanzados - Layout horizontal optimizado
                if (showAdvancedCharts) {
                item {
                    AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(800, easing = EaseOutCubic)
                            ) + fadeIn(animationSpec = tween(800))
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "🎯 Análisis Avanzado",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                
                                // Gráficos avanzados en layout responsivo
                                ResponsiveChartRow(
                                    charts = listOf(
                                        ChartItem("Ventas por Hora", { HourlySalesChart(hourlySales = data.hourlySales ?: emptyList()) }),
                                        ChartItem("Distribución", { SalesDistributionChart(salesDistribution = data.sellerAnalytics?.salesDistribution) }),
                                        ChartItem("Logros y Badges", { AchievementsChart(sellerAchievements = data.sellerAchievements) })
                                    )
                                )
                            }
                        }
                    }
                }

                // Sección de análisis predictivo - Layout horizontal optimizado
                if (showPredictiveCharts) {
                item {
                    AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(1000, easing = EaseOutCubic)
                            ) + fadeIn(animationSpec = tween(1000))
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "🔮 Análisis Predictivo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                
                                // Gráficos de análisis avanzado en layout responsivo
                                ResponsiveChartRow(
                                    charts = listOf(
                                        ChartItem("Predicciones", { PredictionsChart(sellerForecasting = data.sellerForecasting) }),
                                        ChartItem("Comparaciones", { ComparisonsChart(sellerComparisons = data.sellerComparisons) })
                                    )
                                )
                            }
                        }
                    }
                }

                // Información adicional de métricas
                if (showAdditionalMetrics) {
                    item {
                        AdditionalMetricsCard(data = data.overview)
                }
            }
            
            // Top vendedores
                val topSellers = data.topSellers
                if (topSellers?.isNotEmpty() == true) {
                item {
                        TopSellersSection(topSellers = topSellers)
                    }
                }

                // Sección de Branch Analytics - Nueva funcionalidad administrativa
                data.branchAnalytics?.let { branchAnalytics ->
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(1200, easing = EaseOutCubic)
                            ) + fadeIn(animationSpec = tween(1200))
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                    Text(
                                    text = "🏢 Branch Analytics",
                                    style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                                
                                BranchAnalyticsSection(branchAnalytics = branchAnalytics)
                            }
                        }
                    }
                }
                
                // Sección de Seller Management - Nueva funcionalidad administrativa
                data.sellerManagement?.let { sellerManagement ->
                item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(1400, easing = EaseOutCubic)
                            ) + fadeIn(animationSpec = tween(1400))
                        ) {
                    Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "👥 Seller Management",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                
                                SellerManagementSection(sellerManagement = sellerManagement)
                            }
                        }
                    }
                }

                // Sección de System Metrics - Nueva funcionalidad administrativa
                data.systemMetrics?.let { systemMetrics ->
                item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(1600, easing = EaseOutCubic)
                            ) + fadeIn(animationSpec = tween(1600))
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                    Text(
                                    text = "⚙️ System Metrics",
                                    style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                                
                                SystemMetricsSection(systemMetrics = systemMetrics)
                            }
                        }
                    }
                }
                
                // Sección de Administrative Insights - Nueva funcionalidad administrativa
                data.administrativeInsights?.let { adminInsights ->
                item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(1800, easing = EaseOutCubic)
                            ) + fadeIn(animationSpec = tween(1800))
                        ) {
                    Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "🚨 Administrative Insights",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                
                                AdministrativeInsightsSection(adminInsights = adminInsights)
                            }
                        }
                    }
                }

                // Sección de Financial Overview - Nueva funcionalidad administrativa
                data.financialOverview?.let { financialOverview ->
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(2000, easing = EaseOutCubic)
                            ) + fadeIn(animationSpec = tween(2000))
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "💰 Financial Overview",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                
                                FinancialOverviewSection(financialOverview = financialOverview)
                            }
                        }
                    }
                }

                // Sección de Compliance & Security - Nueva funcionalidad administrativa
                data.complianceAndSecurity?.let { complianceSecurity ->
            item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(2200, easing = EaseOutCubic)
                            ) + fadeIn(animationSpec = tween(2200))
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "🔒 Compliance & Security",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                
                                ComplianceSecuritySection(complianceSecurity = complianceSecurity)
                            }
                        }
                    }
                }
                
                // Sección de Análisis Financiero - Nueva funcionalidad
                financialData?.let { financial ->
                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(2200, easing = EaseOutCubic)
                            ) + fadeIn(animationSpec = tween(2200))
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "💰 Análisis Financiero",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                
                                FinancialAnalysisCard(data = financial)
                            }
                        }
                    }
                }
                
                // Sección de Transparencia de Pagos - Nueva funcionalidad
                transparencyData?.let { transparency ->
            item {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(2200, easing = EaseOutCubic)
                            ) + fadeIn(animationSpec = tween(2200))
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "🔍 Transparencia de Pagos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                
                                PaymentTransparencyCard(data = transparency)
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo de filtros de sección
    if (showFiltersDialog) {
        SectionFiltersDialog(
            showBasicCharts = showBasicCharts,
            showAdvancedCharts = showAdvancedCharts,
            showPredictiveCharts = showPredictiveCharts,
            onShowBasicChartsChange = { showBasicCharts = it },
            onShowAdvancedChartsChange = { showAdvancedCharts = it },
            onShowPredictiveChartsChange = { showPredictiveCharts = it },
            onDismiss = { showFiltersDialog = false }
        )
    }
    
    // Diálogo de filtros avanzados de analytics
    if (showAdvancedFiltersDialog) {
        AnalyticsFilterDialog(
            isVisible = showAdvancedFiltersDialog,
            onDismiss = { showAdvancedFiltersDialog = false },
            onApply = { params ->
                showAdvancedFiltersDialog = false
                // Recargar analytics con los nuevos parámetros
                val adminId = userProfile?.adminId?.toIntOrNull()
                if (adminId != null && accessToken != null) {
                    coroutineScope.launch {
                        isLoadingAnalytics = true
                        analyticsError = ""
                        
                        statsService.getAnalytics(
                            adminId = adminId,
                            startDate = null, // Usar fechas por defecto
                            endDate = null,
                            include = params.include,
                            period = params.period,
                            metric = params.metric,
                            confidence = params.confidence,
                            days = params.days,
                            token = accessToken!!
                        ).fold(
                            onSuccess = { response ->
                                analyticsData = response.data
                                isLoadingAnalytics = false
                                Logger.auth("ADMIN_ANALYTICS", "📊 Analytics avanzados cargados: ${response.data.overview.totalSales}")
                            },
                            onFailure = { error ->
                                analyticsError = error.message ?: "Error cargando analytics avanzados"
                                isLoadingAnalytics = false
                                Logger.auth("ADMIN_ANALYTICS", "❌ Error cargando analytics avanzados: ${error.message}")
                            }
                        )
                    }
                }
            }
        )
    }
    
    // Diálogo de filtros financieros
    if (showFinancialFiltersDialog) {
        FinancialFilterDialog(
            isVisible = showFinancialFiltersDialog,
            onDismiss = { showFinancialFiltersDialog = false },
            onApplyFinancial = { params ->
                showFinancialFiltersDialog = false
                // Cargar análisis financiero con los nuevos parámetros
                val adminId = userProfile?.adminId?.toIntOrNull()
                if (adminId != null && accessToken != null) {
                    coroutineScope.launch {
                        isLoadingFinancial = true
                        financialError = ""
                        
                        statsService.getFinancialAnalysis(
                            adminId = adminId,
                            startDate = null, // Usar fechas por defecto
                            endDate = null,
                            include = params.include,
                            currency = params.currency,
                            taxRate = params.taxRate,
                            token = accessToken!!
                        ).fold(
                            onSuccess = { response ->
                                financialData = response.data
                                isLoadingFinancial = false
                                Logger.auth("ADMIN_FINANCIAL", "💰 Análisis financiero cargado: ${response.data.totalRevenue}")
                            },
                            onFailure = { error ->
                                financialError = error.message ?: "Error cargando análisis financiero"
                                isLoadingFinancial = false
                                Logger.auth("ADMIN_FINANCIAL", "❌ Error cargando análisis financiero: ${error.message}")
                            }
                        )
                    }
                }
            },
            onApplyTransparency = { params ->
                showFinancialFiltersDialog = false
                // Cargar transparencia de pagos con los nuevos parámetros
                val adminId = userProfile?.adminId?.toIntOrNull()
                if (adminId != null && accessToken != null) {
                    coroutineScope.launch {
                        isLoadingFinancial = true
                        financialError = ""
                        
                        statsService.getPaymentTransparency(
                            adminId = adminId,
                            startDate = null, // Usar fechas por defecto
                            endDate = null,
                            includeFees = params.includeFees,
                            includeTaxes = params.includeTaxes,
                            includeCommissions = params.includeCommissions,
                            token = accessToken!!
                        ).fold(
                            onSuccess = { response ->
                                transparencyData = response.data
                                isLoadingFinancial = false
                                Logger.auth("ADMIN_TRANSPARENCY", "🔍 Transparencia cargada: ${response.data.transparencyScore}")
                            },
                            onFailure = { error ->
                                financialError = error.message ?: "Error cargando transparencia"
                                isLoadingFinancial = false
                                Logger.auth("ADMIN_TRANSPARENCY", "❌ Error cargando transparencia: ${error.message}")
                            }
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun PrimaryMetricsSection(
    data: org.sysarp.project.data.AnalyticsOverview
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Métrica principal destacada - Total Ventas con layout optimizado
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
                // Lado izquierdo: Información principal
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Total de Ventas del Sistema",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = formatCurrency(data.totalSales),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Lado derecho: Indicador de crecimiento y métricas adicionales
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Indicador de crecimiento
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (data.salesGrowth >= 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (data.salesGrowth >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = formatPercentage(data.salesGrowth),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (data.salesGrowth >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Métricas adicionales compactas
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "${data.totalTransactions} transacciones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Promedio: ${formatCurrency(data.averageTransactionValue)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdditionalMetricsCard(
    data: org.sysarp.project.data.AnalyticsOverview
) {
            Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 Métricas Adicionales del Sistema",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    label = "Crecimiento Ventas",
                    value = formatPercentage(data.salesGrowth),
                    color = if (data.salesGrowth >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    icon = if (data.salesGrowth >= 0) "📈" else "📉"
                )
                MetricItem(
                    label = "Crecimiento Transacciones",
                    value = formatPercentage(data.transactionGrowth),
                    color = if (data.transactionGrowth >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    icon = if (data.transactionGrowth >= 0) "📈" else "📉"
                )
                MetricItem(
                    label = "Promedio General",
                    value = formatPercentage(data.averageGrowth),
                    color = if (data.averageGrowth >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    icon = if (data.averageGrowth >= 0) "📈" else "📉"
                )
            }
        }
    }
}

@Composable
private fun TopSellersSection(
    topSellers: List<org.sysarp.project.data.TopSellerData>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🏆 Top Vendedores",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        // Top vendedores en layout responsivo
        ResponsiveChartRow(
            charts = topSellers.map { seller ->
                ChartItem(
                    title = "#${seller.rank ?: 1} ${seller.sellerName}",
                    content = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = seller.branchName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatCurrency(seller.totalSales),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${seller.transactionCount} transacciones",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        )
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    color: Color,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    color = color.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 20.sp
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

// Nuevas secciones administrativas
@Composable
private fun BranchAnalyticsSection(
    branchAnalytics: org.sysarp.project.data.BranchAnalyticsData
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Performance de sucursales
        Card(
            modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Rendimiento por Sucursal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                branchAnalytics.branchPerformance.forEach { branch ->
                    BranchPerformanceCard(branch = branch)
                }
            }
        }
        
        // Comparación de sucursales
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Comparación de Sucursales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                BranchComparisonCard(comparison = branchAnalytics.branchComparison)
            }
        }
    }
}

@Composable
private fun BranchPerformanceCard(
    branch: org.sysarp.project.data.BranchPerformanceData
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                Text(
                        text = branch.branchName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = branch.branchCode,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (branch.performanceScore > 70) 
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                Text(
                        text = "${branch.performanceScore.toInt()}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (branch.performanceScore > 70) 
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    label = "Ventas",
                    value = formatCurrency(branch.totalSales),
                    color = MaterialTheme.colorScheme.primary,
                    icon = "💰"
                )
                MetricItem(
                    label = "Transacciones",
                    value = branch.totalTransactions.toString(),
                    color = MaterialTheme.colorScheme.secondary,
                    icon = "📊"
                )
                MetricItem(
                    label = "Vendedores",
                    value = "${branch.activeSellers}/${branch.activeSellers + branch.inactiveSellers}",
                    color = MaterialTheme.colorScheme.tertiary,
                    icon = "👥"
                )
                MetricItem(
                    label = "Crecimiento",
                    value = "${branch.growthRate.toInt()}%",
                    color = if (branch.growthRate > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    icon = if (branch.growthRate > 0) "📈" else "📉"
                )
            }
        }
    }
}

@Composable
private fun BranchComparisonCard(
    comparison: org.sysarp.project.data.BranchComparisonData
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🏆 Mejor",
                style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            Text(
                text = comparison.topPerformingBranch.branchName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatCurrency(comparison.topPerformingBranch.sales),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                Text(
                text = "📊 Promedio",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${comparison.averageBranchPerformance.sellers} vendedores",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatCurrency(comparison.averageBranchPerformance.sales),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "⚠️ Menor",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = comparison.lowestPerformingBranch.branchName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatCurrency(comparison.lowestPerformingBranch.sales),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun SellerManagementSection(
    sellerManagement: org.sysarp.project.data.SellerManagementData
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overview de vendedores
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Resumen de Vendedores",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        label = "Total",
                        value = sellerManagement.sellerOverview.totalSellers.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        icon = "👥"
                    )
                    MetricItem(
                        label = "Activos",
                        value = sellerManagement.sellerOverview.activeSellers.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        icon = "✅"
                    )
                    MetricItem(
                        label = "Inactivos",
                        value = sellerManagement.sellerOverview.inactiveSellers.toString(),
                        color = MaterialTheme.colorScheme.error,
                        icon = "❌"
                    )
                    MetricItem(
                        label = "Nuevos",
                        value = sellerManagement.sellerOverview.newSellersThisMonth.toString(),
                        color = MaterialTheme.colorScheme.secondary,
                        icon = "🆕"
                    )
                }
            }
        }
        
        // Distribución de rendimiento
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Distribución de Rendimiento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        label = "Excelente",
                        value = sellerManagement.sellerPerformanceDistribution.excellent.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        icon = "🌟"
                    )
                    MetricItem(
                        label = "Bueno",
                        value = sellerManagement.sellerPerformanceDistribution.good.toString(),
                        color = MaterialTheme.colorScheme.secondary,
                        icon = "👍"
                    )
                    MetricItem(
                        label = "Promedio",
                        value = sellerManagement.sellerPerformanceDistribution.average.toString(),
                        color = MaterialTheme.colorScheme.tertiary,
                        icon = "📊"
                    )
                    MetricItem(
                        label = "Bajo",
                        value = sellerManagement.sellerPerformanceDistribution.poor.toString(),
                        color = MaterialTheme.colorScheme.error,
                        icon = "📉"
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemMetricsSection(
    systemMetrics: org.sysarp.project.data.SystemMetricsData
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Salud del sistema
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Salud del Sistema",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        label = "Uptime",
                        value = "${systemMetrics.overallSystemHealth.systemUptime}%",
                        color = MaterialTheme.colorScheme.primary,
                        icon = "⚡"
                    )
                    MetricItem(
                        label = "Respuesta",
                        value = "${systemMetrics.overallSystemHealth.averageResponseTime}s",
                        color = MaterialTheme.colorScheme.secondary,
                        icon = "⏱️"
                    )
                    MetricItem(
                        label = "Errores",
                        value = "${systemMetrics.overallSystemHealth.errorRate}%",
                        color = MaterialTheme.colorScheme.error,
                        icon = "⚠️"
                    )
                    MetricItem(
                        label = "Usuarios",
                        value = systemMetrics.overallSystemHealth.activeUsers.toString(),
                        color = MaterialTheme.colorScheme.tertiary,
                        icon = "👤"
                    )
                }
            }
        }
        
        // Métricas de pagos
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Sistema de Pagos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        label = "Procesados",
                        value = systemMetrics.paymentSystemMetrics.totalPaymentsProcessed.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        icon = "💳"
                    )
                    MetricItem(
                        label = "Confirmados",
                        value = systemMetrics.paymentSystemMetrics.confirmedPayments.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        icon = "✅"
                    )
                    MetricItem(
                        label = "Pendientes",
                        value = systemMetrics.paymentSystemMetrics.pendingPayments.toString(),
                        color = MaterialTheme.colorScheme.secondary,
                        icon = "⏳"
                    )
                    MetricItem(
                        label = "Éxito",
                        value = "${systemMetrics.paymentSystemMetrics.paymentSuccessRate}%",
                        color = MaterialTheme.colorScheme.primary,
                        icon = "🎯"
                    )
                }
            }
        }
    }
}

@Composable
private fun AdministrativeInsightsSection(
    adminInsights: org.sysarp.project.data.AdministrativeInsightsData
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Alertas de gestión
        if (adminInsights.managementAlerts.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🚨 Alertas de Gestión",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    adminInsights.managementAlerts.forEach { alert ->
                        ManagementAlertCard(alert = alert)
                    }
                }
            }
        }
        
        // Recomendaciones
        if (adminInsights.recommendations.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "💡 Recomendaciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    adminInsights.recommendations.forEach { recommendation ->
                        Text(
                            text = "• $recommendation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
        
        // Oportunidades de crecimiento
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🚀 Oportunidades de Crecimiento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        label = "Nuevas Sucursales",
                        value = adminInsights.growthOpportunities.potentialNewBranches.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        icon = "🏢"
                    )
                    MetricItem(
                        label = "Nuevos Vendedores",
                        value = adminInsights.growthOpportunities.sellerRecruitment.toString(),
                        color = MaterialTheme.colorScheme.secondary,
                        icon = "👥"
                    )
                    MetricItem(
                        label = "Proyección",
                        value = formatCurrency(adminInsights.growthOpportunities.revenueProjection),
                        color = MaterialTheme.colorScheme.tertiary,
                        icon = "💰"
                    )
                }
                
                Text(
                    text = "Expansión: ${adminInsights.growthOpportunities.marketExpansion}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ManagementAlertCard(
    alert: org.sysarp.project.data.ManagementAlertData
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (alert.severity) {
                "high" -> MaterialTheme.colorScheme.errorContainer
                "medium" -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = when (alert.severity) {
                        "high" -> MaterialTheme.colorScheme.onErrorContainer
                        "medium" -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (alert.severity) {
                            "high" -> MaterialTheme.colorScheme.error
                            "medium" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.outline
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = alert.severity.uppercase(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = when (alert.severity) {
                            "high" -> MaterialTheme.colorScheme.onError
                            "medium" -> MaterialTheme.colorScheme.onSecondary
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
                Text(
                text = alert.recommendation,
                style = MaterialTheme.typography.bodyMedium,
                color = when (alert.severity) {
                    "high" -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    "medium" -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                }
            )
        }
    }
}

@Composable
private fun FinancialOverviewSection(
    financialOverview: org.sysarp.project.data.FinancialOverviewData
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Desglose de ingresos
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "💰 Desglose de Ingresos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        label = "Total",
                        value = formatCurrency(financialOverview.revenueBreakdown.totalRevenue),
                        color = MaterialTheme.colorScheme.primary,
                        icon = "💰"
                    )
                    MetricItem(
                        label = "Diario",
                        value = "${financialOverview.revenueBreakdown.revenueGrowth.daily}%",
                        color = MaterialTheme.colorScheme.secondary,
                        icon = "📅"
                    )
                    MetricItem(
                        label = "Semanal",
                        value = "${financialOverview.revenueBreakdown.revenueGrowth.weekly}%",
                        color = MaterialTheme.colorScheme.tertiary,
                        icon = "📊"
                    )
                    MetricItem(
                        label = "Mensual",
                        value = "${financialOverview.revenueBreakdown.revenueGrowth.monthly}%",
                        color = MaterialTheme.colorScheme.primary,
                        icon = "📈"
                    )
                }
            }
        }
        
        // Análisis de costos
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            Text(
                    text = "📊 Análisis de Costos",
                    style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        label = "Operacionales",
                        value = formatCurrency(financialOverview.costAnalysis.operationalCosts),
                        color = MaterialTheme.colorScheme.secondary,
                        icon = "⚙️"
                    )
                    MetricItem(
                        label = "Comisiones",
                        value = formatCurrency(financialOverview.costAnalysis.sellerCommissions),
                        color = MaterialTheme.colorScheme.tertiary,
                        icon = "👥"
                    )
                    MetricItem(
                        label = "Mantenimiento",
                        value = formatCurrency(financialOverview.costAnalysis.systemMaintenance),
                        color = MaterialTheme.colorScheme.outline,
                        icon = "🔧"
                    )
                    MetricItem(
                        label = "Margen",
                        value = "${financialOverview.costAnalysis.profitMargin}%",
                        color = MaterialTheme.colorScheme.primary,
                        icon = "📈"
                    )
                }
                
                Text(
                    text = "Beneficio Neto: ${formatCurrency(financialOverview.costAnalysis.netProfit)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ComplianceSecuritySection(
    complianceSecurity: org.sysarp.project.data.ComplianceAndSecurityData
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Métricas de seguridad
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🔒 Métricas de Seguridad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        label = "Score",
                        value = "${complianceSecurity.securityMetrics.securityScore}%",
                        color = MaterialTheme.colorScheme.primary,
                        icon = "🛡️"
                    )
                    MetricItem(
                        label = "Intentos Fallidos",
                        value = complianceSecurity.securityMetrics.failedLoginAttempts.toString(),
                        color = MaterialTheme.colorScheme.error,
                        icon = "🚫"
                    )
                    MetricItem(
                        label = "Actividades Sospechosas",
                        value = complianceSecurity.securityMetrics.suspiciousActivities.toString(),
                        color = MaterialTheme.colorScheme.secondary,
                        icon = "👁️"
                    )
                    MetricItem(
                        label = "Brechas",
                        value = complianceSecurity.securityMetrics.dataBreaches.toString(),
                        color = MaterialTheme.colorScheme.error,
                        icon = "🔓"
                    )
                }
            }
        }
        
        // Estado de cumplimiento
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "📋 Estado de Cumplimiento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                ComplianceStatusItem(
                    label = "Protección de Datos",
                    status = complianceSecurity.complianceStatus.dataProtection,
                    icon = "🔐"
                )
                ComplianceStatusItem(
                    label = "Auditoría",
                    status = complianceSecurity.complianceStatus.auditTrail,
                    icon = "📊"
                )
                ComplianceStatusItem(
                    label = "Backup",
                    status = complianceSecurity.complianceStatus.backupStatus,
                    icon = "💾"
                )
                
                Text(
                    text = "Última Auditoría: ${complianceSecurity.complianceStatus.lastAudit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ComplianceStatusItem(
    label: String,
    status: String,
    icon: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = icon,
                fontSize = 16.sp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (status == "compliant" || status == "complete" || status == "up_to_date") 
                    MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = status.replace("_", " ").uppercase(),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (status == "compliant" || status == "complete" || status == "up_to_date") 
                    MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}