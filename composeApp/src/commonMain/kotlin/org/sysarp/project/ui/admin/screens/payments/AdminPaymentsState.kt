package org.sysarp.project.ui.admin.screens.payments

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sysarp.project.data.AdminPayment
import org.sysarp.project.data.PaymentSummary
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.ui.admin.screens.payments.components.AdvancedFilters

/**
 * Estado y lógica de negocio para AdminPaymentsScreen
 */
class AdminPaymentsState(
    private val authService: AuthService,
    val paymentService: PaymentService,
    private val coroutineScope: CoroutineScope
) {
    // Estados de datos
    var payments by mutableStateOf<List<AdminPayment>>(emptyList())
        private set
    
    var paymentSummary by mutableStateOf<PaymentSummary?>(null)
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
    
    // Estados de pagos filtrados (para mostrar en la UI)
    var filteredPayments by mutableStateOf<List<AdminPayment>>(emptyList())
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
        // Inicializar filteredPayments con todos los pagos si no hay filtros activos
        if (!advancedFilters.hasActiveFilters()) {
            filteredPayments = payments
        } else {
            applyAdvancedFilters() // Aplicar filtros a los nuevos pagos
        }
    }
    
    /**
     * Agrega más pagos a la lista existente
     */
    fun addMorePayments(morePayments: List<AdminPayment>) {
        this.payments = this.payments + morePayments
        // Inicializar filteredPayments con todos los pagos si no hay filtros activos
        if (!advancedFilters.hasActiveFilters()) {
            filteredPayments = this.payments
        } else {
            applyAdvancedFilters() // Aplicar filtros a los pagos actualizados
        }
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
     * Carga la gestión de pagos del administrador
     */
    fun loadAdminPayments(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        println("ADMIN_PAYMENTS_STATE: loadAdminPayments - accessToken: ${accessToken != null}, adminId: ${userProfile?.adminId}")
        
        if (accessToken != null && userProfile?.adminId != null) {
            coroutineScope.launch {
                println("ADMIN_PAYMENTS_STATE: Iniciando carga de pagos...")
                updateLoading(true)
                clearErrorMessage()
                updateCurrentPage(0)
                updateHasMorePayments(true)

                paymentService.getAdminPaymentManagement(
                    adminId = userProfile?.adminId?.toInt() ?: 0,
                    page = 0,
                    size = 10,
                    status = advancedFilters.status,
                    startDate = startDate,
                    endDate = endDate,
                    token = accessToken ?: ""
                ).fold(
                    onSuccess = { response ->
                        println("ADMIN_PAYMENTS_STATE: Pagos cargados - ${response.data.payments.size} pagos")
                        updatePayments(response.data.payments)
                        updatePaymentSummary(response.data.summary)
                        updateHasMorePayments(response.data.pagination.currentPage < response.data.pagination.totalPages - 1)
                        updateLoading(false)
                        onSuccess()
                    },
                    onFailure = { error ->
                        println("ADMIN_PAYMENTS_STATE: Error cargando pagos: ${error.message}")
                        updateErrorMessage(error.message ?: "Error cargando gestión de pagos")
                        updateLoading(false)
                        onFailure(errorMessage)
                    }
                )
            }
        } else {
            println("ADMIN_PAYMENTS_STATE: No se pueden cargar pagos - faltan credenciales")
            updateErrorMessage("No se pueden cargar pagos: faltan credenciales de usuario")
            onFailure("No se pueden cargar pagos: faltan credenciales de usuario")
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
                    status = advancedFilters.status,
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
        return advancedFilters.status != null || startDate != null || endDate != null || advancedFilters.hasActiveFilters()
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
