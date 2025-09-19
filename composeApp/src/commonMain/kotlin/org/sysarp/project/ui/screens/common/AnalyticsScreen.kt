package org.sysarp.project.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.AnalyticsData
import org.sysarp.project.data.UserRole
import org.sysarp.project.data.YapeTransaction
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.utils.formatCurrency
import org.sysarp.project.utils.formatCurrencyNoDecimals
import org.sysarp.project.utils.formatPercentage
import org.sysarp.project.utils.formatOneDecimalWithUnit
import org.sysarp.project.viewmodel.YapeViewModel
import org.sysarp.project.ui.components.topbar.TopBarComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    authService: AuthService,
    statsService: StatsService,
    viewModel: YapeViewModel,
    onNavigateBack: () -> Unit
) {
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val transactions by viewModel.transactions.collectAsState()
    
    // Estado para los datos de analytics
    var analyticsData by remember { mutableStateOf<AnalyticsData?>(null) }
    var isLoadingAnalytics by remember { mutableStateOf(false) }
    var analyticsError by remember { mutableStateOf("") }
    
    // Cargar datos de analytics
    LaunchedEffect(userProfile?.adminId, accessToken) {
        if (userProfile?.adminId != null && accessToken != null) {
            isLoadingAnalytics = true
            analyticsError = ""
            
            statsService.getAnalytics(
                adminId = userProfile!!.adminId!!.toInt(),
                token = accessToken!!
            ).fold(
                onSuccess = { response ->
                    analyticsData = response.data
                    isLoadingAnalytics = false
                    println("🔍 [ANALYTICS] Datos de analytics cargados: ${response.data.overview.totalSales}")
                },
                onFailure = { error ->
                    analyticsError = error.message ?: "Error cargando analytics"
                    isLoadingAnalytics = false
                    println("🔍 [ANALYTICS] Error cargando analytics: ${error.message}")
                }
            )
            
            // También cargar estadísticas completas del admin
            statsService.getAdminStats(
                adminId = userProfile!!.adminId!!.toInt(),
                startDate = null, // Sin filtro de fecha para obtener todas las estadísticas
                endDate = null,
                token = accessToken!!
            ).fold(
                onSuccess = { _ ->
                    println("📊 [ANALYTICS] Estadísticas completas del admin cargadas exitosamente")
                    // TODO: Usar response.data para mostrar estadísticas adicionales en la UI
                },
                onFailure = { error ->
                    println("❌ [ANALYTICS] Error cargando estadísticas completas: ${error.message}")
                }
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopBarComponent(
                title = "Analytics y Reportes",
                subtitle = "Análisis y estadísticas del sistema",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Resumen general
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Analytics,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Resumen General",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "Estadísticas de transacciones",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // Estadísticas principales
            item {
                Text(
                    text = "Estadísticas Principales",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                if (isLoadingAnalytics) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (analyticsError.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error cargando analytics",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = analyticsError,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val analyticsStats = analyticsData?.let { data ->
                        listOf(
                            AnalyticsStat(
                                title = "Total Vendido",
                                value = formatCurrency(data.overview.totalSales),
                                icon = Icons.Filled.AttachMoney,
                                color = MaterialTheme.colorScheme.primary,
                                trend = formatPercentage(data.overview.salesGrowth)
                            ),
                            AnalyticsStat(
                                title = "Transacciones",
                                value = "${data.overview.totalTransactions}",
                                icon = Icons.Filled.Receipt,
                                color = MaterialTheme.colorScheme.secondary,
                                trend = formatPercentage(data.overview.transactionGrowth)
                            ),
                            AnalyticsStat(
                                title = "Promedio",
                                value = formatCurrency(data.overview.averageTransactionValue),
                                icon = Icons.Filled.TrendingUp,
                                color = MaterialTheme.colorScheme.tertiary,
                                trend = formatPercentage(data.overview.averageGrowth)
                            ),
                            AnalyticsStat(
                                title = "Confirmados",
                                value = "${data.performanceMetrics.confirmedPayments}",
                                icon = Icons.Filled.CheckCircle,
                                color = MaterialTheme.colorScheme.primary,
                                trend = formatPercentage(data.performanceMetrics.claimRate)
                            )
                        )
                    } ?: emptyList()
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(analyticsStats) { stat ->
                            AnalyticsStatCard(
                                title = stat.title,
                                value = stat.value,
                                icon = stat.icon,
                                color = stat.color,
                                trend = stat.trend
                            )
                        }
                    }
                }
            }
            
            // Gráfico de ventas por día
            item {
                Text(
                    text = "Ventas por Día",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Últimos 7 días",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Gráfico de barras con datos reales
                        val dailySales = analyticsData?.dailySales?.map { dailyData ->
                            DailySale(dailyData.dayName, dailyData.sales)
                        } ?: emptyList()
                        
                        if (dailySales.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                dailySales.forEach { day ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(24.dp)
                                                .height((day.amount / 10).dp.coerceAtLeast(4.dp))
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text(
                                            text = day.day,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        
                                        Text(
                                            text = formatCurrencyNoDecimals(day.amount),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No hay datos de ventas diarias disponibles",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            
            // Top vendedores (solo para admin)
            if (userProfile?.role == UserRole.ADMIN) {
                item {
                    Text(
                        text = "Top Vendedores",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val topSellers = analyticsData?.topSellers ?: emptyList()
                        if (topSellers.isNotEmpty()) {
                            topSellers.forEachIndexed { index, seller ->
                                TopSellerCard(
                                    rank = index + 1,
                                    sellerName = seller.sellerName,
                                    branchName = seller.branchName,
                                    totalSales = formatCurrency(seller.totalSales),
                                    totalTransactions = seller.transactionCount
                                )
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = "No hay datos de vendedores disponibles",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Métricas de rendimiento
            item {
                Text(
                    text = "Métricas de Rendimiento",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val performanceMetrics = analyticsData?.performanceMetrics
                    if (performanceMetrics != null) {
                        PerformanceMetricCard(
                            title = "Tiempo Promedio de Confirmación",
                            value = formatOneDecimalWithUnit(performanceMetrics.averageConfirmationTime, "min"),
                            icon = Icons.Filled.Schedule,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        PerformanceMetricCard(
                            title = "Tasa de Confirmación",
                            value = formatPercentage(performanceMetrics.claimRate),
                            icon = Icons.Filled.CheckCircle,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        
                        PerformanceMetricCard(
                            title = "Tasa de Rechazo",
                            value = formatPercentage(performanceMetrics.rejectionRate),
                            icon = Icons.Filled.Cancel,
                            color = MaterialTheme.colorScheme.error
                        )
                        
                        PerformanceMetricCard(
                            title = "Pagos Pendientes",
                            value = "${performanceMetrics.pendingPayments}",
                            icon = Icons.Filled.Schedule,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    } else {
                        PerformanceMetricCard(
                            title = "Tiempo Promedio de Confirmación",
                            value = "Cargando...",
                            icon = Icons.Filled.Schedule,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        PerformanceMetricCard(
                            title = "Tasa de Confirmación",
                            value = "Cargando...",
                            icon = Icons.Filled.CheckCircle,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    PerformanceMetricCard(
                        title = "Transacciones por Hora",
                        value = "12.5",
                        icon = Icons.Filled.Speed,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            // Espacio adicional
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun AnalyticsStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    trend: String
) {
    Card(
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = trend,
                style = MaterialTheme.typography.bodySmall,
                color = if (trend.startsWith("+")) 
                    MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TopSellerCard(
    rank: Int,
    sellerName: String,
    branchName: String,
    totalSales: String,
    totalTransactions: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ranking
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sellerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = branchName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = totalSales,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "$totalTransactions transacciones",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PerformanceMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// Datos de ejemplo
@Composable
private fun getAnalyticsStats(transactions: List<YapeTransaction>): List<AnalyticsStat> {
    val totalAmount = transactions.sumOf { it.amount }
    val totalCount = transactions.size
    
    return listOf(
        AnalyticsStat("Total Vendido", formatCurrency(totalAmount), Icons.Filled.AttachMoney, MaterialTheme.colorScheme.primary, "+12.5%"),
        AnalyticsStat("Transacciones", "$totalCount", Icons.Filled.Receipt, MaterialTheme.colorScheme.secondary, "+8.2%"),
        AnalyticsStat("Promedio", formatCurrency(if (totalCount > 0) totalAmount / totalCount else 0.0), Icons.Filled.TrendingUp, MaterialTheme.colorScheme.tertiary, "+3.1%"),
        AnalyticsStat("Confirmados", "${(totalCount * 0.94).toInt()}", Icons.Filled.CheckCircle, MaterialTheme.colorScheme.primary, "+1.8%")
    )
}

@Composable
private fun getDailySales(): List<DailySale> {
    return listOf(
        DailySale("Lun", 120.0),
        DailySale("Mar", 150.0),
        DailySale("Mié", 180.0),
        DailySale("Jue", 200.0),
        DailySale("Vie", 250.0),
        DailySale("Sáb", 300.0),
        DailySale("Dom", 220.0)
    )
}

@Composable
private fun getTopSellers(): List<TopSeller> {
    return listOf(
        TopSeller("María González", "Sucursal Norte", "S/ 1,250.50", 45),
        TopSeller("Carlos López", "Sucursal Sur", "S/ 890.25", 32),
        TopSeller("Ana Martínez", "Sucursal Centro", "S/ 650.75", 28),
        TopSeller("Luis Rodríguez", "Sucursal Este", "S/ 420.00", 19)
    )
}

data class AnalyticsStat(
    val title: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val trend: String
)

data class DailySale(
    val day: String,
    val amount: Double
)

data class TopSeller(
    val name: String,
    val branch: String,
    val totalSales: String,
    val totalTransactions: Int
)
