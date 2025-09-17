package org.sysarp.project.ui.components.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.sysarp.project.data.PaymentNotificationData
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.data.SellerStats
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.websocket.WebSocketConnectionState
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerActionsSection
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerNotificationSection
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerPaymentListSection
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerPaymentsSection
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerProfileSection
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerStatsSection
import org.sysarp.project.ui.components.seller_dashboard.utils.SellerDashboardLogic

/**
 * Contenido principal del dashboard del vendedor
 * Versión simplificada que usa componentes modulares
 */
@Composable
fun SellerDashboardContent(
    accessToken: String,
    userProfile: UserProfile?,
    onNavigateToHistory: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeactivationRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados principales
    var pendingPayments by remember { mutableStateOf<List<SellerPendingPayment>>(emptyList()) }
    var sellerStats by remember { mutableStateOf<SellerStats?>(null) }
    var connectionState by remember { mutableStateOf(WebSocketConnectionState.DISCONNECTED) }
    var currentNotification by remember { mutableStateOf<PaymentNotificationData?>(null) }
    var newPaymentsCount by remember { mutableStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    var processingPayments by remember { mutableStateOf<Set<Int>>(emptySet()) }
    
    // Estados de UI
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Todos") }
    var showAllPayments by remember { mutableStateOf(false) }
    
    // Estados de mensajes
    var showSuccessMessage by remember { mutableStateOf("") }
    var showErrorMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Filtrar pagos pendientes
    val filteredPayments = remember(pendingPayments, searchQuery, selectedFilter) {
        SellerDashboardLogic.filterPayments(pendingPayments, searchQuery, selectedFilter)
    }
    
    // Función para refrescar datos
    val refreshData = {
        isRefreshing = true
        // TODO: Implementar carga real de datos
        isRefreshing = false
    }
    
    // Función para confirmar pago
    val claimPayment = { paymentId: Int ->
        processingPayments = processingPayments + paymentId
        // TODO: Implementar confirmación real de pago
        showSuccessMessage = "Pago confirmado exitosamente"
        pendingPayments = pendingPayments.filter { it.paymentId != paymentId }
        processingPayments = processingPayments - paymentId
    }
    
    // Función para rechazar pago
    val rejectPayment = { paymentId: Int ->
        processingPayments = processingPayments + paymentId
        // TODO: Implementar rechazo real de pago
        showSuccessMessage = "Pago rechazado exitosamente"
        pendingPayments = pendingPayments.filter { it.paymentId != paymentId }
        processingPayments = processingPayments - paymentId
    }
    
    // Función para manejar notificación
    val handleNotification = { notification: PaymentNotificationData ->
                currentNotification = notification
        newPaymentsCount = 1
    }
    
    // Función para descartar notificación
    val dismissNotification = {
        currentNotification = null
        newPaymentsCount = 0
    }
    
    // Función para confirmar desde notificación
    val claimFromNotification = {
        val notification = currentNotification
        if (notification != null) {
            processingPayments = processingPayments + notification.paymentId
            // TODO: Implementar confirmación real de pago
            showSuccessMessage = "Pago confirmado exitosamente"
            pendingPayments = pendingPayments.filter { it.paymentId != notification.paymentId }
            dismissNotification()
            processingPayments = processingPayments - notification.paymentId
        }
    }
    
    // Función para rechazar desde notificación
    val rejectFromNotification = {
        val notification = currentNotification
        if (notification != null) {
            processingPayments = processingPayments + notification.paymentId
            // TODO: Implementar rechazo real de pago
            showSuccessMessage = "Pago rechazado exitosamente"
            pendingPayments = pendingPayments.filter { it.paymentId != notification.paymentId }
            dismissNotification()
            processingPayments = processingPayments - notification.paymentId
        }
    }
    
    // Manejar mensajes de éxito y error
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(showSuccessMessage)
            showSuccessMessage = ""
        }
    }
    
    LaunchedEffect(showErrorMessage) {
        if (showErrorMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(showErrorMessage)
            showErrorMessage = ""
        }
    }
    
    // Cargar datos iniciales
    LaunchedEffect(accessToken, userProfile?.sellerId) {
        refreshData()
        // TODO: Configurar WebSocket
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de notificaciones
                item {
                SellerNotificationSection(
                    currentNotification = currentNotification,
                    onDismissNotification = dismissNotification,
                    onClaimNotification = claimFromNotification,
                    onRejectNotification = rejectFromNotification
                )
            }
            
            // Sección del perfil del vendedor
            item {
                SellerProfileSection(
                    userProfile = userProfile,
                            connectionState = connectionState
                        )
            }
            
            // Sección de estadísticas
            item {
                SellerStatsSection(
                    confirmedPaymentsCount = sellerStats?.transactionCount ?: 0,
                    totalAmountCollected = sellerStats?.totalSales ?: 0.0,
                    isLoadingStats = sellerStats == null
                )
            }
            
            // Sección de pagos pendientes (header, búsqueda, filtros)
            item {
                SellerPaymentsSection(
                    pendingPayments = pendingPayments,
                    filteredPayments = filteredPayments,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    showFilters = showFilters,
                    onToggleFilters = { showFilters = !showFilters },
                    isRefreshing = isRefreshing,
                    onRefresh = refreshData
                )
            }
            
            // Sección de lista de pagos
            item {
                SellerPaymentListSection(
                    filteredPayments = filteredPayments,
                    pendingPayments = pendingPayments,
                    showAllPayments = showAllPayments,
                    processingPayments = processingPayments,
                    isRefreshing = isRefreshing,
                    onClaimPayment = claimPayment,
                    onRejectPayment = rejectPayment
                )
            }
            
            // Sección de acciones
            item {
                SellerActionsSection(
                    pendingPayments = pendingPayments,
                    showAllPayments = showAllPayments,
                    onToggleShowAllPayments = { showAllPayments = it },
                    onNavigateToHistory = onNavigateToHistory,
                    onNavigateToPendingPayments = onNavigateToPendingPayments,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToDeactivationRequest = onNavigateToDeactivationRequest
                )
            }
        }
    }
}