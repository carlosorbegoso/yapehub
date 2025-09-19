package org.sysarp.project.ui.screens.seller

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import org.sysarp.project.ui.components.topbar.TopBarComponent
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.ui.components.charts.DailySalesBarChart
import org.sysarp.project.ui.components.charts.DateRangeSelector
import org.sysarp.project.ui.components.charts.PerformanceMetricsPieChart
import org.sysarp.project.ui.components.charts.SalesTrendLineChart
import org.sysarp.project.ui.components.charts.SpecificDateSelector
import org.sysarp.project.ui.components.dashboard.rememberSellerStatsManager
import org.sysarp.project.utils.Logger
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatPercentage
import org.sysarp.project.utils.formatOneDecimal

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
    var selectedPeriod by remember { mutableStateOf("📊 Seleccionar período") }
    var showPeriodMenu by remember { mutableStateOf(false) }
    var showDateRangeSelector by remember { mutableStateOf(false) }
    var showSpecificDateSelector by remember { mutableStateOf(false) }
    
    // Opciones de período específicas para analytics
    val periodOptions = listOf(
        "📈 Últimos 7 días" to 7,
        "📊 Última semana" to 7,
        "📅 Último mes" to 30,
        "📈 Último trimestre" to 90,
        "📊 Último año" to 365,
        "📅 Día específico" to -2,
        "📊 Rango personalizado" to -1
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
    
    // Cargar datos al iniciar - Comentado para que el usuario seleccione el período
    // LaunchedEffect(userProfile?.sellerId, accessToken) {
    //     val now = Clock.System.now()
    //     val endDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    //     val startDate = now.minus(7.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    //     loadAnalytics(startDate, endDate)
    // }
    
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
                            Spacer(modifier = Modifier.width(12.dp))
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
                        
                        Box {
                            OutlinedButton(
                                onClick = { showPeriodMenu = true },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.height(40.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Seleccionar", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            
                            DropdownMenu(
                                expanded = showPeriodMenu,
                                onDismissRequest = { showPeriodMenu = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                // Opciones rápidas (períodos predefinidos)
                                periodOptions.filter { it.second > 0 }.forEach { (period, days) ->
                                    DropdownMenuItem(
                                        text = { 
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.TrendingUp,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = period,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedPeriod = period
                                            showPeriodMenu = false
                                            // Calcular fechas basadas en el período seleccionado
                                            val now = Clock.System.now()
                                            val endDate = now.toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                            val startDate = now.minus(days.days).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
                                            loadAnalytics(startDate, endDate)
                                        },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                    )
                                }
                                
                                // Separador visual
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = "━━━━━━━━━━━━━━━━━━━━",
                                            color = MaterialTheme.colorScheme.outline,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    onClick = { },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                )
                                
                                // Opciones avanzadas (calendario)
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.CalendarToday,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "📊 Rango personalizado",
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedPeriod = "📊 Rango personalizado"
                                        showPeriodMenu = false
                                        showDateRangeSelector = true
                                    },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                )
                                
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.CalendarToday,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "📅 Día específico",
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedPeriod = "📅 Día específico"
                                        showPeriodMenu = false
                                        showSpecificDateSelector = true
                                    },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                )
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
                
                // Gráficos en layout responsivo
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Gráfico de ventas diarias
                        DailySalesBarChart(dailySales = data.dailySales)
                        
                        // Gráfico circular de métricas de rendimiento
                        PerformanceMetricsPieChart(performanceMetrics = data.performanceMetrics)
                        
                        // Gráfico de líneas de tendencia
                        SalesTrendLineChart(dailySales = data.dailySales)
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
                            selectedPeriod = "📊 Rango personalizado"
                        }
                    }
                )
            }
            
            if (showSpecificDateSelector) {
                SpecificDateSelector(
                    onDateSelected = { date ->
                        showSpecificDateSelector = false
                        if (date != null) {
                            loadAnalytics(date, date)
                            selectedPeriod = "📅 Día específico"
                        }
                    }
                )
            }
        }
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

