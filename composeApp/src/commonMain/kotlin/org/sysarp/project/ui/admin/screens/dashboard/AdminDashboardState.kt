package org.sysarp.project.ui.admin.screens.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.sysarp.project.data.AdminStatsData
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

/**
 * Estado y lógica de negocio para AdminDashboardScreen
 */
class AdminDashboardState(
    private val authService: AuthService,
    private val sellerService: SellerService,
    val statsService: StatsService,
    private val affiliationService: AffiliationService,
    private val qrService: QRService,
    private val branchService: BranchService,
    private val billingService: BillingService,
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
            
            statsService.getAdminDashboard(
                adminId = adminId,
                token = accessToken
            ).fold(
                onSuccess = { response ->
                    quickSummaryData = response.data
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
            
            statsService.getAdminStatsSummary(
                adminId = adminId,
                token = accessToken
            ).fold(
                onSuccess = { response ->
                    adminStatsData = response.data
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
            // Recargar estadísticas rápidas
            statsService.getAdminDashboard(
                adminId = adminId,
                token = accessToken
            ).fold(
                onSuccess = { response ->
                    quickSummaryData = response.data
                },
                onFailure = { error ->
                    // Error silencioso en refresh
                }
            )
            
            // Recargar estadísticas completas
            statsService.getAdminStatsSummary(
                adminId = adminId,
                token = accessToken
            ).fold(
                onSuccess = { response ->
                    adminStatsData = response.data
                },
                onFailure = { error ->
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
