package org.sysarp.project.ui.admin.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.affiliation.AffiliationService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.branch.BranchService
import org.sysarp.project.service.qr.QRService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.ui.components.GenerateAffiliationCodeDialog
import org.sysarp.project.viewmodel.admin.AdminDashboardViewModel


/**
 * Pantalla principal del dashboard de administración
 * Refactorizada para usar Koin para inyección de dependencias
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    // Navigation callbacks remain as parameters
    onNavigateToBranchManagement: () -> Unit,
    onNavigateToSellerManagement: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeactivationRequests: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onLogout: () -> Unit,
    // Dependencies are now injected by Koin
    authService: AuthService = koinInject(),
    sellerService: SellerService = koinInject(),
    statsService: StatsService = koinInject(),
    affiliationService: AffiliationService = koinInject(),
    qrService: QRService = koinInject(),
    branchService: BranchService = koinInject(),
    webSocketService: PaymentWebSocketService = koinInject()
) {
    // Crear el ViewModel con las dependencias inyectadas
    val viewModel = remember {
        AdminDashboardViewModel(
            authService = authService,
            sellerService = sellerService,
            statsService = statsService,
            affiliationService = affiliationService,
            branchService = branchService,
            webSocketService = webSocketService
        )
    }
    
    // Observar estados del ViewModel
    val userProfile by viewModel.userProfile.collectAsState()
    val accessToken by viewModel.accessToken.collectAsState()
    val dashboardStats by viewModel.dashboardStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Estados de afiliación
    val showAffiliationDialog by viewModel.showAffiliationDialog.collectAsState()
    val isLoadingAffiliation by viewModel.isLoadingAffiliation.collectAsState()
    val affiliationError by viewModel.affiliationError.collectAsState()
    val generatedAffiliationCode by viewModel.generatedAffiliationCode.collectAsState()
    val branches by viewModel.branches.collectAsState()

    // Inicializar el ViewModel
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {


            // Contenido principal del dashboard mejorado
            if (isLoading) {
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
                            text = "Cargando dashboard...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Error al cargar dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = errorMessage ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = { viewModel.refreshDashboard() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            } else {
                // Dashboard del administrador con el mismo estilo que el del vendedor
                org.sysarp.project.ui.admin.components.dashboard.AdminDashboardContent(
                    userProfile = userProfile,
                    enhancedData = viewModel.getEnhancedAdminData(),
                    connectionState = org.sysarp.project.service.websocket.WebSocketConnectionState.CONNECTED, // TODO: Implementar estado real
                    isLoading = isLoading,
                    onNavigateToAnalytics = onNavigateToAnalytics,
                    onNavigateToSellerManagement = onNavigateToSellerManagement,
                    onNavigateToPendingPayments = onNavigateToPendingPayments,
                    onNavigateToBranchManagement = onNavigateToBranchManagement,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToDeactivationRequests = onNavigateToDeactivationRequests,
                    onNavigateToBilling = onNavigateToBilling,
                    onShowAffiliationDialog = { viewModel.showAffiliationDialog() },
                    onLogout = onLogout
                )
            }
        }
        
        // Diálogo de generación de código de afiliación
        if (showAffiliationDialog) {
            GenerateAffiliationCodeDialog(
                isVisible = showAffiliationDialog,
                onDismiss = { viewModel.dismissAffiliationDialog() },
                onGenerate = { expirationHours: Int, maxUses: Int, branchId: Int, notes: String? ->
                    viewModel.generateAffiliationCode(branchId, expirationHours, maxUses, notes)
                },
                branches = branches,
                isLoading = isLoadingAffiliation,
                generatedCode = generatedAffiliationCode,
                errorMessage = affiliationError,
                authService = authService
            )
        }
    }
}
