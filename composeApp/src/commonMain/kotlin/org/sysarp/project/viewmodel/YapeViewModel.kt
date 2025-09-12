package org.sysarp.project.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.sysarp.project.data.BusinessReport
import org.sysarp.project.data.DailyReport
import org.sysarp.project.data.TransactionType
import org.sysarp.project.data.UserProfile
import org.sysarp.project.data.UserRole
import org.sysarp.project.data.YapeTransaction
import org.sysarp.project.data.PendingPayment
import org.sysarp.project.data.PaymentConfirmation
import org.sysarp.project.repository.UserProfileRepository
import org.sysarp.project.repository.YapeTransactionRepository
import org.sysarp.project.service.NotificationCaptureService
import org.sysarp.project.service.PermissionState
import org.sysarp.project.service.CaptureStatus
import org.sysarp.project.service.DebugLogger
import org.sysarp.project.requestPermissionsAutomatically
import org.sysarp.project.checkNotificationPermission
import org.sysarp.project.checkAccessibilityPermission
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class YapeViewModel(
    private val repository: YapeTransactionRepository,
    private val notificationService: NotificationCaptureService,
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
    
    private val _pendingPayments = MutableStateFlow<List<PendingPayment>>(emptyList())
    val pendingPayments: StateFlow<List<PendingPayment>> = _pendingPayments.asStateFlow()
    
    private val _paymentConfirmations = MutableStateFlow<List<PaymentConfirmation>>(emptyList())
    val paymentConfirmations: StateFlow<List<PaymentConfirmation>> = _paymentConfirmations.asStateFlow()
    
    private var isObservingRepository = false
    
    init {
        // ViewModel inicializado - sin referencias específicas de plataforma
    }
    
    fun setCurrentUser(user: UserProfile) {
        _currentUser.value = user
        userProfileRepository.setCurrentUser(user)
        
        if (!isObservingRepository) {
            isObservingRepository = true
            viewModelScope.launch {
                DebugLogger.info("🔍 ViewModel iniciando observación del repositorio...")
                repository.getAllTransactions().collect { allTransactions ->
                    DebugLogger.info("🔄 ViewModel recibió ${allTransactions.size} transacciones del repositorio")
                    DebugLogger.info("🔍 Detalles de transacciones recibidas:")
                    allTransactions.forEachIndexed { index, transaction ->
                        DebugLogger.info("  [$index] ${transaction.senderName} - ${transaction.amount} PEN - ${transaction.transactionId}")
                    }
                    _transactions.value = allTransactions
                    DebugLogger.info("📊 ViewModel actualizado - Transacciones en _transactions: ${_transactions.value.size}")
                    updateBusinessReports(allTransactions)
                    updateDailyReports(allTransactions)
                }
            }
        }
        
        if (user.role == UserRole.ADMIN) {
            viewModelScope.launch {
                requestPermissions()
                notificationService.startCapturing()
                kotlinx.coroutines.delay(3000)
                checkPermissions()
            }
        } else {
            viewModelScope.launch {
                notificationService.stopCapturing()
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
    
    private fun filterTransactionsByUser(transactions: List<YapeTransaction>, user: UserProfile): List<YapeTransaction> {
        // Solo procesar transacciones RECIBIDAS (Yape solo notifica cuando recibes dinero)
        val receivedTransactions = transactions.filter { it.transactionType == TransactionType.RECEIVED }
        
        return when (user.role) {
            UserRole.ADMIN -> receivedTransactions // Admin ve todas las transacciones recibidas
            UserRole.VENDOR -> {
                // Vendedor solo ve transacciones que ÉL confirmó (no todas las de sus tiendas)
                receivedTransactions.filter { transaction ->
                    // Solo mostrar transacciones confirmadas por este vendedor
                    _paymentConfirmations.value.any { confirmation ->
                        confirmation.transactionId == transaction.transactionId && 
                        confirmation.confirmedBy == user.id
                    }
                }
            }
        }
    }
    
    private fun updateBusinessReports(transactions: List<YapeTransaction>) {
        // Solo procesar transacciones recibidas y procesadas
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
        // Solo procesar transacciones recibidas y procesadas
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
    
    fun startNotificationCapture() {
        viewModelScope.launch {
            notificationService.startCapturing()
            _uiState.value = _uiState.value.copy(isCapturing = true)
        }
    }
    
    fun stopNotificationCapture() {
        viewModelScope.launch {
            notificationService.stopCapturing()
            _uiState.value = _uiState.value.copy(isCapturing = false)
        }
    }
    
    fun addTransaction(transaction: YapeTransaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }
    
    fun markTransactionAsProcessed(transactionId: Long) {
        viewModelScope.launch {
            repository.updateTransactionProcessed(transactionId)
        }
    }
    
    fun assignTransactionToBusiness(transactionId: Long, businessName: String) {
        viewModelScope.launch {
            repository.updateTransactionBusiness(transactionId, businessName)
        }
    }
    
    fun deleteTransaction(transactionId: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(transactionId)
        }
    }
    
    fun getTransactionsForReport(): List<YapeTransaction> {
        return _transactions.value
    }
    
    fun exportDatabaseToText(): String {
        return try {
            // Usar el repositorio para obtener datos directamente de SQLite
            val transactions = _transactions.value
            DebugLogger.info("📤 Exportando base de datos - Transacciones únicas en ViewModel: ${transactions.size}")
            val timestamp = kotlinx.datetime.Clock.System.now()
            val dateFormatter = kotlinx.datetime.TimeZone.currentSystemDefault()
            
            val header = """
========================================
YAPE CHAMO - EXPORTACIÓN DE BASE DE DATOS
========================================
Exportado: ${timestamp.toLocalDateTime(dateFormatter)}
Total de transacciones únicas mostradas: ${transactions.size}
Fuente: SQLite Local (solo transacciones únicas mostradas)
Nota: Se guardan TODAS las transacciones en la base de datos
      (incluyendo duplicados), pero se muestran solo las únicas
========================================

"""
            
            val transactionsText = if (transactions.isEmpty()) {
                "No hay transacciones únicas en la base de datos SQLite."
            } else {
                transactions.mapIndexed { index, transaction ->
                    """
[${index + 1}] ID: ${transaction.id}
    Transaction ID: ${transaction.transactionId}
    Monto: ${transaction.amount} ${transaction.currency}
    Remitente: ${transaction.senderName}
    Teléfono: ${transaction.senderPhone ?: "N/A"}
    Mensaje: ${transaction.message ?: "N/A"}
    Tipo: ${transaction.transactionType}
    Negocio: ${transaction.businessName ?: "Sin categorizar"}
    Código de seguridad: ${transaction.securityCode ?: "N/A"}
    Creado: ${transaction.createdAt.toLocalDateTime(dateFormatter)}
    Procesado: ${if (transaction.isProcessed) "Sí" else "No"}
    Procesado en: ${transaction.processedAt?.toLocalDateTime(dateFormatter) ?: "N/A"}
    Notificación original: ${transaction.rawNotification ?: "N/A"}
    ----------------------------------------
""".trimIndent()
                }.joinToString("\n")
            }
            
            header + transactionsText
        } catch (e: Exception) {
            "Error: No se pudo exportar la base de datos SQLite - ${e.message}"
        }
    }
    
    fun getDatabaseExportFileName(): String {
        val timestamp = kotlinx.datetime.Clock.System.now()
        val dateFormatter = kotlinx.datetime.TimeZone.currentSystemDefault()
        val dateTime = timestamp.toLocalDateTime(dateFormatter)
        return "yapechamo_database_${dateTime.year}${dateTime.monthNumber.toString().padStart(2, '0')}${dateTime.dayOfMonth.toString().padStart(2, '0')}_${dateTime.hour.toString().padStart(2, '0')}${dateTime.minute.toString().padStart(2, '0')}${dateTime.second.toString().padStart(2, '0')}.txt"
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun logout() {
        _currentUser.value = null
        _transactions.value = emptyList()
        _businessReports.value = emptyList()
        _dailyReports.value = emptyList()
        _pendingPayments.value = emptyList()
        _paymentConfirmations.value = emptyList()
        isObservingRepository = false
        // userProfileRepository.setCurrentUser(null) // No se puede pasar null
    }
    
    // Métodos para manejar pagos pendientes
    fun generatePendingPayments() {
        viewModelScope.launch {
            val allTransactions = repository.getAllTransactions().first()
            val receivedTransactions = allTransactions.filter { it.transactionType == TransactionType.RECEIVED }
            
            val pendingPayments = receivedTransactions.map { transaction ->
                PendingPayment(
                    id = transaction.id,
                    transactionId = transaction.transactionId,
                    amount = transaction.amount,
                    currency = transaction.currency,
                    createdAt = transaction.createdAt,
                    businessName = transaction.businessName ?: "Sin categorizar",
                    message = transaction.message,
                    isConfirmed = false,
                    confirmedBy = null,
                    securityCode = transaction.securityCode
                )
            }
            
            _pendingPayments.value = pendingPayments
        }
    }
    
    fun generatePendingPaymentsWithDelay() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            generatePendingPayments()
        }
    }
    
    fun confirmPayment(pendingPayment: PendingPayment) {
        viewModelScope.launch {
            val currentUser = _currentUser.value ?: return@launch
            
            // Verificar que el pago no haya sido confirmado por otro vendedor
            val existingConfirmation = _paymentConfirmations.value.find { 
                it.transactionId == pendingPayment.transactionId 
            }
            
            if (existingConfirmation != null) {
                _uiState.value = _uiState.value.copy(
                    error = "Este pago ya fue confirmado por otro vendedor"
                )
                return@launch
            }
            
            // Crear confirmación
            val confirmation = PaymentConfirmation(
                id = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                transactionId = pendingPayment.transactionId,
                amount = pendingPayment.amount,
                currency = pendingPayment.currency,
                confirmedBy = currentUser.id,
                confirmedAt = kotlinx.datetime.Clock.System.now(),
                vendorStore = currentUser.assignedStores.firstOrNull() ?: "Sin tienda",
                isConfirmed = true
            )
            
            // Actualizar listas
            val updatedConfirmations = _paymentConfirmations.value + confirmation
            _paymentConfirmations.value = updatedConfirmations
            
            val updatedPendingPayments = _pendingPayments.value.map { payment ->
                if (payment.id == pendingPayment.id) {
                    payment.copy(
                        isConfirmed = true,
                        confirmedBy = currentUser.id
                    )
                } else {
                    payment
                }
            }
            _pendingPayments.value = updatedPendingPayments
            
            // Marcar transacción como procesada
            repository.updateTransactionProcessed(pendingPayment.id)
        }
    }
    
    fun rejectPayment(pendingPayment: PendingPayment) {
        viewModelScope.launch {
            val updatedPendingPayments = _pendingPayments.value.filter { 
                it.id != pendingPayment.id 
            }
            _pendingPayments.value = updatedPendingPayments
        }
    }
    
    fun getPendingPaymentsForVendor(): List<PendingPayment> {
        val currentUser = _currentUser.value ?: return emptyList()
        
        return when (currentUser.role) {
            UserRole.ADMIN -> _pendingPayments.value // Admin ve todos
            UserRole.VENDOR -> {
                // Vendedor ve TODOS los pagos pendientes (sin info de clientes)
                // pero solo puede confirmar los de sus tiendas asignadas
                _pendingPayments.value.filter { payment ->
                    !payment.isConfirmed // Solo pagos no confirmados
                }
            }
        }
    }
    
    fun canVendorConfirmPayment(payment: PendingPayment): Boolean {
        val currentUser = _currentUser.value ?: return false
        
        return when (currentUser.role) {
            UserRole.ADMIN -> true // Admin puede confirmar cualquier pago
            UserRole.VENDOR -> {
                // Vendedor solo puede confirmar pagos de sus tiendas asignadas
                payment.businessName in currentUser.assignedStores
            }
        }
    }
    
    private suspend fun requestPermissions() {
        // Solicitar permisos automáticamente
        requestPermissionsAutomatically()
        
        // Verificar permisos después de un breve delay
        kotlinx.coroutines.delay(1000)
        checkPermissions()
    }
    
    private suspend fun checkPermissions() {
        try {
            // Verificar si los permisos están habilitados
            val hasNotificationPermission = checkNotificationPermission()
            val hasAccessibilityPermission = checkAccessibilityPermission()
            
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
                    android.util.Log.d("YapeViewModel", "UI updated - permissionState: GRANTED, isCapturing: true")
                }
                hasNotificationPermission || hasAccessibilityPermission -> {
                    val missingPermission = if (!hasNotificationPermission) "notificaciones" else "accesibilidad"
                    _uiState.value = _uiState.value.copy(
                        permissionState = PermissionState.NEEDS_SETUP,
                        permissionMessage = "⚠️ Falta permiso de $missingPermission - Configuración necesaria",
                        captureStatus = CaptureStatus.ERROR,
                        captureMessage = "🔧 Configuración incompleta"
                    )
                    android.util.Log.d("YapeViewModel", "Partial permissions - Status updated to NEEDS_SETUP")
                }
                else -> {
                    _uiState.value = _uiState.value.copy(
                        permissionState = PermissionState.DENIED,
                        permissionMessage = "❌ Ningún permiso habilitado - Configuración requerida",
                        captureStatus = CaptureStatus.ERROR,
                        captureMessage = "🚫 Sin permisos para capturar"
                    )
                    android.util.Log.d("YapeViewModel", "No permissions - Status updated to DENIED")
                }
            }
        } catch (e: Exception) {
            // En caso de error, mostrar estado de configuración necesaria
            _uiState.value = _uiState.value.copy(
                permissionState = PermissionState.NEEDS_SETUP,
                permissionMessage = "⚠️ Error verificando permisos - Configuración necesaria",
                captureStatus = CaptureStatus.ERROR,
                captureMessage = "🔧 Error en verificación"
            )
            android.util.Log.e("YapeViewModel", "Error checking permissions", e)
        }
    }
    
    private suspend fun checkNotificationPermission(): Boolean {
        return org.sysarp.project.checkNotificationPermission()
    }
    
    private suspend fun checkAccessibilityPermission(): Boolean {
        return org.sysarp.project.checkAccessibilityPermission()
    }
    
    fun refreshPermissions() {
        viewModelScope.launch {
            DebugLogger.info("Manual permission refresh requested")
            // Forzar actualización inmediata
            _uiState.value = _uiState.value.copy(
                permissionState = PermissionState.UNKNOWN,
                permissionMessage = "🔄 Verificando permisos...",
                captureStatus = CaptureStatus.UNKNOWN,
                captureMessage = "Verificando estado..."
            )
            kotlinx.coroutines.delay(500) // Delay más largo para asegurar verificación
            checkPermissions()
            
            // Verificación adicional después de un delay
            kotlinx.coroutines.delay(1000)
            DebugLogger.info("Second verification after delay")
            checkPermissions()
        }
    }
    
    fun onAppResumed() {
        // Verificar permisos automáticamente cuando la app regresa del foreground
        viewModelScope.launch {
            DebugLogger.info("App resumed - checking permissions automatically")
            kotlinx.coroutines.delay(500) // Pequeño delay para asegurar que la configuración se haya aplicado
            checkPermissions()
        }
    }
    
    
    /**
     * Función para generar un registro de prueba en SQLite
     */
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

            DebugLogger.info("🧪 [TEST] Insertando transacción de prueba...")
            repository.insertTransaction(testTransaction)
            DebugLogger.info("✅ [TEST] Transacción de prueba insertada")

            // Verificar que se guardó correctamente
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

    /**
     * Verifica la integridad completa del sistema de guardado
     */
    fun verifyDatabaseIntegrity() {
        viewModelScope.launch {
            DebugLogger.info("🔍 [VERIFY] Iniciando verificación integral de base de datos...")

            try {
                // 1. Contar transacciones en ViewModel
                val viewModelCount = _transactions.value.size
                DebugLogger.info("📊 [VERIFY] Transacciones en ViewModel: $viewModelCount")

                // 2. Verificar flujo de datos del repositorio
                val repositoryTransactions = repository.getAllTransactions().first()
                val repositoryCount = repositoryTransactions.size
                DebugLogger.info("📊 [VERIFY] Transacciones desde repositorio: $repositoryCount")

                // 3. Verificar que los datos coinciden
                if (viewModelCount == repositoryCount) {
                    DebugLogger.info("✅ [VERIFY] Coherencia entre ViewModel y Repositorio")
                } else {
                    DebugLogger.error("❌ [VERIFY] INCONSISTENCIA: ViewModel=$viewModelCount, Repositorio=$repositoryCount")
                }

                // 4. Verificar estructura de datos
                repositoryTransactions.forEach { transaction ->
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
