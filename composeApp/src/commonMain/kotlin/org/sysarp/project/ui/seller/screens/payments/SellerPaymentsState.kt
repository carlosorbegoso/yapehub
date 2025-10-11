package org.sysarp.project.ui.seller.screens.payments

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.data.UserProfile
import org.sysarp.project.data.PaymentFilterStatus
import org.sysarp.project.data.PaymentFilterStatusUtils
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.utils.convertPeriodToDates

/**
 * Estado y lógica de negocio para SellerPaymentsScreen
 */
class SellerPaymentsState(
    val paymentService: PaymentService,
    private val coroutineScope: CoroutineScope
) {
    // Estados de tabs
    var selectedTab by mutableStateOf(0)
        private set
    
    // Estados de datos
    var pendingPayments by mutableStateOf<List<SellerPendingPayment>>(emptyList())
        private set
    
    var confirmedPayments by mutableStateOf<List<SellerPendingPayment>>(emptyList())
        private set
    
    // Estados de carga
    var isLoading by mutableStateOf(false)
        private set
    
    // Estados de error
    var errorMessage by mutableStateOf("")
        private set
    
    // Estados de filtros de fechas
    var startDate by mutableStateOf<String?>(null)
        private set
    
    var endDate by mutableStateOf<String?>(null)
        private set
    
    // Estados de filtros dinámicos
    var selectedStatuses by mutableStateOf<List<PaymentFilterStatus>>(emptyList())
        private set
    
    var showAdvancedFilters by mutableStateOf(false)
        private set
    
    // Estados de filtros en tiempo real
    var yapeCodeFilter by mutableStateOf("")
        private set

    var dateRangeFilter by mutableStateOf("📅 30 días") // Período por defecto
        private set
    
    // Estados de pagos filtrados
    var filteredPendingPayments by mutableStateOf<List<SellerPendingPayment>>(emptyList())
        private set
    
    var filteredConfirmedPayments by mutableStateOf<List<SellerPendingPayment>>(emptyList())
        private set
    
    // Estados de usuario (se pasan como parámetros)
    var userProfile: UserProfile? = null
        private set
    
    var accessToken: String? = null
        private set
    
    /**
     * Cambia el tab seleccionado
     */
    fun changeSelectedTab(tabIndex: Int) {
        selectedTab = tabIndex
    }
    
    /**
     * Establece los pagos pendientes
     */
    fun updatePendingPayments(payments: List<SellerPendingPayment>) {
        pendingPayments = payments
        applyRealTimeFilters()
    }
    
    /**
     * Establece los pagos confirmados
     */
    fun updateConfirmedPayments(payments: List<SellerPendingPayment>) {
        confirmedPayments = payments
        applyRealTimeFilters()
    }
    
    /**
     * Establece el estado de carga
     */
    fun updateLoading(loading: Boolean) {
        isLoading = loading
    }
    
    /**
     * Establece el mensaje de error
     */
    fun updateErrorMessage(message: String) {
        errorMessage = message
    }
    
    /**
     * Limpia el mensaje de error
     */
    fun clearErrorMessage() {
        errorMessage = ""
    }
    
    /**
     * Establece el perfil de usuario
     */
    fun updateUserProfile(profile: UserProfile?) {
        userProfile = profile
    }
    
    /**
     * Establece el token de acceso
     */
    fun updateAccessToken(token: String?) {
        accessToken = token
    }
    
    /**
     * Inicializa los filtros por defecto
     */
    fun initializeDefaultFilters() {
        // Inicializar filtros de fecha por defecto
        val (start, end) = convertPeriodToDates(dateRangeFilter)
        updateDateRange(start, end)
    }
    
    /**
     * Actualiza el filtro de código Yape y aplica filtrado en tiempo real
     */
    fun updateYapeCodeFilter(filter: String) {
        yapeCodeFilter = filter
        applyRealTimeFilters()
    }
    
    /**
     * Actualiza el filtro de rango de fechas y aplica filtrado en tiempo real
     */
    fun updateDateRangeFilter(filter: String) {
        dateRangeFilter = filter
        // Convertir el período seleccionado en fechas específicas
        val (start, end) = convertPeriodToDates(filter)
        updateDateRange(start, end)
        applyRealTimeFilters()
    }
    
    /**
     * Convierte un período del calendario en fechas específicas para la API
     */
    private fun convertPeriodToDates(period: String): Pair<String?, String?> {
        return org.sysarp.project.utils.convertPeriodToDates(period)
    }
    
    /**
     * Aplica filtros en tiempo real a los pagos cargados
     */
    private fun applyRealTimeFilters() {
        filteredPendingPayments = pendingPayments.filter { payment ->
            matchesFilters(payment)
        }
        
        filteredConfirmedPayments = confirmedPayments.filter { payment ->
            matchesFilters(payment)
        }
    }
    
    /**
     * Verifica si un pago coincide con los filtros aplicados
     */
    private fun matchesFilters(payment: SellerPendingPayment): Boolean {
        // Filtro por código Yape
        val matchesYapeCode = yapeCodeFilter.isEmpty() || 
            payment.yapeCode.contains(yapeCodeFilter, ignoreCase = true)
        
        // Filtro por fecha (si está implementado)
        val matchesDateRange = dateRangeFilter.isEmpty() || 
            matchesDateRange(payment, dateRangeFilter)
        
        return matchesYapeCode && matchesDateRange
    }
    
    /**
     * Verifica si un pago coincide con el rango de fechas seleccionado
     */
    private fun matchesDateRange(payment: SellerPendingPayment, dateRange: String): Boolean {
        // TODO: Implementar lógica de filtrado por fecha
        // Por ahora retorna true para no filtrar por fecha
        return true
    }
    
    /**
     * Establece las fechas de filtro
     */
    fun updateDateRange(startDate: String?, endDate: String?) {
        this.startDate = startDate
        this.endDate = endDate
    }
    
    /**
     * Establece los estados de filtro seleccionados
     */
    fun updateSelectedStatuses(statuses: List<PaymentFilterStatus>) {
        selectedStatuses = statuses
    }
    
    /**
     * Alterna la visibilidad de filtros avanzados
     */
    fun toggleAdvancedFilters() {
        showAdvancedFilters = !showAdvancedFilters
    }
    
    /**
     * Obtiene el string de estados para la API
     */
    fun getStatusString(): String {
        return if (selectedStatuses.isEmpty()) {
            // Si no hay filtros, usar el tab seleccionado
            when (selectedTab) {
                0 -> PaymentFilterStatus.PENDING.value
                1 -> PaymentFilterStatus.CLAIMED.value
                else -> PaymentFilterStatus.PENDING.value
            }
        } else {
            PaymentFilterStatusUtils.toCommaSeparatedString(selectedStatuses)
        }
    }
    
    /**
     * Verifica si hay filtros activos
     */
    fun hasActiveFilters(): Boolean {
        return selectedStatuses.isNotEmpty() || startDate != null || endDate != null
    }
    
    /**
     * Limpia todos los filtros
     */
    fun clearAllFilters() {
        selectedStatuses = emptyList()
        startDate = null
        endDate = null
        showAdvancedFilters = false
        yapeCodeFilter = ""
        dateRangeFilter = ""
        filteredPendingPayments = pendingPayments
        filteredConfirmedPayments = confirmedPayments
    }
    
    /**
     * Carga los pagos con filtros dinámicos
     */
    fun loadPaymentsWithFilters(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (accessToken != null && userProfile?.sellerId != null) {
            coroutineScope.launch {
                updateLoading(true)
                clearErrorMessage()

                val statusString = getStatusString()
                
                paymentService.getPayments(
                    sellerId = userProfile?.sellerId?.toInt() ?: 0,
                    status = statusString,
                    page = 0,
                    size = 50,
                    startDate = startDate,
                    endDate = endDate,
                    token = accessToken ?: ""
                ).fold(
                    onSuccess = { response ->
                        // Separar pagos por estado
                        val pending = response.data.payments.filter { it.status == "PENDING" }
                        val confirmed = response.data.payments.filter { it.status == "CLAIMED" }
                        
                        updatePendingPayments(pending)
                        updateConfirmedPayments(confirmed)
                        updateLoading(false)
                        onSuccess()
                    },
                    onFailure = { error ->
                        updateErrorMessage(error.message ?: "Error cargando pagos")
                        updateLoading(false)
                        onFailure(errorMessage)
                    }
                )
            }
        }
    }
    
    /**
     * Carga los pagos pendientes (método de compatibilidad)
     */
    fun loadPendingPayments(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        loadPaymentsWithFilters(onSuccess, onFailure)
    }
    
    /**
     * Carga los pagos confirmados (método de compatibilidad)
     */
    fun loadConfirmedPayments(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        loadPaymentsWithFilters(onSuccess, onFailure)
    }

    /**
     * Confirma un pago
     */
    fun claimPayment(
        paymentId: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (userProfile?.sellerId != null && accessToken != null) {
            coroutineScope.launch {
                paymentService.claimPayment(
                    sellerId = userProfile?.sellerId?.toInt() ?: 0,
                    paymentId = paymentId,
                    token = accessToken ?: ""
                ).fold(
                    onSuccess = { response ->
                        // Recargar con filtros actuales
                        refreshAllPayments(
                            onSuccess = { },
                            onFailure = { }
                        )
                        onSuccess()
                    },
                    onFailure = { error ->
                        updateErrorMessage("Error confirmando pago: ${error.message}")
                        onFailure(errorMessage)
                    }
                )
            }
        }
    }

    /**
     * Recarga todos los pagos con filtros actuales
     */
    fun refreshAllPayments(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        loadPaymentsWithFilters(onSuccess, onFailure)
    }
    
    /**
     * Verifica si se puede cargar pagos
     */
    fun canLoadPayments(): Boolean {
        return accessToken != null && userProfile?.sellerId != null
    }
    
    /**
     * Verifica si hay pagos pendientes
     */
    fun hasPendingPayments(): Boolean {
        return pendingPayments.isNotEmpty()
    }
    
    /**
     * Verifica si hay pagos confirmados
     */
    fun hasConfirmedPayments(): Boolean {
        return confirmedPayments.isNotEmpty()
    }
    
    /**
     * Obtiene el total de pagos pendientes
     */
    fun getPendingPaymentsTotal(): Double {
        return pendingPayments.sumOf { it.amount }
    }
    
    /**
     * Obtiene el total de pagos confirmados
     */
    fun getConfirmedPaymentsTotal(): Double {
        return confirmedPayments.sumOf { it.amount }
    }
    
    /**
     * Obtiene el número de pagos pendientes
     */
    fun getPendingPaymentsCount(): Int {
        return pendingPayments.size
    }
    
    /**
     * Obtiene el número de pagos confirmados
     */
    fun getConfirmedPaymentsCount(): Int {
        return confirmedPayments.size
    }
    
    /**
     * Filtra por rango de fechas y recarga
     */
    fun filterByDateRange(
        startDate: String?,
        endDate: String?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        updateDateRange(startDate, endDate)
        refreshAllPayments(onSuccess, onFailure)
    }
    
    /**
     * Aplica filtros avanzados y recarga
     */
    fun applyAdvancedFilters(
        statuses: List<PaymentFilterStatus>,
        startDate: String?,
        endDate: String?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        updateSelectedStatuses(statuses)
        updateDateRange(startDate, endDate)
        refreshAllPayments(onSuccess, onFailure)
    }
}
