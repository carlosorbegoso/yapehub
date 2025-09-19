package org.sysarp.project.viewmodel

// Imports de funciones iOS eliminados - no se necesitan para Android
// PendingPayment eliminado - no se utiliza
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime
import org.sysarp.project.data.BusinessReport
import org.sysarp.project.data.DailyReport
import org.sysarp.project.data.UserProfile
import org.sysarp.project.data.UserRole
import org.sysarp.project.data.YapeTransaction
import org.sysarp.project.repository.UserProfileRepository
import org.sysarp.project.service.DebugLogger
import org.sysarp.project.service.PermissionChecker
import org.sysarp.project.service.SimpleNotificationService

/**
 * Estados de permisos para el servicio de notificaciones
 */
enum class PermissionState {
    UNKNOWN,
    GRANTED,
    DENIED,
    NEEDS_SETUP
}

/**
 * Estados de captura de notificaciones
 */
enum class CaptureStatus {
    UNKNOWN,
    ACTIVE,
    INACTIVE,
    ERROR
}

class YapeViewModel(
    private val notificationService: SimpleNotificationService,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(YapeUiState())
    val uiState: StateFlow<YapeUiState> = _uiState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()
    
    private val _transactions = MutableStateFlow<List<YapeTransaction>>(emptyList())
    val transactions: StateFlow<List<YapeTransaction>> = _transactions.asStateFlow()
    
    private val _businessReports = MutableStateFlow<List<BusinessReport>>(emptyList())
    val businessReports: StateFlow<List<BusinessReport>> = _businessReports.asStateFlow()
    
    private val _dailyReports = MutableStateFlow<List<DailyReport>>(emptyList())
    val dailyReports: StateFlow<List<DailyReport>> = _dailyReports.asStateFlow()
    
    // Sistema de pagos pendientes eliminado - no se utiliza
    
    private var isObservingRepository = false

    
    fun setCurrentUser(user: UserProfile) {
        _currentUser.value = user
        userProfileRepository.setCurrentUser(user)
        
        if (!isObservingRepository) {
            isObservingRepository = true
            viewModelScope.launch {
                DebugLogger.info("🔍 ViewModel iniciando observación del repositorio...")
                // Sin base de datos local, mantener lista vacía
                DebugLogger.info("🔄 ViewModel sin base de datos local - usando lista vacía")
                _transactions.value = emptyList()
                DebugLogger.info("📊 ViewModel actualizado - Transacciones: ${_transactions.value.size}")
                updateBusinessReports(emptyList())
                updateDailyReports(emptyList())
            }
        }
        
        if (user.role == UserRole.ADMIN) {
            viewModelScope.launch {
                requestPermissions()
                notificationService.startCapture()
                kotlinx.coroutines.delay(3000)
                checkPermissions()
            }
        } else {
            viewModelScope.launch {
                notificationService.stopCapture()
                _uiState.value = _uiState.value.copy(
                    isCapturing = false,
                    permissionState = PermissionState.UNKNOWN,
                    captureStatus = CaptureStatus.INACTIVE,
                    permissionMessage = "",
                    captureMessage = "Esperando confirmaciones de pago"
                )
            }
        }
    }
    
    private fun updateBusinessReports(transactions: List<YapeTransaction>) {
        val reports = transactions
            .filter { it.isProcessed }
            .groupBy { it.businessName ?: "Sin categorizar" }
            .map { (businessName, businessTransactions) ->
                BusinessReport(
                    businessName = businessName,
                    totalAmount = businessTransactions.sumOf { it.amount },
                    transactionCount = businessTransactions.size
                )
            }
        _businessReports.value = reports
    }
    
    private fun updateDailyReports(transactions: List<YapeTransaction>) {
        val reports = transactions
            .filter { it.isProcessed }
            .groupBy { 
                val date = it.createdAt.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                "${date.year}-${date.monthNumber}-${date.dayOfMonth}"
            }
            .flatMap { (date, dateTransactions) ->
                dateTransactions.groupBy { it.businessName ?: "Sin categorizar" }
                    .map { (businessName, businessTransactions) ->
                        DailyReport(
                            date = date,
                            businessName = businessName,
                            totalAmount = businessTransactions.sumOf { it.amount },
                            transactionCount = businessTransactions.size
                        )
                    }
            }
        _dailyReports.value = reports
    }

    private suspend fun requestPermissions() {
        // Método simplificado para Android - sin funciones iOS
        kotlinx.coroutines.delay(1000)
        checkPermissions()
    }
    
    private fun checkPermissions() {
        try {
            // Verificación real de permisos usando PermissionChecker
            val hasNotificationPermission = PermissionChecker.isNotificationServiceEnabled()
            val hasAccessibilityPermission = PermissionChecker.isAccessibilityServiceEnabled()
            
            DebugLogger.info("Resultados de permisos - Notificaciones: $hasNotificationPermission, Accesibilidad: $hasAccessibilityPermission")
            
            when {
                hasNotificationPermission && hasAccessibilityPermission -> {
                    DebugLogger.info("Todos los permisos GRANTED - Actualizando UI a GRANTED")
                    _uiState.value = _uiState.value.copy(
                        permissionState = PermissionState.GRANTED,
                        permissionMessage = "✅ Todos los permisos están habilitados",
                        captureStatus = CaptureStatus.ACTIVE,
                        captureMessage = "🎯 Capturando notificaciones de Yape",
                        isCapturing = true
                    )
                }
                hasNotificationPermission || hasAccessibilityPermission -> {
                    val missingPermission = if (!hasNotificationPermission) "notificaciones" else "accesibilidad"
                    _uiState.value = _uiState.value.copy(
                        permissionState = PermissionState.NEEDS_SETUP,
                        permissionMessage = "⚠️ Falta permiso de $missingPermission",
                        captureStatus = CaptureStatus.ERROR,
                        captureMessage = "🔧 Configuración incompleta"
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        permissionState = PermissionState.DENIED,
                        permissionMessage = "❌ Ningún permiso habilitado",
                        captureStatus = CaptureStatus.ERROR,
                        captureMessage = "🚫 Sin permisos para capturar"
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                permissionState = PermissionState.NEEDS_SETUP,
                permissionMessage = "⚠️ Error verificando permisos",
                captureStatus = CaptureStatus.ERROR,
                captureMessage = "🔧 Error en verificación"
            )
        }
    }

    
    // Métodos de permisos no utilizados eliminados - AppLifecycleManager maneja esto

}

data class YapeUiState(
    val isCapturing: Boolean = false,
    val error: String? = null,
    val permissionState: PermissionState = PermissionState.UNKNOWN,
    val captureStatus: CaptureStatus = CaptureStatus.UNKNOWN,
    val permissionMessage: String = "",
    val captureMessage: String = ""
)
