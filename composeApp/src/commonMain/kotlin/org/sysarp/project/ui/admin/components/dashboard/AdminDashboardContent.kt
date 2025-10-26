package org.sysarp.project.ui.admin.components.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.websocket.WebSocketConnectionState
import org.sysarp.project.viewmodel.admin.EnhancedAdminDashboardData
import org.sysarp.project.ui.admin.components.dashboard.sections.*

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
                onNavigateToBilling = onNavigateToBilling
            )
        }
    }
}