package org.sysarp.project.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Test simple para verificar que la encriptación funciona
 */
class SimpleEncryptionTest {

    @Test
    fun `simple encryption and decryption should work`() {
        // Given
        val message = "Hello World"
        val key = "test_key"
        
        // When
        val encrypted = EncryptionUtils.encryptWithKey(message, key)
        val decrypted = EncryptionUtils.decryptWithKey(encrypted, key)
        
        // Then
        assertNotNull(encrypted)
        assertNotNull(decrypted)
        println("Original: $message")
        println("Encrypted: $encrypted")
        println("Decrypted: $decrypted")
        
        // Verificar que el texto encriptado es diferente al original
        assert(encrypted != message)
        
        // Verificar que se puede desencriptar correctamente
        assertEquals(message, decrypted)
    }
    
    @Test
    fun `encryption should handle Yape notification format`() {
        // Given - Simular notificación de Yape como texto plano
        val yapeNotification = """
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
        
        val deviceFingerprint = "yapehub_1758866622_abc123"
        
        // When
        val encrypted = EncryptionUtils.encryptWithKey(yapeNotification, deviceFingerprint)
        val decrypted = EncryptionUtils.decryptWithKey(encrypted, deviceFingerprint)
        
        // Then
        assertNotNull(encrypted)
        assertNotNull(decrypted)
        println("Original Yape notification: $yapeNotification")
        println("Encrypted: $encrypted")
        println("Decrypted: $decrypted")
        
        // Verificar que se puede desencriptar correctamente
        assertEquals(yapeNotification, decrypted)
        
        // Verificar que contiene información de Yape
        assert(decrypted.contains("com.bcp.innovacxion.yapeapp"))
        assert(decrypted.contains("Yape"))
        assert(decrypted.contains("Recibiste S/ 25.50"))
        assert(decrypted.contains("Juan Pérez"))
    }
}
