package org.sysarp.project.ui.screens.seller

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.ui.components.charts.DailySalesBarChart
import org.sysarp.project.ui.components.charts.DateRangeSelector
import org.sysarp.project.ui.components.charts.PerformanceMetricsPieChart
import org.sysarp.project.ui.components.charts.SalesTrendLineChart
import org.sysarp.project.ui.components.charts.HourlySalesChart
import org.sysarp.project.ui.components.charts.GoalsProgressChart
import org.sysarp.project.ui.components.charts.AchievementsChart
import org.sysarp.project.ui.components.charts.PredictionsChart
import org.sysarp.project.ui.components.charts.SalesDistributionChart
import org.sysarp.project.ui.components.charts.ComparisonsChart
import org.sysarp.project.ui.components.charts.SpecificDateSelector
import org.sysarp.project.ui.components.dashboard.rememberSellerStatsManager
import org.sysarp.project.ui.components.topbar.TopBarComponent
import org.sysarp.project.ui.components.topbar.TopBarMenuItem
import org.sysarp.project.utils.Logger
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatPercentage
import kotlin.time.Duration.Companion.days

/**
 * Pantalla de analytics detallados para el vendedor
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
    
    // Estados para los datos de analytics
    var analyticsData by remember { mutableStateOf<org.sysarp.project.data.AnalyticsData?>(null) }
    var isLoadingAnalytics by remember { mutableStateOf(false) }
    var analyticsError by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("📅 Seleccionar período") }
    var showPeriodMenu by remember { mutableStateOf(false) }
    var showDateRangeSelector by remember { mutableStateOf(false) }
    var showSpecificDateSelector by remember { mutableStateOf(false) }
    
    // Estado para filtros de sección
    var showBasicCharts by remember { mutableStateOf(true) }
    var showAdvancedCharts by remember { mutableStateOf(true) }
    var showPredictiveCharts by remember { mutableStateOf(true) }
    var showAdditionalMetrics by remember { mutableStateOf(true) }
    var showFiltersDialog by remember { mutableStateOf(false) }
    
    // Opciones de período específicas para analytics
    val periodOptions = listOf(
        "🕐" to 1,      // Hoy
        "📅" to 7,      // Última semana  
        "📆" to 30,     // Último mes
        "🗓️" to 90,     // Últimos 3 meses
        "📊" to 365,    // Último año
        "🎯" to -2,     // Día específico
        "⚙️" to -1      // Rango personalizado
    )
    
    // Manager de estadísticas
    val statsManager = rememberSellerStatsManager(statsService)
    
    // Cargar datos de analytics con filtros de fecha
    val loadAnalytics: (String?, String?) -> Unit = { startDate, endDate ->
        coroutineScope.launch {
            val sellerId = userProfile?.sellerId?.toIntOrNull()
            if (sellerId != null && accessToken != null) {
                isLoadingAnalytics = true
                analyticsError = ""
                
                statsManager.loadSellerAnalyticsWithDates(
                    accessToken = accessToken!!,
                    sellerId = userProfile!!.sellerId!!.toLong(),
                    startDate = startDate,
                    endDate = endDate,
                    onSuccess = { response ->
                        analyticsData = response.data
                        isLoadingAnalytics = false
                        Logger.auth("SELLER_ANALYTICS", "📊 Analytics del vendedor cargados: ${response.data.overview.totalSales}")
                    },
                    onError = { error ->
                        analyticsError = error
                        isLoadingAnalytics = false
                        Logger.auth("SELLER_ANALYTICS", "❌ Error cargando analytics: $error")
                    }
                )
            }
        }
    }
    
    // Cargar datos al iniciar - Últimos 7 días por defecto
    LaunchedEffect(userProfile?.sellerId, accessToken) {
        val now = Clock.System.now()
        val endDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        val startDate = now.minus(7.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        selectedPeriod = "📈 Últimos 7 días"
        loadAnalytics(startDate, endDate)
    }
    
    Scaffold(
        topBar = {
            TopBarComponent(
                title = "Analytics",
                subtitle = "Análisis de ventas y rendimiento",
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
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Column {
                                Text(
                                    text = "Período de análisis",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedPeriod,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botón de filtros
                            // Botón de filtros simplificado (solo icono)
                            OutlinedButton(
                                onClick = { showFiltersDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FilterList,
                                    contentDescription = "Filtros",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            // Botón de calendario profesional (solo icono)
                            OutlinedButton(
                                onClick = { showPeriodMenu = true },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = "Seleccionar período",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        // Calendario avanzado y profesional
                        DropdownMenu(
                            expanded = showPeriodMenu,
                            onDismissRequest = { showPeriodMenu = false },
                            modifier = Modifier
                                .background(Color.White)
                                .padding(8.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            // Título del calendario
                            Text(
                                text = "📅 Seleccionar Período",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 8.dp)
                            )
                            
                            // Opciones rápidas (períodos predefinidos) - Layout horizontal compacto
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                periodOptions.filter { it.second > 0 }.take(4).forEach { (period, days) ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .clickable {
                                                    selectedPeriod = period
                                                    showPeriodMenu = false
                                                    // Calcular fechas basadas en el período seleccionado
                                                    val now = Clock.System.now()
                                                    val endDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                                    val startDate = now.minus(days.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                                    loadAnalytics(startDate, endDate)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = period,
                                                fontSize = 22.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = when (period) {
                                                "🕐" -> "Hoy"
                                                "📅" -> "7 días"
                                                "📆" -> "30 días"
                                                "🗓️" -> "3 meses"
                                                else -> ""
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                            
                            // Más opciones
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                periodOptions.filter { it.second > 0 }.drop(4).forEach { (period, days) ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .clickable {
                                                    selectedPeriod = period
                                                    showPeriodMenu = false
                                                    // Calcular fechas basadas en el período seleccionado
                                                    val now = Clock.System.now()
                                                    val endDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                                    val startDate = now.minus(days.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                                    loadAnalytics(startDate, endDate)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = period,
                                                fontSize = 22.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = "1 año",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                            
                            // Separador visual elegante
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.Gray.copy(alpha = 0.2f))
                                    .padding(horizontal = 16.dp)
                            )
                            
                            // Opciones avanzadas del calendario
                            Text(
                                text = "Opciones Avanzadas",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 4.dp)
                            )
                            
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Rango personalizado
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .background(
                                                color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable {
                                                selectedPeriod = "⚙️ Rango personalizado"
                                                showPeriodMenu = false
                                                showDateRangeSelector = true
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "⚙️",
                                            fontSize = 22.sp,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                    Text(
                                        text = "Rango",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                                
                                // Día específico
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .background(
                                                color = Color(0xFF2196F3).copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable {
                                                selectedPeriod = "🎯 Día específico"
                                                showPeriodMenu = false
                                                showSpecificDateSelector = true
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "🎯",
                                            fontSize = 22.sp,
                                            color = Color(0xFF2196F3)
                                        )
                                    }
                                    Text(
                                        text = "Día",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
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
                                text = "Selecciona un período para ver tus analytics",
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
                                text = "Cargando analytics...",
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
                                        ChartItem("Ventas por Hora", { HourlySalesChart(hourlySales = data.hourlySales) }),
                                        ChartItem("Progreso de Objetivos", { GoalsProgressChart(sellerGoals = data.sellerGoals) }),
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
                                        ChartItem("Distribución", { SalesDistributionChart(salesDistribution = data.sellerAnalytics?.salesDistribution) }),
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
            }
        }
    }
    
    // Selectores de fechas con calendario - Posicionados en el centro con fondo semitransparente
    if (showDateRangeSelector || showSpecificDateSelector) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
                )
                .clickable { 
                    showDateRangeSelector = false
                    showSpecificDateSelector = false
                },
            contentAlignment = Alignment.Center
        ) {
            // Selectores de fechas con calendario
            if (showDateRangeSelector) {
                DateRangeSelector(
                    onDateRangeSelected = { startDate, endDate ->
                        showDateRangeSelector = false
                        if (startDate != null && endDate != null) {
                            loadAnalytics(startDate, endDate)
                            selectedPeriod = "⚙️ Rango personalizado"
                        }
                    }
                )
            }
            
            if (showSpecificDateSelector) {
                SpecificDateSelector(
                    onDateSelected = { date: String? ->
                        showSpecificDateSelector = false
                        if (date != null) {
                            loadAnalytics(date, date)
                            selectedPeriod = "🎯 Día específico"
                        }
                    }
                )
            }
        }
    }
    
    // Diálogo de filtros de sección
    if (showFiltersDialog) {
        SectionFiltersDialog(
            showBasicCharts = showBasicCharts,
            showAdvancedCharts = showAdvancedCharts,
            showPredictiveCharts = showPredictiveCharts,
            showAdditionalMetrics = showAdditionalMetrics,
            onShowBasicChartsChange = { showBasicCharts = it },
            onShowAdvancedChartsChange = { showAdvancedCharts = it },
            onShowPredictiveChartsChange = { showPredictiveCharts = it },
            onShowAdditionalMetricsChange = { showAdditionalMetrics = it },
            onDismiss = { showFiltersDialog = false }
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
                            text = "Total de Ventas",
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
                text = "📊 Métricas Adicionales",
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

// Clase de datos para organizar gráficos
private data class ChartItem(
    val title: String,
    val content: @Composable () -> Unit
)

// Componente responsivo para mostrar gráficos en filas
@Composable
private fun ResponsiveChartRow(
    charts: List<ChartItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Siempre mostrar verticalmente para evitar distorsión
        // En el futuro se puede implementar detección de tamaño de pantalla
        charts.forEach { chart ->
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = chart.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                chart.content()
            }
        }
    }
}

// Diálogo para filtrar secciones
@Composable
private fun SectionFiltersDialog(
    showBasicCharts: Boolean,
    showAdvancedCharts: Boolean,
    showPredictiveCharts: Boolean,
    showAdditionalMetrics: Boolean,
    onShowBasicChartsChange: (Boolean) -> Unit,
    onShowAdvancedChartsChange: (Boolean) -> Unit,
    onShowPredictiveChartsChange: (Boolean) -> Unit,
    onShowAdditionalMetricsChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🔧 Filtros de Sección",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = "Selecciona qué secciones mostrar en tu dashboard de analytics",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                // Filtros de sección
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterItem(
                        title = "📊 Gráficos Principales",
                        subtitle = "Ventas diarias, métricas de rendimiento y tendencias",
                        isChecked = showBasicCharts,
                        onCheckedChange = onShowBasicChartsChange
                    )
                    
                    FilterItem(
                        title = "🎯 Análisis Avanzado",
                        subtitle = "Ventas por hora, progreso de objetivos y logros",
                        isChecked = showAdvancedCharts,
                        onCheckedChange = onShowAdvancedChartsChange
                    )
                    
                    FilterItem(
                        title = "🔮 Análisis Predictivo",
                        subtitle = "Predicciones, distribución y comparaciones",
                        isChecked = showPredictiveCharts,
                        onCheckedChange = onShowPredictiveChartsChange
                    )
                    
                    FilterItem(
                        title = "📈 Métricas Adicionales",
                        subtitle = "Información adicional de crecimiento y tendencias",
                        isChecked = showAdditionalMetrics,
                        onCheckedChange = onShowAdditionalMetricsChange
                    )
                }
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Aplicar",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Componente para cada filtro
@Composable
private fun FilterItem(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        
        androidx.compose.material3.Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = androidx.compose.material3.CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

