package org.sysarp.project.ui.admin.screens.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.sysarp.project.data.AdminStatsData
import org.sysarp.project.data.BillingDashboard
import org.sysarp.project.data.BranchInfo
import org.sysarp.project.data.ConnectedSellersData
import org.sysarp.project.data.DeactivationRequest
import org.sysarp.project.data.GenerateAffiliationCodeResponse
import org.sysarp.project.data.QRCodeData
import org.sysarp.project.data.QuickSummaryData
import org.sysarp.project.service.SellerService
import org.sysarp.project.service.affiliation.AffiliationService
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.billing.BillingService
import org.sysarp.project.service.branch.BranchService
import org.sysarp.project.service.qr.QRService
import org.sysarp.project.service.stats.StatsService
import kotlin.time.ExperimentalTime

/**
 * Estado y lógica de negocio para AdminDashboardScreen
 */
@OptIn(ExperimentalTime::class)
class AdminDashboardState(
    private val authService: AuthService,
    private val sellerService: SellerService,
    val statsService: StatsService,
    private val affiliationService: AffiliationService,
    private val qrService: QRService,
    private val branchService: BranchService,
    val webSocketService: org.sysarp.project.service.websocket.PaymentWebSocketService,
    private val coroutineScope: CoroutineScope
) {
    // Estados de datos principales
    var quickSummaryData by mutableStateOf<QuickSummaryData?>(null)
        private set
    
    var adminStatsData by mutableStateOf<AdminStatsData?>(null)
        private set
    
    var connectedSellersData by mutableStateOf<ConnectedSellersData?>(null)
        private set
    
    var branches by mutableStateOf<List<BranchInfo>>(emptyList())
        private set
    
    var pendingRequests by mutableStateOf<List<DeactivationRequest>>(emptyList())
        private set
    
    
    // Estados de carga
    var isLoadingStats by mutableStateOf(false)
        private set
    
    var isLoadingAdminStats by mutableStateOf(false)
        private set
    
    var isLoadingSellers by mutableStateOf(false)
        private set
    
    var isLoadingBranches by mutableStateOf(false)
        private set
    
    var isLoadingAffiliation by mutableStateOf(false)
        private set
    
    var isLoadingQR by mutableStateOf(false)
        private set
    
    
    // Estados de error
    var statsError by mutableStateOf("")
        private set
    
    var adminStatsError by mutableStateOf("")
        private set
    
    var sellersError by mutableStateOf("")
        private set
    
    var affiliationError by mutableStateOf<String?>(null)
        private set
    
    var qrError by mutableStateOf<String?>(null)
        private set
    
    
    // Estados de diálogos
    var showAffiliationDialog by mutableStateOf(false)
        private set
    
    var showQRDialog by mutableStateOf(false)
        private set
    
    var showCalendarDialog by mutableStateOf(false)
        private set
    
    // Estados de fechas
    var selectedDateRange by mutableStateOf("📅 30 días")
        private set
    
    var startDate by mutableStateOf<String?>(null)
        private set
    
    var endDate by mutableStateOf<String?>(null)
        private set
    
    init {
        // Inicializar fechas por defecto: último mes
        val now = kotlin.time.Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val lastMonth = today.minus(30, DateTimeUnit.DAY)
        
        startDate = lastMonth.toString()
        endDate = today.toString()
        selectedDateRange = "📅 30 días"
    }
    
    // Datos generados
    var generatedAffiliationCode by mutableStateOf<GenerateAffiliationCodeResponse?>(null)
        private set
    
    var generatedQRCode by mutableStateOf<QRCodeData?>(null)
        private set
    
    /**
     * Carga las estadísticas rápidas del dashboard
     */
    fun loadQuickStats(adminId: Int, accessToken: String) {
        coroutineScope.launch {
            isLoadingStats = true
            statsError = ""
            
            // Log de las fechas que se van a enviar
            println("DEBUG DASHBOARD: Enviando fechas - startDate: $startDate, endDate: $endDate")
            
            statsService.getUnifiedStatsSummary(
                adminId = adminId,
                sellerId = null,
                startDate = startDate,
                endDate = endDate,
                token = accessToken
            ).fold(
                onSuccess = { response ->
                    // Convertir UnifiedStatsResponse a QuickSummaryData para mantener compatibilidad
                    quickSummaryData = QuickSummaryData(
                        totalSales = response.data.overview.totalSales,
                        totalTransactions = response.data.overview.totalTransactions,
                        averageTransactionValue = response.data.overview.averageTransactionValue,
                        salesGrowth = response.data.overview.salesGrowth,
                        transactionGrowth = response.data.overview.transactionGrowth,
                        averageGrowth = response.data.overview.averageGrowth,
                        pendingPayments = response.data.performanceMetrics.pendingPayments,
                        confirmedPayments = response.data.performanceMetrics.confirmedPayments,
                        rejectedPayments = response.data.performanceMetrics.rejectedPayments,
                        claimRate = response.data.performanceMetrics.claimRate,
                        averageConfirmationTime = response.data.performanceMetrics.averageConfirmationTime
                    )
                    isLoadingStats = false
                },
                onFailure = { error ->
                    statsError = error.message ?: "Error cargando estadísticas"
                    isLoadingStats = false
                }
            )
        }
    }
    
    /**
     * Carga las estadísticas completas del admin
     */
    fun loadAdminStats(adminId: Int, accessToken: String) {
        coroutineScope.launch {
            isLoadingAdminStats = true
            adminStatsError = ""
            
            statsService.getUnifiedStatsSummary(
                adminId = adminId,
                sellerId = null,
                startDate = null,
                endDate = null,
                token = accessToken
            ).fold(
                onSuccess = { response ->
                    // Convertir UnifiedStatsResponse a AdminStatsData para mantener compatibilidad
                    adminStatsData = AdminStatsData(
                        dailySales = response.data.dailySales?.map { daily ->
                            org.sysarp.project.data.DailySalesData(
                                date = daily.date,
                                dayName = daily.dayName,
                                sales = daily.sales,
                                transactions = daily.transactions
                            )
                        } ?: emptyList(),
                        performanceMetrics = org.sysarp.project.data.PerformanceMetricsData(
                            averageConfirmationTime = response.data.performanceMetrics.averageConfirmationTime,
                            claimRate = response.data.performanceMetrics.claimRate,
                            rejectionRate = response.data.performanceMetrics.rejectionRate,
                            pendingPayments = response.data.performanceMetrics.pendingPayments,
                            confirmedPayments = response.data.performanceMetrics.confirmedPayments,
                            rejectedPayments = response.data.performanceMetrics.rejectedPayments
                        ),
                        topSellers = emptyList(), // No disponible en el endpoint unificado
                        overview = org.sysarp.project.data.SellerOverviewSummaryData(
                            totalSales = response.data.overview.totalSales,
                            totalTransactions = response.data.overview.totalTransactions,
                            averageTransactionValue = response.data.overview.averageTransactionValue,
                            salesGrowth = response.data.overview.salesGrowth,
                            transactionGrowth = response.data.overview.transactionGrowth,
                            averageGrowth = response.data.overview.averageGrowth
                        )
                    )
                    isLoadingAdminStats = false
                },
                onFailure = { error ->
                    adminStatsError = error.message ?: "Error cargando estadísticas completas"
                    isLoadingAdminStats = false
                }
            )
        }
    }
    
    /**
     * Carga los vendedores conectados
     */
    fun loadConnectedSellers(adminId: Int, accessToken: String) {
        coroutineScope.launch {
            isLoadingSellers = true
            sellersError = ""
            
            sellerService.getConnectedSellers(
                adminId = adminId,
                token = accessToken
            ).fold(
                onSuccess = { response ->
                    connectedSellersData = response.data
                    isLoadingSellers = false
                },
                onFailure = { error ->
                    sellersError = error.message ?: "Error cargando vendedores"
                    isLoadingSellers = false
                }
            )
        }
    }
    
    /**
     * Carga las sucursales
     */
    fun loadBranches(adminId: Int, accessToken: String) {
        coroutineScope.launch {
            isLoadingBranches = true
            
            branchService.getBranches(
                adminId = adminId,
                accessToken = accessToken
            ).fold(
                onSuccess = { branchesData ->
                    branches = branchesData.branches
                    isLoadingBranches = false
                },
                onFailure = { error ->
                    isLoadingBranches = false
                }
            )
        }
    }
    
    /**
     * Carga las solicitudes de baja pendientes
     */
    fun loadPendingRequests() {
        coroutineScope.launch {
            sellerService.getPendingDeactivationRequests().fold(
                onSuccess = { requests -> 
                    pendingRequests = requests 
                },
                onFailure = { /* Manejar error */ }
            )
        }
    }
    
    /**
     * Refresca todas las estadísticas del admin
     */
    fun refreshAdminStats(adminId: Int, accessToken: String) {
        coroutineScope.launch {
            // Recargar estadísticas rápidas usando endpoint unificado
            statsService.getUnifiedStatsSummary(
                adminId = adminId,
                sellerId = null,
                startDate = null,
                endDate = null,
                token = accessToken
            ).fold(
                onSuccess = { response ->
                    // Convertir UnifiedStatsResponse a QuickSummaryData para mantener compatibilidad
                    quickSummaryData = QuickSummaryData(
                        totalSales = response.data.overview.totalSales,
                        totalTransactions = response.data.overview.totalTransactions,
                        averageTransactionValue = response.data.overview.averageTransactionValue,
                        salesGrowth = response.data.overview.salesGrowth,
                        transactionGrowth = response.data.overview.transactionGrowth,
                        averageGrowth = response.data.overview.averageGrowth,
                        pendingPayments = response.data.performanceMetrics.pendingPayments,
                        confirmedPayments = response.data.performanceMetrics.confirmedPayments,
                        rejectedPayments = response.data.performanceMetrics.rejectedPayments,
                        claimRate = response.data.performanceMetrics.claimRate,
                        averageConfirmationTime = response.data.performanceMetrics.averageConfirmationTime
                    )
                },
                onFailure = { _ ->
                    // Error silencioso en refresh
                }
            )
            
            // Recargar estadísticas completas usando endpoint unificado
            statsService.getUnifiedStatsSummary(
                adminId = adminId,
                sellerId = null,
                startDate = null,
                endDate = null,
                token = accessToken
            ).fold(
                onSuccess = { response ->
                    // Convertir UnifiedStatsResponse a AdminStatsData para mantener compatibilidad
                    adminStatsData = AdminStatsData(
                        dailySales = response.data.dailySales?.map { daily ->
                            org.sysarp.project.data.DailySalesData(
                                date = daily.date,
                                dayName = daily.dayName,
                                sales = daily.sales,
                                transactions = daily.transactions
                            )
                        } ?: emptyList(),
                        performanceMetrics = org.sysarp.project.data.PerformanceMetricsData(
                            averageConfirmationTime = response.data.performanceMetrics.averageConfirmationTime,
                            claimRate = response.data.performanceMetrics.claimRate,
                            rejectionRate = response.data.performanceMetrics.rejectionRate,
                            pendingPayments = response.data.performanceMetrics.pendingPayments,
                            confirmedPayments = response.data.performanceMetrics.confirmedPayments,
                            rejectedPayments = response.data.performanceMetrics.rejectedPayments
                        ),
                        topSellers = emptyList(), // No disponible en el endpoint unificado
                        overview = org.sysarp.project.data.SellerOverviewSummaryData(
                            totalSales = response.data.overview.totalSales,
                            totalTransactions = response.data.overview.totalTransactions,
                            averageTransactionValue = response.data.overview.averageTransactionValue,
                            salesGrowth = response.data.overview.salesGrowth,
                            transactionGrowth = response.data.overview.transactionGrowth,
                            averageGrowth = response.data.overview.averageGrowth
                        )
                    )
                },
                onFailure = { _ ->
                    // Error silencioso en refresh
                }
            )
            
        }
    }
    
    /**
     * Genera un código de afiliación
     */
    fun generateAffiliationCode(
        adminId: Int,
        branchId: Int,
        expirationHours: Int,
        maxUses: Int,
        notes: String,
        accessToken: String
    ) {
        coroutineScope.launch {
            isLoadingAffiliation = true
            affiliationError = null
            
            affiliationService.generateAffiliationCode(
                adminId = adminId,
                branchId = branchId,
                expirationHours = expirationHours,
                maxUses = maxUses,
                notes = notes,
                accessToken = accessToken
            ).fold(
                onSuccess = { affiliationData ->
                    generatedAffiliationCode = affiliationData
                    isLoadingAffiliation = false
                },
                onFailure = { error ->
                    affiliationError = error.message ?: "Error generando código de afiliación"
                    isLoadingAffiliation = false
                }
            )
        }
    }
    
    /**
     * Genera un código QR desde un código de afiliación
     */
    fun generateQRFromAffiliationCode(
        affiliationCode: String,
        accessToken: String
    ) {
        coroutineScope.launch {
            isLoadingQR = true
            qrError = null
            
            qrService.generateQRFromAffiliationCode(
                affiliationCode = affiliationCode,
                accessToken = accessToken
            ).fold(
                onSuccess = { qrData ->
                    generatedQRCode = qrData
                    isLoadingQR = false
                    // Cerrar el diálogo de afiliación y mostrar el QR
                    showAffiliationDialog = false
                    showQRDialog = true
                },
                onFailure = { error ->
                    qrError = error.message ?: "Error generando código QR"
                    isLoadingQR = false
                }
            )
        }
    }
    
    /**
     * Muestra el diálogo de afiliación
     */
    fun showAffiliationDialog() {
        showAffiliationDialog = true
    }
    
    /**
     * Cierra el diálogo de afiliación
     */
    fun dismissAffiliationDialog() {
        showAffiliationDialog = false
        generatedAffiliationCode = null
        affiliationError = null
    }
    
    /**
     * Cierra el diálogo de QR
     */
    fun dismissQRDialog() {
        showQRDialog = false
        generatedQRCode = null
        qrError = null
    }
    
    /**
     * Muestra el diálogo del calendario
     */
    fun showCalendarDialog() {
        showCalendarDialog = true
    }
    
    /**
     * Cierra el diálogo del calendario
     */
    fun dismissCalendarDialog() {
        showCalendarDialog = false
    }
    
    /**
     * Actualiza el rango de fechas seleccionado
     */
    fun updateDateRange(period: String, adminId: Int, accessToken: String) {
        selectedDateRange = period
        calculateDateRange(period)
        loadQuickStats(adminId, accessToken)
    }
    
    /**
     * Calcula las fechas basadas en el período seleccionado
     */
    private fun calculateDateRange(period: String) {
        val now = kotlin.time.Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        when (period) {
            "📅 Hoy" -> {
                startDate = today.toString()
                endDate = today.toString()
            }
            "📅 7 días" -> {
                startDate = today.minus(7, DateTimeUnit.DAY).toString()
                endDate = today.toString()
            }
            "📅 30 días" -> {
                startDate = today.minus(30, DateTimeUnit.DAY).toString()
                endDate = today.toString()
            }
            "📅 90 días" -> {
                startDate = today.minus(90, DateTimeUnit.DAY).toString()
                endDate = today.toString()
            }
            else -> {
                // Para rangos personalizados, parsear las fechas
                if (period.contains(" - ")) {
                    val parts = period.split(" - ")
                    if (parts.size == 2) {
                        startDate = parts[0].trim()
                        endDate = parts[1].trim()
                    }
                } else {
                    // Fecha específica
                    startDate = period
                    endDate = period
                }
            }
        }
    }
    
    
    /**
     * Limpia todos los errores
     */
    fun clearErrors() {
        statsError = ""
        adminStatsError = ""
        sellersError = ""
        affiliationError = null
        qrError = null
    }
}
