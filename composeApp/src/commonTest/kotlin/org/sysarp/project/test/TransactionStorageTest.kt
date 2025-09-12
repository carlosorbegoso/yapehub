package org.sysarp.project.test

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.sysarp.project.data.TransactionType
import org.sysarp.project.data.YapeTransaction
import org.sysarp.project.repository.YapeTransactionRepository
import org.sysarp.project.service.DebugLogger
import kotlin.test.*

/**
 * Test completo para verificar el sistema de guardado de transacciones
 * - Guarda TODAS las transacciones (incluyendo duplicados)
 * - Muestra solo las únicas en la UI
 * - Verifica integridad de la base de datos
 */
class TransactionStorageTest {

    private lateinit var repository: YapeTransactionRepository

    @BeforeTest
    fun setup() {
        DebugLogger.info("🧪 [TEST] Iniciando configuración de test de almacenamiento")
        // En un test real, aquí se configuraría un repositorio de prueba
        // repository = YapeTransactionRepositoryImpl(testContext)
    }

    @Test
    fun `test guardar transaccion unica`() = runTest {
        DebugLogger.info("🧪 [TEST] Probando guardado de transacción única")
        
        val transaction = createTestTransaction(
            senderName = "Juan Pérez",
            amount = 50.0,
            securityCode = "123"
        )
        
        // Simular guardado
        repository.insertTransaction(transaction)
        
        // Verificar que se guardó
        val allTransactions = repository.getAllTransactions().first()
        val uniqueTransactions = allTransactions.filter { it.securityCode == "123" }
        
        assertTrue(uniqueTransactions.isNotEmpty(), "La transacción única debe estar guardada")
        assertEquals(1, uniqueTransactions.size, "Debe haber exactamente 1 transacción única")
        assertEquals("Juan Pérez", uniqueTransactions.first().senderName)
        assertEquals(50.0, uniqueTransactions.first().amount)
        
        DebugLogger.info("✅ [TEST] Transacción única guardada correctamente")
    }

    @Test
    fun `test guardar transacciones duplicadas`() = runTest {
        DebugLogger.info("🧪 [TEST] Probando guardado de transacciones duplicadas")
        
        val transaction1 = createTestTransaction(
            senderName = "María García",
            amount = 25.0,
            securityCode = "456"
        )
        
        val transaction2 = createTestTransaction(
            senderName = "María García",
            amount = 25.0,
            securityCode = "456" // Mismo código de seguridad
        )
        
        // Guardar ambas transacciones (duplicadas)
        repository.insertTransaction(transaction1)
        repository.insertTransaction(transaction2)
        
        // Verificar que AMBAS se guardaron en la base de datos
        val allTransactions = repository.getAllTransactions().first()
        val duplicateTransactions = allTransactions.filter { it.securityCode == "456" }
        
        // En la base de datos debe haber 2 transacciones
        assertTrue(duplicateTransactions.size >= 2, "Debe haber al menos 2 transacciones duplicadas guardadas")
        
        // Pero en la UI solo debe mostrarse 1 (única)
        val uniqueTransactions = allTransactions.distinctBy { it.securityCode }
        val uniqueByCode456 = uniqueTransactions.filter { it.securityCode == "456" }
        assertEquals(1, uniqueByCode456.size, "En la UI debe mostrarse solo 1 transacción única")
        
        DebugLogger.info("✅ [TEST] Transacciones duplicadas guardadas correctamente")
        DebugLogger.info("📊 [TEST] Total guardadas: ${duplicateTransactions.size}, Únicas mostradas: ${uniqueByCode456.size}")
    }

    @Test
    fun `test filtrado de transacciones unicas`() = runTest {
        DebugLogger.info("🧪 [TEST] Probando filtrado de transacciones únicas")
        
        // Crear múltiples transacciones con códigos duplicados
        val transactions = listOf(
            createTestTransaction("Ana López", 100.0, "789"),
            createTestTransaction("Ana López", 100.0, "789"), // Duplicado
            createTestTransaction("Carlos Ruiz", 75.0, "101"),
            createTestTransaction("Carlos Ruiz", 75.0, "101"), // Duplicado
            createTestTransaction("Carlos Ruiz", 75.0, "101"), // Duplicado
            createTestTransaction("Elena Vega", 200.0, "202")
        )
        
        // Guardar todas las transacciones
        transactions.forEach { repository.insertTransaction(it) }
        
        // Obtener transacciones únicas (como las vería la UI)
        val allTransactions = repository.getAllTransactions().first()
        val uniqueTransactions = allTransactions.distinctBy { it.securityCode }
        
        // Verificar que solo se muestran las únicas
        assertEquals(3, uniqueTransactions.size, "Debe haber exactamente 3 transacciones únicas")
        
        val securityCodes = uniqueTransactions.map { it.securityCode }.sorted()
        assertEquals(listOf("101", "202", "789"), securityCodes, "Deben estar los 3 códigos únicos")
        
        DebugLogger.info("✅ [TEST] Filtrado de transacciones únicas funciona correctamente")
        DebugLogger.info("📊 [TEST] Total guardadas: ${allTransactions.size}, Únicas mostradas: ${uniqueTransactions.size}")
    }

    @Test
    fun `test integridad de base de datos`() = runTest {
        DebugLogger.info("🧪 [TEST] Probando integridad de base de datos")
        
        val transactions = listOf(
            createTestTransaction("Test User 1", 10.0, "T001"),
            createTestTransaction("Test User 2", 20.0, "T002"),
            createTestTransaction("Test User 1", 10.0, "T001"), // Duplicado
            createTestTransaction("Test User 3", 30.0, "T003")
        )
        
        // Guardar todas
        transactions.forEach { repository.insertTransaction(it) }
        
        val allTransactions = repository.getAllTransactions().first()
        
        // Verificar integridad
        assertTrue(allTransactions.isNotEmpty(), "Debe haber transacciones guardadas")
        
        // Verificar que no hay transacciones con datos inválidos
        allTransactions.forEach { transaction ->
            assertFalse(transaction.transactionId.isEmpty(), "TransactionId no debe estar vacío")
            assertFalse(transaction.senderName.isNullOrEmpty(), "SenderName no debe estar vacío")
            assertTrue(transaction.amount > 0, "Amount debe ser mayor a 0")
            assertNotNull(transaction.transactionType, "TransactionType no debe ser null")
        }
        
        // Verificar que los duplicados están guardados
        val t001Transactions = allTransactions.filter { it.securityCode == "T001" }
        assertTrue(t001Transactions.size >= 2, "Debe haber al menos 2 transacciones T001 guardadas")
        
        DebugLogger.info("✅ [TEST] Integridad de base de datos verificada")
        DebugLogger.info("📊 [TEST] Total transacciones: ${allTransactions.size}")
    }

    @Test
    fun `test estadisticas de transacciones`() = runTest {
        DebugLogger.info("🧪 [TEST] Probando estadísticas de transacciones")
        
        // Crear escenario complejo
        val testData = mapOf(
            "A001" to 3, // 3 duplicados
            "A002" to 1, // 1 único
            "A003" to 2, // 2 duplicados
            "A004" to 1  // 1 único
        )
        
        testData.forEach { (code, count) ->
            repeat(count) { index ->
                val transaction = createTestTransaction(
                    senderName = "User $code",
                    amount = 10.0 * (index + 1),
                    securityCode = code
                )
                repository.insertTransaction(transaction)
            }
        }
        
        val allTransactions = repository.getAllTransactions().first()
        val uniqueTransactions = allTransactions.distinctBy { it.securityCode }
        
        val totalExpected = testData.values.sum()
        val uniqueExpected = testData.keys.size
        
        assertEquals(totalExpected, allTransactions.size, "Total de transacciones guardadas")
        assertEquals(uniqueExpected, uniqueTransactions.size, "Total de transacciones únicas mostradas")
        
        val duplicateCount = allTransactions.size - uniqueTransactions.size
        val expectedDuplicates = totalExpected - uniqueExpected
        assertEquals(expectedDuplicates, duplicateCount, "Cantidad de duplicados")
        
        DebugLogger.info("✅ [TEST] Estadísticas verificadas correctamente")
        DebugLogger.info("📊 [TEST] Total: $totalExpected, Únicas: $uniqueExpected, Duplicadas: $duplicateCount")
    }

    @Test
    fun `test manejo de transacciones sin codigo de seguridad`() = runTest {
        DebugLogger.info("🧪 [TEST] Probando transacciones sin código de seguridad")
        
        val transactionWithoutCode = createTestTransaction(
            senderName = "Usuario Sin Código",
            amount = 15.0,
            securityCode = null
        )
        
        repository.insertTransaction(transactionWithoutCode)
        
        val allTransactions = repository.getAllTransactions().first()
        val transactionsWithoutCode = allTransactions.filter { it.securityCode == null }
        
        assertTrue(transactionsWithoutCode.isNotEmpty(), "Debe guardar transacciones sin código de seguridad")
        assertEquals("Usuario Sin Código", transactionsWithoutCode.first().senderName)
        
        DebugLogger.info("✅ [TEST] Transacciones sin código de seguridad manejadas correctamente")
    }

    private fun createTestTransaction(
        senderName: String,
        amount: Double,
        securityCode: String?
    ): YapeTransaction {
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

    @AfterTest
    fun cleanup() {
        DebugLogger.info("🧹 [TEST] Limpiando datos de test")
        // En un test real, aquí se limpiaría la base de datos de prueba
    }
}
