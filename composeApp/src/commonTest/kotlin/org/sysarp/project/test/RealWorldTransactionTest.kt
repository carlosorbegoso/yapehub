package org.sysarp.project.test

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.sysarp.project.data.TransactionType
import org.sysarp.project.data.YapeTransaction
import org.sysarp.project.service.DebugLogger
import kotlin.test.*

/**
 * Test que simula el comportamiento real del sistema YapeChamo
 * - Simula notificaciones reales de Yape
 * - Verifica que se guarden TODAS las transacciones
 * - Verifica que la UI muestre solo las únicas
 */
class RealWorldTransactionTest {

    private lateinit var mockRepository: MockYapeTransactionRepository

    @BeforeTest
    fun setup() {
        DebugLogger.info("🌍 [REAL WORLD TEST] Iniciando test de comportamiento real")
        mockRepository = MockYapeTransactionRepository()
    }

    @Test
    fun `test simulacion de notificaciones reales de yape`() = runTest {
        DebugLogger.info("🌍 [REAL WORLD TEST] Simulando notificaciones reales de Yape")
        
        // Simular notificaciones reales que llegan del sistema Yape
        val realNotifications = listOf(
            "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 0.1. El cód. de seguridad es: 904",
            "Confirmación de Pago María García te envió un pago por S/ 25.50. El cód. de seguridad es: 123",
            "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 0.1. El cód. de seguridad es: 904", // Duplicado
            "Confirmación de Pago Ana López te envió un pago por S/ 100.0. El cód. de seguridad es: 456",
            "Confirmación de Pago María García te envió un pago por S/ 25.50. El cód. de seguridad es: 123", // Duplicado
            "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 0.1. El cód. de seguridad es: 904" // Duplicado
        )
        
        // Procesar cada notificación como lo haría el sistema real
        realNotifications.forEach { notification ->
            val transaction = parseYapeNotification(notification)
            mockRepository.insertTransaction(transaction)
            DebugLogger.info("📱 [REAL WORLD TEST] Procesada: ${transaction.senderName} - S/ ${transaction.amount}")
        }
        
        // Verificar resultados
        val allTransactions = mockRepository.getAllTransactions().first()
        val uniqueTransactions = allTransactions.distinctBy { it.securityCode }
        
        // Debe haber guardado TODAS las transacciones (6)
        assertEquals(6, allTransactions.size, "Debe guardar TODAS las transacciones recibidas")
        
        // Pero solo debe mostrar las únicas (3)
        assertEquals(3, uniqueTransactions.size, "Debe mostrar solo las transacciones únicas")
        
        // Verificar que los duplicados están guardados
        val carlosTransactions = allTransactions.filter { it.senderName == "Carlos Orbegoso L." }
        assertEquals(3, carlosTransactions.size, "Debe haber 3 transacciones de Carlos guardadas")
        
        val mariaTransactions = allTransactions.filter { it.senderName == "María García" }
        assertEquals(2, mariaTransactions.size, "Debe haber 2 transacciones de María guardadas")
        
        DebugLogger.info("✅ [REAL WORLD TEST] Simulación completada exitosamente")
        DebugLogger.info("📊 [REAL WORLD TEST] Total guardadas: ${allTransactions.size}, Únicas mostradas: ${uniqueTransactions.size}")
    }

    @Test
    fun `test escenario de alta frecuencia de transacciones`() = runTest {
        DebugLogger.info("🌍 [REAL WORLD TEST] Simulando alta frecuencia de transacciones")
        
        // Simular 100 transacciones con algunos duplicados
        val transactions = mutableListOf<YapeTransaction>()
        
        repeat(50) { index ->
            val code = (index % 10).toString().padStart(3, '0') // Códigos 000-009
            val amount = (index + 1) * 10.0
            val sender = "Usuario ${index % 5}" // 5 usuarios diferentes
            
            transactions.add(createRealTransaction(sender, amount, code))
        }
        
        // Agregar algunos duplicados intencionalmente
        repeat(25) { index ->
            val originalIndex = index % 25
            transactions.add(transactions[originalIndex].copy(
                transactionId = "", // Se generará nuevo ID
                createdAt = Clock.System.now()
            ))
        }
        
        // Procesar todas las transacciones
        transactions.forEach { transaction ->
            mockRepository.insertTransaction(transaction)
        }
        
        val allTransactions = mockRepository.getAllTransactions().first()
        val uniqueTransactions = allTransactions.distinctBy { it.securityCode }
        
        // Verificar que se guardaron todas (75)
        assertEquals(75, allTransactions.size, "Debe guardar todas las 75 transacciones")
        
        // Pero solo debe mostrar las únicas (50)
        assertEquals(50, uniqueTransactions.size, "Debe mostrar solo las 50 transacciones únicas")
        
        DebugLogger.info("✅ [REAL WORLD TEST] Alta frecuencia procesada correctamente")
        DebugLogger.info("📊 [REAL WORLD TEST] Total: ${allTransactions.size}, Únicas: ${uniqueTransactions.size}")
    }

    @Test
    fun `test verificacion de integridad completa`() = runTest {
        DebugLogger.info("🌍 [REAL WORLD TEST] Verificando integridad completa del sistema")
        
        // Crear escenario complejo con múltiples tipos de transacciones
        val testScenario = listOf(
            Triple("Cliente A", 100.0, "A001"),
            Triple("Cliente B", 50.0, "B002"),
            Triple("Cliente A", 100.0, "A001"), // Duplicado
            Triple("Cliente C", 200.0, "C003"),
            Triple("Cliente B", 50.0, "B002"), // Duplicado
            Triple("Cliente A", 100.0, "A001"), // Duplicado
            Triple("Cliente D", 75.0, "D004"),
            Triple("Cliente E", 300.0, "E005"),
            Triple("Cliente B", 50.0, "B002"), // Duplicado
            Triple("Cliente F", 150.0, "F006")
        )
        
        // Procesar escenario
        testScenario.forEach { (sender, amount, code) ->
            val transaction = createRealTransaction(sender, amount, code)
            mockRepository.insertTransaction(transaction)
        }
        
        // Verificar integridad
        val allTransactions = mockRepository.getAllTransactions().first()
        val uniqueTransactions = allTransactions.distinctBy { it.securityCode }
        
        // Verificar que se guardaron todas (10)
        assertEquals(10, allTransactions.size, "Debe guardar todas las transacciones del escenario")
        
        // Verificar que solo se muestran las únicas (6)
        assertEquals(6, uniqueTransactions.size, "Debe mostrar solo las transacciones únicas")
        
        // Verificar duplicados específicos
        val clienteATransactions = allTransactions.filter { it.senderName == "Cliente A" }
        assertEquals(3, clienteATransactions.size, "Cliente A debe tener 3 transacciones guardadas")
        
        val clienteBTransactions = allTransactions.filter { it.senderName == "Cliente B" }
        assertEquals(3, clienteBTransactions.size, "Cliente B debe tener 3 transacciones guardadas")
        
        // Verificar que no hay transacciones con datos inválidos
        allTransactions.forEach { transaction ->
            assertFalse(transaction.transactionId.isEmpty(), "TransactionId no debe estar vacío")
            assertFalse(transaction.senderName.isNullOrEmpty(), "SenderName no debe estar vacío")
            assertTrue(transaction.amount > 0, "Amount debe ser mayor a 0")
            assertNotNull(transaction.transactionType, "TransactionType no debe ser null")
        }
        
        DebugLogger.info("✅ [REAL WORLD TEST] Integridad completa verificada")
        DebugLogger.info("📊 [REAL WORLD TEST] Escenario procesado: ${allTransactions.size} guardadas, ${uniqueTransactions.size} mostradas")
    }

    private fun parseYapeNotification(notification: String): YapeTransaction {
        // Simular el parsing de una notificación real de Yape
        val amountRegex = "S/ (\\d+\\.?\\d*)".toRegex()
        val codeRegex = "cód\\. de seguridad es: (\\d+)".toRegex()
        val senderRegex = "Confirmación de Pago (.+?) te envió".toRegex()
        
        val amount = amountRegex.find(notification)?.groupValues?.get(1)?.toDouble() ?: 0.0
        val code = codeRegex.find(notification)?.groupValues?.get(1)
        val sender = senderRegex.find(notification)?.groupValues?.get(1) ?: "Usuario Desconocido"
        
        return createRealTransaction(sender, amount, code)
    }

    private fun createRealTransaction(senderName: String, amount: Double, securityCode: String?): YapeTransaction {
        return YapeTransaction(
            id = 0L,
            transactionId = "",
            amount = amount,
            currency = "PEN",
            senderName = senderName,
            senderPhone = "+51999999999",
            message = "Confirmación de Pago $senderName te envió un pago por S/ $amount",
            transactionType = TransactionType.RECEIVED,
            businessName = "Negocio de $senderName",
            createdAt = Clock.System.now(),
            processedAt = null,
            isProcessed = false,
            rawNotification = "Confirmación de Pago $senderName te envió un pago por S/ $amount. El cód. de seguridad es: $securityCode",
            securityCode = securityCode
        )
    }

    @AfterTest
    fun cleanup() {
        DebugLogger.info("🧹 [REAL WORLD TEST] Limpiando test de comportamiento real")
    }
}

/**
 * Mock repository para simular el comportamiento del repositorio real
 */
class MockYapeTransactionRepository {
    private val allTransactions = mutableListOf<YapeTransaction>()
    private val _dataChangeTrigger = kotlinx.coroutines.flow.MutableStateFlow(0L)
    
    suspend fun insertTransaction(transaction: YapeTransaction) {
        val uniqueId = "YAPE_${System.currentTimeMillis()}_${transaction.securityCode ?: "NOCODE"}"
        val transactionWithId = transaction.copy(transactionId = uniqueId)
        
        allTransactions.add(transactionWithId)
        _dataChangeTrigger.value = System.currentTimeMillis()
        
        DebugLogger.info("💾 [MOCK] Transacción guardada: ${transaction.senderName} - S/ ${transaction.amount}")
    }
    
    fun getAllTransactions() = _dataChangeTrigger.map { 
        allTransactions.distinctBy { it.securityCode } // Simular filtrado de únicas
    }
}
