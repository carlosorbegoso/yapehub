package org.sysarp.project.ui.components.dashboard

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.sysarp.project.data.PaymentNotificationData
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.data.SellerStatsData
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.notification.HybridNotificationManager
import org.sysarp.project.service.websocket.WebSocketConnectionState
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerActionsSection
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerNotificationSection
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerPaymentListSection
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerPaymentsSection
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerProfileSection
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerStatsSection
import org.sysarp.project.ui.components.seller_dashboard.utils.SellerDashboardLogic
import kotlin.time.ExperimentalTime

/**
 * Contenido principal del dashboard del vendedor
 * Versión simplificada que usa componentes modulares
 */
@OptIn(ExperimentalTime::class)
@Composable
fun SellerDashboardContent(
    accessToken: String,
    userProfile: UserProfile?,
    paymentService: org.sysarp.project.service.payment.PaymentService,
    statsService: org.sysarp.project.service.stats.StatsService,
    webSocketService: org.sysarp.project.service.websocket.PaymentWebSocketService,
    hybridNotificationManager: HybridNotificationManager,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeactivationRequest: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    // Estados principales
    var pendingPayments by remember { mutableStateOf<List<SellerPendingPayment>>(emptyList()) }
    var sellerStats by remember { mutableStateOf<SellerStatsData?>(null) }
    var connectionState by remember { mutableStateOf(WebSocketConnectionState.DISCONNECTED) }
    var currentNotification by remember { mutableStateOf<PaymentNotificationData?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var processingPayments by remember { mutableStateOf<Set<Int>>(emptySet()) }
    
    // Estados de UI
    var searchQuery by remember { mutableStateOf("") }
    var showAllPayments by remember { mutableStateOf(false) }
    
    // Estados de mensajes
    var showSuccessMessage by remember { mutableStateOf("") }
    var showErrorMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Conectar con el sistema híbrido para actualizar pagos pendientes
    LaunchedEffect(Unit) {
        hybridNotificationManager.setOnNewNotificationCallback { newPayments ->
            println("[SELLER_DASHBOARD_CONTENT] 🔔 Actualizando UI con ${newPayments.size} pagos híbridos")
            // Actualizar la lista de pagos pendientes con los nuevos pagos
            pendingPayments = newPayments
            showSuccessMessage = "Nuevos pagos recibidos: ${newPayments.size}"
        }
    }
    
    // Filtrar pagos pendientes
    val filteredPayments = remember(pendingPayments, searchQuery) {
        SellerDashboardLogic.filterPayments(pendingPayments, searchQuery, "Todos")
    }
    
    // Función para refrescar datos
    val refreshData = {
        coroutineScope.launch {
            isRefreshing = true
            try {
                val sellerId = userProfile?.sellerId?.toIntOrNull()
                if (sellerId != null && accessToken.isNotEmpty()) {
                    // Cargar pagos pendientes
                    val paymentsResult = paymentService.getPendingPayments(sellerId, 0, 20, null, null, accessToken)
                    paymentsResult.fold(
                        onSuccess = { response ->
                            pendingPayments = response.data.payments
                        },
                        onFailure = { error ->
                            showErrorMessage = "Error cargando pagos: ${error.message}"
                        }
                    )
                    
                    // Cargar estadísticas del vendedor
                    val statsResult = statsService.getSellerStatsSummary(sellerId, null, null, accessToken)
                    statsResult.fold(
                        onSuccess = { response ->
                            sellerStats = response.data
                        },
                        onFailure = { error ->
                            showErrorMessage = "Error cargando estadísticas: ${error.message}"
                        }
                    )
                }
            } catch (e: Exception) {
                showErrorMessage = "Error cargando datos: ${e.message}"
            } finally {
                isRefreshing = false
            }
        }
        Unit
    }
    
    // Función para confirmar pago
    val claimPayment = { paymentId: Int ->
        coroutineScope.launch {
            processingPayments = processingPayments + paymentId
            try {
                val sellerId = userProfile?.sellerId?.toIntOrNull()
                if (sellerId != null && accessToken.isNotEmpty()) {
                    val result = paymentService.claimPayment(sellerId, paymentId, accessToken)
                    result.fold(
                        onSuccess = { response ->
                            showSuccessMessage = "Pago confirmado exitosamente"
                            pendingPayments = pendingPayments.filter { it.paymentId != paymentId }
                            
                            // Enviar notificación WebSocket al servidor
                            try {
                                val websocketMessage = """
                                {
                                    "type": "PAYMENT_CONFIRMED",
                                    "data": {
                                        "paymentId": $paymentId,
                                        "sellerId": $sellerId,
                                        "status": "CONFIRMED",
                                        "timestamp": "${kotlin.time.Clock.System.now().toEpochMilliseconds()}",
                                        "message": "Pago confirmado por el vendedor"
                                    }
                                }
                                """.trimIndent()
                                webSocketService.sendMessage(websocketMessage)
                            } catch (e: Exception) {
                            }
                        },
                        onFailure = { error ->
                            showErrorMessage = "Error confirmando pago: ${error.message}"
                        }
                    )
                }
            } catch (e: Exception) {
                showErrorMessage = "Error confirmando pago: ${e.message}"
            } finally {
                processingPayments = processingPayments - paymentId
            }
        }
        Unit
    }
    
    // Función para rechazar pago
    val rejectPayment = { paymentId: Int ->
        coroutineScope.launch {
            processingPayments = processingPayments + paymentId
            try {
                val sellerId = userProfile?.sellerId?.toIntOrNull()
                if (sellerId != null && accessToken.isNotEmpty()) {
                    val result = paymentService.rejectPayment(sellerId, paymentId, "Rechazado por el vendedor", accessToken)
                    result.fold(
                        onSuccess = { response ->
                            showSuccessMessage = "Pago rechazado exitosamente"
                            pendingPayments = pendingPayments.filter { it.paymentId != paymentId }
                            
                            // Enviar notificación WebSocket al servidor
                            try {
                                val websocketMessage = """
                                {
                                    "type": "PAYMENT_REJECTED",
                                    "data": {
                                        "paymentId": $paymentId,
                                        "sellerId": $sellerId,
                                        "status": "REJECTED",
                                        "timestamp": "${kotlin.time.Clock.System.now().toEpochMilliseconds()}",
                                        "message": "Pago rechazado por el vendedor",
                                        "reason": "Rechazado por el vendedor"
                                    }
                                }
                                """.trimIndent()
                                webSocketService.sendMessage(websocketMessage)
                            } catch (e: Exception) {
                            }
                        },
                        onFailure = { error ->
                            showErrorMessage = "Error rechazando pago: ${error.message}"
                        }
                    )
                }
            } catch (e: Exception) {
                showErrorMessage = "Error rechazando pago: ${e.message}"
            } finally {
                processingPayments = processingPayments - paymentId
            }
        }
        Unit
    }
    
    // Función para manejar notificación
    val handleNotification = { notification: PaymentNotificationData ->
        currentNotification = notification
    }
    
    // Función para descartar notificación
    val dismissNotification = {
        currentNotification = null
    }
    
    // Función para confirmar desde notificación
    val claimFromNotification = {
        val notification = currentNotification
        if (notification != null) {
            coroutineScope.launch {
                processingPayments = processingPayments + notification.paymentId
                try {
                    val sellerId = userProfile?.sellerId?.toIntOrNull()
                    if (sellerId != null && accessToken.isNotEmpty()) {
                        val result = paymentService.claimPayment(sellerId, notification.paymentId, accessToken)
                        result.fold(
                            onSuccess = { response ->
                                showSuccessMessage = "Pago confirmado exitosamente"
                                pendingPayments = pendingPayments.filter { it.paymentId != notification.paymentId }
                                dismissNotification()
                                
                                // Enviar notificación WebSocket al servidor
                                try {
                                    val websocketMessage = """
                                    {
                                        "type": "PAYMENT_CONFIRMED",
                                        "data": {
                                            "paymentId": ${notification.paymentId},
                                            "sellerId": $sellerId,
                                            "status": "CONFIRMED",
                                            "timestamp": "${kotlin.time.Clock.System.now().toEpochMilliseconds()}",
                                            "message": "Pago confirmado por el vendedor desde notificación"
                                        }
                                    }
                                    """.trimIndent()
                                    webSocketService.sendMessage(websocketMessage)
                                } catch (e: Exception) {

                                }
                            },
                            onFailure = { error ->
                                showErrorMessage = "Error confirmando pago: ${error.message}"
                            }
                        )
                    }
                } catch (e: Exception) {
                    showErrorMessage = "Error confirmando pago: ${e.message}"
                } finally {
                    processingPayments = processingPayments - notification.paymentId
                }
            }
        }
    }
    
    // Función para rechazar desde notificación
    val rejectFromNotification = {
        val notification = currentNotification
        if (notification != null) {
            coroutineScope.launch {
                processingPayments = processingPayments + notification.paymentId
                try {
                    val sellerId = userProfile?.sellerId?.toIntOrNull()
                    if (sellerId != null && accessToken.isNotEmpty()) {
                        val result = paymentService.rejectPayment(sellerId, notification.paymentId, "Rechazado por el vendedor", accessToken)
                        result.fold(
                            onSuccess = { response ->
                                showSuccessMessage = "Pago rechazado exitosamente"
                                pendingPayments = pendingPayments.filter { it.paymentId != notification.paymentId }
                                dismissNotification()
                                
                                // Enviar notificación WebSocket al servidor
                                try {
                                    val websocketMessage = """
                                    {
                                        "type": "PAYMENT_REJECTED",
                                        "data": {
                                            "paymentId": ${notification.paymentId},
                                            "sellerId": $sellerId,
                                            "status": "REJECTED",
                                            "timestamp": "${kotlin.time.Clock.System.now().toEpochMilliseconds()}",
                                            "message": "Pago rechazado por el vendedor desde notificación",
                                            "reason": "Rechazado por el vendedor"
                                        }
                                    }
                                    """.trimIndent()
                                    webSocketService.sendMessage(websocketMessage)
                                } catch (e: Exception) {
                                }
                            },
                            onFailure = { error ->
                                showErrorMessage = "Error rechazando pago: ${error.message}"
                            }
                        )
                    }
                } catch (e: Exception) {
                    showErrorMessage = "Error rechazando pago: ${e.message}"
                } finally {
                    processingPayments = processingPayments - notification.paymentId
                }
            }
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
        
        // Configurar WebSocket
        if (userProfile?.sellerId != null) {
            webSocketService.startAutoConnect()
        }
    }
    
    // Manejar cambios de estado de conexión WebSocket
    LaunchedEffect(webSocketService) {
        webSocketService.connectionState.collect { state ->
            connectionState = state
        }
    }
    
    // Manejar notificaciones de WebSocket
    LaunchedEffect(webSocketService) {
        webSocketService.paymentNotifications.collect { notification ->
            handleNotification(notification)
        }
    }
    
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
            // Sección de notificaciones
            SellerNotificationSection(
                currentNotification = currentNotification,
                onDismissNotification = dismissNotification,
                onClaimNotification = claimFromNotification,
                onRejectNotification = rejectFromNotification
            )
            
            // Sección del perfil del vendedor
            SellerProfileSection(
                userProfile = userProfile,
                connectionState = connectionState
            )
            
            // Sección de estadísticas
            SellerStatsSection(
                confirmedPaymentsCount = sellerStats?.summary?.confirmedPayments ?: 0,
                totalAmountCollected = sellerStats?.summary?.totalSales ?: 0.0,
                isLoadingStats = sellerStats == null
            )
            
            // Sección de pagos pendientes (header, búsqueda, filtros)
            SellerPaymentsSection(
                pendingPayments = pendingPayments,
                filteredPayments = filteredPayments,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                showFilters = false,
                onToggleFilters = { },
                isRefreshing = isRefreshing,
                onRefresh = refreshData
            )
            
            // Sección de lista de pagos
            SellerPaymentListSection(
                filteredPayments = filteredPayments,
                pendingPayments = pendingPayments,
                showAllPayments = showAllPayments,
                processingPayments = processingPayments,
                isRefreshing = isRefreshing,
                onClaimPayment = claimPayment,
                onRejectPayment = rejectPayment
            )
            
            // Sección de acciones
            SellerActionsSection(
                pendingPayments = pendingPayments,
                showAllPayments = showAllPayments,
                onToggleShowAllPayments = { showAllPayments = it },
                onNavigateToAnalytics = onNavigateToAnalytics,
                onNavigateToPendingPayments = onNavigateToPendingPayments,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToDeactivationRequest = onNavigateToDeactivationRequest,
                onNavigateToNotifications = onNavigateToNotifications
            )
        }
    }
}