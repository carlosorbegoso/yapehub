package org.sysarp.project.ui.admin.components.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.websocket.WebSocketConnectionState
import org.sysarp.project.ui.admin.components.dashboard.sections.AdminActionsSection
import org.sysarp.project.ui.admin.components.dashboard.sections.AdminPerformanceSection
import org.sysarp.project.ui.admin.components.dashboard.sections.AdminProfileSection
import org.sysarp.project.ui.admin.components.dashboard.sections.AdminStatsSection
import org.sysarp.project.ui.admin.components.dashboard.sections.AdminTopSellersSection
import org.sysarp.project.viewmodel.admin.EnhancedAdminDashboardData

/**
 * Contenido principal del dashboard del administrador
 * Replica la estructura y estilo del SellerDashboardContent pero con información de admin
 */
@Composable
fun AdminDashboardContent(
    userProfile: UserProfile?,
    enhancedData: EnhancedAdminDashboardData?,
    connectionState: WebSocketConnectionState,
    isLoading: Boolean,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSellerManagement: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    onNavigateToBranchManagement: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeactivationRequests: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onShowAffiliationDialog: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de perfil del administrador (similar a SellerProfileSection)
            AdminProfileSection(
                userProfile = userProfile,
                connectionState = connectionState
            )

            // Sección de estadísticas del administrador (similar a SellerStatsSection)
            AdminStatsSection(
                enhancedData = enhancedData,
                isLoadingStats = isLoading
            )

            // Sección de top vendedores (específica para admin)
            if (enhancedData?.topSellers?.isNotEmpty() == true) {
                AdminTopSellersSection(
                    topSellers = enhancedData.topSellers,
                    onNavigateToSellerManagement = onNavigateToSellerManagement
                )
            }

            // Sección de métricas de rendimiento (similar a SellerPaymentsSection)
            AdminPerformanceSection(
                enhancedData = enhancedData,
                onNavigateToPendingPayments = onNavigateToPendingPayments
            )

            // Sección de acciones del administrador (similar a SellerActionsSection)
            AdminActionsSection(
                enhancedData = enhancedData,
                onNavigateToAnalytics = onNavigateToAnalytics,
                onNavigateToSellerManagement = onNavigateToSellerManagement,
                onNavigateToPendingPayments = onNavigateToPendingPayments,
                onNavigateToBranchManagement = onNavigateToBranchManagement,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToDeactivationRequests = onNavigateToDeactivationRequests,
                onNavigateToBilling = onNavigateToBilling,
                onShowAffiliationDialog = onShowAffiliationDialog,
                onLogout = onLogout
            )
        }
    }
}