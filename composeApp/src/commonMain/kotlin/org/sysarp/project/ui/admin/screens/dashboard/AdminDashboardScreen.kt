package org.sysarp.project.ui.admin.screens.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.affiliation.AffiliationService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.billing.BillingService
import org.sysarp.project.service.branch.BranchService
import org.sysarp.project.service.qr.QRService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.ui.components.GenerateAffiliationCodeDialog
import org.sysarp.project.ui.common.components.topbar.TopBarComponent
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
    webSocketService: PaymentWebSocketService = koinInject(),
    billingService: BillingService = koinInject()
) {
    // Crear el ViewModel con las dependencias inyectadas
    val viewModel = remember {
        AdminDashboardViewModel(
            authService = authService,
            sellerService = sellerService,
            statsService = statsService,
            affiliationService = affiliationService,
            branchService = branchService,
            billingService = billingService,
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
    
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopBarComponent(
                title = "Dashboard Admin",
                subtitle = "Panel de administración",
                menuItems = createTopBarMenuItems(
                    onShowAffiliationDialog = { viewModel.showAffiliationDialog() },
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToSettings = onNavigateToSettings,
                    onLogout = { handleLogout(viewModel, coroutineScope, onLogout) }
                ),
                showMenu = true
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header con información del negocio
            BusinessHeaderCard(
                businessName = userProfile?.businessName ?: "Mi Negocio"
            )

            // Contenido principal del dashboard usando el ViewModel
            DashboardContent(
                quickSummaryData = viewModel.getQuickSummaryData(),
                isLoadingStats = isLoading,
                statsError = errorMessage ?: "",
                connectedSellersData = null, // TODO: Implementar en el ViewModel
                isLoadingSellers = false, // TODO: Implementar en el ViewModel
                sellersError = "",
                pendingRequestsCount = 0, // TODO: Implementar en el ViewModel
                onNavigateToBranchManagement = onNavigateToBranchManagement,
                onNavigateToSellerManagement = onNavigateToSellerManagement,
                onNavigateToAnalytics = onNavigateToAnalytics,
                onNavigateToPendingPayments = onNavigateToPendingPayments,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToDeactivationRequests = onNavigateToDeactivationRequests,
                onNavigateToBilling = onNavigateToBilling,
                billingService = billingService
            )
        }
        
        // Diálogo de generación de código de afiliación
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
