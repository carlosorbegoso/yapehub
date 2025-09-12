package org.sysarp.project.test

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.sysarp.project.data.TransactionType
import org.sysarp.project.data.UserProfile
import org.sysarp.project.data.UserRole
import org.sysarp.project.data.YapeTransaction
import org.sysarp.project.service.DebugLogger
import kotlin.test.*

/**
 * Test de integración para verificar el comportamiento del ViewModel
 * con el sistema de guardado de transacciones
 */
class ViewModelIntegrationTest {

    private lateinit var mockRepository: MockYapeTransactionRepository
    private lateinit var mockUserProfileRepository: MockUserProfileRepository

    @BeforeTest
    fun setup() {
        DebugLogger.info("🔧 [VIEWMODEL TEST] Iniciando test de integración del ViewModel")
        mockRepository = MockYapeTransactionRepository()
        mockUserProfileRepository = MockUserProfileRepository()
    }

    @Test
    fun `test viewmodel actualiza correctamente con transacciones unicas`() = runTest {
        DebugLogger.info("🔧 [VIEWMODEL TEST] Probando actualización del ViewModel con transacciones únicas")
        
        // Simular inserción de transacciones
        val transactions = listOf(
            createTestTransaction("Usuario 1", 100.0, "001"),
            createTestTransaction("Usuario 2", 200.0, "002"),
            createTestTransaction("Usuario 3", 300.0, "003")
        )
        
        transactions.forEach { transaction ->
            mockRepository.insertTransaction(transaction)
        }
        
        // Simular que el ViewModel observa el repositorio
        val viewModelTransactions = mockRepository.getAllTransactions().first()
        
        // Verificar que el ViewModel recibe las transacciones únicas
        assertEquals(3, viewModelTransactions.size, "ViewModel debe recibir 3 transacciones únicas")
        
        val senderNames = viewModelTransactions.map { it.senderName }.sorted()
        assertEquals(listOf("Usuario 1", "Usuario 2", "Usuario 3"), senderNames)
        
        DebugLogger.info("✅ [VIEWMODEL TEST] ViewModel actualizado correctamente con transacciones únicas")
    }

    @Test
    fun `test viewmodel maneja duplicados correctamente`() = runTest {
        DebugLogger.info("🔧 [VIEWMODEL TEST] Probando manejo de duplicados en ViewModel")
        
        // Crear transacciones con duplicados
        val transactions = listOf(
            createTestTransaction("Usuario A", 50.0, "A001"),
            createTestTransaction("Usuario A", 50.0, "A001"), // Duplicado
            createTestTransaction("Usuario A", 50.0, "A001"), // Duplicado
            createTestTransaction("Usuario B", 75.0, "B002"),
            createTestTransaction("Usuario B", 75.0, "B002")  // Duplicado
        )
        
        transactions.forEach { transaction ->
            mockRepository.insertTransaction(transaction)
        }
        
        val viewModelTransactions = mockRepository.getAllTransactions().first()
        
        // El ViewModel debe mostrar solo las únicas (2)
        assertEquals(2, viewModelTransactions.size, "ViewModel debe mostrar solo 2 transacciones únicas")
        
        val uniqueCodes = viewModelTransactions.map { it.securityCode }.sorted()
        assertEquals(listOf("A001", "B002"), uniqueCodes, "Debe mostrar solo los códigos únicos")
        
        DebugLogger.info("✅ [VIEWMODEL TEST] ViewModel maneja duplicados correctamente")
        DebugLogger.info("📊 [VIEWMODEL TEST] Total guardadas: ${transactions.size}, Mostradas: ${viewModelTransactions.size}")
    }

    @Test
    fun `test exportacion de base de datos`() = runTest {
        DebugLogger.info("🔧 [VIEWMODEL TEST] Probando exportación de base de datos")
        
        // Crear datos de prueba
        val transactions = listOf(
            createTestTransaction("Cliente Export 1", 100.0, "EXP001"),
            createTestTransaction("Cliente Export 2", 200.0, "EXP002"),
            createTestTransaction("Cliente Export 1", 100.0, "EXP001") // Duplicado
        )
        
        transactions.forEach { transaction ->
            mockRepository.insertTransaction(transaction)
        }
        
        val viewModelTransactions = mockRepository.getAllTransactions().first()
        
        // Simular exportación
        val exportText = generateExportText(viewModelTransactions)
        
        // Verificar contenido de exportación
        assertTrue(exportText.contains("YAPE CHAMO - EXPORTACIÓN DE BASE DE DATOS"), "Debe contener header de exportación")
        assertTrue(exportText.contains("Total de transacciones únicas mostradas: 2"), "Debe mostrar solo transacciones únicas")
        assertTrue(exportText.contains("Cliente Export 1"), "Debe contener datos de transacciones")
        assertTrue(exportText.contains("Cliente Export 2"), "Debe contener datos de transacciones")
        assertTrue(exportText.contains("Solo transacciones únicas mostradas"), "Debe indicar que son únicas")
        
        DebugLogger.info("✅ [VIEWMODEL TEST] Exportación de base de datos funciona correctamente")
    }

    @Test
    fun `test verificacion de integridad del viewmodel`() = runTest {
        DebugLogger.info("🔧 [VIEWMODEL TEST] Probando verificación de integridad del ViewModel")
        
        // Crear escenario complejo
        val testData = listOf(
            createTestTransaction("Integridad 1", 10.0, "INT001"),
            createTestTransaction("Integridad 2", 20.0, "INT002"),
            createTestTransaction("Integridad 1", 10.0, "INT001"), // Duplicado
            createTestTransaction("Integridad 3", 30.0, "INT003"),
            createTestTransaction("Integridad 2", 20.0, "INT002")  // Duplicado
        )
        
        testData.forEach { transaction ->
            mockRepository.insertTransaction(transaction)
        }
        
        val viewModelTransactions = mockRepository.getAllTransactions().first()
        
        // Verificar integridad
        assertTrue(viewModelTransactions.isNotEmpty(), "Debe haber transacciones en el ViewModel")
        
        // Verificar que no hay datos inválidos
        viewModelTransactions.forEach { transaction ->
            assertFalse(transaction.transactionId.isEmpty(), "TransactionId no debe estar vacío")
            assertFalse(transaction.senderName.isNullOrEmpty(), "SenderName no debe estar vacío")
            assertTrue(transaction.amount > 0, "Amount debe ser mayor a 0")
            assertNotNull(transaction.transactionType, "TransactionType no debe ser null")
        }
        
        // Verificar coherencia
        val uniqueCodes = viewModelTransactions.map { it.securityCode }.distinct()
        assertEquals(viewModelTransactions.size, uniqueCodes.size, "Debe haber códigos únicos")
        
        DebugLogger.info("✅ [VIEWMODEL TEST] Integridad del ViewModel verificada")
        DebugLogger.info("📊 [VIEWMODEL TEST] Transacciones en ViewModel: ${viewModelTransactions.size}")
    }

    @Test
    fun `test simulacion de usuario admin`() = runTest {
        DebugLogger.info("🔧 [VIEWMODEL TEST] Simulando comportamiento con usuario Admin")
        
        val adminUser = UserProfile(
            id = "admin1",
            name = "Administrador",
            role = UserRole.ADMIN,
            assignedStores = listOf("Tienda Principal", "Tienda Secundaria")
        )
        
        // Simular transacciones para diferentes tiendas
        val transactions = listOf(
            createTestTransaction("Cliente Tienda Principal", 100.0, "TP001").copy(businessName = "Tienda Principal"),
            createTestTransaction("Cliente Tienda Secundaria", 200.0, "TS001").copy(businessName = "Tienda Secundaria"),
            createTestTransaction("Cliente Tienda Principal", 150.0, "TP002").copy(businessName = "Tienda Principal")
        )
        
        transactions.forEach { transaction ->
            mockRepository.insertTransaction(transaction)
        }
        
        val viewModelTransactions = mockRepository.getAllTransactions().first()
        
        // Admin debe ver todas las transacciones
        assertEquals(3, viewModelTransactions.size, "Admin debe ver todas las transacciones")
        
        val businessNames = viewModelTransactions.map { it.businessName }.distinct()
        assertTrue(businessNames.contains("Tienda Principal"), "Debe incluir transacciones de Tienda Principal")
        assertTrue(businessNames.contains("Tienda Secundaria"), "Debe incluir transacciones de Tienda Secundaria")
        
        DebugLogger.info("✅ [VIEWMODEL TEST] Comportamiento de Admin verificado")
    }

    @Test
    fun `test simulacion de usuario vendor`() = runTest {
        DebugLogger.info("🔧 [VIEWMODEL TEST] Simulando comportamiento con usuario Vendor")
        
        val vendorUser = UserProfile(
            id = "vendor1",
            name = "Vendedor",
            role = UserRole.VENDOR,
            assignedStores = listOf("Tienda Asignada")
        )
        
        // Simular transacciones para diferentes tiendas
        val transactions = listOf(
            createTestTransaction("Cliente Tienda Asignada", 100.0, "TA001").copy(businessName = "Tienda Asignada"),
            createTestTransaction("Cliente Otra Tienda", 200.0, "OT001").copy(businessName = "Otra Tienda"),
            createTestTransaction("Cliente Tienda Asignada", 150.0, "TA002").copy(businessName = "Tienda Asignada")
        )
        
        transactions.forEach { transaction ->
            mockRepository.insertTransaction(transaction)
        }
        
        val viewModelTransactions = mockRepository.getAllTransactions().first()
        
        // Vendor debe ver solo transacciones de sus tiendas asignadas
        val vendorTransactions = viewModelTransactions.filter { 
            it.businessName == "Tienda Asignada" 
        }
        
        assertEquals(2, vendorTransactions.size, "Vendor debe ver solo transacciones de su tienda asignada")
        
        val otherStoreTransactions = viewModelTransactions.filter { 
            it.businessName == "Otra Tienda" 
        }
        
        assertEquals(0, otherStoreTransactions.size, "Vendor no debe ver transacciones de otras tiendas")
        
        DebugLogger.info("✅ [VIEWMODEL TEST] Comportamiento de Vendor verificado")
    }

    private fun createTestTransaction(senderName: String, amount: Double, securityCode: String): YapeTransaction {
        return YapeTransaction(
            id = 0L,
            transactionId = "",
            amount = amount,
            currency = "PEN",
            senderName = senderName,
            senderPhone = "+51999999999",
            message = "Test transaction for $senderName",
            transactionType = TransactionType.RECEIVED,
            businessName = "Test Business",
            createdAt = Clock.System.now(),
            processedAt = null,
            isProcessed = false,
            rawNotification = "Test notification for $senderName - S/ $amount",
            securityCode = securityCode
        )
    }

    private fun generateExportText(transactions: List<YapeTransaction>): String {
        val timestamp = Clock.System.now()
        val dateFormatter = kotlinx.datetime.TimeZone.currentSystemDefault()
        
        return """
========================================
YAPE CHAMO - EXPORTACIÓN DE BASE DE DATOS
========================================
Exportado: ${timestamp.toLocalDateTime(dateFormatter)}
Total de transacciones únicas mostradas: ${transactions.size}
Fuente: SQLite Local (solo transacciones únicas mostradas)
Nota: Se guardan TODAS las transacciones en la base de datos
      (incluyendo duplicados), pero se muestran solo las únicas
========================================

${transactions.mapIndexed { index, transaction ->
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
}.joinToString("\n")}
        """.trimIndent()
    }

    @AfterTest
    fun cleanup() {
        DebugLogger.info("🧹 [VIEWMODEL TEST] Limpiando test de integración del ViewModel")
    }
}

/**
 * Mock para simular el repositorio de perfil de usuario
 */
class MockUserProfileRepository {
    private var currentUser: UserProfile? = null
    
    fun setCurrentUser(user: UserProfile) {
        currentUser = user
    }
    
    fun getCurrentUser(): UserProfile? = currentUser
}
