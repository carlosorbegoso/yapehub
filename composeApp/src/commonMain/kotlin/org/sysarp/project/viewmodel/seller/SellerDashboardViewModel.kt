package org.sysarp.project.viewmodel.seller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sysarp.project.data.SellerPendingPayment
import org.sysarp.project.data.SellerStatsData
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.notification.HybridNotificationManager
import org.sysarp.project.service.payment.PaymentService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.service.websocket.PaymentWebSocketService

/**
 * ViewModel específico para el dashboard del vendedor
 * Maneja toda la lógica de negocio relacionada con el dashboard del vendedor
 */
class SellerDashboardViewModel(
    private val authService: AuthService,
    private val paymentService: PaymentService,
    private val statsService: StatsService,
    private val webSocketService: PaymentWebSocketService,
    private val hybridNotificationManager: HybridNotificationManager,
    private val coroutineScope: CoroutineScope
) {
    
    // Estados de datos principales
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()
    
    // Estados de pagos
    private val _pendingPayments = MutableStateFlow<List<SellerPendingPayment>>(emptyList())
    val pendingPayments: StateFlow<List<SellerPendingPayment>> = _pendingPayments.asStateFlow()
    
    private val _confirmedPayments = MutableStateFlow<List<SellerPendingPayment>>(emptyList())
    val confirmedPayments: StateFlow<List<SellerPendingPayment>> = _confirmedPayments.asStateFlow()
    
    // Estados de estadísticas
    private val _sellerStats = MutableStateFlow<SellerStatsData?>(null)
    val sellerStats: StateFlow<SellerStatsData?> = _sellerStats.asStateFlow()
    
    // Estados de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    // Estados de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Estados de conexión
    private val _webSocketConnected = MutableStateFlow(false)
    val webSocketConnected: StateFlow<Boolean> = _webSocketConnected.asStateFlow()
    
    // Estados de procesamiento
    private val _processingPayments = MutableStateFlow<Set<Int>>(emptySet())
    val processingPayments: StateFlow<Set<Int>> = _processingPayments.asStateFlow()
    
    init {
        loadUserData()
        connectWebSocket()
    }
    
    /**
     * Cargar datos del usuario
     */
    private fun loadUserData() {
        coroutineScope.launch {
            try {
                val profile = authService.userProfile.value
                val token = authService.accessToken.value
                
                _userProfile.value = profile
                _accessToken.value = token
                
                if (profile != null && token != null) {
                    loadInitialData(profile, token)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error cargando datos del usuario: ${e.message}"
            }
        }
    }
    
    /**
     * Cargar datos iniciales con lazy loading
     */
    private fun loadInitialData(userProfile: UserProfile, accessToken: String) {
        coroutineScope.launch {
            _isLoading.value = true
            
            try {
                // Cargar datos críticos primero (pagos pendientes)
                val pendingResult = paymentService.getPendingPayments(userProfile.sellerId!!.toInt(), 0, 20, null, null, accessToken)
                
                pendingResult.fold(
                    onSuccess = { response ->
                        _pendingPayments.value = response.data.payments
                        
                        // Cargar datos adicionales en background
                        loadAdditionalSellerData(userProfile, accessToken)
                    },
                    onFailure = { error ->
                        _errorMessage.value = "Error cargando pagos pendientes: ${error.message}"
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Cargar datos adicionales del vendedor en background
     */
    private fun loadAdditionalSellerData(userProfile: UserProfile, accessToken: String) {
        coroutineScope.launch {
            try {
                // Cargar datos menos críticos en paralelo
                val confirmedResult = paymentService.getConfirmedPayments(userProfile.sellerId!!.toInt(), 0, 20, null, null, accessToken)
                val statsResult = statsService.getSellerStatsSummary(userProfile.sellerId!!.toInt(), null, null, accessToken)
                
                // Procesar resultados cuando estén listos
                confirmedResult.fold(
                    onSuccess = { response -> _confirmedPayments.value = response.data.payments },
                    onFailure = { error -> _errorMessage.value = "Error cargando pagos confirmados: ${error.message}" }
                )
                
                statsResult.fold(
                    onSuccess = { response -> _sellerStats.value = response.data },
                    onFailure = { error -> _errorMessage.value = "Error cargando estadísticas: ${error.message}" }
                )
                
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error cargando datos adicionales: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Conectar WebSocket
     */
    private fun connectWebSocket() {
        coroutineScope.launch {
            try {
                val userProfile = _userProfile.value
                val accessToken = _accessToken.value
                
                if (userProfile != null && accessToken != null) {
                    webSocketService.startAutoConnect()
                    _webSocketConnected.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error conectando WebSocket: ${e.message}"
                _webSocketConnected.value = false
            }
        }
    }
    
    /**
     * Refrescar datos
     */
    fun refreshData() {
        val userProfile = _userProfile.value
        val accessToken = _accessToken.value
        
        if (userProfile != null && accessToken != null) {
            _isRefreshing.value = true
            
            coroutineScope.launch {
                try {
                    // Refrescar pagos pendientes
                    paymentService.getPendingPayments(userProfile.sellerId!!.toInt(), 0, 20, null, null, accessToken)
                        .fold(
                            onSuccess = { response -> _pendingPayments.value = response.data.payments },
                            onFailure = { error -> _errorMessage.value = "Error refrescando pagos: ${error.message}" }
                        )
                    
                    // Refrescar estadísticas
                    statsService.getSellerStatsSummary(userProfile.sellerId!!.toInt(), null, null, accessToken)
                        .fold(
                            onSuccess = { response -> _sellerStats.value = response.data },
                            onFailure = { error -> _errorMessage.value = "Error refrescando estadísticas: ${error.message}" }
                        )
                    
                    _isRefreshing.value = false
                } catch (e: Exception) {
                    _errorMessage.value = "Error inesperado: ${e.message}"
                    _isRefreshing.value = false
                }
            }
        }
    }
    
    /**
     * Confirmar pago
     */
    fun confirmPayment(
        paymentId: Int,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userProfile = _userProfile.value
        val accessToken = _accessToken.value
        
        if (userProfile == null || accessToken == null) {
            onFailure("No se pudo obtener información del usuario")
            return
        }
        
        coroutineScope.launch {
            _processingPayments.value = _processingPayments.value + paymentId
            
            try {
                val result = paymentService.claimPayment(userProfile.sellerId!!.toInt(), paymentId, accessToken)
                
                result.fold(
                    onSuccess = { response ->
                        _processingPayments.value = _processingPayments.value - paymentId
                        // Actualizar lista de pagos pendientes
                        _pendingPayments.value = _pendingPayments.value.filter { it.paymentId != paymentId }
                        onSuccess()
                    },
                    onFailure = { error ->
                        _processingPayments.value = _processingPayments.value - paymentId
                        onFailure(error.message ?: "Error confirmando pago")
                    }
                )
            } catch (e: Exception) {
                _processingPayments.value = _processingPayments.value - paymentId
                onFailure(e.message ?: "Error inesperado")
            }
        }
    }
    
    /**
     * Desconectar WebSocket
     */
    fun disconnectWebSocket() {
        coroutineScope.launch {
            webSocketService.stop()
            _webSocketConnected.value = false
        }
    }
    
    /**
     * Limpiar mensajes de error
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
