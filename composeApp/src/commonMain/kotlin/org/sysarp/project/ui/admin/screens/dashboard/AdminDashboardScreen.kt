package org.sysarp.project.ui.admin.screens.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.affiliation.AffiliationService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.ui.common.components.topbar.TopBarComponent

/**
 * Pantalla principal del dashboard de administración
 * Refactorizada para usar componentes modulares
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    authService: AuthService,
    sellerService: SellerService,
    statsService: org.sysarp.project.service.stats.StatsService,
    affiliationService: AffiliationService,
    qrService: org.sysarp.project.service.qr.QRService,
    branchService: org.sysarp.project.service.branch.BranchService,
    webSocketService: PaymentWebSocketService,
    billingService: org.sysarp.project.service.billing.BillingService,
    onNavigateToBranchManagement: () -> Unit,
    onNavigateToSellerManagement: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeactivationRequests: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onLogout: () -> Unit
) {
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // Crear el estado del dashboard
    val state = remember {
        AdminDashboardState(
            authService = authService,
            sellerService = sellerService,
            statsService = statsService,
            affiliationService = affiliationService,
            qrService = qrService,
            branchService = branchService,
            billingService = billingService,
            webSocketService = webSocketService,
            coroutineScope = coroutineScope
        )
    }
    
    Scaffold(
        topBar = {
            TopBarComponent(
                title = "Dashboard Admin",
                subtitle = "Panel de administración",
                menuItems = createTopBarMenuItems(
                    onShowAffiliationDialog = { state.showAffiliationDialog() },
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToSettings = onNavigateToSettings,
                    onLogout = { handleLogout(authService, coroutineScope, onLogout) }
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
            
            // Contenido principal del dashboard
            DashboardContent(
                quickSummaryData = state.quickSummaryData,
                isLoadingStats = state.isLoadingStats,
                statsError = state.statsError,
                connectedSellersData = state.connectedSellersData,
                isLoadingSellers = state.isLoadingSellers,
                sellersError = state.sellersError,
                pendingRequestsCount = state.pendingRequests.size,
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
    }
    
    // Manejar acciones y efectos secundarios
    AdminDashboardActions(
        authService = authService,
        state = state,
        userProfile = userProfile,
        accessToken = accessToken,
        onLogout = onLogout,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToSettings = onNavigateToSettings
    )
}
