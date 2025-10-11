package org.sysarp.project.ui.admin.screens.payments

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sysarp.project.data.AdminPayment
import org.sysarp.project.data.PaymentSummary
import org.sysarp.project.data.UserProfile
import org.sysarp.project.data.PaymentFilterStatus
import org.sysarp.project.data.PaymentFilterStatusUtils
import org.sysarp.project.data.UnifiedStatsResponse
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.ui.admin.screens.payments.components.AdvancedFilters
import org.sysarp.project.utils.convertPeriodToDates

/**
 * Estado y lógica de negocio para AdminPaymentsScreen
 */
class AdminPaymentsState(
    private val authService: AuthService,
    val paymentService: PaymentService,
    private val statsService: StatsService,
    private val coroutineScope: CoroutineScope
) {
    // Estados de datos
    var payments by mutableStateOf<List<AdminPayment>>(emptyList())
        private set
    
    var paymentSummary by mutableStateOf<PaymentSummary?>(null)
        private set
    
    var adminStats by mutableStateOf<UnifiedStatsResponse?>(null)
        private set
    
    var analyticsUrls by mutableStateOf<org.sysarp.project.data.UnifiedAnalyticsUrls?>(null)
        private set
    
    // Estados de carga
    var isLoading by mutableStateOf(false)
        private set
    
    var isLoadingMore by mutableStateOf(false)
        private set
    
    // Estados de error
    var errorMessage by mutableStateOf("")
        private set
    
    // Estados de paginación
    var currentPage by mutableStateOf(0)
        private set
    
    var hasMorePayments by mutableStateOf(true)
        private set
    
    // Estados de filtros (removido selectedStatus - ahora se usa advancedFilters.status)
    
    // Estados de filtros de fechas
    var startDate by mutableStateOf<String?>(null)
        private set
    
    var endDate by mutableStateOf<String?>(null)
        private set
    
    // Estados de filtros avanzados
    var advancedFilters by mutableStateOf(AdvancedFilters())
        private set
    
    // Estados de filtros dinámicos
    var selectedStatuses by mutableStateOf<List<PaymentFilterStatus>>(emptyList())
        private set
    
    var showAdvancedFilters by mutableStateOf(false)
        private set
    
    // Estados de pagos filtrados (para mostrar en la UI)
    var filteredPayments by mutableStateOf<List<AdminPayment>>(emptyList())
        private set
    
    // Estados de filtros en tiempo real
    var yapeCodeFilter by mutableStateOf("")
        private set

    var dateRangeFilter by mutableStateOf("📅 30 días") // Período por defecto
        private set
    
    // Estados de usuario (se pasan como parámetros)
    var userProfile: UserProfile? = null
        private set
    
    var accessToken: String? = null
        private set
    
    /**
     * Establece los pagos
     */
    fun updatePayments(payments: List<AdminPayment>) {
        this.payments = payments
        // Aplicar filtros en tiempo real a los nuevos pagos
        applyRealTimeFilters()
    }
    
    /**
     * Agrega más pagos a la lista existente
     */
    fun addMorePayments(morePayments: List<AdminPayment>) {
        this.payments = this.payments + morePayments
        // Aplicar filtros en tiempo real a los pagos actualizados
        applyRealTimeFilters()
    }
    
    /**
     * Establece el resumen de pagos
     */
    fun updatePaymentSummary(summary: PaymentSummary?) {
        this.paymentSummary = summary
    }
    
    /**
     * Establece el estado de carga
     */
    fun updateLoading(loading: Boolean) {
        isLoading = loading
    }
    
    /**
     * Establece el estado de carga de más elementos
     */
    fun updateLoadingMore(loading: Boolean) {
        isLoadingMore = loading
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
     * Establece la página actual
     */
    fun updateCurrentPage(page: Int) {
        currentPage = page
    }
    
    /**
     * Establece si hay más pagos disponibles
     */
    fun updateHasMorePayments(hasMore: Boolean) {
        hasMorePayments = hasMore
    }
    
    /**
     * Establece el estado seleccionado para filtrar (ahora se usa advancedFilters.status)
     */
    fun updateSelectedStatus(status: String?) {
        advancedFilters = advancedFilters.copy(status = status)
        applyAdvancedFilters()
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
            // Si no hay filtros, usar ALL para admin
            PaymentFilterStatus.ALL.value
        } else {
            PaymentFilterStatusUtils.toCommaSeparatedString(selectedStatuses)
        }
    }
    
    /**
     * Verifica si hay filtros activos
     */
    fun hasActiveFilters(): Boolean {
        return selectedStatuses.isNotEmpty() || startDate != null || endDate != null || advancedFilters.hasActiveFilters()
    }
    
    /**
     * Limpia todos los filtros
     */
    fun clearAllFilters() {
        selectedStatuses = emptyList()
        startDate = null
        endDate = null
        showAdvancedFilters = false
        advancedFilters = AdvancedFilters()
        yapeCodeFilter = ""
        dateRangeFilter = ""
        filteredPayments = payments
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
        filteredPayments = payments.filter { payment ->
            // Filtro por código Yape
            val matchesYapeCode = yapeCodeFilter.isEmpty() || 
                payment.yapeCode.contains(yapeCodeFilter, ignoreCase = true)
            
            // Filtro por fecha (si está implementado)
            val matchesDateRange = dateRangeFilter.isEmpty() || 
                matchesDateRange(payment, dateRangeFilter)
            
            matchesYapeCode && matchesDateRange
        }
    }
    
    /**
     * Verifica si un pago coincide con el rango de fechas seleccionado
     */
    private fun matchesDateRange(payment: AdminPayment, dateRange: String): Boolean {
        // TODO: Implementar lógica de filtrado por fecha
        // Por ahora retorna true para no filtrar por fecha
        return true
    }
    
    /**
     * Carga la gestión de pagos del administrador con filtros dinámicos
     */
    fun loadAdminPayments(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (accessToken != null && userProfile?.adminId != null) {
            coroutineScope.launch {
                updateLoading(true)
                clearErrorMessage()
                updateCurrentPage(0)
                updateHasMorePayments(true)

                val statusString = getStatusString()
                
                paymentService.getAdminPaymentManagement(
                    adminId = userProfile?.adminId?.toInt() ?: 0,
                    page = 0,
                    size = 10,
                    status = statusString,
                    startDate = startDate,
                    endDate = endDate,
                    token = accessToken ?: ""
                ).fold(
                    onSuccess = { response ->
                        updatePayments(response.data.payments)
                        updatePaymentSummary(response.data.summary)
                        updateHasMorePayments(response.data.pagination.currentPage < response.data.pagination.totalPages - 1)
                        updateLoading(false)
                        onSuccess()
                    },
                    onFailure = { error ->
                        updateErrorMessage(error.message ?: "Error cargando gestión de pagos")
                        updateLoading(false)
                        onFailure(errorMessage)
                    }
                )
            }
        } else {
            updateErrorMessage("No se pueden cargar pagos: faltan credenciales de usuario")
            onFailure("No se pueden cargar pagos: faltan credenciales de usuario")
        }
    }
    
    /**
     * Carga las estadísticas del admin usando el endpoint unificado
     */
    fun loadAdminStats(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (accessToken != null && userProfile?.adminId != null) {
            coroutineScope.launch {
                println("ADMIN_STATS: Cargando estadísticas unificadas para adminId: ${userProfile?.adminId}, fechas: $startDate - $endDate")
                statsService.getUnifiedStatsSummary(
                    adminId = userProfile?.adminId?.toInt() ?: 0,
                    sellerId = null,
                    startDate = startDate,
                    endDate = endDate,
                    token = accessToken ?: ""
                ).fold(
                    onSuccess = { response ->
                        println("ADMIN_STATS: Estadísticas unificadas cargadas exitosamente: ${response.data}")
                        adminStats = response
                        analyticsUrls = response.data.urls
                        onSuccess()
                    },
                    onFailure = { error ->
                        println("ADMIN_STATS: Error cargando estadísticas unificadas: ${error.message}")
                        updateErrorMessage("Error cargando estadísticas: ${error.message}")
                        onFailure(errorMessage)
                    }
                )
            }
        } else {
            println("ADMIN_STATS: No se pueden cargar estadísticas - accessToken: ${accessToken != null}, adminId: ${userProfile?.adminId}")
        }
    }
    
    /**
     * Carga analytics específicos usando las URLs del endpoint unificado
     */
    fun loadAnalyticsFromUrl(
        url: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (accessToken != null) {
            coroutineScope.launch {
                println("ADMIN_ANALYTICS: Cargando analytics desde URL: $url")
                statsService.getAnalyticsFromUrl(url, accessToken ?: "").fold(
                    onSuccess = { response ->
                        println("ADMIN_ANALYTICS: Analytics cargados exitosamente desde: $url")
                        onSuccess(response)
                    },
                    onFailure = { error ->
                        println("ADMIN_ANALYTICS: Error cargando analytics desde $url: ${error.message}")
                        onFailure(error.message ?: "Error desconocido")
                    }
                )
            }
        } else {
            println("ADMIN_ANALYTICS: No se pueden cargar analytics - accessToken: ${accessToken != null}")
        }
    }
    
    /**
     * Carga más pagos
     */
    fun loadMorePayments(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (accessToken != null && userProfile?.adminId != null && hasMorePayments && !isLoadingMore) {
            coroutineScope.launch {
                updateLoadingMore(true)
                val nextPage = currentPage + 1

                paymentService.getAdminPaymentManagement(
                    adminId = userProfile?.adminId?.toInt() ?: 0,
                    page = nextPage,
                    size = 10,
                    status = getStatusString(),
                    startDate = startDate,
                    endDate = endDate,
                    token = accessToken ?: ""
                ).fold(
                    onSuccess = { response ->
                        addMorePayments(response.data.payments)
                        updateCurrentPage(nextPage)
                        updateHasMorePayments(response.data.pagination.currentPage < response.data.pagination.totalPages - 1)
                        updateLoadingMore(false)
                        onSuccess()
                    },
                    onFailure = { error ->
                        updateErrorMessage(error.message ?: "Error cargando más pagos")
                        updateLoadingMore(false)
                        onFailure(errorMessage)
                    }
                )
            }
        }
    }
    
    /**
     * Filtra por estado
     */
    fun filterByStatus(
        status: String?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        updateSelectedStatus(status)
        loadAdminPayments(onSuccess, onFailure)
    }
    
    /**
     * Filtra por rango de fechas
     */
    fun filterByDateRange(
        startDate: String?,
        endDate: String?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        updateDateRange(startDate, endDate)
        loadAdminPayments(onSuccess, onFailure)
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
        loadAdminPayments(onSuccess, onFailure)
    }
    
    /**
     * Verifica si se puede cargar pagos
     */
    fun canLoadPayments(): Boolean {
        return accessToken != null && userProfile?.adminId != null
    }
    
    /**
     * Verifica si hay pagos
     */
    fun hasPayments(): Boolean {
        return payments.isNotEmpty()
    }
    
    /**
     * Verifica si se puede cargar más pagos
     */
    fun canLoadMorePayments(): Boolean {
        return hasMorePayments && payments.isNotEmpty() && !isLoadingMore
    }
    
    /**
     * Verifica si hay un filtro activo
     */
    fun hasActiveFilter(): Boolean {
        return hasActiveFilters()
    }
    
    /**
     * Obtiene el mensaje de estado vacío
     */
    fun getEmptyStateMessage(): String {
        return if (hasActiveFilter() || advancedFilters.hasActiveFilters()) {
            "No hay pagos que coincidan con los filtros aplicados"
        } else {
            "No hay pagos registrados"
        }
    }
    
    /**
     * Actualiza los filtros avanzados
     */
    fun updateAdvancedFilters(filters: AdvancedFilters) {
        advancedFilters = filters
        applyAdvancedFilters()
    }
    
    /**
     * Aplica los filtros avanzados a los pagos cargados con optimización
     */
    private fun applyAdvancedFilters() {
        // Solo recalcular si hay filtros activos
        if (!advancedFilters.hasActiveFilters()) {
            filteredPayments = payments
            return
        }

        filteredPayments = payments.filter { payment ->
            // Filtro por estado
            if (advancedFilters.status != null && payment.status != advancedFilters.status) {
                return@filter false
            }

            // Filtro por vendedor (usando sellerName ya que AdminPayment no tiene sellerId)
            if (advancedFilters.sellerName.isNotEmpty() &&
                !payment.sellerName.contains(advancedFilters.sellerName, ignoreCase = true)) {
                return@filter false
            }

            // Filtro por sucursal
            if (advancedFilters.branchName != null && payment.branchName != advancedFilters.branchName) {
                return@filter false
            }

            // Filtro por rango de montos
            val minAmount = advancedFilters.minAmount
            val maxAmount = advancedFilters.maxAmount
            if (minAmount != null && payment.amount < minAmount) {
                return@filter false
            }
            if (maxAmount != null && payment.amount > maxAmount) {
                return@filter false
            }

            // Filtro por código Yape
            if (advancedFilters.yapeCode.isNotEmpty() &&
                !payment.yapeCode.contains(advancedFilters.yapeCode, ignoreCase = true)) {
                return@filter false
            }

            // Filtro por nombre del cliente
            if (advancedFilters.customerName.isNotEmpty() &&
                !payment.senderName.contains(advancedFilters.customerName, ignoreCase = true)) {
                return@filter false
            }

            true
        }
    }
    
    /**
     * Limpia todos los filtros avanzados
     */
    fun clearAdvancedFilters() {
        advancedFilters = AdvancedFilters()
        filteredPayments = payments
    }
    
    /**
     * Obtiene los pagos filtrados para mostrar en la UI
     */
    fun getDisplayPayments(): List<AdminPayment> {
        return if (advancedFilters.hasActiveFilters()) {
            filteredPayments
        } else {
            payments
        }
    }
    
    /**
     * Verifica si hay filtros avanzados activos
     */
    fun hasAdvancedFilters(): Boolean {
        return advancedFilters.hasActiveFilters()
    }
    
    /**
     * Obtiene el conteo de filtros activos
     */
    fun getActiveFiltersCount(): Int {
        var count = 0
        if (startDate != null) count++
        if (endDate != null) count++
        count += advancedFilters.getActiveFiltersCount()
        return count
    }
}
