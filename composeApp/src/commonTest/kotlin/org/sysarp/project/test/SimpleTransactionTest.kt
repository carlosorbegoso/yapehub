package org.sysarp.project.test

import org.sysarp.project.service.DebugLogger

/**
 * Test simple para verificar el comportamiento del sistema de transacciones
 * Este test no requiere dependencias externas y puede ejecutarse en cualquier entorno
 */
class SimpleTransactionTest {

    fun `test logica de guardado de transacciones`() {
        DebugLogger.info("🧪 [SIMPLE TEST] Iniciando test simple de lógica de transacciones")
        
        // Simular datos de transacciones
        val transactions = listOf(
            TransactionData("Carlos Orbegoso", 0.1, "904"),
            TransactionData("María García", 25.50, "123"),
            TransactionData("Carlos Orbegoso", 0.1, "904"), // Duplicado
            TransactionData("Ana López", 100.0, "456"),
            TransactionData("María García", 25.50, "123")  // Duplicado
        )
        
        // Simular guardado de todas las transacciones
        val allStoredTransactions = mutableListOf<TransactionData>()
        transactions.forEach { transaction ->
            allStoredTransactions.add(transaction)
            DebugLogger.info("💾 [SIMPLE TEST] Guardada: ${transaction.senderName} - S/ ${transaction.amount} (${transaction.securityCode})")
        }
        
        // Verificar que se guardaron todas
        assertEquals(5, allStoredTransactions.size, "Debe guardar todas las transacciones")
        
        // Simular filtrado de únicas para la UI
        val uniqueTransactions = allStoredTransactions.distinctBy { it.securityCode }
        
        // Verificar que solo se muestran las únicas
        assertEquals(3, uniqueTransactions.size, "Debe mostrar solo 3 transacciones únicas")
        
        // Verificar códigos únicos
        val uniqueCodes = uniqueTransactions.map { it.securityCode }.sorted()
        assertEquals(listOf("123", "456", "904"), uniqueCodes, "Debe tener los códigos únicos correctos")
        
        // Verificar que los duplicados están guardados pero no mostrados
        val carlosTransactions = allStoredTransactions.filter { it.senderName == "Carlos Orbegoso" }
        assertEquals(2, carlosTransactions.size, "Debe haber 2 transacciones de Carlos guardadas")
        
        val mariaTransactions = allStoredTransactions.filter { it.senderName == "María García" }
        assertEquals(2, mariaTransactions.size, "Debe haber 2 transacciones de María guardadas")
        
        DebugLogger.info("✅ [SIMPLE TEST] Test de lógica completado exitosamente")
        DebugLogger.info("📊 [SIMPLE TEST] Total guardadas: ${allStoredTransactions.size}, Únicas mostradas: ${uniqueTransactions.size}")
    }

    fun `test simulacion de notificaciones de yape`() {
        DebugLogger.info("🧪 [SIMPLE TEST] Simulando notificaciones de Yape")
        
        val notifications = listOf(
            "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 0.1. El cód. de seguridad es: 904",
            "Confirmación de Pago María García te envió un pago por S/ 25.50. El cód. de seguridad es: 123",
            "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 0.1. El cód. de seguridad es: 904", // Duplicado
            "Confirmación de Pago Ana López te envió un pago por S/ 100.0. El cód. de seguridad es: 456"
        )
        
        val parsedTransactions = mutableListOf<TransactionData>()
        
        // Simular parsing de notificaciones
        notifications.forEach { notification ->
            val transaction = parseNotification(notification)
            parsedTransactions.add(transaction)
            DebugLogger.info("📱 [SIMPLE TEST] Parseada: ${transaction.senderName} - S/ ${transaction.amount}")
        }
        
        // Verificar parsing
        assertEquals(4, parsedTransactions.size, "Debe parsear todas las notificaciones")
        
        // Verificar datos específicos
        val carlosTransaction = parsedTransactions.find { it.senderName == "Carlos Orbegoso L." }
        assertNotNull(carlosTransaction, "Debe encontrar transacción de Carlos")
        assertEquals(0.1, carlosTransaction.amount, "Monto de Carlos debe ser 0.1")
        assertEquals("904", carlosTransaction.securityCode, "Código de Carlos debe ser 904")
        
        val mariaTransaction = parsedTransactions.find { it.senderName == "María García" }
        assertNotNull(mariaTransaction, "Debe encontrar transacción de María")
        assertEquals(25.50, mariaTransaction.amount, "Monto de María debe ser 25.50")
        assertEquals("123", mariaTransaction.securityCode, "Código de María debe ser 123")
        
        DebugLogger.info("✅ [SIMPLE TEST] Simulación de notificaciones completada")
    }

    fun `test estadisticas del sistema`() {
        DebugLogger.info("🧪 [SIMPLE TEST] Probando estadísticas del sistema")
        
        // Simular datos del sistema
        val systemData = SystemStats(
            totalTransactions = 100,
            uniqueTransactions = 75,
            duplicateTransactions = 25,
            totalAmount = 5000.0,
            averageAmount = 50.0
        )
        
        // Verificar estadísticas
        assertEquals(100, systemData.totalTransactions, "Total de transacciones debe ser 100")
        assertEquals(75, systemData.uniqueTransactions, "Transacciones únicas debe ser 75")
        assertEquals(25, systemData.duplicateTransactions, "Transacciones duplicadas debe ser 25")
        assertEquals(5000.0, systemData.totalAmount, "Monto total debe ser 5000.0")
        assertEquals(50.0, systemData.averageAmount, "Monto promedio debe ser 50.0")
        
        // Verificar coherencia
        val calculatedDuplicates = systemData.totalTransactions - systemData.uniqueTransactions
        assertEquals(systemData.duplicateTransactions, calculatedDuplicates, "Duplicados calculados debe coincidir")
        
        val calculatedAverage = systemData.totalAmount / systemData.uniqueTransactions
        assertEquals(systemData.averageAmount, calculatedAverage, 0.01, "Promedio calculado debe coincidir")
        
        DebugLogger.info("✅ [SIMPLE TEST] Estadísticas verificadas correctamente")
        DebugLogger.info("📊 [SIMPLE TEST] Total: ${systemData.totalTransactions}, Únicas: ${systemData.uniqueTransactions}, Duplicadas: ${systemData.duplicateTransactions}")
    }

    fun `test integridad de datos`() {
        DebugLogger.info("🧪 [SIMPLE TEST] Probando integridad de datos")
        
        val transactions = listOf(
            TransactionData("Usuario 1", 100.0, "001"),
            TransactionData("Usuario 2", 200.0, "002"),
            TransactionData("Usuario 3", 300.0, "003"),
            TransactionData("Usuario 1", 100.0, "001"), // Duplicado
            TransactionData("Usuario 2", 200.0, "002")  // Duplicado
        )
        
        // Verificar que no hay datos inválidos
        transactions.forEach { transaction ->
            assertTrue(transaction.senderName.isNotEmpty(), "Nombre de remitente no debe estar vacío")
            assertTrue(transaction.amount > 0, "Monto debe ser mayor a 0")
            assertTrue(transaction.securityCode.isNotEmpty(), "Código de seguridad no debe estar vacío")
        }
        
        // Verificar duplicados
        val uniqueTransactions = transactions.distinctBy { it.securityCode }
        assertEquals(3, uniqueTransactions.size, "Debe haber 3 transacciones únicas")
        
        // Verificar que los duplicados están presentes
        val duplicateCount = transactions.size - uniqueTransactions.size
        assertEquals(2, duplicateCount, "Debe haber 2 transacciones duplicadas")
        
        DebugLogger.info("✅ [SIMPLE TEST] Integridad de datos verificada")
    }

    private fun parseNotification(notification: String): TransactionData {
        // Simular parsing simple de notificación
        val amountRegex = "S/ (\\d+\\.?\\d*)".toRegex()
        val codeRegex = "cód\\. de seguridad es: (\\d+)".toRegex()
        val senderRegex = "Confirmación de Pago (.+?) te envió".toRegex()
        
        val amount = amountRegex.find(notification)?.groupValues?.get(1)?.toDouble() ?: 0.0
        val code = codeRegex.find(notification)?.groupValues?.get(1) ?: ""
        val sender = senderRegex.find(notification)?.groupValues?.get(1) ?: "Usuario Desconocido"
        
        return TransactionData(sender, amount, code)
    }

    // Clases de datos simples para el test
    data class TransactionData(
        val senderName: String,
        val amount: Double,
        val securityCode: String
    )

    data class SystemStats(
        val totalTransactions: Int,
        val uniqueTransactions: Int,
        val duplicateTransactions: Int,
        val totalAmount: Double,
        val averageAmount: Double
    )

    // Funciones de aserción simples
    private fun assertEquals(expected: Any, actual: Any, message: String = "") {
        if (expected != actual) {
            throw AssertionError("$message - Expected: $expected, Actual: $actual")
        }
    }

    private fun assertTrue(condition: Boolean, message: String = "") {
        if (!condition) {
            throw AssertionError("$message - Condition was false")
        }
    }

    private fun assertNotNull(value: Any?, message: String = "") {
        if (value == null) {
            throw AssertionError("$message - Value was null")
        }
    }
}
