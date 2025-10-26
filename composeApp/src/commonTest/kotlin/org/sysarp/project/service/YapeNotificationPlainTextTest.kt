package org.sysarp.project.service

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test específico para demostrar que las notificaciones de Yape se envían como texto plano encriptado
 * Este test verifica el comportamiento actual del sistema
 */
class YapeNotificationPlainTextTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `Yape notification is sent as encrypted plain text, not structured object`() {
        // Given - Simular notificación de Yape como texto plano (comportamiento actual)
        val plainTextNotification = """
            {
                "packageName": "com.bcp.innovacxion.yapeapp",
                "title": "Yape",
                "text": "Recibiste S/ 25.50",
                "bigText": "Recibiste S/ 25.50 de Juan Pérez\nComercio: Mi Tienda\nFecha: 26/09/2025 15:30",
                "fullText": "Recibiste S/ 25.50 de Juan Pérez\nComercio: Mi Tienda\nFecha: 26/09/2025 15:30",
                "timestamp": 1758866622000,
                "notificationId": 12345
            }
        """.trimIndent()
        
        val deviceFingerprint = "yapehub_1758866622_abc123"

        // When - Encriptar el texto plano (como lo hace actualmente)
        val encryptedNotification = org.sysarp.project.utils.EncryptionUtils.encryptWithKey(plainTextNotification, deviceFingerprint)
        
        // Then - Verificar que se envía como texto plano encriptado
        assertNotNull(encryptedNotification)
        assertTrue(encryptedNotification.isNotEmpty())
        
        // Verificar que se puede desencriptar para obtener el texto plano original
        val decryptedText = org.sysarp.project.utils.EncryptionUtils.decryptWithKey(encryptedNotification, deviceFingerprint)
        assertEquals(plainTextNotification, decryptedText)
        
        // Verificar que el texto desencriptado es JSON válido
        val parsed = json.decodeFromString<Map<String, Any>>(decryptedText)
        assertEquals("com.bcp.innovacxion.yapeapp", parsed["packageName"])
        assertEquals("Yape", parsed["title"])
        assertEquals("Recibiste S/ 25.50", parsed["text"])
        
        // IMPORTANTE: Demostrar que se envía como texto plano, no como objeto estructurado
        assertTrue(encryptedNotification is String)
        assertTrue(decryptedText.contains("packageName"))
        assertTrue(decryptedText.contains("title"))
        assertTrue(decryptedText.contains("text"))
        assertTrue(decryptedText.contains("bigText"))
        assertTrue(decryptedText.contains("fullText"))
    }

    @Test
    fun `notification text contains all Yape notification fields as plain text`() {
        // Given - Notificación de Yape con todos los campos
        val plainTextNotification = """
            {
                "packageName": "com.bcp.innovacxion.yapeapp",
                "title": "Yape",
                "text": "Recibiste S/ 25.50",
                "bigText": "Recibiste S/ 25.50 de Juan Pérez\nComercio: Mi Tienda\nFecha: 26/09/2025 15:30",
                "fullText": "Recibiste S/ 25.50 de Juan Pérez\nComercio: Mi Tienda\nFecha: 26/09/2025 15:30",
                "timestamp": 1758866622000,
                "notificationId": 12345
            }
        """.trimIndent()
        
        val deviceFingerprint = "test_device_123"
        
        // When
        val encryptedNotification = org.sysarp.project.utils.EncryptionUtils.encryptWithKey(plainTextNotification, deviceFingerprint)
        val decryptedText = org.sysarp.project.utils.EncryptionUtils.decryptWithKey(encryptedNotification, deviceFingerprint)
        
        // Then - Verificar que todos los campos están presentes como texto plano
        assertTrue(decryptedText.contains("\"packageName\""))
        assertTrue(decryptedText.contains("\"title\""))
        assertTrue(decryptedText.contains("\"text\""))
        assertTrue(decryptedText.contains("\"bigText\""))
        assertTrue(decryptedText.contains("\"fullText\""))
        assertTrue(decryptedText.contains("\"timestamp\""))
        assertTrue(decryptedText.contains("\"notificationId\""))
        
        // Verificar que es JSON válido
        val parsed = json.decodeFromString<Map<String, Any>>(decryptedText)
        assertEquals("com.bcp.innovacxion.yapeapp", parsed["packageName"])
        assertEquals("Yape", parsed["title"])
        assertEquals("Recibiste S/ 25.50", parsed["text"])
        assertNotNull(parsed["bigText"])
        assertNotNull(parsed["fullText"])
        assertEquals(1758866622000L, parsed["timestamp"])
        assertEquals(12345, parsed["notificationId"])
    }

    @Test
    fun `notification is sent as single encrypted string, not parsed into structured fields`() {
        // Given - Notificación de Yape
        val plainTextNotification = """
            {
                "packageName": "com.bcp.innovacxion.yapeapp",
                "title": "Yape",
                "text": "Recibiste S/ 25.50",
                "bigText": "Recibiste S/ 25.50 de Juan Pérez",
                "fullText": "Recibiste S/ 25.50 de Juan Pérez",
                "timestamp": 1758866622000,
                "notificationId": 12345
            }
        """.trimIndent()
        
        val deviceFingerprint = "test_device_123"
        
        // When
        val encryptedNotification = org.sysarp.project.utils.EncryptionUtils.encryptWithKey(plainTextNotification, deviceFingerprint)
        
        // Then - Verificar que se envía como un solo string encriptado
        assertTrue(encryptedNotification is String)
        assertTrue(encryptedNotification.isNotEmpty())
        
        // Verificar que se puede desencriptar para obtener el texto plano original
        val decryptedText = org.sysarp.project.utils.EncryptionUtils.decryptWithKey(encryptedNotification, deviceFingerprint)
        assertEquals(plainTextNotification, decryptedText)
    }

    @Test
    fun `notification text preserves original Yape notification format`() {
        // Given - Notificación de Yape en formato original
        val originalYapeNotification = """
            {
                "packageName": "com.bcp.innovacxion.yapeapp",
                "title": "Yape",
                "text": "Recibiste S/ 25.50",
                "bigText": "Recibiste S/ 25.50 de Juan Pérez\nComercio: Mi Tienda\nFecha: 26/09/2025 15:30",
                "fullText": "Recibiste S/ 25.50 de Juan Pérez\nComercio: Mi Tienda\nFecha: 26/09/2025 15:30",
                "timestamp": 1758866622000,
                "notificationId": 12345
            }
        """.trimIndent()
        
        val deviceFingerprint = "test_device_123"
        
        // When
        val encryptedNotification = org.sysarp.project.utils.EncryptionUtils.encryptWithKey(originalYapeNotification, deviceFingerprint)
        val decryptedText = org.sysarp.project.utils.EncryptionUtils.decryptWithKey(encryptedNotification, deviceFingerprint)
        
        // Then - Verificar que se preserva el formato original
        assertEquals(originalYapeNotification, decryptedText)
        
        // Verificar que contiene la información específica de Yape
        assertTrue(decryptedText.contains("com.bcp.innovacxion.yapeapp"))
        assertTrue(decryptedText.contains("Yape"))
        assertTrue(decryptedText.contains("Recibiste S/ 25.50"))
        assertTrue(decryptedText.contains("Juan Pérez"))
        assertTrue(decryptedText.contains("Mi Tienda"))
        assertTrue(decryptedText.contains("26/09/2025 15:30"))
    }

    @Test
    fun `notification text can contain multiple Yape transactions`() {
        // Given - Notificación de Yape con múltiples transacciones
        val multiTransactionNotification = """
            {
                "packageName": "com.bcp.innovacxion.yapeapp",
                "title": "Yape",
                "text": "Recibiste S/ 25.50",
                "bigText": "Recibiste S/ 25.50 de Juan Pérez\nRecibiste S/ 10.00 de María García\nRecibiste S/ 5.00 de Carlos López",
                "fullText": "Recibiste S/ 25.50 de Juan Pérez\nRecibiste S/ 10.00 de María García\nRecibiste S/ 5.00 de Carlos López",
                "timestamp": 1758866622000,
                "notificationId": 12345
            }
        """.trimIndent()
        
        val deviceFingerprint = "test_device_123"
        
        // When
        val encryptedNotification = org.sysarp.project.utils.EncryptionUtils.encryptWithKey(multiTransactionNotification, deviceFingerprint)
        val decryptedText = org.sysarp.project.utils.EncryptionUtils.decryptWithKey(encryptedNotification, deviceFingerprint)
        
        // Then - Verificar que se preservan todas las transacciones
        assertEquals(multiTransactionNotification, decryptedText)
        assertTrue(decryptedText.contains("Juan Pérez"))
        assertTrue(decryptedText.contains("María García"))
        assertTrue(decryptedText.contains("Carlos López"))
        assertTrue(decryptedText.contains("S/ 25.50"))
        assertTrue(decryptedText.contains("S/ 10.00"))
        assertTrue(decryptedText.contains("S/ 5.00"))
    }

    @Test
    fun `notification text handles Yape error messages`() {
        // Given - Notificación de error de Yape
        val errorNotification = """
            {
                "packageName": "com.bcp.innovacxion.yapeapp",
                "title": "Yape",
                "text": "Error en la transacción",
                "bigText": "Error en la transacción\nNo se pudo procesar el pago\nIntenta nuevamente",
                "fullText": "Error en la transacción\nNo se pudo procesar el pago\nIntenta nuevamente",
                "timestamp": 1758866622000,
                "notificationId": 12345
            }
        """.trimIndent()
        
        val deviceFingerprint = "test_device_123"
        
        // When
        val encryptedNotification = org.sysarp.project.utils.EncryptionUtils.encryptWithKey(errorNotification, deviceFingerprint)
        val decryptedText = org.sysarp.project.utils.EncryptionUtils.decryptWithKey(encryptedNotification, deviceFingerprint)
        
        // Then - Verificar que se preservan los mensajes de error
        assertEquals(errorNotification, decryptedText)
        assertTrue(decryptedText.contains("Error en la transacción"))
        assertTrue(decryptedText.contains("No se pudo procesar el pago"))
        assertTrue(decryptedText.contains("Intenta nuevamente"))
    }
}