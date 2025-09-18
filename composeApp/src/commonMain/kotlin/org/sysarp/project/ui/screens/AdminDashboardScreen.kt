package org.sysarp.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.affiliation.AffiliationService
import org.sysarp.project.data.AdminStatsData
import org.sysarp.project.data.AffiliationCodeData
import org.sysarp.project.ui.components.GenerateAffiliationCodeDialog
import androidx.compose.runtime.*
import org.sysarp.project.data.DeactivationRequest
import org.sysarp.project.data.QuickSummaryData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    authService: AuthService,
    sellerService: SellerService,
    statsService: org.sysarp.project.service.stats.StatsService,
    affiliationService: AffiliationService,
    qrService: org.sysarp.project.service.qr.QRService,
    branchService: org.sysarp.project.service.branch.BranchService,
    onNavigateToSellerManagement: () -> Unit,
    onNavigateToBranchManagement: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeactivationRequests: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    // Verificar sesión al entrar a la pantalla
    LaunchedEffect(Unit) {
        authService.updateActivity()
        if (!authService.isSessionValid()) {
            println("⏰ [DASHBOARD] Sesión expirada, cerrando sesión...")
            authService.logout()
            onLogout()
        }
    }
    
    // Verificación periódica de tokens (cada 2 minutos)
    LaunchedEffect(Unit) {
        while (true) {
            delay(120_000) // 2 minutos
            try {
                val refreshSuccess = authService.checkAndRefreshTokenIfNeeded()
                if (!refreshSuccess) {
                    println("⏰ [DASHBOARD] Error en refresh periódico, verificando sesión...")
                    if (!authService.isSessionValid()) {
                        println("⏰ [DASHBOARD] Sesión inválida después de refresh fallido, cerrando sesión...")
                        authService.logout()
                        onLogout()
                        break
                    }
                }
            } catch (e: Exception) {
                println("⏰ [DASHBOARD] Error en verificación periódica: ${e.message}")
            }
        }
    }
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Estado para las estadísticas rápidas
    var quickSummaryData by remember { mutableStateOf<QuickSummaryData?>(null) }
    var isLoadingStats by remember { mutableStateOf(false) }
    var statsError by remember { mutableStateOf("") }
    
    // Estado para vendedores conectados
    var connectedSellersData by remember { mutableStateOf<org.sysarp.project.data.ConnectedSellersData?>(null) }
    var isLoadingSellers by remember { mutableStateOf(false) }
    var sellersError by remember { mutableStateOf("") }
    
    // Estado para códigos de afiliación
    var showAffiliationDialog by remember { mutableStateOf(false) }
    var isLoadingAffiliation by remember { mutableStateOf(false) }
    var generatedAffiliationCode by remember { mutableStateOf<AffiliationCodeData?>(null) }
    var affiliationError by remember { mutableStateOf<String?>(null) }
    
    // Estados para QR
    var generatedQRCode by remember { mutableStateOf<org.sysarp.project.data.QRCodeData?>(null) }
    var qrError by remember { mutableStateOf<String?>(null) }
    var isLoadingQR by remember { mutableStateOf(false) }
    var showQRDialog by remember { mutableStateOf(false) }
    
    // Estado para sucursales
    var branches by remember { mutableStateOf<List<org.sysarp.project.data.BranchInfo>>(emptyList()) }
    var isLoadingBranches by remember { mutableStateOf(false) }
    
    // Cargar sucursales cuando se abre el diálogo
    LaunchedEffect(showAffiliationDialog, userProfile?.adminId, accessToken) {
        if (showAffiliationDialog && userProfile?.adminId != null && accessToken != null) {
            isLoadingBranches = true
            branchService.getBranches(
                adminId = userProfile!!.adminId!!.toInt(),
                accessToken = accessToken!!
            ).fold(
                onSuccess = { branchesData ->
                    branches = branchesData.branches
                    isLoadingBranches = false
                },
                onFailure = { error ->
                    isLoadingBranches = false
                }
            )
        }
    }
    
    // Cargar estadísticas rápidas
    LaunchedEffect(userProfile?.adminId, accessToken) {
        if (userProfile?.adminId != null && accessToken != null) {
            isLoadingStats = true
            statsError = ""
            
            statsService.getQuickSummary(
                adminId = userProfile!!.adminId!!.toInt(),
                token = accessToken!!
            ).fold(
                onSuccess = { response ->
                    quickSummaryData = response.data
                    isLoadingStats = false
                },
                onFailure = { error ->
                    statsError = error.message ?: "Error cargando estadísticas"
                    isLoadingStats = false
                }
            )
        }
    }
    
    // Cargar vendedores conectados
    LaunchedEffect(userProfile?.adminId, accessToken) {
        if (userProfile?.adminId != null && accessToken != null) {
            isLoadingSellers = true
            sellersError = ""
            
            sellerService.getConnectedSellers(
                adminId = userProfile!!.adminId!!.toInt(),
                token = accessToken!!
            ).fold(
                onSuccess = { response ->
                    connectedSellersData = response.data
                    isLoadingSellers = false
                },
                onFailure = { error ->
                    sellersError = error.message ?: "Error cargando vendedores"
                    isLoadingSellers = false
                }
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Dashboard Admin",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showAffiliationDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.QrCode,
                            contentDescription = "Generar código de afiliación"
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Mi Perfil"
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Configuración"
                        )
                    }
                    IconButton(onClick = {
                        coroutineScope.launch {
                            authService.logout()
                            onLogout()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con información del negocio
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
                            imageVector = Icons.Filled.Business,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = userProfile?.businessName ?: "Mi Negocio",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "Panel de Administración",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // Estadísticas rápidas
            item {
                Text(
                    text = "Estadísticas del Día",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                if (isLoadingStats) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (statsError.isNotEmpty()) {
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
                                text = "Error cargando estadísticas",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = statsError,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val quickStats = quickSummaryData?.let { data ->
                        listOf(
                            QuickStat(
                                title = "Total Vendido",
                                value = "S/ ${String.format("%.2f", data.totalSales)}",
                                icon = Icons.Filled.AttachMoney,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            QuickStat(
                                title = "Transacciones",
                                value = "${data.totalTransactions}",
                                icon = Icons.Filled.Payment,
                                color = MaterialTheme.colorScheme.secondary
                            ),
                            QuickStat(
                                title = "Promedio",
                                value = "S/ ${String.format("%.2f", data.averageTransactionValue)}",
                                icon = Icons.Filled.TrendingUp,
                                color = MaterialTheme.colorScheme.tertiary
                            ),
                            QuickStat(
                                title = "Pendientes",
                                value = "${data.pendingPayments}",
                                icon = Icons.Filled.Schedule,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    } ?: emptyList()
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(quickStats) { stat ->
                            StatCard(
                                title = stat.title,
                                value = stat.value,
                                icon = stat.icon,
                                color = stat.color
                            )
                        }
                    }
                }
            }
            
            // Solicitudes de baja pendientes
            item {
                var pendingRequests by remember { mutableStateOf<List<DeactivationRequest>>(emptyList()) }
                
                LaunchedEffect(Unit) {
                    sellerService.getPendingDeactivationRequests().fold(
                        onSuccess = { requests -> pendingRequests = requests },
                        onFailure = { /* Manejar error */ }
                    )
                }
                
                if (pendingRequests.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Solicitudes de Baja Pendientes",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Text(
                                text = "${'$'}{pendingRequests.size} vendedor(es) han solicitado baja",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Button(
                                onClick = onNavigateToDeactivationRequests,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Revisar Solicitudes",
                                    color = MaterialTheme.colorScheme.onError
                                )
                            }
                        }
                    }
                }
            }
            
            // Acciones principales
            item {
                Text(
                    text = "Acciones Principales",
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
                    ActionCard(
                        title = "Gestionar Vendedores",
                        subtitle = "Agregar, editar y generar códigos QR",
                        icon = Icons.Filled.People,
                        onClick = onNavigateToSellerManagement
                    )
                    
                    ActionCard(
                        title = "Gestionar Sucursales",
                        subtitle = "Crear, editar y administrar sucursales",
                        icon = Icons.Filled.Business,
                        onClick = onNavigateToBranchManagement
                    )
                    
                    ActionCard(
                        title = "Ver Analytics",
                        subtitle = "Reportes y estadísticas detalladas",
                        icon = Icons.Filled.Analytics,
                        onClick = onNavigateToAnalytics
                    )
                    
                    ActionCard(
                        title = "Pagos Pendientes",
                        subtitle = "Revisar y confirmar pagos",
                        icon = Icons.Filled.Payment,
                        onClick = onNavigateToPendingPayments
                    )
                    
                    ActionCard(
                        title = "Configuración",
                        subtitle = "Ajustes del sistema y perfil",
                        icon = Icons.Filled.Settings,
                        onClick = onNavigateToSettings
                    )
                }
            }
            
            // Vendedores conectados
            item {
                Text(
                    text = "Vendedores Conectados",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                if (isLoadingSellers) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (sellersError.isNotEmpty()) {
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
                                text = "Error cargando vendedores",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = sellersError,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val sellersData = connectedSellersData
                    if (sellersData != null) {
                        // Estadísticas de conexión
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ConnectionStatItem("Conectados", sellersData.totalConnected.toString(), Icons.Filled.CheckCircle)
                                ConnectionStatItem("Total", sellersData.connectedSellers.size.toString(), Icons.Filled.People)
                                ConnectionStatItem("Última actualización", formatTimestamp(sellersData.timestamp), Icons.Filled.Schedule)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Lista de vendedores
                        if (sellersData.connectedSellers.isEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.People,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Text(
                                        text = "No hay vendedores conectados",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        text = "Los vendedores aparecerán aquí cuando se conecten",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                                sellersData.connectedSellers.forEach { seller ->
                                    ConnectedSellerCard(
                                        sellerInfo = seller
                                    )
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No hay datos de vendedores",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Espacio adicional
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
    
    // Diálogo para generar códigos de afiliación
    GenerateAffiliationCodeDialog(
        isVisible = showAffiliationDialog,
        onDismiss = { 
            showAffiliationDialog = false
            generatedAffiliationCode = null
            affiliationError = null
        },
        onGenerate = { expirationHours, maxUses, branchId, notes ->
            if (userProfile?.adminId != null && accessToken != null) {
                isLoadingAffiliation = true
                affiliationError = null
                
                coroutineScope.launch {
                    affiliationService.generateAffiliationCode(
                        adminId = userProfile!!.adminId!!.toInt(),
                        branchId = branchId,
                        expirationHours = expirationHours,
                        maxUses = maxUses,
                        notes = notes ?: "",
                        accessToken = accessToken!!
                    ).fold(
                        onSuccess = { affiliationData ->
                            generatedAffiliationCode = affiliationData
                            isLoadingAffiliation = false
                        },
                        onFailure = { error ->
                            affiliationError = error.message ?: "Error generando código de afiliación"
                            isLoadingAffiliation = false
                        }
                    )
                }
            }
        },
        branches = branches,
        isLoading = isLoadingAffiliation,
        generatedCode = generatedAffiliationCode,
        errorMessage = affiliationError,
        onGenerateQR = { affiliationCode ->
            if (accessToken != null) {
                isLoadingQR = true
                qrError = null
                
                coroutineScope.launch {
                    qrService.generateQRFromAffiliationCode(
                        affiliationCode = affiliationCode,
                        accessToken = accessToken!!
                    ).fold(
                        onSuccess = { qrData ->
                            generatedQRCode = qrData
                            isLoadingQR = false
                            // Cerrar el diálogo de afiliación y mostrar el QR
                            showAffiliationDialog = false
                            showQRDialog = true
                        },
                        onFailure = { error ->
                            qrError = error.message ?: "Error generando código QR"
                            isLoadingQR = false
                        }
                    )
                }
            }
        },
        isLoadingQR = isLoadingQR,
        qrError = qrError
    )
    
    // Diálogo para mostrar QR generado
    if (showQRDialog && generatedQRCode != null) {
        ServerQRDisplayScreen(
            qrData = generatedQRCode!!,
            onNavigateBack = { 
                showQRDialog = false
                generatedQRCode = null
                qrError = null
            },
            onShareQR = { 
                // TODO: Implementar compartir QR
            },
            onInvalidateQR = { 
                // TODO: Implementar invalidar QR
                showQRDialog = false
                generatedQRCode = null
                qrError = null
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
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
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SellerStatusCard(
    sellerName: String,
    branchName: String,
    isOnline: Boolean,
    lastSeen: String,
    totalPayments: Int
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
            // Indicador de estado
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = if (isOnline) 
                            MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.outline,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sellerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = branchName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = if (isOnline) "En línea" else "Última vez: $lastSeen",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOnline) 
                        MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "$totalPayments pagos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Datos de ejemplo
@Composable
private fun getQuickStats(): List<QuickStat> {
    return listOf(
        QuickStat("Pagos Hoy", "S/ 1,250", Icons.Filled.Payment, MaterialTheme.colorScheme.primary),
        QuickStat("Vendedores", "5", Icons.Filled.People, MaterialTheme.colorScheme.secondary),
        QuickStat("Confirmados", "23", Icons.Filled.CheckCircle, MaterialTheme.colorScheme.tertiary),
        QuickStat("Pendientes", "2", Icons.Filled.Schedule, MaterialTheme.colorScheme.error)
    )
}

@Composable
private fun getConnectedSellers(): List<SellerInfo> {
    return listOf(
        SellerInfo("María González", "Sucursal Norte", true, "", 45),
        SellerInfo("Carlos López", "Sucursal Sur", true, "", 32),
        SellerInfo("Ana Martínez", "Sucursal Centro", false, "hace 5 min", 28),
        SellerInfo("Luis Rodríguez", "Sucursal Este", true, "", 19)
    )
}

data class QuickStat(
    val title: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color
)

data class SellerInfo(
    val name: String,
    val branch: String,
    val isOnline: Boolean,
    val lastSeen: String,
    val totalPayments: Int
)

@Composable
fun ConnectionStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ConnectedSellerCard(
    sellerInfo: org.sysarp.project.data.ConnectedSellerInfo
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de estado
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = if (sellerInfo.isConnected) 
                            MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.outline,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sellerInfo.sellerName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = sellerInfo.branchName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = sellerInfo.email,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = sellerInfo.phone,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = if (sellerInfo.isConnected) "En línea" else "Última vez: ${formatLastSeen(sellerInfo.lastSeen)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (sellerInfo.isConnected) 
                        MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatLastSeen(lastSeen: String): String {
    return try {
        // Formatear la fecha para mostrar de manera más amigable
        val date = java.time.Instant.parse(lastSeen)
        val now = java.time.Instant.now()
        val duration = java.time.Duration.between(date, now)
        
        when {
            duration.toMinutes() < 1 -> "hace menos de 1 min"
            duration.toMinutes() < 60 -> "hace ${duration.toMinutes()} min"
            duration.toHours() < 24 -> "hace ${duration.toHours()} h"
            else -> "hace ${duration.toDays()} días"
        }
    } catch (e: Exception) {
        "desconocido"
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        // Formatear timestamp para mostrar hora de actualización
        val date = java.time.Instant.parse(timestamp)
        val localTime = java.time.LocalDateTime.ofInstant(date, java.time.ZoneId.systemDefault())
        val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
        localTime.format(formatter)
    } catch (e: Exception) {
        "N/A"
    }
}
