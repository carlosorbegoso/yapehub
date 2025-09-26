package org.sysarp.project.utils

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests unitarios para las utilidades de encriptación
 */
class EncryptionUtilsTest {

    @Test
    fun `encryptWithKey and decryptWithKey should be symmetric`() {
        // Given
        val message = "Hello, World!"
        val key = "test_key_123"
        
        // When
        val encrypted = EncryptionUtils.encryptWithKey(message, key)
        val decrypted = EncryptionUtils.decryptWithKey(encrypted, key)
        
        // Then
        assertEquals(message, decrypted)
        assertTrue(encrypted != message) // Should be different from original
    }

    @Test
    fun `encryption should work with different keys`() {
        // Given
        val message = "Test message"
        val key1 = "key1"
        val key2 = "key2"
        
        // When
        val encrypted1 = EncryptionUtils.encryptWithKey(message, key1)
        val encrypted2 = EncryptionUtils.encryptWithKey(message, key2)
        
        // Then
        assertTrue(encrypted1 != encrypted2) // Different keys should produce different encrypted text
        assertEquals(message, EncryptionUtils.decryptWithKey(encrypted1, key1))
        assertEquals(message, EncryptionUtils.decryptWithKey(encrypted2, key2))
    }

    @Test
    fun `encryption should handle empty string`() {
        // Given
        val message = ""
        val key = "test_key"
        
        // When
        val encrypted = EncryptionUtils.encryptWithKey(message, key)
        val decrypted = EncryptionUtils.decryptWithKey(encrypted, key)
        
        // Then
        assertEquals(message, decrypted)
    }

    @Test
    fun `encryption should handle special characters`() {
        // Given
        val message = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?"
        val key = "test_key"
        
        // When
        val encrypted = EncryptionUtils.encryptWithKey(message, key)
        val decrypted = EncryptionUtils.decryptWithKey(encrypted, key)
        
        // Then
        assertEquals(message, decrypted)
    }

    @Test
    fun `encryption should handle unicode characters`() {
        // Given
        val message = "Unicode: ñáéíóú, 中文, العربية, 🚀💰"
        val key = "test_key"
        
        // When
        val encrypted = EncryptionUtils.encryptWithKey(message, key)
        val decrypted = EncryptionUtils.decryptWithKey(encrypted, key)
        
        // Then
        assertEquals(message, decrypted)
    }

    @Test
    fun `encryption should handle JSON strings`() {
        // Given
        val jsonMessage = """
            {
                "packageName": "com.bcp.innovacxion.yapeapp",
                "title": "Yape",
                "text": "Recibiste S/ 25.50",
                "bigText": "Recibiste S/ 25.50 de Juan Pérez\nComercio: Mi Tienda",
                "timestamp": 1758866622000,
                "notificationId": 12345
            }
        """.trimIndent()
        val key = "device_fingerprint_123"
        
        // When
        val encrypted = EncryptionUtils.encryptWithKey(jsonMessage, key)
        val decrypted = EncryptionUtils.decryptWithKey(encrypted, key)
        
        // Then
        assertEquals(jsonMessage, decrypted)
        
        // Verify it's still valid JSON
        val json = Json { ignoreUnknownKeys = true }
        val parsed = json.decodeFromString<Map<String, Any>>(decrypted)
        assertEquals("com.bcp.innovacxion.yapeapp", parsed["packageName"])
        assertEquals("Yape", parsed["title"])
    }

    @Test
    fun `encryption should handle very long strings`() {
        // Given
        val longMessage = "A".repeat(10000) // 10KB
        val key = "test_key"
        
        // When
        val encrypted = EncryptionUtils.encryptWithKey(longMessage, key)
        val decrypted = EncryptionUtils.decryptWithKey(encrypted, key)
        
        // Then
        assertEquals(longMessage, decrypted)
    }

    @Test
    fun `encryption should handle very short key`() {
        // Given
        val message = "Test message"
        val shortKey = "a" // Single character key
        
        // When
        val encrypted = EncryptionUtils.encryptWithKey(message, shortKey)
        val decrypted = EncryptionUtils.decryptWithKey(encrypted, shortKey)
        
        // Then
        assertEquals(message, decrypted)
    }

    @Test
    fun `encryption should handle key longer than message`() {
        // Given
        val message = "Hi"
        val longKey = "very_long_key_that_is_longer_than_the_message"
        
        // When
        val encrypted = EncryptionUtils.encryptWithKey(message, longKey)
        val decrypted = EncryptionUtils.decryptWithKey(encrypted, longKey)
        
        // Then
        assertEquals(message, decrypted)
    }

    @Test
    fun `generateDeviceFingerprint should return consistent format`() {
        // When
        val fingerprint1 = EncryptionUtils.generateDeviceFingerprint()
        val fingerprint2 = EncryptionUtils.generateDeviceFingerprint()
        
        // Then
        assertNotNull(fingerprint1)
        assertNotNull(fingerprint2)
        assertTrue(fingerprint1.length == 16) // Should be 16 characters
        assertTrue(fingerprint2.length == 16)
        // Note: These should be different since they're generated with different timestamps
    }

    @Test
    fun `generateUniqueNotificationFingerprint should return unique values`() {
        // When
        val fingerprint1 = EncryptionUtils.generateUniqueNotificationFingerprint()
        val fingerprint2 = EncryptionUtils.generateUniqueNotificationFingerprint()
        
        // Then
        assertNotNull(fingerprint1)
        assertNotNull(fingerprint2)
        assertTrue(fingerprint1.length == 16)
        assertTrue(fingerprint2.length == 16)
        assertTrue(fingerprint1 != fingerprint2) // Should be different
    }

    @Test
    fun `encryption should be deterministic with same input`() {
        // Given
        val message = "Deterministic test"
        val key = "test_key"
        
        // When
        val encrypted1 = EncryptionUtils.encryptWithKey(message, key)
        val encrypted2 = EncryptionUtils.encryptWithKey(message, key)
        
        // Then
        assertEquals(encrypted1, encrypted2) // Should be the same
    }

    @Test
    fun `decryption with wrong key should produce different result`() {
        // Given
        val message = "Secret message"
        val correctKey = "correct_key"
        val wrongKey = "wrong_key"
        
        // When
        val encrypted = EncryptionUtils.encryptWithKey(message, correctKey)
        val decryptedWithWrongKey = EncryptionUtils.decryptWithKey(encrypted, wrongKey)
        
        // Then
        assertTrue(decryptedWithWrongKey != message) // Should be different
    }
}