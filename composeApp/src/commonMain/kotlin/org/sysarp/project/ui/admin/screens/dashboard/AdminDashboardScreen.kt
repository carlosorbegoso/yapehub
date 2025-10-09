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
import org.koin.compose.koinInject
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.affiliation.AffiliationService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.billing.BillingService
import org.sysarp.project.service.branch.BranchService
import org.sysarp.project.service.qr.QRService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.ui.common.components.topbar.TopBarComponent

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
    val userProfile by authService.userProfile.collectAsState()
    val accessToken by authService.accessToken.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // The state holder receives the injected dependencies
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

            // Filtro de fechas
            AdminDashboardDateFilter(
                selectedDateRange = state.selectedDateRange,
                onDateRangeSelected = { period ->
                    val adminId = userProfile?.adminId?.toIntOrNull()
                    val token = accessToken
                    if (adminId != null && !token.isNullOrEmpty()) {
                        state.updateDateRange(period, adminId, token)
                    }
                },
                showCalendar = state.showCalendarDialog,
                onShowCalendar = { state.showCalendarDialog() },
                onDismissCalendar = { state.dismissCalendarDialog() }
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
                billingService = billingService // This can also be injected inside DashboardContent if needed
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
