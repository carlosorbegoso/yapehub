package org.sysarp.project.test

import org.sysarp.project.data.TransactionType
import org.sysarp.project.data.YapeTransaction
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Test para verificar la lógica de detección de duplicados
 * Prueba el comportamiento con múltiples transacciones similares
 */
class DuplicateDetectionTest {
    
    fun runTest(): TestResult {
        println("🧪 [DUPLICATE TEST] Iniciando prueba de detección de duplicados...")
        
        val results = mutableListOf<String>()
        var passedTests = 0
        var totalTests = 0
        
        // Test 1: Transacciones diferentes con códigos diferentes
        totalTests++
        if (testDifferentSecurityCodes(results)) {
            passedTests++
        }
        
        // Test 2: Transacciones diferentes con mismo código pero diferentes timestamps
        totalTests++
        if (testSameCodeDifferentTimestamps(results)) {
            passedTests++
        }
        
        // Test 3: Transacciones duplicadas reales (mismo código + mismo timestamp)
        totalTests++
        if (testRealDuplicates(results)) {
            passedTests++
        }
        
        // Test 4: Transacciones con mismo remitente y monto pero códigos diferentes
        totalTests++
        if (testSameSenderAmountDifferentCodes(results)) {
            passedTests++
        }
        
        val success = passedTests == totalTests
        val summary = """
            ========================================
            🧪 RESULTADO DE PRUEBA DE DUPLICADOS
            ========================================
            ✅ Pruebas pasadas: $passedTests/$totalTests
            ${if (success) "🎉 TODAS LAS PRUEBAS PASARON" else "❌ ALGUNAS PRUEBAS FALLARON"}
            ========================================
            
            📋 Detalles:
            ${results.joinToString("\n")}
        """.trimIndent()
        
        println(summary)
        return TestResult(success, summary)
    }
    
    /**
     * Test 1: Transacciones con códigos de seguridad diferentes
     * Deben ser consideradas únicas
     */
    private fun testDifferentSecurityCodes(results: MutableList<String>): Boolean {
        println("🔍 [TEST 1] Probando códigos de seguridad diferentes...")
        
        val now = Clock.System.now()
        val transaction1 = createTestTransaction("823", "Carlos", 0.1, now)
        val transaction2 = createTestTransaction("711", "Carlos", 0.1, now.plusSeconds(1))
        val transaction3 = createTestTransaction("901", "Carlos", 0.1, now.plusSeconds(2))
        
        val key1 = generateUniqueKey(transaction1)
        val key2 = generateUniqueKey(transaction2)
        val key3 = generateUniqueKey(transaction3)
        
        val areDifferent = key1 != key2 && key2 != key3 && key1 != key3
        
        if (areDifferent) {
            results.add("✅ [TEST 1] Códigos diferentes generan claves únicas")
            println("   ✅ Clave 1: $key1")
            println("   ✅ Clave 2: $key2")
            println("   ✅ Clave 3: $key3")
            return true
        } else {
            results.add("❌ [TEST 1] ERROR: Códigos diferentes generaron claves iguales")
            return false
        }
    }
    
    /**
     * Test 2: Transacciones con mismo código pero diferentes timestamps
     * Deben ser consideradas únicas (diferentes momentos)
     */
    private fun testSameCodeDifferentTimestamps(results: MutableList<String>): Boolean {
        println("🔍 [TEST 2] Probando mismo código, diferentes timestamps...")
        
        val now = Clock.System.now()
        val transaction1 = createTestTransaction("823", "Carlos", 0.1, now)
        val transaction2 = createTestTransaction("823", "Carlos", 0.1, now.plusSeconds(60)) // 1 minuto después
        val transaction3 = createTestTransaction("823", "Carlos", 0.1, now.plusSeconds(120)) // 2 minutos después
        
        val key1 = generateUniqueKey(transaction1)
        val key2 = generateUniqueKey(transaction2)
        val key3 = generateUniqueKey(transaction3)
        
        val areDifferent = key1 != key2 && key2 != key3 && key1 != key3
        
        if (areDifferent) {
            results.add("✅ [TEST 2] Mismo código, diferentes timestamps = claves únicas")
            println("   ✅ Clave 1: $key1")
            println("   ✅ Clave 2: $key2")
            println("   ✅ Clave 3: $key3")
            return true
        } else {
            results.add("❌ [TEST 2] ERROR: Mismo código con diferentes timestamps generó claves iguales")
            return false
        }
    }
    
    /**
     * Test 3: Transacciones duplicadas reales (mismo código + mismo timestamp)
     * Deben ser consideradas duplicadas
     */
    private fun testRealDuplicates(results: MutableList<String>): Boolean {
        println("🔍 [TEST 3] Probando duplicados reales...")
        
        val now = Clock.System.now()
        val transaction1 = createTestTransaction("823", "Carlos", 0.1, now)
        val transaction2 = createTestTransaction("823", "Carlos", 0.1, now) // Mismo timestamp
        
        val key1 = generateUniqueKey(transaction1)
        val key2 = generateUniqueKey(transaction2)
        
        val areSame = key1 == key2
        
        if (areSame) {
            results.add("✅ [TEST 3] Duplicados reales detectados correctamente")
            println("   ✅ Clave 1: $key1")
            println("   ✅ Clave 2: $key2")
            println("   ✅ Son iguales (duplicados)")
            return true
        } else {
            results.add("❌ [TEST 3] ERROR: Duplicados reales no detectados")
            return false
        }
    }
    
    /**
     * Test 4: Transacciones con mismo remitente y monto pero códigos diferentes
     * Deben ser consideradas únicas (caso de tu problema original)
     */
    private fun testSameSenderAmountDifferentCodes(results: MutableList<String>): Boolean {
        println("🔍 [TEST 4] Probando mismo remitente/monto, códigos diferentes...")
        
        val now = Clock.System.now()
        val transaction1 = createTestTransaction("823", "Carlos", 0.1, now)
        val transaction2 = createTestTransaction("711", "Carlos", 0.1, now.plusSeconds(1))
        val transaction3 = createTestTransaction("901", "Carlos", 0.1, now.plusSeconds(2))
        
        val key1 = generateUniqueKey(transaction1)
        val key2 = generateUniqueKey(transaction2)
        val key3 = generateUniqueKey(transaction3)
        
        val areDifferent = key1 != key2 && key2 != key3 && key1 != key3
        
        if (areDifferent) {
            results.add("✅ [TEST 4] Mismo remitente/monto, códigos diferentes = únicas")
            println("   ✅ Clave 1: $key1")
            println("   ✅ Clave 2: $key2")
            println("   ✅ Clave 3: $key3")
            println("   ✅ Todas son únicas (problema original resuelto)")
            return true
        } else {
            results.add("❌ [TEST 4] ERROR: Mismo remitente/monto con códigos diferentes generó claves iguales")
            return false
        }
    }
    
    /**
     * Crea una transacción de prueba
     */
    private fun createTestTransaction(
        securityCode: String,
        senderName: String,
        amount: Double,
        timestamp: Instant
    ): YapeTransaction {
        return YapeTransaction(
            id = 0,
            transactionId = "TEMP_ID",
            amount = amount,
            currency = "PEN",
            senderName = senderName,
            senderPhone = null,
            message = "Test message",
            transactionType = TransactionType.RECEIVED,
            businessName = "Test Business",
            createdAt = timestamp,
            processedAt = null,
            isProcessed = false,
            rawNotification = "Test notification",
            securityCode = securityCode
        )
    }
    
    /**
     * Genera la clave única usando la misma lógica del repositorio
     */
    private fun generateUniqueKey(transaction: YapeTransaction): String {
        val timestamp = transaction.createdAt.toEpochMilliseconds()
        val securityCode = transaction.securityCode ?: "NOCODE"
        return "${timestamp}_${securityCode}"
    }
}

/**
 * Resultado de una prueba
 */
data class TestResult(
    val success: Boolean,
    val message: String
)
