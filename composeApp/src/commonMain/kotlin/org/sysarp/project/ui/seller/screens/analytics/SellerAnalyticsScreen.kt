package org.sysarp.project.ui.seller.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.sysarp.project.data.UnifiedAnalyticsUrls
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.stats.StatsService

/**
 * Pantalla de Analytics del Seller usando endpoint unificado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerAnalyticsScreen(
    authService: AuthService,
    statsService: StatsService,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var analyticsUrls by remember { mutableStateOf<UnifiedAnalyticsUrls?>(null) }
    var performanceData by remember { mutableStateOf<String?>(null) }
    var dailySalesData by remember { mutableStateOf<String?>(null) }
    var monthlySalesData by remember { mutableStateOf<String?>(null) }
    
    // Cargar datos iniciales
    LaunchedEffect(userProfile, accessToken) {
        val currentUserProfile = userProfile
        val currentAccessToken = accessToken
        
        if (currentUserProfile?.sellerId != null && currentAccessToken != null) {
            isLoading = true
            errorMessage = null
            
            try {
                // Obtener estadísticas unificadas con fechas por defecto
                val currentDate = java.time.LocalDate.now()
                val startOfYear = currentDate.withDayOfYear(1).toString()
                val endOfYear = currentDate.withDayOfYear(currentDate.lengthOfYear()).toString()
                
                val result = statsService.getUnifiedStatsSummary(
                    adminId = null,
                    sellerId = currentUserProfile.sellerId!!.toInt(),
                    startDate = startOfYear,
                    endDate = endOfYear,
                    token = currentAccessToken
                )
                
                result.fold(
                    onSuccess = { response ->
                        analyticsUrls = response.data.urls
                        isLoading = false
                        
                        // Cargar datos específicos de analytics
                        coroutineScope.launch {
                            loadAnalyticsData(
                                urls = response.data.urls,
                                token = currentAccessToken,
                                statsService = statsService,
                                onPerformanceLoaded = { performanceData = it },
                                onDailySalesLoaded = { dailySalesData = it },
                                onMonthlySalesLoaded = { monthlySalesData = it },
                                onError = { errorMessage = it }
                            )
                        }
                    },
                    onFailure = { error ->
                        errorMessage = error.message ?: "Error cargando analytics"
                        isLoading = false
                    }
                )
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error inesperado"
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Analytics") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("← Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Cargando mis analytics...")
                        }
                    }
                }
                
                errorMessage != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage ?: "Error desconocido",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Mis Analytics",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Performance Details (solo si está disponible)
                        analyticsUrls?.performanceDetails?.let { performanceUrl ->
                            item {
                                SellerAnalyticsCard(
                                    title = "Mi Rendimiento",
                                    data = performanceData,
                                    isLoading = performanceData == null
                                )
                            }
                        }
                        
                        // Daily Sales
                        item {
                            SellerAnalyticsCard(
                                title = "Mis Ventas Diarias",
                                data = dailySalesData,
                                isLoading = dailySalesData == null
                            )
                        }
                        
                        // Monthly Sales
                        item {
                            SellerAnalyticsCard(
                                title = "Mis Ventas Mensuales",
                                data = monthlySalesData,
                                isLoading = monthlySalesData == null
                            )
                        }
                        
                        // URLs disponibles
                        analyticsUrls?.let { urls ->
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = "URLs de Analytics Disponibles",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        urls.performanceDetails?.let { 
                                            Text(
                                                text = "Performance: $it",
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        Text(
                                            text = "Daily Sales: ${urls.dailySales}",
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "Monthly Sales: ${urls.monthlySales}",
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SellerAnalyticsCard(
    title: String,
    data: String?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            when {
                isLoading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cargando...",
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                
                data != null -> {
                    Text(
                        text = data,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                else -> {
                    Text(
                        text = "No hay datos disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private suspend fun loadAnalyticsData(
    urls: UnifiedAnalyticsUrls,
    token: String,
    statsService: StatsService,
    onPerformanceLoaded: (String) -> Unit,
    onDailySalesLoaded: (String) -> Unit,
    onMonthlySalesLoaded: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        // Cargar Performance Details (solo si está disponible)
        urls.performanceDetails?.let { performanceUrl ->
            statsService.getAnalyticsFromUrl(performanceUrl, token).fold(
                onSuccess = { onPerformanceLoaded(it) },
                onFailure = { onError("Error cargando performance: ${it.message}") }
            )
        }
        
        // Cargar Daily Sales
        statsService.getAnalyticsFromUrl(urls.dailySales, token).fold(
            onSuccess = { onDailySalesLoaded(it) },
            onFailure = { onError("Error cargando ventas diarias: ${it.message}") }
        )
        
        // Cargar Monthly Sales
        statsService.getAnalyticsFromUrl(urls.monthlySales, token).fold(
            onSuccess = { onMonthlySalesLoaded(it) },
            onFailure = { onError("Error cargando ventas mensuales: ${it.message}") }
        )
    } catch (e: Exception) {
        onError("Error cargando analytics: ${e.message}")
    }
}