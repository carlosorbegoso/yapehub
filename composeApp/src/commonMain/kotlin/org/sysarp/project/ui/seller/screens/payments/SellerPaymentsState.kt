package org.sysarp.project.ui.seller.screens.payments

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sysarp.project.data.DailySalesData
import org.sysarp.project.data.PaymentFilterStatus
import org.sysarp.project.data.PaymentFilterStatusUtils
import org.sysarp.project.data.PaymentSummary
import org.sysarp.project.data.PerformanceMetricsData
import org.sysarp.project.data.SellerOverviewSummaryData
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.data.SellerStatsData
import org.sysarp.project.data.UnifiedAnalyticsUrls
import org.sysarp.project.data.UnifiedStatsData
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.utils.convertPeriodToDates

/**
 * Estado y lógica de negocio para SellerPaymentsScreen
 * Gestiona:
 * - Estados de datos (pagos, estadísticas, filtros)
 * - Operaciones de API (carga, claim, reject)
 * - Lógica de filtrados en tiempo real
 */
class SellerPaymentsState(
    val paymentService: PaymentService,
    val statsService: StatsService,
    private val coroutineScope: CoroutineScope
) {
    // ===== Estados de Datos =====
    var pendingPayments by mutableStateOf<List<SellerPendingPayment>>(emptyList())
        private set

    var confirmedPayments by mutableStateOf<List<SellerPendingPayment>>(emptyList())
        private set

    var paymentSummary by mutableStateOf<PaymentSummary?>(null)
        private set

    var sellerStats by mutableStateOf<SellerStatsData?>(null)
        private set

    var analyticsUrls by mutableStateOf<UnifiedAnalyticsUrls?>(null)
        private set

    var filteredPendingPayments by mutableStateOf<List<SellerPendingPayment>>(emptyList())
        private set

    var filteredConfirmedPayments by mutableStateOf<List<SellerPendingPayment>>(emptyList())
        private set

    // ===== Estados de UI =====
    var selectedTab by mutableStateOf(0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isLoadingMore by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf("")

    var showAdvancedFilters by mutableStateOf(false)
        private set

    // ===== Estados de Filtros =====
    var yapeCodeFilter by mutableStateOf("")
        private set

    var dateRangeFilter by mutableStateOf("📅 30 días")
        private set

    var selectedStatuses by mutableStateOf<List<PaymentFilterStatus>>(emptyList())
        private set

    var startDate by mutableStateOf<String?>(null)
        private set

    var endDate by mutableStateOf<String?>(null)
        private set

    // ===== Estados de Paginación =====
    var currentPage by mutableStateOf(0)
        private set

    var hasMorePayments by mutableStateOf(true)
        private set

    // ===== Estados de Usuario =====
    var userProfile: UserProfile? = null
        private set

    var accessToken: String? = null
        private set

    // ===== Propiedades Calculadas =====

    val canLoadData: Boolean
        get() = accessToken != null && userProfile?.sellerId != null

    val isSellerIdValid: Boolean
        get() = userProfile?.sellerId?.toIntOrNull() != null

    // ===== Acciones Públicas =====

    fun changeSelectedTab(tabIndex: Int) {
        selectedTab = tabIndex
        loadPaymentsWithFilters(onSuccess = {}, onFailure = {})
    }

    fun toggleAdvancedFilters() {
        showAdvancedFilters = !showAdvancedFilters
    }

    fun updateYapeCodeFilter(filter: String) {
        yapeCodeFilter = filter
        applyRealTimeFilters()
    }

    fun updateDateRangeFilter(filter: String) {
        dateRangeFilter = filter
        val (start, end) = convertPeriodToDates(filter)
        updateDateRange(start, end)
        applyRealTimeFilters()
    }

    fun updateSelectedStatuses(statuses: List<PaymentFilterStatus>) {
        selectedStatuses = statuses
    }

    fun clearErrorMessage() {
        errorMessage = ""
    }

    fun updateUserProfile(profile: UserProfile?) {
        userProfile = profile
    }

    fun updateAccessToken(token: String?) {
        accessToken = token
    }

    fun initializeDefaultFilters() {
        val (start, end) = convertPeriodToDates(dateRangeFilter)
        updateDateRange(start, end)
    }

    fun clearAllFilters() {
        selectedStatuses = emptyList()
        startDate = null
        endDate = null
        showAdvancedFilters = false
        yapeCodeFilter = ""
        dateRangeFilter = ""
        applyRealTimeFilters()
    }

    // ===== Operaciones de Carga =====

    fun loadPaymentsWithFilters(
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        if (!canLoadData) return

        coroutineScope.launch {
            isLoading = true
            clearErrorMessage()
            currentPage = 0
            hasMorePayments = true

            val statusString = getStatusString()

            paymentService.getPayments(
                sellerId = userProfile?.sellerId?.toInt() ?: 0,
                status = statusString,
                page = 0,
                size = 20,
                startDate = startDate,
                endDate = endDate,
                token = accessToken ?: ""
            ).fold(
                onSuccess = { response ->
                    updatePaymentsFromResponse(response)
                    isLoading = false
                    onSuccess()
                },
                onFailure = { error ->
                    handleLoadError("Error cargando pagos: ${error.message}", onFailure)
                }
            )
        }
    }

    fun loadMorePayments(
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        if (!canLoadData || !hasMorePayments || isLoadingMore) return

        coroutineScope.launch {
            isLoadingMore = true
            val nextPage = currentPage + 1
            val statusString = getStatusString()

            paymentService.getPayments(
                sellerId = userProfile?.sellerId?.toInt() ?: 0,
                status = statusString,
                page = nextPage,
                size = 20,
                startDate = startDate,
                endDate = endDate,
                token = accessToken ?: ""
            ).fold(
                onSuccess = { response ->
                    addMorePaymentsFromResponse(response, nextPage)
                    isLoadingMore = false
                    onSuccess()
                },
                onFailure = { error ->
                    handleLoadError("Error cargando más pagos: ${error.message}", onFailure)
                }
            )
        }
    }

    fun claimPayment(
        paymentId: Int,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        if (!canLoadData) return

        coroutineScope.launch {
            paymentService.claimPayment(
                sellerId = userProfile?.sellerId?.toInt() ?: 0,
                paymentId = paymentId,
                token = accessToken ?: ""
            ).fold(
                onSuccess = {
                    refreshAllPayments(onSuccess, onFailure)
                },
                onFailure = { error ->
                    handleLoadError("Error confirmando pago: ${error.message}", onFailure)
                }
            )
        }
    }

    fun rejectPayment(
        paymentId: Int,
        reason: String = "Rechazado por vendedor",
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        if (!canLoadData) return

        coroutineScope.launch {
            paymentService.rejectPayment(
                sellerId = userProfile?.sellerId?.toInt() ?: 0,
                paymentId = paymentId,
                reason = reason,
                token = accessToken ?: ""
            ).fold(
                onSuccess = {
                    refreshAllPayments(onSuccess, onFailure)
                },
                onFailure = { error ->
                    handleLoadError("Error rechazando pago: ${error.message}", onFailure)
                }
            )
        }
    }

    fun refreshAllPayments(
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        loadPaymentsWithFilters(onSuccess, onFailure)
    }

    fun loadSellerStats(
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        if (!canLoadData) return

        coroutineScope.launch {
            statsService.getUnifiedStatsSummary(
                adminId = null,
                sellerId = userProfile?.sellerId?.toInt() ?: 0,
                startDate = startDate,
                endDate = endDate,
                token = accessToken ?: ""
            ).fold(
                onSuccess = { response ->
                    sellerStats = buildSellerStatsData(response.data)
                    analyticsUrls = response.data.urls
                    onSuccess()
                },
                onFailure = { error ->
                    handleLoadError("Error cargando estadísticas: ${error.message}", onFailure)
                }
            )
        }
    }

    // ===== Operaciones Privadas =====

    private fun updatePaymentsFromResponse(response: Any) {
        val typedResponse = response as? org.sysarp.project.data.PendingPaymentsResponse ?: return

        val pending = typedResponse.data.payments.filter { it.status == "PENDING" }
        val confirmed = typedResponse.data.payments.filter { it.status == "CLAIMED" }

        pendingPayments = pending
        confirmedPayments = confirmed
        paymentSummary = typedResponse.data.summary
        hasMorePayments = typedResponse.data.pagination.currentPage <
                typedResponse.data.pagination.totalPages - 1

        applyRealTimeFilters()
    }

    private fun addMorePaymentsFromResponse(response: Any, nextPage: Int) {
        val typedResponse = response as? org.sysarp.project.data.PendingPaymentsResponse ?: return

        val pending = typedResponse.data.payments.filter { it.status == "PENDING" }
        val confirmed = typedResponse.data.payments.filter { it.status == "CLAIMED" }

        pendingPayments = pendingPayments + pending
        confirmedPayments = confirmedPayments + confirmed
        currentPage = nextPage
        hasMorePayments = typedResponse.data.pagination.currentPage <
                typedResponse.data.pagination.totalPages - 1

        applyRealTimeFilters()
    }

    private fun applyRealTimeFilters() {
        filteredPendingPayments = pendingPayments.filter { matchesFilters(it) }
        filteredConfirmedPayments = confirmedPayments.filter { matchesFilters(it) }
    }

    private fun matchesFilters(payment: SellerPendingPayment): Boolean {
        val matchesYapeCode = yapeCodeFilter.isEmpty() ||
                payment.yapeCode.contains(yapeCodeFilter, ignoreCase = true)

        return matchesYapeCode
    }

    private fun getStatusString(): String {
        return if (selectedStatuses.isEmpty()) {
            when (selectedTab) {
                0 -> PaymentFilterStatus.PENDING.value
                1 -> PaymentFilterStatus.CLAIMED.value
                else -> PaymentFilterStatus.PENDING.value
            }
        } else {
            PaymentFilterStatusUtils.toCommaSeparatedString(selectedStatuses)
        }
    }

    private fun buildSellerStatsData(data: UnifiedStatsData): SellerStatsData? {
        return SellerStatsData(
            sellerId = userProfile?.sellerId?.toInt(),
            sellerName = userProfile?.name,
            performanceMetrics = PerformanceMetricsData(
                averageConfirmationTime = data.performanceMetrics.averageConfirmationTime,
                claimRate = data.performanceMetrics.claimRate,
                rejectionRate = data.performanceMetrics.rejectionRate,
                pendingPayments = data.performanceMetrics.pendingPayments,
                confirmedPayments = data.performanceMetrics.confirmedPayments,
                rejectedPayments = data.performanceMetrics.rejectedPayments
            ),
            dailySales = data.dailySales?.map { daily ->
                DailySalesData(
                    date = daily.date,
                    dayName = daily.dayName,
                    sales = daily.sales,
                    transactions = daily.transactions
                )
            } ?: emptyList(),
            overview = SellerOverviewSummaryData(
                totalSales = data.overview.confirmedSales,
                totalTransactions = data.overview.totalTransactions,
                averageTransactionValue = data.overview.averageTransactionValue,
                salesGrowth = data.overview.salesGrowth,
                transactionGrowth = data.overview.transactionGrowth,
                averageGrowth = data.overview.averageGrowth
            )
        )
    }

    private fun updateDateRange(startDate: String?, endDate: String?) {
        this.startDate = startDate
        this.endDate = endDate
    }

    private fun handleLoadError(message: String, onFailure: (String) -> Unit) {
        errorMessage = message
        isLoading = false
        isLoadingMore = false
        onFailure(message)
    }
}