package org.sysarp.project.ui.seller.components.dashboard

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.sysarp.project.data.DailySalesData
import org.sysarp.project.data.PaymentNotificationData
import org.sysarp.project.data.PerformanceMetricsData
import org.sysarp.project.data.SellerOverviewSummaryData
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.data.SellerStatsData
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.notification.HybridNotificationManager
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.service.websocket.PaymentWebSocketService
import org.sysarp.project.service.websocket.WebSocketConnectionState
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerActionsSection
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerNotificationSection
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerPaymentListSection
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerPaymentsSection
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerProfileSection
import org.sysarp.project.ui.components.seller_dashboard.sections.SellerStatsSection
import org.sysarp.project.ui.seller.components.dashboard.utils.SellerDashboardLogic
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
    paymentService: PaymentService,
    statsService: StatsService,
    webSocketService: PaymentWebSocketService,
    hybridNotificationManager: HybridNotificationManager,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToPendingPayments: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeactivationRequest: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado centralizado del dashboard
    var pendingPayments by remember { mutableStateOf<List<SellerPendingPayment>>(emptyList()) }
    var sellerStats by remember { mutableStateOf<SellerStatsData?>(null) }
    var connectionState by remember { mutableStateOf(WebSocketConnectionState.DISCONNECTED) }
    var currentNotification by remember { mutableStateOf<PaymentNotificationData?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var processingPayments by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var searchQuery by remember { mutableStateOf("") }
    var showAllPayments by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf("") }
    var showErrorMessage by remember { mutableStateOf("") }

    val filteredPayments = remember(pendingPayments, searchQuery) {
        SellerDashboardLogic.filterPayments(pendingPayments, searchQuery, "Todos")
    }

    val sellerId = userProfile?.sellerId?.toIntOrNull()

    // ===== Operaciones de Datos =====

    fun showSuccess(message: String) {
        showSuccessMessage = message
    }

    fun showError(message: String) {
        showErrorMessage = message
    }

    fun removePaymentFromList(paymentId: Int) {
        pendingPayments = pendingPayments.filter { it.paymentId != paymentId }
    }

    fun loadDashboardData() {
        coroutineScope.launch {
            if (sellerId == null || accessToken.isEmpty()) return@launch

            isRefreshing = true
            try {
                // Cargar pagos pendientes
                paymentService.getPendingPayments(sellerId, 0, 20, null, null, accessToken)
                    .fold(
                        onSuccess = { response ->
                            pendingPayments = response.data.payments
                        },
                        onFailure = { error ->
                            showError("Error cargando pagos: ${error.message}")
                        }
                    )

                // Cargar estadísticas del vendedor
                statsService.getUnifiedStatsSummary(
                    adminId = null,
                    sellerId = sellerId,
                    startDate = null,
                    endDate = null,
                    token = accessToken
                ).fold(
                    onSuccess = { response ->
                        sellerStats = SellerStatsData(
                            sellerId = sellerId,
                            sellerName = userProfile?.name,
                            performanceMetrics = PerformanceMetricsData(
                                averageConfirmationTime = response.data.performanceMetrics.averageConfirmationTime,
                                claimRate = response.data.performanceMetrics.claimRate,
                                rejectionRate = response.data.performanceMetrics.rejectionRate,
                                pendingPayments = response.data.performanceMetrics.pendingPayments,
                                confirmedPayments = response.data.performanceMetrics.confirmedPayments,
                                rejectedPayments = response.data.performanceMetrics.rejectedPayments
                            ),
                            dailySales = response.data.dailySales?.map { daily ->
                                DailySalesData(
                                    date = daily.date,
                                    dayName = daily.dayName,
                                    sales = daily.sales,
                                    transactions = daily.transactions
                                )
                            } ?: emptyList(),
                            overview = SellerOverviewSummaryData(
                                totalSales = response.data.overview.totalSales,
                                totalTransactions = response.data.overview.totalTransactions,
                                averageTransactionValue = response.data.overview.averageTransactionValue,
                                salesGrowth = response.data.overview.salesGrowth,
                                transactionGrowth = response.data.overview.transactionGrowth,
                                averageGrowth = response.data.overview.averageGrowth
                            )
                        )
                    },
                    onFailure = { error ->
                        showError("Error cargando estadísticas: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                showError("Error cargando datos: ${e.message}")
            } finally {
                isRefreshing = false
            }
        }
    }

    fun claimPayment(paymentId: Int) {
        coroutineScope.launch {
            if (sellerId == null || accessToken.isEmpty()) return@launch

            processingPayments = processingPayments + paymentId
            try {
                paymentService.claimPayment(sellerId, paymentId, accessToken).fold(
                    onSuccess = {
                        showSuccess("Pago confirmado exitosamente")
                        removePaymentFromList(paymentId)
                    },
                    onFailure = { error ->
                        showError("Error confirmando pago: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                showError("Error confirmando pago: ${e.message}")
            } finally {
                processingPayments = processingPayments - paymentId
            }
        }
    }

    fun rejectPayment(paymentId: Int) {
        coroutineScope.launch {
            if (sellerId == null || accessToken.isEmpty()) return@launch

            processingPayments = processingPayments + paymentId
            try {
                paymentService.rejectPayment(sellerId, paymentId, "Rechazado por el vendedor", accessToken).fold(
                    onSuccess = {
                        showSuccess("Pago rechazado exitosamente")
                        removePaymentFromList(paymentId)
                    },
                    onFailure = { error ->
                        showError("Error rechazando pago: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                showError("Error rechazando pago: ${e.message}")
            } finally {
                processingPayments = processingPayments - paymentId
            }
        }
    }

    fun claimFromNotification() {
        val notification = currentNotification ?: return

        coroutineScope.launch {
            if (sellerId == null || accessToken.isEmpty()) return@launch

            processingPayments = processingPayments + notification.paymentId
            try {
                val existingPayment = pendingPayments.find { it.paymentId == notification.paymentId }

                if (existingPayment != null) {
                    paymentService.claimPayment(sellerId, notification.paymentId, accessToken).fold(
                        onSuccess = {
                            showSuccess("Pago confirmado exitosamente")
                            removePaymentFromList(notification.paymentId)
                            currentNotification = null
                        },
                        onFailure = { error ->
                            showError("Error confirmando pago: ${error.message}")
                        }
                    )
                } else {
                    currentNotification = null
                    showSuccess("Pago ya procesado")
                }
            } catch (e: Exception) {
                showError("Error confirmando pago: ${e.message}")
            } finally {
                processingPayments = processingPayments - notification.paymentId
            }
        }
    }

    fun rejectFromNotification() {
        val notification = currentNotification ?: return

        coroutineScope.launch {
            if (sellerId == null || accessToken.isEmpty()) return@launch

            processingPayments = processingPayments + notification.paymentId
            try {
                val existingPayment = pendingPayments.find { it.paymentId == notification.paymentId }

                if (existingPayment != null) {
                    paymentService.rejectPayment(sellerId, notification.paymentId, "Rechazado por el vendedor", accessToken).fold(
                        onSuccess = {
                            showSuccess("Pago rechazado exitosamente")
                            removePaymentFromList(notification.paymentId)
                            currentNotification = null
                        },
                        onFailure = { error ->
                            showError("Error rechazando pago: ${error.message}")
                        }
                    )
                } else {
                    currentNotification = null
                    showSuccess("Pago ya procesado")
                }
            } catch (e: Exception) {
                showError("Error rechazando pago: ${e.message}")
            } finally {
                processingPayments = processingPayments - notification.paymentId
            }
        }
    }

    // ===== Efectos =====

    LaunchedEffect(Unit) {
        hybridNotificationManager.setOnNewNotificationCallback { newPayments ->
            pendingPayments = newPayments
            showSuccess("Nuevos pagos recibidos: ${newPayments.size}")
        }
    }

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

    LaunchedEffect(accessToken, userProfile?.sellerId) {
        loadDashboardData()
        if (userProfile?.sellerId != null) {
            webSocketService.startAutoConnect()
        }
    }

    LaunchedEffect(webSocketService) {
        webSocketService.connectionState.collect { state ->
            connectionState = state
        }
    }

    // Rastrear notificaciones ya vistas
    var processedNotificationIds by remember { mutableStateOf<Set<Int>>(emptySet()) }

    LaunchedEffect(webSocketService) {
        webSocketService.paymentNotifications.collect { notification ->
            // Solo mostrar notificaciones que no hayan sido procesadas
            if (!processedNotificationIds.contains(notification.paymentId)) {
                currentNotification = notification
                processedNotificationIds = processedNotificationIds + notification.paymentId
            }
        }
    }

    // Limpiar notificación al desmontar el composable
    DisposableEffect(Unit) {
        onDispose {
            currentNotification = null
            processedNotificationIds = emptySet()
        }
    }

    // ===== UI =====

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
            SellerNotificationSection(
                currentNotification = currentNotification,
                onDismissNotification = { currentNotification = null },
                onClaimNotification = { claimFromNotification() },
                onRejectNotification = { rejectFromNotification() }
            )

            SellerProfileSection(
                userProfile = userProfile,
                connectionState = connectionState
            )

            SellerStatsSection(
                confirmedPaymentsCount = sellerStats?.performanceMetrics?.confirmedPayments ?: 0,
                totalAmountCollected = sellerStats?.overview?.totalSales ?: 0.0,
                isLoadingStats = sellerStats == null
            )

            SellerPaymentsSection(
                pendingPayments = pendingPayments,
                filteredPayments = filteredPayments,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                showFilters = false,
                onToggleFilters = { },
                isRefreshing = isRefreshing,
                onRefresh = { loadDashboardData() }
            )

            SellerPaymentListSection(
                filteredPayments = filteredPayments,
                pendingPayments = pendingPayments,
                showAllPayments = showAllPayments,
                processingPayments = processingPayments,
                isRefreshing = isRefreshing,
                onClaimPayment = { paymentId -> claimPayment(paymentId) },
                onRejectPayment = { paymentId -> rejectPayment(paymentId) }
            )

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