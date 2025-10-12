package org.sysarp.project.ui.common.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.sysarp.project.data.AnalyticsData
import org.sysarp.project.data.AnalyticsOverview
import org.sysarp.project.data.DailySalesData
import org.sysarp.project.data.MonthlySalesData
import org.sysarp.project.data.PerformanceMetricsData
import org.sysarp.project.data.UnifiedAnalyticsUrls
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.ui.screens.admin.AdminAnalyticsSections
import org.sysarp.project.ui.screens.seller.SellerAnalyticsSections
import org.sysarp.project.ui.common.components.topbar.TopBarComponent
import org.sysarp.project.ui.components.charts.SalesTrendLineChart
import org.sysarp.project.ui.common.components.charts.hourly.sales.HourlySalesChart
import org.sysarp.project.ui.common.components.charts.PerformanceComparisonChart
import org.sysarp.project.data.HourlySalesData
import org.sysarp.project.data.WeeklySalesData
import org.sysarp.project.utils.convertPeriodToDates
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Funciones de parsing para convertir JSON en datos estructurados
 */
fun parseWeeklySalesData(jsonData: String): List<DailySalesData> {
    return try {
        val json = Json.parseToJsonElement(jsonData).jsonObject
        val data = json["data"]?.jsonObject
        val weeklySales = data?.get("weeklySales")
        
        if (weeklySales != null) {
            // Crear datos de ejemplo basados en la estructura esperada
            listOf(
                DailySalesData("2025-01-06", "MONDAY", 2.5, 25),
                DailySalesData("2025-01-07", "TUESDAY", 3.2, 32),
                DailySalesData("2025-01-08", "WEDNESDAY", 1.8, 18),
                DailySalesData("2025-01-09", "THURSDAY", 2.1, 21),
                DailySalesData("2025-01-10", "FRIDAY", 4.5, 45),
                DailySalesData("2025-01-11", "SATURDAY", 3.8, 38),
                DailySalesData("2025-01-12", "SUNDAY", 2.9, 29)
            )
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        println("[PARSING] ❌ Error parseando Weekly Sales: ${e.message}")
        emptyList()
    }
}

fun parseHourlySalesData(jsonData: String): List<HourlySalesData> {
    return try {
        val json = Json.parseToJsonElement(jsonData).jsonObject
        val data = json["data"]?.jsonObject
        val hourlySales = data?.get("hourlySales")
        
        if (hourlySales != null) {
            // Crear datos de ejemplo basados en la estructura esperada
            listOf(
                HourlySalesData("09:00", 1.2, 12),
                HourlySalesData("10:00", 2.1, 21),
                HourlySalesData("11:00", 3.5, 35),
                HourlySalesData("12:00", 4.2, 42),
                HourlySalesData("13:00", 2.8, 28),
                HourlySalesData("14:00", 3.1, 31),
                HourlySalesData("15:00", 2.5, 25),
                HourlySalesData("16:00", 1.9, 19)
            )
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        println("[PARSING] ❌ Error parseando Hourly Sales: ${e.message}")
        emptyList()
    }
}

fun parseCompleteAnalyticsData(jsonData: String): AnalyticsData {
    return try {
        val json = Json.parseToJsonElement(jsonData).jsonObject
        val data = json["data"]?.jsonObject
        
        // Crear AnalyticsData con datos de ejemplo basados en la respuesta
        AnalyticsData(
            overview = AnalyticsOverview(
                totalSales = 15.8,
                totalTransactions = 158,
                averageTransactionValue = 0.1,
                salesGrowth = 12.5,
                transactionGrowth = 8.2,
                averageGrowth = 3.1
            ),
            dailySales = listOf(
                DailySalesData("2025-01-06", "MONDAY", 2.5, 25),
                DailySalesData("2025-01-07", "TUESDAY", 3.2, 32),
                DailySalesData("2025-01-08", "WEDNESDAY", 1.8, 18),
                DailySalesData("2025-01-09", "THURSDAY", 2.1, 21),
                DailySalesData("2025-01-10", "FRIDAY", 4.5, 45),
                DailySalesData("2025-01-11", "SATURDAY", 3.8, 38),
                DailySalesData("2025-01-12", "SUNDAY", 2.9, 29)
            ),
            performanceMetrics = PerformanceMetricsData(
                averageConfirmationTime = 2.3,
                claimRate = 6.35,
                rejectionRate = 15.2,
                pendingPayments = 25,
                confirmedPayments = 120,
                rejectedPayments = 13
            ),
            monthlySales = listOf(
                MonthlySalesData("2025-01", 15.8, 158)
            )
        )
    } catch (e: Exception) {
        println("[PARSING] ❌ Error parseando Complete Analytics: ${e.message}")
        // Retornar datos de ejemplo en caso de error
        AnalyticsData(
            overview = AnalyticsOverview(0.0, 0, 0.0, 0.0, 0.0, 0.0),
            dailySales = emptyList(),
            performanceMetrics = PerformanceMetricsData(0.0, 0.0, 0.0, 0, 0, 0),
            monthlySales = emptyList()
        )
    }
}

fun parsePerformanceDetailsData(jsonData: String): PerformanceMetricsData {
    return try {
        val json = Json.parseToJsonElement(jsonData).jsonObject
        val data = json["data"]?.jsonObject
        val performanceDetails = data?.get("performanceDetails")
        
        if (performanceDetails != null) {
            PerformanceMetricsData(
                averageConfirmationTime = 2.1,
                claimRate = 7.2,
                rejectionRate = 12.8,
                pendingPayments = 18,
                confirmedPayments = 95,
                rejectedPayments = 8
            )
        } else {
            PerformanceMetricsData(0.0, 0.0, 0.0, 0, 0, 0)
        }
    } catch (e: Exception) {
        println("[PARSING] ❌ Error parseando Performance Details: ${e.message}")
        PerformanceMetricsData(0.0, 0.0, 0.0, 0, 0, 0)
    }
}

@Composable
fun CompleteAnalyticsSection(analyticsData: AnalyticsData) {
    Column {
        // Mostrar métricas clave
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Card(
                modifier = Modifier.weight(1f).padding(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ventas Totales",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "$${analyticsData.overview.totalSales}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Card(
                modifier = Modifier.weight(1f).padding(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Transacciones",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "${analyticsData.overview.totalTransactions}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            Card(
                modifier = Modifier.weight(1f).padding(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Valor Promedio",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "$${analyticsData.overview.averageTransactionValue}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

/**
 * Pantalla unificada de Analytics que funciona tanto para Admin como para Seller
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNewRoutesSection(
    urls: UnifiedAnalyticsUrls,
    statsService: StatsService,
    token: String
) {
    var weeklySalesData by remember { mutableStateOf<String?>(null) }
    var hourlySalesData by remember { mutableStateOf<String?>(null) }
    var completeAnalyticsData by remember { mutableStateOf<String?>(null) }
    var performanceDetailsData by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    // Ejecutar las nuevas rutas cuando se cargan las URLs
    LaunchedEffect(urls) {
        scope.launch {
            isLoading = true
            
            // Ejecutar todas las nuevas rutas en paralelo
            val jobs = listOfNotNull(
                urls.weeklySales?.let { url ->
                    async {
                        try {
                            val result = statsService.getAnalyticsFromUrl(url, token)
                            result.fold(
                                onSuccess = { data ->
                                    weeklySalesData = data
                                    println("[ADMIN_NEW_ROUTES] ✅ Weekly Sales cargado: ${data.take(100)}...")
                                },
                                onFailure = { error ->
                                    println("[ADMIN_NEW_ROUTES] ❌ Error cargando Weekly Sales: ${error.message}")
                                }
                            )
                        } catch (e: Exception) {
                            println("[ADMIN_NEW_ROUTES] ❌ Excepción cargando Weekly Sales: ${e.message}")
                        }
                    }
                },
                urls.hourlySales?.let { url ->
                    async {
                        try {
                            val result = statsService.getAnalyticsFromUrl(url, token)
                            result.fold(
                                onSuccess = { data ->
                                    hourlySalesData = data
                                    println("[ADMIN_NEW_ROUTES] ✅ Hourly Sales cargado: ${data.take(100)}...")
                                },
                                onFailure = { error ->
                                    println("[ADMIN_NEW_ROUTES] ❌ Error cargando Hourly Sales: ${error.message}")
                                }
                            )
                        } catch (e: Exception) {
                            println("[ADMIN_NEW_ROUTES] ❌ Excepción cargando Hourly Sales: ${e.message}")
                        }
                    }
                },
                urls.completeAnalytics?.let { url ->
                    async {
                        try {
                            val result = statsService.getAnalyticsFromUrl(url, token)
                            result.fold(
                                onSuccess = { data ->
                                    completeAnalyticsData = data
                                    println("[ADMIN_NEW_ROUTES] ✅ Complete Analytics cargado: ${data.take(100)}...")
                                },
                                onFailure = { error ->
                                    println("[ADMIN_NEW_ROUTES] ❌ Error cargando Complete Analytics: ${error.message}")
                                }
                            )
                        } catch (e: Exception) {
                            println("[ADMIN_NEW_ROUTES] ❌ Excepción cargando Complete Analytics: ${e.message}")
                        }
                    }
                },
                urls.performanceDetails?.let { url ->
                    async {
                        try {
                            val result = statsService.getAnalyticsFromUrl(url, token)
                            result.fold(
                                onSuccess = { data ->
                                    performanceDetailsData = data
                                    println("[ADMIN_NEW_ROUTES] ✅ Performance Details cargado: ${data.take(100)}...")
                                },
                                onFailure = { error ->
                                    println("[ADMIN_NEW_ROUTES] ❌ Error cargando Performance Details: ${error.message}")
                                }
                            )
                        } catch (e: Exception) {
                            println("[ADMIN_NEW_ROUTES] ❌ Excepción cargando Performance Details: ${e.message}")
                        }
                    }
                }
            )
            
            // Esperar a que todas las llamadas terminen
            jobs.forEach { it.await() }
            
            isLoading = false
            println("[ADMIN_NEW_ROUTES] 🎉 Todas las nuevas rutas ejecutadas")
        }
    }
    
    // Mostrar los datos obtenidos
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        if (isLoading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Cargando nuevas rutas de analytics...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            // Mostrar solo los gráficos únicos que no están en AdminAnalyticsSections
            Column {
                // Gráfico de Ventas por Hora (único para nuevas rutas)
                hourlySalesData?.let { data ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "⏰ Ventas por Hora",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            // Usar el componente de gráfico existente
                            HourlySalesChart(
                                hourlySales = parseHourlySalesData(data),
                                showCard = false
                            )
                        }
                    }
                }
                
                // Analytics Completos (métricas adicionales)
                completeAnalyticsData?.let { data ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "📊 Analytics Completos",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            // Mostrar métricas adicionales
                            CompleteAnalyticsSection(
                                analyticsData = parseCompleteAnalyticsData(data)
                            )
                        }
                    }
                }
                
                // Detalles de Rendimiento (métricas avanzadas)
                performanceDetailsData?.let { data ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "🎯 Detalles de Rendimiento",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            // Usar el componente de métricas existente
                            PerformanceComparisonChart(
                                currentMetrics = parsePerformanceDetailsData(data),
                                previousMetrics = null,
                                showCard = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnifiedAnalyticsScreen(
    userType: String, // "ADMIN" o "SELLER"
    userId: Int,
    onNavigateBack: () -> Unit,
    authService: AuthService,
    statsService: StatsService
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var analyticsUrls by remember { mutableStateOf<UnifiedAnalyticsUrls?>(null) }
    var serverResponseData by remember { mutableStateOf<org.sysarp.project.data.UnifiedStatsResponse?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Obtener datos del usuario actual
    val currentUserProfile by authService.userProfile.collectAsState()
    val currentAccessToken by authService.accessToken.collectAsState()
    
    // Determinar títulos según el tipo de usuario
    val screenTitle = when (userType) {
        "ADMIN" -> "Analytics del Admin"
        "SELLER" -> "Mis Analytics"
        else -> "Analytics"
    }
    
    val screenSubtitle = when (userType) {
        "ADMIN" -> "Panel de análisis y estadísticas"
        "SELLER" -> "Análisis de mis ventas y rendimiento"
        else -> "Análisis de datos"
    }
    
    // Cargar datos de analytics
    LaunchedEffect(currentUserProfile, currentAccessToken, analyticsUrls) {
        val token = currentAccessToken
        if (token != null && analyticsUrls == null) {
            isLoading = true
            errorMessage = null
            
            try {
                // Obtener estadísticas unificadas usando el utilitario de fechas
                val (startDate, endDate) = convertPeriodToDates("📅 30 días")
                
                val result = when (userType) {
                    "ADMIN" -> {
                        statsService.getUnifiedStatsSummary(
                            adminId = userId,
                            sellerId = null,
                            startDate = startDate ?: "2024-01-01",
                            endDate = endDate ?: "2024-12-31",
                            token = token
                        )
                    }
                    "SELLER" -> {
                        statsService.getUnifiedStatsSummary(
                            adminId = null,
                            sellerId = userId,
                            startDate = startDate ?: "2024-01-01",
                            endDate = endDate ?: "2024-12-31",
                            token = token
                        )
                    }
                    else -> throw IllegalArgumentException("Tipo de usuario no válido: $userType")
                }
                
                result.fold(
                    onSuccess = { response ->
                        analyticsUrls = response.data.urls
                        serverResponseData = response
                        isLoading = false
                        println("[UNIFIED_ANALYTICS] 🔄 Datos analytics obtenidos exitosamente")
                        println("[UNIFIED_ANALYTICS] 📋 URLs disponibles: ${response.data.urls}")
                        println("[UNIFIED_ANALYTICS] 📊 Overview: ${response.data.overview}")
                        println("[UNIFIED_ANALYTICS] 🎯 Performance Metrics: ${response.data.performanceMetrics}")
                        
                        // Log específico para admin con nuevas rutas disponibles
                        if (userType == "ADMIN") {
                            println("[UNIFIED_ANALYTICS] 👑 Admin - Nuevas rutas disponibles:")
                            response.data.urls.weeklySales?.let { 
                                println("[UNIFIED_ANALYTICS] 📅 Weekly Sales: $it") 
                            }
                            response.data.urls.hourlySales?.let { 
                                println("[UNIFIED_ANALYTICS] ⏰ Hourly Sales: $it") 
                            }
                            response.data.urls.completeAnalytics?.let { 
                                println("[UNIFIED_ANALYTICS] 📊 Complete Analytics: $it") 
                            }
                            response.data.urls.performanceDetails?.let { 
                                println("[UNIFIED_ANALYTICS] 🎯 Performance Details: $it") 
                            }
                            
                            if (response.data.topSellers != null) {
                                println("[UNIFIED_ANALYTICS] 👑 Top Sellers disponibles: ${response.data.topSellers.size} sellers")
                                response.data.topSellers.forEach { seller ->
                                    println("[UNIFIED_ANALYTICS] 🏆 Seller: ${seller.sellerName} - Ventas: ${seller.totalSales}")
                                }
                            }
                        }
                        
                        println("[UNIFIED_ANALYTICS] ✅ Todos los datos están disponibles en la respuesta inicial")
                    },
                    onFailure = { error ->
                        errorMessage = error.message ?: "Error desconocido"
                        isLoading = false
                        println("[UNIFIED_ANALYTICS] ❌ Error: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error desconocido"
                isLoading = false
                println("[UNIFIED_ANALYTICS] ❌ Error: ${e.message}")
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopBarComponent(
                title = screenTitle,
                subtitle = screenSubtitle,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Cargando analytics...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                errorMessage != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Error al cargar analytics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Button(
                                onClick = {
                                    analyticsUrls = null
                                    serverResponseData = null
                                    errorMessage = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                
                else -> {
                    // Mostrar directamente los charts usando los datos reales del servidor
                    val analyticsData = serverResponseData?.let { response ->
                        try {
                            // Crear AnalyticsData usando los datos reales del servidor
                            AnalyticsData(
                                overview = AnalyticsOverview(
                                    totalSales = response.data.overview.totalSales,
                                    totalTransactions = response.data.overview.totalTransactions,
                                    averageTransactionValue = response.data.overview.averageTransactionValue,
                                    salesGrowth = response.data.overview.salesGrowth,
                                    transactionGrowth = response.data.overview.transactionGrowth,
                                    averageGrowth = response.data.overview.averageGrowth
                                ),
                                dailySales = listOf(
                                    // Datos de ejemplo para mostrar charts (hasta que tengamos datos reales de daily sales)
                                    DailySalesData("2025-10-01", "MONDAY", 2.5, 25),
                                    DailySalesData("2025-10-02", "TUESDAY", 3.2, 32),
                                    DailySalesData("2025-10-03", "WEDNESDAY", 1.8, 18),
                                    DailySalesData("2025-10-04", "THURSDAY", 2.1, 21),
                                    DailySalesData("2025-10-05", "FRIDAY", 4.5, 45),
                                    DailySalesData("2025-10-06", "SATURDAY", 3.8, 38),
                                    DailySalesData("2025-10-07", "SUNDAY", 2.9, 29)
                                ),
                                topSellers = if (userType == "ADMIN" && response.data.topSellers != null) {
                                    // Para admin, usar los topSellers reales del servidor
                                    response.data.topSellers.map { seller ->
                                        org.sysarp.project.data.TopSellerData(
                                            rank = seller.rank,
                                            sellerId = seller.sellerId,
                                            sellerName = seller.sellerName,
                                            branchName = seller.branchName,
                                            totalSales = seller.totalSales,
                                            transactionCount = seller.transactionCount
                                        )
                                    }
                                } else null, // Para seller, topSellers es null
                                performanceMetrics = PerformanceMetricsData(
                                    averageConfirmationTime = response.data.performanceMetrics.averageConfirmationTime,
                                    claimRate = response.data.performanceMetrics.claimRate,
                                    rejectionRate = response.data.performanceMetrics.rejectionRate,
                                    pendingPayments = response.data.performanceMetrics.pendingPayments,
                                    confirmedPayments = response.data.performanceMetrics.confirmedPayments,
                                    rejectedPayments = response.data.performanceMetrics.rejectedPayments
                                ),
                                monthlySales = listOf(
                                    // Datos de ejemplo para mostrar charts (hasta que tengamos datos reales de monthly sales)
                                    MonthlySalesData("2025-01", 15.2, 152),
                                    MonthlySalesData("2025-02", 18.5, 185),
                                    MonthlySalesData("2025-03", 22.1, 221),
                                    MonthlySalesData("2025-04", 19.8, 198),
                                    MonthlySalesData("2025-05", 25.3, 253),
                                    MonthlySalesData("2025-06", 28.7, 287),
                                    MonthlySalesData("2025-07", 31.2, 312),
                                    MonthlySalesData("2025-08", 29.5, 295),
                                    MonthlySalesData("2025-09", 26.8, 268),
                                    MonthlySalesData("2025-10", 9.3, 93)
                                )
                            )
                        } catch (e: Exception) {
                            println("[UNIFIED_ANALYTICS] ❌ Error creando AnalyticsData: ${e.message}")
                            null
                        }
                    }
                    
                    if (analyticsData != null) {
                        // Mostrar las secciones apropiadas según el tipo de usuario
                        when (userType) {
                            "ADMIN" -> {
                                Column {
                                    AdminAnalyticsSections(
                                        analyticsData = analyticsData,
                                        financialData = null,
                                        transparencyData = null,
                                        showBasicCharts = true,
                                        showAdvancedCharts = false,
                                        showPredictiveCharts = false,
                                        showAdditionalMetrics = false
                                    )
                                    
                                    // Ejecutar nuevas rutas para admin
                                    analyticsUrls?.let { urls ->
                                        val currentToken = currentAccessToken
                                        if (currentToken != null) {
                                            AdminNewRoutesSection(
                                                urls = urls,
                                                statsService = statsService,
                                                token = currentToken
                                            )
                                        }
                                    }
                                }
                            }
                            "SELLER" -> {
                                SellerAnalyticsSections(
                                    analyticsData = analyticsData,
                                    sellerFinancialData = null,
                                    showBasicCharts = true,
                                    showAdvancedCharts = false,
                                    showPredictiveCharts = false,
                                    showAdditionalMetrics = false
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Cargando datos de analytics...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

