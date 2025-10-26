package org.sysarp.project.viewmodel.admin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.sysarp.project.data.AffiliationCodeData
import org.sysarp.project.data.BranchInfo
import org.sysarp.project.data.QuickSummaryData
import org.sysarp.project.data.UserProfile
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.affiliation.AffiliationService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.branch.BranchService
import org.sysarp.project.service.stats.StatsService
import org.sysarp.project.service.websocket.PaymentWebSocketService

/**
 * Data class para las estadísticas del dashboard
 */
data class DashboardStats(
    val totalSellers: Int,
    val activeSellers: Int,
    val totalBranches: Int,
    val totalTransactions: Int,
    val totalRevenue: Double,
    val pendingPayments: Int
)

/**
 * Data class para datos enriquecidos del dashboard del administrador
 */
data class EnhancedAdminDashboardData(
    val overview: org.sysarp.project.data.UnifiedOverviewData,
    val topSellers: List<org.sysarp.project.data.UnifiedTopSellerData>,
    val performanceMetrics: org.sysarp.project.data.UnifiedPerformanceMetricsData,
    val urls: org.sysarp.project.data.UnifiedAnalyticsUrls,
    val dashboardStats: DashboardStats,
    val userType: String,
    val userId: Int
)

/**
 * ViewModel para el dashboard del administrador
 * Centraliza la lógica de negocio y estado
 */
class AdminDashboardViewModel(
    private val authService: AuthService,
    private val sellerService: SellerService,
    private val statsService: StatsService,
    private val affiliationService: AffiliationService,
    private val branchService: BranchService,
    private val webSocketService: PaymentWebSocketService
) {

    private val coroutineScope = CoroutineScope(kotlinx.coroutines.Dispatchers.Default)

    // Estados del ViewModel usando authService directamente
    val userProfile: StateFlow<UserProfile?> = authService.userProfile
    val accessToken: StateFlow<String?> = authService.accessToken

    private val _dashboardStats = MutableStateFlow(DashboardStats(0, 0, 0, 0, 0.0, 0))
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats.asStateFlow()


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Estados de afiliación
    private val _showAffiliationDialog = MutableStateFlow(false)
    val showAffiliationDialog: StateFlow<Boolean> = _showAffiliationDialog.asStateFlow()
    
    private val _isLoadingAffiliation = MutableStateFlow(false)
    val isLoadingAffiliation: StateFlow<Boolean> = _isLoadingAffiliation.asStateFlow()
    
    private val _affiliationError = MutableStateFlow<String?>(null)
    val affiliationError: StateFlow<String?> = _affiliationError.asStateFlow()
    
    private val _generatedAffiliationCode = MutableStateFlow<AffiliationCodeData?>(null)
    val generatedAffiliationCode: StateFlow<AffiliationCodeData?> = _generatedAffiliationCode.asStateFlow()
    
    private val _branches = MutableStateFlow<List<BranchInfo>>(emptyList())
    val branches: StateFlow<List<BranchInfo>> = _branches.asStateFlow()

    // Estados adicionales para cálculos avanzados
    private val _previousStats = MutableStateFlow<DashboardStats?>(null)
    private val _rejectedPaymentsCount = MutableStateFlow(0)
    private val _averageConfirmationTime = MutableStateFlow(0.0)
    
    // Nuevo: Almacenar la respuesta completa de la API para datos enriquecidos
    private val _unifiedStatsResponse = MutableStateFlow<org.sysarp.project.data.UnifiedStatsResponse?>(null)
    val unifiedStatsResponse: StateFlow<org.sysarp.project.data.UnifiedStatsResponse?> = _unifiedStatsResponse.asStateFlow()

    /**
     * Inicializar el ViewModel
     */
    fun initialize() {
        coroutineScope.launch {
            try {
                // Observar cambios en el perfil de usuario desde authService
                authService.userProfile.collect { profile ->
                    if (profile != null) {
                        val token = authService.accessToken.value
                        if (token != null) {
                            loadDashboardStats(profile, token)
                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error inicializando datos del usuario: ${e.message}"
            }
        }
    }

    /**
     * Cargar estadísticas del dashboard con lazy loading
     */
    private fun loadDashboardStats(userProfile: UserProfile, accessToken: String) {
        coroutineScope.launch {
            _isLoading.value = true

            try {
                // Cargar estadísticas básicas usando endpoint unificado
                val statsResult = statsService.getUnifiedStatsSummary(
                    adminId = userProfile.adminId!!.toInt(),
                    sellerId = null,
                    startDate = null,
                    endDate = null,
                    token = accessToken
                )

                statsResult.fold(
                    onSuccess = { stats ->
                        // Almacenar la respuesta completa para datos enriquecidos
                        _unifiedStatsResponse.value = stats
                        
                        // Actualizar UI inmediatamente con datos básicos
                        val newStats = DashboardStats(
                            totalSellers = 0, // Se cargará después
                            activeSellers = 0, // Se cargará después
                            totalBranches = 0, // Se cargará después
                            totalTransactions = stats.data.overview.totalTransactions,
                            totalRevenue = stats.data.overview.confirmedSales,
                            pendingPayments = stats.data.performanceMetrics.pendingPayments
                        )
                        
                        // Calcular métricas avanzadas
                        calculateAdvancedMetrics(newStats)
                        
                        _dashboardStats.value = newStats
                        
                        // Cargar datos adicionales en background (lazy loading)
                        loadAdditionalData(userProfile, accessToken)
                    },
                    onFailure = { error ->
                        _errorMessage.value = "Error cargando estadísticas: ${error.message}"
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
     * Cargar datos adicionales en background
     */
    private fun loadAdditionalData(userProfile: UserProfile, accessToken: String) {
        coroutineScope.launch {
            try {
                // Cargar vendedores y sucursales en paralelo (sin bloquear UI)
                val sellersResult =
                    sellerService.getMySellers(userProfile.adminId!!.toInt(), 1, 30, accessToken)
                val branchesResult =
                    branchService.getBranches(userProfile.adminId!!.toInt(), accessToken)

                // Procesar resultados cuando estén listos
                sellersResult.fold(
                    onSuccess = { sellersResponse ->
                        branchesResult.fold(
                            onSuccess = { branchesResponse ->
                                // Actualizar solo los campos que faltaban
                                _dashboardStats.value = _dashboardStats.value.copy(
                                    totalSellers = sellersResponse.data?.sellers?.size ?: 0,
                                    activeSellers = sellersResponse.data?.sellers?.count { it.isActive }
                                        ?: 0,
                                    totalBranches = branchesResponse.branches.size
                                )
                                _isLoading.value = false
                            },
                            onFailure = { error ->
                                _errorMessage.value = "Error cargando sucursales: ${error.message}"
                                _isLoading.value = false
                            }
                        )
                    },
                    onFailure = { error ->
                        _errorMessage.value = "Error cargando vendedores: ${error.message}"
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Error cargando datos adicionales: ${e.message}"
                _isLoading.value = false
            }
        }
    }


    /**
     * Desconectar WebSocket
     */
    fun disconnectWebSocket() {
        coroutineScope.launch {
            webSocketService.stop()
        }
    }
    
    /**
     * Generar datos enriquecidos para el dashboard del administrador
     */
    fun getEnhancedAdminData(): EnhancedAdminDashboardData? {
        val response = _unifiedStatsResponse.value ?: return null
        val stats = _dashboardStats.value
        
        return EnhancedAdminDashboardData(
            overview = response.data.overview,
            topSellers = response.data.topSellers ?: emptyList(),
            performanceMetrics = response.data.performanceMetrics,
            urls = response.data.urls,
            dashboardStats = stats,
            userType = response.data.userType,
            userId = response.data.userId
        )
    }
    
    /**
     * Generar QuickSummaryData mejorado usando los nuevos datos de la API
     * Mantiene compatibilidad con la UI existente
     */
    fun getEnhancedQuickSummaryData(): QuickSummaryData? {
        val response = _unifiedStatsResponse.value ?: return null
        val overview = response.data.overview
        val performance = response.data.performanceMetrics
        
        return QuickSummaryData(
            confirmedSales = overview.confirmedSales,
            allSales = overview.allSales,
            totalTransactions = overview.totalTransactions,
            averageTransactionValue = overview.averageTransactionValue,
            salesGrowth = overview.salesGrowth,
            transactionGrowth = overview.transactionGrowth,
            averageGrowth = overview.averageGrowth,
            pendingPayments = performance.pendingPayments,
            confirmedPayments = performance.confirmedPayments,
            rejectedPayments = performance.rejectedPayments,
            claimRate = performance.claimRate,
            averageConfirmationTime = performance.averageConfirmationTime,
            confirmedTransactions = overview.confirmedTransactions,
            pendingTransactions = overview.pendingTransactions,
            rejectedTransactions = overview.rejectedTransactions
        )
    }

    /**
     * Limpiar mensajes de error
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Refrescar datos del dashboard
     */
    fun refreshDashboard() {
        val userProfile = authService.userProfile.value
        val accessToken = authService.accessToken.value

        if (userProfile != null && accessToken != null) {
            loadDashboardStats(userProfile, accessToken)
        }
    }

    /**
     * Verificar si la sesión es válida
     */
    suspend fun isSessionValid(): Boolean {
        val accessToken = authService.accessToken.value
        return accessToken != null && authService.userProfile.value != null
    }

    /**
     * Calcular métricas avanzadas basadas en estadísticas actuales y previas
     */
    private fun calculateAdvancedMetrics(currentStats: DashboardStats) {
        val previousStats = _previousStats.value
        
        // Calcular crecimiento de ventas
        val salesGrowth = if (previousStats != null && previousStats.totalRevenue > 0) {
            ((currentStats.totalRevenue - previousStats.totalRevenue) / previousStats.totalRevenue) * 100
        } else {
            0.0
        }
        
        // Calcular crecimiento de transacciones
        val transactionGrowth = if (previousStats != null && previousStats.totalTransactions > 0) {
            ((currentStats.totalTransactions - previousStats.totalTransactions).toDouble() / previousStats.totalTransactions) * 100
        } else {
            0.0
        }
        
        // Calcular crecimiento promedio
        val averageGrowth = (salesGrowth + transactionGrowth) / 2
        
        // Calcular tiempo promedio de confirmación (simulado basado en datos históricos)
        val averageConfirmationTime = calculateAverageConfirmationTime(currentStats)
        
        // Calcular pagos rechazados (estimación basada en patrones)
        val rejectedPayments = calculateRejectedPayments(currentStats)
        
        // Actualizar estados
        _averageConfirmationTime.value = averageConfirmationTime
        _rejectedPaymentsCount.value = rejectedPayments
        
        // Guardar estadísticas actuales como previas para el próximo cálculo
        _previousStats.value = currentStats
    }
    
    /**
     * Calcular tiempo promedio de confirmación de pagos
     */
    private fun calculateAverageConfirmationTime(stats: DashboardStats): Double {
        // Simulación basada en patrones de negocio
        return when {
            stats.totalTransactions > 1000 -> 1.5 // Empresas grandes: más rápido
            stats.totalTransactions > 100 -> 2.3   // Empresas medianas
            stats.totalTransactions > 10 -> 3.1   // Empresas pequeñas
            else -> 4.0 // Empresas muy pequeñas: más lento
        }
    }
    
    /**
     * Calcular número estimado de pagos rechazados
     */
    private fun calculateRejectedPayments(stats: DashboardStats): Int {
        // Estimación basada en estadísticas de la industria (2-5% de rechazo)
        val rejectionRate = 0.03 // 3% de rechazo promedio
        return (stats.totalTransactions * rejectionRate).toInt()
    }

    /**
     * Convertir DashboardStats a QuickSummaryData con métricas calculadas
     */
    private fun convertToQuickSummaryData(stats: DashboardStats): QuickSummaryData {
        val previousStats = _previousStats.value
        
        // Calcular crecimiento de ventas
        val salesGrowth = if (previousStats != null && previousStats.totalRevenue > 0) {
            ((stats.totalRevenue - previousStats.totalRevenue) / previousStats.totalRevenue) * 100
        } else {
            0.0
        }
        
        // Calcular crecimiento de transacciones
        val transactionGrowth = if (previousStats != null && previousStats.totalTransactions > 0) {
            ((stats.totalTransactions - previousStats.totalTransactions).toDouble() / previousStats.totalTransactions) * 100
        } else {
            0.0
        }
        
        // Calcular crecimiento promedio
        val averageGrowth = (salesGrowth + transactionGrowth) / 2
        
        return QuickSummaryData(
            confirmedSales = stats.totalRevenue,
            allSales = stats.totalRevenue, // For admin, both values are the same initially
            totalTransactions = stats.totalTransactions,
            averageTransactionValue = if (stats.totalTransactions > 0) stats.totalRevenue / stats.totalTransactions else 0.0,
            salesGrowth = salesGrowth,
            transactionGrowth = transactionGrowth,
            averageGrowth = averageGrowth,
            pendingPayments = stats.pendingPayments,
            confirmedPayments = stats.totalTransactions - stats.pendingPayments - _rejectedPaymentsCount.value,
            rejectedPayments = _rejectedPaymentsCount.value,
            claimRate = if (stats.totalTransactions > 0) (stats.pendingPayments.toDouble() / stats.totalTransactions) * 100 else 0.0,
            averageConfirmationTime = _averageConfirmationTime.value,
            confirmedTransactions = stats.totalTransactions - stats.pendingPayments - _rejectedPaymentsCount.value,
            pendingTransactions = stats.pendingPayments,
            rejectedTransactions = _rejectedPaymentsCount.value
        )
    }

    /**
     * Obtener QuickSummaryData para el dashboard
     */
    fun getQuickSummaryData(): QuickSummaryData? {
        return convertToQuickSummaryData(_dashboardStats.value)
    }
    
    /**
     * Logout
     */
    suspend fun logout() {
        authService.logout()
    }
    
    /**
     * Mostrar diálogo de afiliación
     */
    fun showAffiliationDialog() {
        _showAffiliationDialog.value = true
        loadBranches()
    }
    
    /**
     * Cerrar diálogo de afiliación
     */
    fun dismissAffiliationDialog() {
        _showAffiliationDialog.value = false
        _generatedAffiliationCode.value = null
        _affiliationError.value = null
    }
    
    /**
     * Cargar sucursales para el diálogo de afiliación
     */
    private fun loadBranches() {
        coroutineScope.launch {
            val userProfile = authService.userProfile.value
            val accessToken = authService.accessToken.value
            
            if (userProfile != null && accessToken != null) {
                try {
                    val branchesResult = branchService.getBranches(
                        userProfile.adminId!!.toInt(),
                        accessToken
                    )
                    
                    branchesResult.fold(
                        onSuccess = { response ->
                            _branches.value = response.branches
                        },
                        onFailure = { error ->
                            _affiliationError.value = "Error cargando sucursales: ${error.message}"
                        }
                    )
                } catch (e: Exception) {
                    _affiliationError.value = "Error inesperado: ${e.message}"
                }
            }
        }
    }
    
    /**
     * Generar código de afiliación
     */
    fun generateAffiliationCode(
        branchId: Int,
        expirationHours: Int,
        maxUses: Int,
        notes: String?
    ) {
        coroutineScope.launch {
            val userProfile = authService.userProfile.value
            val accessToken = authService.accessToken.value
            
            if (userProfile != null && accessToken != null) {
                _isLoadingAffiliation.value = true
                _affiliationError.value = null
                
                try {
                    val result = affiliationService.generateAffiliationCode(
                        adminId = userProfile.adminId!!.toInt(),
                        branchId = branchId,
                        expirationHours = expirationHours,
                        maxUses = maxUses,
                        notes = notes ?: "",
                        accessToken = accessToken
                    )
                    
                    result.fold(
                        onSuccess = { response ->
                            if (response.success && response.data != null) {
                                _generatedAffiliationCode.value = response.data
                            } else {
                                _affiliationError.value = response.message ?: "Error generando código"
                            }
                            _isLoadingAffiliation.value = false
                        },
                        onFailure = { error ->
                            _affiliationError.value = error.message ?: "Error generando código de afiliación"
                            _isLoadingAffiliation.value = false
                        }
                    )
                } catch (e: Exception) {
                    _affiliationError.value = "Error inesperado: ${e.message}"
                    _isLoadingAffiliation.value = false
                }
            } else {
                _affiliationError.value = "Sesión no válida"
            }
        }
    }
}