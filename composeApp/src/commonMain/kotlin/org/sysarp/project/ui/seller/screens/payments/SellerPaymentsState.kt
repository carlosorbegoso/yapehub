package org.sysarp.project.ui.seller.screens.payments

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.payment.PaymentService

/**
 * Estado y lógica de negocio para SellerPaymentsScreen
 */
class SellerPaymentsState(
    private val authService: AuthService,
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
    }
    
    /**
     * Establece los pagos confirmados
     */
    fun updateConfirmedPayments(payments: List<SellerPendingPayment>) {
        confirmedPayments = payments
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
     * Establece las fechas de filtro
     */
    fun updateDateRange(startDate: String?, endDate: String?) {
        this.startDate = startDate
        this.endDate = endDate
    }
    
    /**
     * Carga los pagos pendientes
     */
    fun loadPendingPayments(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (accessToken != null && userProfile?.sellerId != null) {
            coroutineScope.launch {
                updateLoading(true)
                clearErrorMessage()

                paymentService.getPendingPayments(
                    sellerId = userProfile?.sellerId?.toInt() ?: 0,
                    page = 0,
                    limit = 50,
                    startDate = startDate,
                    endDate = endDate,
                    token = accessToken ?: ""
                ).fold(
                    onSuccess = { response ->
                        updatePendingPayments(response.data.payments)
                        updateLoading(false)
                        onSuccess()
                    },
                    onFailure = { error ->
                        updateErrorMessage(error.message ?: "Error cargando pagos pendientes")
                        updateLoading(false)
                        onFailure(errorMessage)
                    }
                )
            }
        }
    }
    
    /**
     * Carga los pagos confirmados
     */
    fun loadConfirmedPayments(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (accessToken != null && userProfile?.sellerId != null) {
            coroutineScope.launch {
                updateLoading(true)
                clearErrorMessage()

                paymentService.getConfirmedPayments(
                    sellerId = userProfile?.sellerId?.toInt() ?: 0,
                    page = 0,
                    size = 50,
                    startDate = startDate,
                    endDate = endDate,
                    token = accessToken ?: ""
                ).fold(
                    onSuccess = { response ->
                        updateConfirmedPayments(response.data.payments)
                        updateLoading(false)
                        onSuccess()
                    },
                    onFailure = { error ->
                        updateErrorMessage(error.message ?: "Error cargando pagos confirmados")
                        updateLoading(false)
                        onFailure(errorMessage)
                    }
                )
            }
        }
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
                        // Recargar ambas listas
                        loadPendingPayments(
                            onSuccess = { },
                            onFailure = { }
                        )
                        loadConfirmedPayments(
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
     * Recarga todos los pagos
     */
    fun refreshAllPayments(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        loadPendingPayments(
            onSuccess = { },
            onFailure = { }
        )
        loadConfirmedPayments(
            onSuccess = { },
            onFailure = { }
        )
        onSuccess()
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
     * Filtra por rango de fechas
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
}
