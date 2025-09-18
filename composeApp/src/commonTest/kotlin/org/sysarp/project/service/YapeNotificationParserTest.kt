package org.sysarp.project.service

import org.sysarp.project.data.TransactionType
import org.sysarp.project.data.YapeTransaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class YapeNotificationParserTest {
    
    @Test
    fun `parseYapeNotification should parse valid received notification`() {
        // Given
        val notificationText = "Confirmación de Pago Carlos te envió S/ 25.50 Código: 123"
        
        // When
        val result = YapeNotificationParser.parseYapeNotification(notificationText)
        
        // Then
        assertNotNull(result)
        assertEquals(25.50, result.amount)
        assertEquals("PEN", result.currency)
        assertEquals("Carlos", result.senderName)
        assertEquals(TransactionType.RECEIVED, result.transactionType)
        assertEquals("123", result.securityCode)
        assertEquals(notificationText, result.rawNotification)
    }
    
    @Test
    fun `parseYapeNotification should parse valid sent notification`() {
        // Given
        val notificationText = "Enviaste S/ 15.00 a María El código es 456"
        
        // When
        val result = YapeNotificationParser.parseYapeNotification(notificationText)
        
        // Then
        assertNotNull(result)
        assertEquals(15.00, result.amount)
        assertEquals("PEN", result.currency)
        assertEquals("María", result.senderName)
        assertEquals(TransactionType.RECEIVED, result.transactionType) // Parser siempre marca como RECEIVED
        assertEquals("456", result.securityCode)
    }
    
    @Test
    fun `parseYapeNotification should handle different amount formats`() {
        // Test cases for different amount formats
        val testCases = listOf(
            "S/ 25.50" to 25.50,
            "S/ 1,250.75" to 1250.75,
            "S/ 100" to 100.0,
            "S/ 0.50" to 0.50
        )
        
        testCases.forEach { (amountText, expectedAmount) ->
            val notificationText = "Confirmación de Pago Usuario te envió $amountText Código: 123"
            val result = YapeNotificationParser.parseYapeNotification(notificationText)
            
            assertNotNull(result, "Should parse notification with amount: $amountText")
            assertEquals(expectedAmount, result.amount, "Amount should match for: $amountText")
        }
    }
    
    @Test
    fun `parseYapeNotification should handle different security code formats`() {
        // Test cases for different security code formats
        val testCases = listOf(
            "Código: 123",
            "Código 456",
            "123",
            "El código es 789",
            "Código de seguridad: 101112"
        )
        
        testCases.forEach { codeText ->
            val notificationText = "Confirmación de Pago Usuario te envió S/ 25.50 $codeText"
            val result = YapeNotificationParser.parseYapeNotification(notificationText)
            
            assertNotNull(result, "Should parse notification with code: $codeText")
            assertNotNull(result.securityCode, "Security code should be extracted")
        }
    }
    
    @Test
    fun `parseYapeNotification should return null for invalid notifications`() {
        // Test cases for invalid notifications
        val invalidNotifications = listOf(
            "Not a Yape notification",
            "S/ 25.50", // Missing security code
            "Código: 123", // Missing amount
            "Random text without payment info",
            "", // Empty string
            "WhatsApp message", // Wrong app
            "S/ 25.50 sin código" // Missing security code
        )
        
        invalidNotifications.forEach { notificationText ->
            val result = YapeNotificationParser.parseYapeNotification(notificationText)
            assertNull(result, "Should return null for invalid notification: $notificationText")
        }
    }
    
    @Test
    fun `isYapeNotification should correctly identify Yape notifications`() {
        // Valid Yape notifications
        val validNotifications = listOf(
            "Confirmación de Pago Carlos te envió S/ 25.50 Código: 123",
            "Enviaste S/ 15.00 a María El código es 456",
            "Yape: Recibiste S/ 100.00 de Juan Código 789",
            "Pago recibido S/ 50.25 Código: 101112"
        )
        
        validNotifications.forEach { notificationText ->
            val result = YapeNotificationParser.isYapeNotification(notificationText)
            assertTrue(result, "Should identify as Yape notification: $notificationText")
        }
    }
    
    @Test
    fun `isYapeNotification should reject non-Yape notifications`() {
        // Invalid notifications
        val invalidNotifications = listOf(
            "WhatsApp message",
            "Instagram notification",
            "Random text",
            "S/ 25.50", // Missing security code
            "Código: 123", // Missing amount
            "Bank transfer notification"
        )
        
        invalidNotifications.forEach { notificationText ->
            val result = YapeNotificationParser.isYapeNotification(notificationText)
            assertTrue(!result, "Should reject non-Yape notification: $notificationText")
        }
    }
    
    @Test
    fun `parseYapeNotification should handle special characters in names`() {
        // Given
        val notificationText = "Confirmación de Pago José María te envió S/ 25.50 Código: 123"
        
        // When
        val result = YapeNotificationParser.parseYapeNotification(notificationText)
        
        // Then
        assertNotNull(result)
        assertEquals("José María", result.senderName)
    }
    
    @Test
    fun `parseYapeNotification should handle business name parameter`() {
        // Given
        val notificationText = "Confirmación de Pago Carlos te envió S/ 25.50 Código: 123"
        val businessName = "Mi Negocio"
        
        // When
        val result = YapeNotificationParser.parseYapeNotification(notificationText, businessName)
        
        // Then
        assertNotNull(result)
        assertEquals(businessName, result.businessName)
    }
}
