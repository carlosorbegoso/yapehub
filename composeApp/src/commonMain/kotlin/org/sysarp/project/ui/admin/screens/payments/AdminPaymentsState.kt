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
    
    // Estados de filtros
    var selectedStatus by mutableStateOf<String?>(null)
        private set
    
    // Estados de filtros de fechas
    var startDate by mutableStateOf<String?>(null)
        private set
    
    var endDate by mutableStateOf<String?>(null)
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
    }
    
    /**
     * Agrega más pagos a la lista existente
     */
    fun addMorePayments(morePayments: List<AdminPayment>) {
        this.payments = this.payments + morePayments
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
     * Establece el estado seleccionado para filtrar
     */
    fun updateSelectedStatus(status: String?) {
        selectedStatus = status
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
        if (accessToken != null && userProfile?.adminId != null) {
            coroutineScope.launch {
                updateLoading(true)
                clearErrorMessage()
                updateCurrentPage(0)
                updateHasMorePayments(true)

                paymentService.getAdminPaymentManagement(
                    adminId = userProfile?.adminId?.toInt() ?: 0,
                    page = 0,
                    size = 10,
                    status = selectedStatus,
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
                    status = selectedStatus,
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
        return selectedStatus != null || startDate != null || endDate != null
    }
    
    /**
     * Obtiene el mensaje de estado vacío
     */
    fun getEmptyStateMessage(): String {
        return if (hasActiveFilter()) {
            "No hay pagos con el estado seleccionado"
        } else {
            "No hay pagos registrados"
        }
    }
}
