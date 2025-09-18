package org.sysarp.project.viewmodel

// Imports de funciones iOS eliminados - no se necesitan para Android
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime
import org.sysarp.project.data.BusinessReport
import org.sysarp.project.data.DailyReport
// PendingPayment eliminado - no se utiliza
import org.sysarp.project.data.TransactionType
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
    
    // Métodos de captura eliminados - se manejan directamente en setCurrentUser()
    
    fun addTransaction(transaction: YapeTransaction) {
        viewModelScope.launch {
            // Sin base de datos local, solo agregar a la lista en memoria
            val currentTransactions = _transactions.value.toMutableList()
            currentTransactions.add(transaction)
            _transactions.value = currentTransactions
            updateBusinessReports(currentTransactions)
            updateDailyReports(currentTransactions)
        }
    }
    
    fun markTransactionAsProcessed(transactionId: Long) {
        viewModelScope.launch {
            // Sin base de datos local, actualizar en memoria
            val currentTransactions = _transactions.value.toMutableList()
            val index = currentTransactions.indexOfFirst { it.id == transactionId }
            if (index != -1) {
                currentTransactions[index] = currentTransactions[index].copy(isProcessed = true)
                _transactions.value = currentTransactions
                updateBusinessReports(currentTransactions)
                updateDailyReports(currentTransactions)
            }
        }
    }
    
    fun assignTransactionToBusiness(transactionId: Long, businessName: String) {
        viewModelScope.launch {
            // Sin base de datos local, actualizar en memoria
            val currentTransactions = _transactions.value.toMutableList()
            val index = currentTransactions.indexOfFirst { it.id == transactionId }
            if (index != -1) {
                currentTransactions[index] = currentTransactions[index].copy(businessName = businessName)
                _transactions.value = currentTransactions
                updateBusinessReports(currentTransactions)
                updateDailyReports(currentTransactions)
            }
        }
    }
    
    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            // Sin base de datos local, eliminar de memoria
            val currentTransactions = _transactions.value.toMutableList()
            currentTransactions.removeAll { it.id == transactionId }
            _transactions.value = currentTransactions
            updateBusinessReports(currentTransactions)
            updateDailyReports(currentTransactions)
        }
    }
    
    // Método getTransactionsForReport() eliminado - no se utiliza
    
    fun exportTransactionsToText(): String {
        return try {
            val transactions = _transactions.value
            DebugLogger.info("📤 Exportando transacciones - Total: ${transactions.size}")
            val timestamp = kotlinx.datetime.Clock.System.now()
            val dateFormatter = kotlinx.datetime.TimeZone.currentSystemDefault()
            
            val header = """
========================================
YAPE CHAMO - EXPORTACIÓN DE TRANSACCIONES
========================================
Exportado: ${timestamp.toLocalDateTime(dateFormatter)}
Total de transacciones: ${transactions.size}
Nota: Datos en memoria (sin persistencia local)
========================================

"""
            
            val transactionsText = if (transactions.isEmpty()) {
                "No hay transacciones en memoria."
            } else {
                transactions.mapIndexed { index, transaction ->
                    """
[${index + 1}] ID: ${transaction.id}
    Transaction ID: ${transaction.transactionId}
    Monto: ${transaction.amount} ${transaction.currency}
    Remitente: ${transaction.senderName}
    Código de seguridad: ${transaction.securityCode ?: "N/A"}
    Creado: ${transaction.createdAt.toLocalDateTime(dateFormatter)}
    ----------------------------------------
""".trimIndent()
                }.joinToString("\n")
            }
            
            header + transactionsText
        } catch (e: Exception) {
            "Error: No se pudo exportar las transacciones - ${e.message}"
        }
    }
    
    // Métodos de exportación duplicados y UI eliminados - no se utilizan
    
    // Sistema de pagos pendientes eliminado - no se utiliza
    
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
    
    private suspend fun checkNotificationPermission(): Boolean {
        // Verificación real de permisos usando PermissionChecker
        return PermissionChecker.isNotificationServiceEnabled()
    }
    
    private suspend fun checkAccessibilityPermission(): Boolean {
        // Verificación real de permisos usando PermissionChecker
        return PermissionChecker.isAccessibilityServiceEnabled()
    }
    
    // Métodos de permisos no utilizados eliminados - AppLifecycleManager maneja esto
    
    fun insertTestTransaction() {
        viewModelScope.launch {
            val testTransaction = YapeTransaction(
                id = 0L,
                transactionId = "",
                amount = 25.5,
                currency = "PEN",
                senderName = "Usuario de Prueba",
                senderPhone = "+51999999999",
                message = "Pago de prueba para verificar funcionamiento",
                transactionType = TransactionType.RECEIVED,
                businessName = "Negocio de Prueba",
                createdAt = kotlinx.datetime.Clock.System.now(),
                processedAt = null,
                isProcessed = false,
                rawNotification = "Confirmación de Pago Usuario de Prueba te envió un pago por S/ 25.5. El cód. de seguridad es: 123",
                securityCode = "123"
            )

            DebugLogger.info("🧪 [TEST] Agregando transacción de prueba...")
            addTransaction(testTransaction)
            DebugLogger.info("✅ [TEST] Transacción de prueba agregada")

            kotlinx.coroutines.delay(1000)
            val currentTransactions = _transactions.value
            val testFound = currentTransactions.any {
                it.senderName == "Usuario de Prueba" && it.amount == 25.5
            }

            if (testFound) {
                DebugLogger.info("✅ [TEST] Verificación exitosa: transacción encontrada en lista")
            } else {
                DebugLogger.error("❌ [TEST] ERROR: transacción no encontrada en lista después de insertar")
            }
        }
    }

    fun verifyTransactionsIntegrity() {
        viewModelScope.launch {
            DebugLogger.info("🔍 [VERIFY] Iniciando verificación integral de base de datos...")

            try {
                val viewModelCount = _transactions.value.size
                DebugLogger.info("📊 [VERIFY] Transacciones en ViewModel: $viewModelCount")

                // Sin repositorio de base de datos, solo verificar ViewModel
                DebugLogger.info("📊 [VERIFY] Transacciones en memoria: $viewModelCount")
                DebugLogger.info("✅ [VERIFY] Verificación de memoria completada")

                // Verificar transacciones en memoria
                _transactions.value.forEach { transaction ->
                    if (transaction.transactionId.isEmpty()) {
                        DebugLogger.error("❌ [VERIFY] Transacción con ID vacío: ${transaction.id}")
                    }
                    if (transaction.senderName.isNullOrEmpty()) {
                        DebugLogger.warn("⚠️ [VERIFY] Transacción sin nombre de remitente: ${transaction.id}")
                    }
                    if (transaction.amount <= 0) {
                        DebugLogger.error("❌ [VERIFY] Transacción con monto inválido: ${transaction.id} - ${transaction.amount}")
                    }
                }

                DebugLogger.info("✅ [VERIFY] Verificación integral completada")

            } catch (e: Exception) {
                DebugLogger.error("❌ [VERIFY] Error durante verificación: ${e.message}")
            }
        }
    }
}

data class YapeUiState(
    val isCapturing: Boolean = false,
    val error: String? = null,
    val permissionState: PermissionState = PermissionState.UNKNOWN,
    val captureStatus: CaptureStatus = CaptureStatus.UNKNOWN,
    val permissionMessage: String = "",
    val captureMessage: String = ""
)
