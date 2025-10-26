package org.sysarp.project.ui.common.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import org.sysarp.project.ui.seller.screens.analytics.SellerAnalyticsSections
import org.sysarp.project.ui.common.components.topbar.TopBarComponent
import org.sysarp.project.ui.common.components.charts.hourly.sales.HourlySalesChart
import org.sysarp.project.ui.common.components.charts.PerformanceComparisonChart
import org.sysarp.project.data.HourlySalesData
import org.sysarp.project.utils.convertPeriodToDates
import org.sysarp.project.ui.common.components.DateFilterComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull

/**
 * Funciones de parsing para convertir JSON en datos estructurados
 */
fun parseWeeklySalesData(jsonData: String): List<DailySalesData> {
    return try {
        val json = Json.parseToJsonElement(jsonData).jsonObject
        val data = json["data"]?.jsonObject
        val weeklySales = data?.get("weeklySales")
        
        if (weeklySales != null) {
            // Parsear datos reales del servidor
            val weeklySalesArray = weeklySales.jsonArray
            weeklySalesArray.mapNotNull { item ->
                try {
                    val itemObj = item.jsonObject
                    DailySalesData(
                        date = itemObj["date"]?.jsonPrimitive?.content ?: "",
                        dayName = itemObj["dayName"]?.jsonPrimitive?.content ?: "",
                        sales = itemObj["sales"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                        transactions = itemObj["transactions"]?.jsonPrimitive?.intOrNull ?: 0
                    )
                } catch (e: Exception) {
                    println("[PARSING] ❌ Error parseando item de Weekly Sales: ${e.message}")
                    null
                }
            }
        } else {
            println("[PARSING] ⚠️ No se encontraron datos de Weekly Sales en la respuesta")
            emptyList()
        }
    } catch (e: Exception) {
        println("[PARSING] ❌ Error parseando Weekly Sales: ${e.message}")
        println("[PARSING] 📄 JSON recibido: ${jsonData.take(200)}...")
        emptyList()
    }
}

fun parseHourlySalesData(jsonData: String): List<HourlySalesData> {
    return try {
        val json = Json.parseToJsonElement(jsonData).jsonObject
        val data = json["data"]?.jsonObject
        val hourlySales = data?.get("hourlySales")
        
        when {
            hourlySales == null -> {
                println("[PARSING] ⚠️ hourlySales es null, retornando lista vacía")
                emptyList()
            }
            hourlySales is kotlinx.serialization.json.JsonNull -> {
                println("[PARSING] ⚠️ hourlySales es JsonNull, retornando lista vacía")
                emptyList()
            }
            hourlySales is kotlinx.serialization.json.JsonArray -> {
                println("[PARSING] ✅ hourlySales parseado exitosamente: ${hourlySales.size} elementos")
                hourlySales.mapNotNull { item ->
                    try {
                        val itemObj = item.jsonObject
                        HourlySalesData(
                            hour = itemObj["hour"]?.jsonPrimitive?.content ?: "",
                            sales = itemObj["sales"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                            transactions = itemObj["transactions"]?.jsonPrimitive?.intOrNull ?: 0
                        )
                    } catch (e: Exception) {
                        println("[PARSING] ❌ Error parseando item de Hourly Sales: ${e.message}")
                        null
                    }
                }
            }
            else -> {
                println("[PARSING] ❌ hourlySales no es un tipo válido: ${hourlySales::class.simpleName}")
                emptyList()
            }
        }
    } catch (e: Exception) {
        println("[PARSING] ❌ Error parseando Hourly Sales: ${e.message}")
        println("[PARSING] 📄 JSON recibido: ${jsonData.take(200)}...")
        emptyList()
    }
}

fun parseCompleteAnalyticsData(jsonData: String): AnalyticsData {
    return try {
        val json = Json.parseToJsonElement(jsonData).jsonObject
        val data = json["data"]?.jsonObject
        
        if (data != null) {
            // Parsear datos reales del servidor
            val overview = data["overview"]?.jsonObject
            val dailySales = data["dailySales"]?.jsonArray
            val performanceMetrics = data["performanceMetrics"]?.jsonObject
            val monthlySales = data["monthlySales"]?.jsonArray
            
            AnalyticsData(
                overview = AnalyticsOverview(
                    totalSales = overview?.get("totalSales")?.jsonPrimitive?.doubleOrNull ?: 0.0,
                    totalTransactions = overview?.get("totalTransactions")?.jsonPrimitive?.intOrNull ?: 0,
                    averageTransactionValue = overview?.get("averageTransactionValue")?.jsonPrimitive?.doubleOrNull ?: 0.0,
                    salesGrowth = overview?.get("salesGrowth")?.jsonPrimitive?.doubleOrNull ?: 0.0,
                    transactionGrowth = overview?.get("transactionGrowth")?.jsonPrimitive?.doubleOrNull ?: 0.0,
                    averageGrowth = overview?.get("averageGrowth")?.jsonPrimitive?.doubleOrNull ?: 0.0
                ),
                dailySales = dailySales?.mapNotNull { item ->
                    try {
                        val itemObj = item.jsonObject
                        DailySalesData(
                            date = itemObj["date"]?.jsonPrimitive?.content ?: "",
                            dayName = itemObj["dayName"]?.jsonPrimitive?.content ?: "",
                            sales = itemObj["sales"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                            transactions = itemObj["transactions"]?.jsonPrimitive?.intOrNull ?: 0
                        )
                    } catch (e: Exception) {
                        println("[PARSING] ❌ Error parseando item de Daily Sales: ${e.message}")
                        null
                    }
                } ?: emptyList(),
                performanceMetrics = PerformanceMetricsData(
                    averageConfirmationTime = performanceMetrics?.get("averageConfirmationTime")?.jsonPrimitive?.doubleOrNull ?: 0.0,
                    claimRate = performanceMetrics?.get("claimRate")?.jsonPrimitive?.doubleOrNull ?: 0.0,
                    rejectionRate = performanceMetrics?.get("rejectionRate")?.jsonPrimitive?.doubleOrNull ?: 0.0,
                    pendingPayments = performanceMetrics?.get("pendingPayments")?.jsonPrimitive?.intOrNull ?: 0,
                    confirmedPayments = performanceMetrics?.get("confirmedPayments")?.jsonPrimitive?.intOrNull ?: 0,
                    rejectedPayments = performanceMetrics?.get("rejectedPayments")?.jsonPrimitive?.intOrNull ?: 0
                ),
                monthlySales = monthlySales?.mapNotNull { item ->
                    try {
                        val itemObj = item.jsonObject
                        MonthlySalesData(
                            month = itemObj["month"]?.jsonPrimitive?.content ?: "",
                            sales = itemObj["sales"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                            transactions = itemObj["transactions"]?.jsonPrimitive?.intOrNull ?: 0
                        )
                    } catch (e: Exception) {
                        println("[PARSING] ❌ Error parseando item de Monthly Sales: ${e.message}")
                        null
                    }
                } ?: emptyList()
            )
        } else {
            println("[PARSING] ⚠️ No se encontraron datos de Complete Analytics en la respuesta")
            AnalyticsData(
                overview = AnalyticsOverview(0.0, 0, 0.0, 0.0, 0.0, 0.0),
                dailySales = emptyList(),
                performanceMetrics = PerformanceMetricsData(0.0, 0.0, 0.0, 0, 0, 0),
                monthlySales = emptyList()
            )
        }
    } catch (e: Exception) {
        println("[PARSING] ❌ Error parseando Complete Analytics: ${e.message}")
        println("[PARSING] 📄 JSON recibido: ${jsonData.take(200)}...")
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
            val detailsObj = performanceDetails.jsonObject
            PerformanceMetricsData(
                averageConfirmationTime = detailsObj["averageConfirmationTime"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                claimRate = detailsObj["claimRate"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                rejectionRate = detailsObj["rejectionRate"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                pendingPayments = detailsObj["pendingPayments"]?.jsonPrimitive?.intOrNull ?: 0,
                confirmedPayments = detailsObj["confirmedPayments"]?.jsonPrimitive?.intOrNull ?: 0,
                rejectedPayments = detailsObj["rejectedPayments"]?.jsonPrimitive?.intOrNull ?: 0
            )
        } else {
            println("[PARSING] ⚠️ No se encontraron datos de Performance Details en la respuesta")
            PerformanceMetricsData(0.0, 0.0, 0.0, 0, 0, 0)
        }
    } catch (e: Exception) {
        println("[PARSING] ❌ Error parseando Performance Details: ${e.message}")
        println("[PARSING] 📄 JSON recibido: ${jsonData.take(200)}...")
        PerformanceMetricsData(0.0, 0.0, 0.0, 0, 0, 0)
    }
}

fun parseDailySalesData(jsonData: String): List<DailySalesData> {
    return try {
        val json = Json.parseToJsonElement(jsonData).jsonObject
        val data = json["data"]?.jsonObject
        val dailySales = data?.get("dailySales")?.jsonArray
        
        if (dailySales != null) {
            dailySales.mapNotNull { item ->
                try {
                    val itemObj = item.jsonObject
                    DailySalesData(
                        date = itemObj["date"]?.jsonPrimitive?.content ?: "",
                        dayName = itemObj["dayName"]?.jsonPrimitive?.content ?: "",
                        sales = itemObj["sales"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                        transactions = itemObj["transactions"]?.jsonPrimitive?.intOrNull ?: 0
                    )
                } catch (e: Exception) {
                    println("[PARSING] ❌ Error parseando item de Daily Sales: ${e.message}")
                    null
                }
            }
        } else {
            println("[PARSING] ⚠️ No se encontraron datos de Daily Sales en la respuesta")
            emptyList()
        }
    } catch (e: Exception) {
        println("[PARSING] ❌ Error parseando Daily Sales: ${e.message}")
        println("[PARSING] 📄 JSON recibido: ${jsonData.take(200)}...")
        emptyList()
    }
}

fun parseMonthlySalesData(jsonData: String): List<MonthlySalesData> {
    return try {
        val json = Json.parseToJsonElement(jsonData).jsonObject
        val data = json["data"]?.jsonObject
        val monthlySales = data?.get("monthlySales")?.jsonArray
        
        if (monthlySales != null) {
            monthlySales.mapNotNull { item ->
                try {
                    val itemObj = item.jsonObject
                    MonthlySalesData(
                        month = itemObj["month"]?.jsonPrimitive?.content ?: "0",
                        sales = itemObj["sales"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                        transactions = itemObj["transactions"]?.jsonPrimitive?.intOrNull ?: 0
                    )
                } catch (e: Exception) {
                    println("[PARSING] ❌ Error parseando item de Monthly Sales: ${e.message}")
                    null
                }
            }
        } else {
            println("[PARSING] ⚠️ No se encontraron datos de Monthly Sales en la respuesta")
            emptyList()
        }
    } catch (e: Exception) {
        println("[PARSING] ❌ Error parseando Monthly Sales: ${e.message}")
        println("[PARSING] 📄 JSON recibido: ${jsonData.take(200)}...")
        emptyList()
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
    var dailySalesData by remember { mutableStateOf<String?>(null) }
    var monthlySalesData by remember { mutableStateOf<String?>(null) }
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
                urls.dailySales?.let { url ->
                    async {
                        try {
                            val result = statsService.getAnalyticsFromUrl(url, token)
                            result.fold(
                                onSuccess = { data ->
                                    dailySalesData = data
                                    println("[ADMIN_NEW_ROUTES] ✅ Daily Sales cargado: ${data.take(100)}...")
                                },
                                onFailure = { error ->
                                    println("[ADMIN_NEW_ROUTES] ❌ Error cargando Daily Sales: ${error.message}")
                                }
                            )
                        } catch (e: Exception) {
                            println("[ADMIN_NEW_ROUTES] ❌ Excepción cargando Daily Sales: ${e.message}")
                        }
                    }
                },
                urls.monthlySales?.let { url ->
                    async {
                        try {
                            val result = statsService.getAnalyticsFromUrl(url, token)
                            result.fold(
                                onSuccess = { data ->
                                    monthlySalesData = data
                                    println("[ADMIN_NEW_ROUTES] ✅ Monthly Sales cargado: ${data.take(100)}...")
                                },
                                onFailure = { error ->
                                    println("[ADMIN_NEW_ROUTES] ❌ Error cargando Monthly Sales: ${error.message}")
                                }
                            )
                        } catch (e: Exception) {
                            println("[ADMIN_NEW_ROUTES] ❌ Excepción cargando Monthly Sales: ${e.message}")
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

/**
 * Dashboard de Analytics unificado que funciona tanto para Admin como para Seller
 * Incluye filtro de fechas y visualización de gráficos y estadísticas
 */
@Composable
fun AnalyticsDashboardScreen(
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
    
    // Estados para datos adicionales
    var dailySalesData by remember { mutableStateOf<String?>(null) }
    var monthlySalesData by remember { mutableStateOf<String?>(null) }
    
    // Estados para el filtro de fechas
    var selectedDateRange by remember { mutableStateOf("Último mes") }
    var showCalendar by remember { mutableStateOf(false) }
    
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
    
    // Función para cargar datos con filtro de fechas
    fun loadAnalyticsData() {
        isLoading = true
        analyticsUrls = null
        serverResponseData = null
        errorMessage = null
        
        scope.launch {
            try {
                val token = currentAccessToken
                if (token != null) {
                    // Obtener estadísticas unificadas usando el filtro de fechas seleccionado
                    val (startDate, endDate) = convertPeriodToDates(selectedDateRange)
                
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
                            
                            // Cargar datos adicionales si hay URLs disponibles
                            response.data.urls.dailySales?.let { url ->
                                scope.launch {
                                    statsService.getAnalyticsFromUrl(url, token).fold(
                                        onSuccess = { data -> dailySalesData = data },
                                        onFailure = { /* Ignorar errores silenciosamente */ }
                                    )
                                }
                            }
                            
                            response.data.urls.monthlySales?.let { url ->
                                scope.launch {
                                    statsService.getAnalyticsFromUrl(url, token).fold(
                                        onSuccess = { data -> monthlySalesData = data },
                                        onFailure = { /* Ignorar errores silenciosamente */ }
                                    )
                                }
                            }
                            
                            isLoading = false
                            println("[UNIFIED_ANALYTICS] ✅ Datos cargados exitosamente para $userType: $userId con filtro: $selectedDateRange")
                        },
                        onFailure = { error ->
                            errorMessage = error.message ?: "Error desconocido"
                            isLoading = false
                            println("[UNIFIED_ANALYTICS] ❌ Error: ${error.message}")
                        }
                    )
                } else {
                    errorMessage = "Token de acceso no disponible"
                    isLoading = false
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error desconocido"
                isLoading = false
                println("[UNIFIED_ANALYTICS] ❌ Error: ${e.message}")
            }
        }
    }
    
    // Cargar datos iniciales
    LaunchedEffect(userId, userType) {
        loadAnalyticsData()
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
            // Filtro de fechas
            DateFilterComponent(
                selectedDateRange = selectedDateRange,
                onDateRangeSelected = { period ->
                    selectedDateRange = period
                    loadAnalyticsData()
                },
                showCalendar = showCalendar,
                onShowCalendar = { showCalendar = true },
                onDismissCalendar = { showCalendar = false },
                title = "Filtrar analytics por fecha",
                description = "Selecciona un período para filtrar los datos de analytics"
            )
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
                                    totalSales = response.data.overview.confirmedSales,
                                    totalTransactions = response.data.overview.totalTransactions,
                                    averageTransactionValue = response.data.overview.averageTransactionValue,
                                    salesGrowth = response.data.overview.salesGrowth,
                                    transactionGrowth = response.data.overview.transactionGrowth,
                                    averageGrowth = response.data.overview.averageGrowth
                                ),
                                dailySales = dailySalesData?.let { parseDailySalesData(it) } ?: emptyList(),
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
                                monthlySales = monthlySalesData?.let { parseMonthlySalesData(it) } ?: emptyList()
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

