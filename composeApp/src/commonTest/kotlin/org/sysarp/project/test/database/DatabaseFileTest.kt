package org.sysarp.project.test.database

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
 * Test que genera archivos de base de datos con diferentes escenarios
 */
class DatabaseFileTest {

    @Test
    fun testGenerateDatabaseFilesWithDifferentScenarios() {
        // Crear directorio de test si no existe
        val testDir = File("test_logs/database")
        if (!testDir.exists()) {
            testDir.mkdirs()
        }

        val timestamp = Clock.System.now().toString().substring(0, 19).replace(":", "-").replace("T", "_")

        // Escenario 1: Base de datos con transacciones
        val transactionsWithData = createTransactionsWithData()
        val fileWithData = Paths.get("test_logs/database", "yapehub_database_with_data_${timestamp}.txt")
        Files.write(fileWithData, generateDatabaseExportContent(transactionsWithData).toByteArray())

        // Escenario 2: Base de datos vacía
        val fileEmpty = Paths.get("test_logs/database", "yapehub_database_empty_${timestamp}.txt")
        Files.write(fileEmpty, generateDatabaseExportContent(emptyList()).toByteArray())

        // Escenario 3: Base de datos con solo transacciones recibidas
        val receivedOnly = createReceivedOnlyTransactions()
        val fileReceivedOnly = Paths.get("test_logs/database", "yapehub_database_received_only_${timestamp}.txt")
        Files.write(fileReceivedOnly, generateDatabaseExportContent(receivedOnly).toByteArray())

        // Escenario 4: Base de datos con solo transacciones enviadas
        val sentOnly = createSentOnlyTransactions()
        val fileSentOnly = Paths.get("test_logs/database", "yapehub_database_sent_only_${timestamp}.txt")
        Files.write(fileSentOnly, generateDatabaseExportContent(sentOnly).toByteArray())

        // Verificar que todos los archivos se crearon
        assertTrue(fileWithData.toFile().exists(), "Archivo con datos debería existir")
        assertTrue(fileEmpty.toFile().exists(), "Archivo vacío debería existir")
        assertTrue(fileReceivedOnly.toFile().exists(), "Archivo solo recibidos debería existir")
        assertTrue(fileSentOnly.toFile().exists(), "Archivo solo enviados debería existir")

        println("✅ Archivos de base de datos generados:")
        println("   📊 Con datos: ${fileWithData.toFile().absolutePath}")
        println("   📊 Vacía: ${fileEmpty.toFile().absolutePath}")
        println("   📊 Solo recibidos: ${fileReceivedOnly.toFile().absolutePath}")
        println("   📊 Solo enviados: ${fileSentOnly.toFile().absolutePath}")
    }

    @Test
    fun testGenerateDatabaseWithLargeDataset() {
        val testDir = File("test_logs/database")
        if (!testDir.exists()) {
            testDir.mkdirs()
        }

        val timestamp = Clock.System.now().toString().substring(0, 19).replace(":", "-").replace("T", "_")

        // Crear un dataset grande (100 transacciones)
        val largeDataset = createLargeDataset(100)
        val fileLarge = Paths.get("test_logs/database", "yapehub_database_large_${timestamp}.txt")
        Files.write(fileLarge, generateDatabaseExportContent(largeDataset).toByteArray())

        val file = fileLarge.toFile()
        assertTrue(file.exists(), "Archivo grande debería existir")
        assertTrue(file.length() > 10000, "Archivo grande debería tener más de 10KB")

        println("✅ Archivo de base de datos grande generado:")
        println("   📊 Archivo: ${file.absolutePath}")
        println("   📊 Tamaño: ${file.length()} bytes")
        println("   📊 Transacciones: 100")
    }

    @Test
    fun testGenerateDatabaseWithSpecialCharacters() {
        val testDir = File("test_logs/database")
        if (!testDir.exists()) {
            testDir.mkdirs()
        }

        val timestamp = Clock.System.now().toString().substring(0, 19).replace(":", "-").replace("T", "_")

        // Crear transacciones con caracteres especiales
        val specialTransactions = createSpecialCharacterTransactions()
        val fileSpecial = Paths.get("test_logs/database", "yapehub_database_special_chars_${timestamp}.txt")
        Files.write(fileSpecial, generateDatabaseExportContent(specialTransactions).toByteArray())

        val file = fileSpecial.toFile()
        assertTrue(file.exists(), "Archivo con caracteres especiales debería existir")

        val content = file.readText()
        assertTrue(content.contains("José María"), "Debería contener nombres con acentos")
        assertTrue(content.contains("Ñoño"), "Debería contener caracteres especiales")
        assertTrue(content.contains("S/ 1,234.56"), "Debería contener montos con comas")

        println("✅ Archivo con caracteres especiales generado:")
        println("   📊 Archivo: ${file.absolutePath}")
        println("   📊 Tamaño: ${file.length()} bytes")
    }

    private fun createTransactionsWithData(): List<YapeTransaction> {
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

    private fun createReceivedOnlyTransactions(): List<YapeTransaction> {
        return listOf(
            YapeTransaction(
                id = 1L,
                transactionId = "YAPE_REC_001",
                amount = 75.0,
                currency = "PEN",
                senderName = "Ana López",
                senderPhone = "+51987654324",
                message = "Confirmación de Pago Ana López te envió un pago por S/ 75.0. El cód. de seguridad es: 111",
                transactionType = TransactionType.RECEIVED,
                businessName = "Test Business",
                createdAt = Clock.System.now(),
                processedAt = null,
                isProcessed = false,
                rawNotification = "Confirmación de Pago Ana López te envió un pago por S/ 75.0. El cód. de seguridad es: 111",
                securityCode = "111"
            ),
            YapeTransaction(
                id = 2L,
                transactionId = "YAPE_REC_002",
                amount = 200.0,
                currency = "PEN",
                senderName = "Pedro Martín",
                senderPhone = "+51987654325",
                message = "Confirmación de Pago Pedro Martín te envió un pago por S/ 200.0. El cód. de seguridad es: 222",
                transactionType = TransactionType.RECEIVED,
                businessName = "Test Business",
                createdAt = Clock.System.now(),
                processedAt = null,
                isProcessed = false,
                rawNotification = "Confirmación de Pago Pedro Martín te envió un pago por S/ 200.0. El cód. de seguridad es: 222",
                securityCode = "222"
            )
        )
    }

    private fun createSentOnlyTransactions(): List<YapeTransaction> {
        return listOf(
            YapeTransaction(
                id = 1L,
                transactionId = "YAPE_SENT_001",
                amount = 150.0,
                currency = "PEN",
                senderName = "Luis Rodríguez",
                senderPhone = "+51987654326",
                message = "Enviaste un pago de S/ 150.0 a Luis Rodríguez. Código de seguridad: 333",
                transactionType = TransactionType.SENT,
                businessName = "Test Business",
                createdAt = Clock.System.now(),
                processedAt = null,
                isProcessed = false,
                rawNotification = "Enviaste un pago de S/ 150.0 a Luis Rodríguez. Código de seguridad: 333",
                securityCode = "333"
            ),
            YapeTransaction(
                id = 2L,
                transactionId = "YAPE_SENT_002",
                amount = 300.0,
                currency = "PEN",
                senderName = "Carmen Silva",
                senderPhone = "+51987654327",
                message = "Enviaste un pago de S/ 300.0 a Carmen Silva. Código de seguridad: 444",
                transactionType = TransactionType.SENT,
                businessName = "Test Business",
                createdAt = Clock.System.now(),
                processedAt = null,
                isProcessed = false,
                rawNotification = "Enviaste un pago de S/ 300.0 a Carmen Silva. Código de seguridad: 444",
                securityCode = "444"
            )
        )
    }

    private fun createSpecialCharacterTransactions(): List<YapeTransaction> {
        return listOf(
            YapeTransaction(
                id = 1L,
                transactionId = "YAPE_SPECIAL_001",
                amount = 1234.56,
                currency = "PEN",
                senderName = "José María",
                senderPhone = "+51987654328",
                message = "Confirmación de Pago José María te envió un pago por S/ 1,234.56. El cód. de seguridad es: 555",
                transactionType = TransactionType.RECEIVED,
                businessName = "Test Business",
                createdAt = Clock.System.now(),
                processedAt = null,
                isProcessed = false,
                rawNotification = "Confirmación de Pago José María te envió un pago por S/ 1,234.56. El cód. de seguridad es: 555",
                securityCode = "555"
            ),
            YapeTransaction(
                id = 2L,
                transactionId = "YAPE_SPECIAL_002",
                amount = 999.99,
                currency = "PEN",
                senderName = "Ñoño Ñuñez",
                senderPhone = "+51987654329",
                message = "Confirmación de Pago Ñoño Ñuñez te envió un pago por S/ 999.99. El cód. de seguridad es: 666",
                transactionType = TransactionType.RECEIVED,
                businessName = "Test Business",
                createdAt = Clock.System.now(),
                processedAt = null,
                isProcessed = false,
                rawNotification = "Confirmación de Pago Ñoño Ñuñez te envió un pago por S/ 999.99. El cód. de seguridad es: 666",
                securityCode = "666"
            )
        )
    }

    private fun createLargeDataset(size: Int): List<YapeTransaction> {
        return (1..size).map { index ->
            YapeTransaction(
                id = index.toLong(),
                transactionId = "YAPE_LARGE_${String.format("%03d", index)}",
                amount = (index * 10.0),
                currency = "PEN",
                senderName = "Usuario $index",
                senderPhone = "+519876543${String.format("%02d", index)}",
                message = "Transacción de prueba $index por S/ ${index * 10.0}",
                transactionType = if (index % 2 == 0) TransactionType.RECEIVED else TransactionType.SENT,
                businessName = "Test Business",
                createdAt = Clock.System.now(),
                processedAt = null,
                isProcessed = false,
                rawNotification = "Transacción de prueba $index",
                securityCode = String.format("%03d", index)
            )
        }
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
}
