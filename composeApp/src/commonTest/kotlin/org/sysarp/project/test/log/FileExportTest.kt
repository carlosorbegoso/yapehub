package org.sysarp.project.test.log

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.sysarp.project.data.TransactionType
import org.sysarp.project.data.YapeTransaction
import kotlinx.datetime.Clock
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Test que realmente genera y guarda archivos de exportación de base de datos y logs
 */
class FileExportTest {

    @Test
    fun testGenerateAndSaveDatabaseExportFile() {
        // Crear directorio de test si no existe
        val testDir = File("test_logs")
        if (!testDir.exists()) {
            testDir.mkdirs()
        }

        // Crear transacciones de prueba
        val testTransactions = createTestTransactions()

        // Generar contenido de exportación
        val exportContent = generateDatabaseExportContent(testTransactions)

        // Guardar archivo
        val timestamp = Clock.System.now().toString().substring(0, 19).replace(":", "-").replace("T", "_")
        val fileName = "yapehub_database_test_${timestamp}.txt"
        val filePath = Paths.get("test_logs", fileName)
        
        Files.write(filePath, exportContent.toByteArray())

        // Verificar que el archivo se creó
        val file = filePath.toFile()
        assertTrue(file.exists(), "El archivo de exportación debería existir")
        assertTrue(file.length() > 0, "El archivo no debería estar vacío")

        // Verificar contenido del archivo
        val fileContent = file.readText()
        assertNotNull(fileContent, "El contenido del archivo no debería ser null")
        assertTrue(fileContent.contains("YAPEHUB - EXPORTACIÓN DE BASE DE DATOS"), "Debería contener el header correcto")
        assertTrue(fileContent.contains("Total de transacciones: 3"), "Debería mostrar el total correcto")

        println("✅ Archivo de base de datos generado: ${file.absolutePath}")
        println("📊 Tamaño del archivo: ${file.length()} bytes")
    }

    @Test
    fun testGenerateAndSaveLogExportFile() {
        // Crear directorio de test si no existe
        val testDir = File("test_logs")
        if (!testDir.exists()) {
            testDir.mkdirs()
        }

        // Crear logs de prueba
        val testLogs = createTestLogs()

        // Generar contenido de logs
        val logContent = generateLogExportContent(testLogs)

        // Guardar archivo
        val timestamp = Clock.System.now().toString().substring(0, 19).replace(":", "-").replace("T", "_")
        val fileName = "yapehub_logs_test_${timestamp}.txt"
        val filePath = Paths.get("test_logs", fileName)
        
        Files.write(filePath, logContent.toByteArray())

        // Verificar que el archivo se creó
        val file = filePath.toFile()
        assertTrue(file.exists(), "El archivo de logs debería existir")
        assertTrue(file.length() > 0, "El archivo no debería estar vacío")

        // Verificar contenido del archivo
        val fileContent = file.readText()
        assertNotNull(fileContent, "El contenido del archivo no debería ser null")
        assertTrue(fileContent.contains("YAPEHUB - DEBUG LOGS"), "Debería contener el header correcto")
        assertTrue(fileContent.contains("Total de logs: 11"), "Debería mostrar el total correcto")

        println("✅ Archivo de logs generado: ${file.absolutePath}")
        println("📊 Tamaño del archivo: ${file.length()} bytes")
    }

    @Test
    fun testGenerateMultipleExportFiles() {
        // Crear directorio de test si no existe
        val testDir = File("test_logs")
        if (!testDir.exists()) {
            testDir.mkdirs()
        }

        val timestamp = Clock.System.now().toString().substring(0, 19).replace(":", "-").replace("T", "_")

        // Generar archivo de base de datos
        val dbContent = generateDatabaseExportContent(createTestTransactions())
        val dbFile = Paths.get("test_logs", "yapehub_database_${timestamp}.txt")
        Files.write(dbFile, dbContent.toByteArray())

        // Generar archivo de logs
        val logContent = generateLogExportContent(createTestLogs())
        val logFile = Paths.get("test_logs", "yapehub_logs_${timestamp}.txt")
        Files.write(logFile, logContent.toByteArray())

        // Verificar ambos archivos
        assertTrue(dbFile.toFile().exists(), "Archivo de BD debería existir")
        assertTrue(logFile.toFile().exists(), "Archivo de logs debería existir")

        println("✅ Archivos de exportación generados:")
        println("   📊 Base de datos: ${dbFile.toFile().absolutePath}")
        println("   📝 Logs: ${logFile.toFile().absolutePath}")
    }

    @Test
    fun testExportFileStructure() {
        val testDir = File("test_logs")
        if (!testDir.exists()) {
            testDir.mkdirs()
        }

        val timestamp = Clock.System.now().toString().substring(0, 19).replace(":", "-").replace("T", "_")
        val fileName = "yapehub_export_structure_test_${timestamp}.txt"
        val filePath = Paths.get("test_logs", fileName)

        // Crear contenido estructurado
        val structuredContent = createStructuredExportContent()

        Files.write(filePath, structuredContent.toByteArray())

        val file = filePath.toFile()
        assertTrue(file.exists(), "El archivo estructurado debería existir")

        val content = file.readText()
        val lines = content.split("\n")

        // Verificar estructura
        assertTrue(lines.any { it.contains("YAPEHUB - EXPORTACIÓN COMPLETA") }, "Debería tener header principal")
        assertTrue(lines.any { it.contains("=== BASE DE DATOS ===") }, "Debería tener sección de BD")
        assertTrue(lines.any { it.contains("=== LOGS ===") }, "Debería tener sección de logs")
        assertTrue(lines.any { it.contains("=== RESUMEN ===") }, "Debería tener sección de resumen")

        println("✅ Archivo estructurado generado: ${file.absolutePath}")
        println("📊 Líneas totales: ${lines.size}")
    }

    private fun createTestTransactions(): List<YapeTransaction> {
        return listOf(
            YapeTransaction(
                id = 1L,
                transactionId = "YAPE_1234567890_001",
                amount = 50.0,
                currency = "PEN",
                senderName = "Carlos Orbegoso",
                senderPhone = "+51987654321",
                message = "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 50.0. El cód. de seguridad es: 123",
                transactionType = TransactionType.RECEIVED,
                businessName = "Test Business",
                createdAt = Clock.System.now(),
                processedAt = null,
                isProcessed = false,
                rawNotification = "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 50.0. El cód. de seguridad es: 123",
                securityCode = "123"
            ),
            YapeTransaction(
                id = 2L,
                transactionId = "YAPE_1234567891_002",
                amount = 25.5,
                currency = "PEN",
                senderName = "María García",
                senderPhone = "+51987654322",
                message = "Confirmación de Pago María García te envió un pago por S/ 25.5. El cód. de seguridad es: 456",
                transactionType = TransactionType.RECEIVED,
                businessName = "Test Business",
                createdAt = Clock.System.now(),
                processedAt = null,
                isProcessed = false,
                rawNotification = "Confirmación de Pago María García te envió un pago por S/ 25.5. El cód. de seguridad es: 456",
                securityCode = "456"
            ),
            YapeTransaction(
                id = 3L,
                transactionId = "YAPE_1234567892_003",
                amount = 100.0,
                currency = "PEN",
                senderName = "Juan Pérez",
                senderPhone = "+51987654323",
                message = "Enviaste un pago de S/ 100.0 a Juan Pérez. Código de seguridad: 789",
                transactionType = TransactionType.SENT,
                businessName = "Test Business",
                createdAt = Clock.System.now(),
                processedAt = null,
                isProcessed = false,
                rawNotification = "Enviaste un pago de S/ 100.0 a Juan Pérez. Código de seguridad: 789",
                securityCode = "789"
            )
        )
    }

    private fun createTestLogs(): List<String> {
        return listOf(
            "[19:09:41.706] [INFO] Cargadas 0 transacciones desde SQLite",
            "[19:09:47.641] [INFO] Resultados de permisos - Notificaciones: true, Accesibilidad: true",
            "[19:10:27.479] [DEBUG] 🔍 Verificando package Yape: com.bcp.innovacxion.yapeapp -> true",
            "[19:10:27.499] [INFO] 🔍 Procesando notificación de Yape: Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 0.1. El cód. de seguridad es: 031",
            "[19:10:27.501] [DEBUG] 🔍 Verificando si es notificación de Yape: true",
            "[19:10:27.502] [DEBUG] 📱 Indicador encontrado en: Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 0.1. El cód. de seguridad es: 031",
            "[19:10:27.503] [INFO] ✅ Es notificación de Yape válida, parseando...",
            "[19:10:27.504] [INFO] ✅ Transacción parseada exitosamente: 0.1 PEN de Carlos Orbegoso L.",
            "[19:10:27.505] [INFO] 💾 Guardando transacción en repositorio...",
            "[19:10:27.506] [INFO] ✅ Transacción guardada exitosamente en repositorio",
            "[19:10:32.69] [WARN] ⚠️ No se pudo procesar la notificación"
        )
    }

    private fun generateDatabaseExportContent(transactions: List<YapeTransaction>): String {
        val timestamp = Clock.System.now().toString()
        val separator = "========================================"
        
        val header = """
            $separator
            YAPEHUB - EXPORTACIÓN DE BASE DE DATOS
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

    private fun generateLogExportContent(logs: List<String>): String {
        val timestamp = Clock.System.now().toString()
        val separator = "========================================"
        
        val header = """
            $separator
            YAPEHUB - DEBUG LOGS
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

    private fun createStructuredExportContent(): String {
        val timestamp = Clock.System.now().toString()
        val separator = "========================================"
        
        return """
            $separator
            YAPEHUB - EXPORTACIÓN COMPLETA
            $separator
            Generado: $timestamp
            Versión: 1.0.0
            $separator

            === BASE DE DATOS ===
            ${generateDatabaseExportContent(createTestTransactions())}

            === LOGS ===
            ${generateLogExportContent(createTestLogs())}

            === RESUMEN ===
            - Transacciones procesadas: 3
            - Logs generados: 11
            - Estado: Exitoso
            - Archivos creados: 2

            $separator
            Exportación completa finalizada
            $separator
        """.trimIndent()
    }
}
