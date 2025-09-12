package org.sysarp.project.test.integration

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import org.sysarp.project.data.TransactionType
import org.sysarp.project.data.YapeTransaction
import org.sysarp.project.service.YapeNotificationParser
import kotlinx.datetime.Clock

/**
 * Test completo del flujo desde notificación hasta exportación
 * Simula todo el proceso: notificación → parsing → guardado → exportación
 */
class CompleteFlowTest {

    @Test
    fun testCompleteFlowFromNotificationToExport() {
        println("🚀 Iniciando test de flujo completo...")
        
        // PASO 1: Simular llegada de notificación de Yape
        val yapeNotification = "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 50.0. El cód. de seguridad es: 123"
        println("📱 Notificación recibida: $yapeNotification")
        
        // PASO 2: Verificar que es notificación de Yape
        val isYapeNotification = YapeNotificationParser.isYapeNotification(yapeNotification)
        assertTrue(isYapeNotification, "Debe detectar que es notificación de Yape")
        println("✅ Notificación de Yape detectada correctamente")
        
        // PASO 3: Parsear la notificación
        val parsedTransaction = YapeNotificationParser.parseYapeNotification(yapeNotification)
        assertNotNull(parsedTransaction, "La transacción debe parsearse correctamente")
        println("✅ Transacción parseada: ${parsedTransaction.amount} PEN de ${parsedTransaction.senderName}")
        
        // PASO 4: Verificar datos parseados
        assertEquals(50.0, parsedTransaction.amount, "El monto debe ser 50.0")
        assertEquals("PEN", parsedTransaction.currency, "La moneda debe ser PEN")
        assertTrue(parsedTransaction.senderName?.contains("Carlos") == true, "Debe contener el nombre Carlos")
        assertEquals("123", parsedTransaction.securityCode, "El código de seguridad debe ser 123")
        assertEquals(TransactionType.RECEIVED, parsedTransaction.transactionType, "Debe ser transacción recibida")
        println("✅ Datos de la transacción verificados correctamente")
        
        // PASO 5: Simular guardado en base de datos (crear transacción con ID)
        val savedTransaction = parsedTransaction.copy(
            id = 1L,
            transactionId = "YAPE_${System.currentTimeMillis()}_001",
            createdAt = Clock.System.now(),
            isProcessed = true,
            processedAt = Clock.System.now()
        )
        println("💾 Transacción guardada en base de datos con ID: ${savedTransaction.id}")
        
        // PASO 6: Simular exportación de base de datos
        val exportResult = simulateDatabaseExport(listOf(savedTransaction))
        assertNotNull(exportResult, "La exportación no debe ser null")
        assertTrue(exportResult.isNotEmpty(), "La exportación no debe estar vacía")
        println("📤 Base de datos exportada exitosamente")
        
        // PASO 7: Verificar contenido de la exportación
        val exportContent = exportResult.lowercase()
        assertTrue(exportContent.contains("carlos"), "La exportación debe contener el nombre Carlos")
        assertTrue(exportContent.contains("50.0"), "La exportación debe contener el monto 50.0")
        assertTrue(exportContent.contains("123"), "La exportación debe contener el código 123")
        assertTrue(exportContent.contains("pen"), "La exportación debe contener la moneda PEN")
        assertTrue(exportContent.contains("recibido"), "La exportación debe indicar que es recibido")
        println("✅ Contenido de exportación verificado correctamente")
        
        // PASO 8: Simular exportación de logs
        val logResult = simulateLogExport(listOf("Transacción parseada exitosamente", "Guardada en base de datos"))
        assertNotNull(logResult, "Los logs no deben ser null")
        assertTrue(logResult.isNotEmpty(), "Los logs no deben estar vacíos")
        println("📋 Logs exportados exitosamente")
        
        // PASO 9: Verificar que el flujo completo funciona
        assertTrue(exportResult.length > 100, "La exportación debe tener contenido sustancial")
        assertTrue(logResult.length > 50, "Los logs deben tener contenido sustancial")
        println("✅ Flujo completo verificado exitosamente")
        
        println("🎉 ¡FLUJO COMPLETO EXITOSO! Notificación → Parsing → Guardado → Exportación")
    }

    @Test
    fun testMultipleTransactionsFlow() {
        println("🚀 Iniciando test de flujo con múltiples transacciones...")
        
        // Simular múltiples notificaciones
        val notifications = listOf(
            "Confirmación de Pago María García te envió un pago por S/ 25.5. El cód. de seguridad es: 456",
            "Confirmación de Pago Juan Pérez te envió un pago por S/ 100.0. El cód. de seguridad es: 789",
            "Enviaste un pago de S/ 75.0 a Ana López. Código de seguridad: 321"
        )
        
        val parsedTransactions = mutableListOf<YapeTransaction>()
        
        // Procesar cada notificación
        notifications.forEachIndexed { index, notification ->
            println("📱 Procesando notificación ${index + 1}: $notification")
            
            val isYape = YapeNotificationParser.isYapeNotification(notification)
            assertTrue(isYape, "Debe detectar notificación de Yape ${index + 1}")
            
            val parsed = YapeNotificationParser.parseYapeNotification(notification)
            assertNotNull(parsed, "Debe parsear notificación ${index + 1}")
            
            val saved = parsed.copy(
                id = (index + 1).toLong(),
                transactionId = "YAPE_${System.currentTimeMillis()}_00${index + 1}",
                createdAt = Clock.System.now(),
                isProcessed = true,
                processedAt = Clock.System.now()
            )
            
            parsedTransactions.add(saved)
            println("✅ Transacción ${index + 1} procesada: ${saved.amount} PEN de ${saved.senderName}")
        }
        
        // Exportar todas las transacciones
        val exportResult = simulateDatabaseExport(parsedTransactions)
        assertNotNull(exportResult, "La exportación no debe ser null")
        assertTrue(exportResult.isNotEmpty(), "La exportación no debe estar vacía")
        
        // Verificar que contiene todas las transacciones
        val exportContent = exportResult.lowercase()
        assertTrue(exportContent.contains("maría"), "Debe contener transacción de María")
        assertTrue(exportContent.contains("juan"), "Debe contener transacción de Juan")
        assertTrue(exportContent.contains("ana"), "Debe contener transacción de Ana")
        assertTrue(exportContent.contains("25.5"), "Debe contener monto 25.5")
        assertTrue(exportContent.contains("100.0"), "Debe contener monto 100.0")
        assertTrue(exportContent.contains("75.0"), "Debe contener monto 75.0")
        
        println("✅ Múltiples transacciones procesadas y exportadas exitosamente")
        println("🎉 ¡FLUJO MÚLTIPLE EXITOSO! ${parsedTransactions.size} transacciones procesadas")
    }

    @Test
    fun testErrorHandlingFlow() {
        println("🚀 Iniciando test de manejo de errores...")
        
        // Simular notificación inválida
        val invalidNotification = "Mensaje de WhatsApp: Hola, ¿cómo estás?"
        println("📱 Notificación inválida: $invalidNotification")
        
        val isYape = YapeNotificationParser.isYapeNotification(invalidNotification)
        assertTrue(!isYape, "No debe detectar como notificación de Yape")
        println("✅ Notificación inválida correctamente rechazada")
        
        // Simular exportación de base de datos vacía
        val emptyExport = simulateDatabaseExport(emptyList())
        assertNotNull(emptyExport, "La exportación vacía no debe ser null")
        assertTrue(emptyExport.contains("No hay transacciones"), "Debe indicar que no hay transacciones")
        println("✅ Base de datos vacía manejada correctamente")
        
        // Simular exportación de logs de error
        val errorLogs = listOf(
            "Error: No se pudo parsear la notificación",
            "WARN: Notificación inválida recibida"
        )
        val errorLogExport = simulateLogExport(errorLogs)
        assertNotNull(errorLogExport, "Los logs de error no deben ser null")
        assertTrue(errorLogExport.contains("Error"), "Debe contener logs de error")
        println("✅ Logs de error exportados correctamente")
        
        println("🎉 ¡MANEJO DE ERRORES EXITOSO! Errores manejados correctamente")
    }

    /**
     * Simula la exportación de base de datos
     */
    private fun simulateDatabaseExport(transactions: List<YapeTransaction>): String {
        val timestamp = Clock.System.now().toString()
        val separator = "========================================"
        
        val header = """
            $separator
            YAPE CHAMO - EXPORTACIÓN DE BASE DE DATOS
            $separator
            Exportado: $timestamp
            Total de transacciones: ${transactions.size}
            $separator
        """.trimIndent()

        if (transactions.isEmpty()) {
            return """
                $header
                No hay transacciones en la base de datos.
                $separator
            """.trimIndent()
        }

        val transactionLines = transactions.map { transaction ->
            """
            ID: ${transaction.id}
            Transaction ID: ${transaction.transactionId}
            Monto: S/ ${transaction.amount} ${transaction.currency}
            Remitente: ${transaction.senderName}
            Teléfono: ${transaction.senderPhone ?: "N/A"}
            Tipo: ${if (transaction.transactionType == TransactionType.RECEIVED) "Recibido" else "Enviado"}
            Código de Seguridad: ${transaction.securityCode}
            Fecha: ${transaction.createdAt}
            Mensaje: ${transaction.message}
            ---
            """.trimIndent()
        }

        val footer = """
            $separator
            Exportación completada exitosamente
            $separator
        """.trimIndent()

        return listOf(header, transactionLines.joinToString("\n"), footer).joinToString("\n")
    }

    /**
     * Simula la exportación de logs
     */
    private fun simulateLogExport(logs: List<String>): String {
        val timestamp = Clock.System.now().toString()
        val separator = "========================================"
        
        val header = """
            $separator
            YAPE CHAMO - DEBUG LOGS
            $separator
            Generado: $timestamp
            Total de logs: ${logs.size}
            $separator
        """.trimIndent()

        val logLines = logs.joinToString("\n")
        
        val footer = """
            $separator
            Fin de logs
            $separator
        """.trimIndent()

        return listOf(header, logLines, footer).joinToString("\n")
    }
}
