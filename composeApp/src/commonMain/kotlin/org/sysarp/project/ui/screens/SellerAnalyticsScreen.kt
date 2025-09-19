package org.sysarp.project.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService
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
    var selectedPeriod by remember { mutableStateOf("Últimos 30 días") }
    var showPeriodMenu by remember { mutableStateOf(false) }
    
    // Opciones de período
    val periodOptions = listOf(
        "Últimos 7 días" to 7,
        "Últimos 15 días" to 15,
        "Últimos 30 días" to 30,
        "Últimos 60 días" to 60,
        "Últimos 90 días" to 90
    )
    
    // Manager de estadísticas
    val statsManager = rememberSellerStatsManager(statsService)
    
    // Cargar datos de analytics
    val loadAnalytics = {
        coroutineScope.launch {
            val sellerId = userProfile?.sellerId?.toIntOrNull()
            if (sellerId != null && accessToken != null) {
                isLoadingAnalytics = true
                analyticsError = ""
                
                statsManager.loadSellerAnalytics(
                    accessToken = accessToken!!,
                    sellerId = userProfile!!.sellerId!!.toLong(),
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
    
    // Cargar datos al iniciar
    LaunchedEffect(userProfile?.sellerId, accessToken) {
        loadAnalytics()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analytics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { loadAnalytics() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con período seleccionado - Compacto para móvil
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
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
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedPeriod,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Box {
                            OutlinedButton(
                                onClick = { showPeriodMenu = true },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Cambiar", style = MaterialTheme.typography.bodySmall)
                            }
                            
                            DropdownMenu(
                                expanded = showPeriodMenu,
                                onDismissRequest = { showPeriodMenu = false }
                            ) {
                                periodOptions.forEach { (period, _) ->
                                    DropdownMenuItem(
                                        text = { Text(period) },
                                        onClick = {
                                            selectedPeriod = period
                                            showPeriodMenu = false
                                            loadAnalytics() // Recargar datos con nuevo período
                                        }
                                    )
                                }
                            }
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
                                onClick = { loadAnalytics() },
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
            
            // Analytics data - Compacto para móvil
            analyticsData?.let { data ->
                // Resumen general compacto
                item {
                    CompactAnalyticsOverviewCard(data = data.overview)
                }
                
                // Métricas de rendimiento compactas
                item {
                    CompactPerformanceMetricsCard(data = data.performanceMetrics)
                }
                
                // Estadísticas diarias compactas
                if (data.dailySales.isNotEmpty()) {
                    item {
                        Text(
                            text = "Ventas Diarias",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    
                    items(data.dailySales.take(7)) { dailyStat ->
                        CompactDailySalesCard(dailyStat = dailyStat)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactAnalyticsOverviewCard(
    data: org.sysarp.project.data.AnalyticsOverview
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Resumen General",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Métricas principales en grid compacto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactMetricItem(
                    label = "Total Ventas",
                    value = formatCurrency(data.totalSales),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                CompactMetricItem(
                    label = "Transacciones",
                    value = data.totalTransactions.toString(),
                    icon = Icons.Filled.Analytics,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactMetricItem(
                    label = "Crecimiento",
                    value = formatPercentage(data.salesGrowth),
                    icon = if (data.salesGrowth >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    color = if (data.salesGrowth >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                
                CompactMetricItem(
                    label = "Promedio",
                    value = formatCurrency(data.averageTransactionValue),
                    icon = Icons.Filled.Analytics,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun CompactPerformanceMetricsCard(
    data: org.sysarp.project.data.PerformanceMetricsData
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Rendimiento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Métricas de rendimiento compactas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactMetricItem(
                    label = "Tiempo Prom.",
                    value = "${formatOneDecimal(data.averageConfirmationTime)} min",
                    icon = Icons.Filled.Analytics,
                    color = MaterialTheme.colorScheme.primary
                )
                
                CompactMetricItem(
                    label = "Confirmación",
                    value = formatPercentage(data.claimRate),
                    icon = Icons.Filled.Analytics,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompactMetricItem(
                    label = "Rechazos",
                    value = formatPercentage(data.rejectionRate),
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    color = MaterialTheme.colorScheme.error
                )
                
                CompactMetricItem(
                    label = "Pendientes",
                    value = data.pendingPayments.toString(),
                    icon = Icons.Filled.Analytics,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun CompactDailySalesCard(
    dailyStat: org.sysarp.project.data.DailySalesData
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dailyStat.dayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dailyStat.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatCurrency(dailyStat.sales),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${dailyStat.transactions} trans.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CompactMetricItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

