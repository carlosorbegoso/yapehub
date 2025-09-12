package org.sysarp.project.service

import org.junit.Test
import org.junit.runner.RunWith
import org.sysarp.project.data.TransactionType
import org.sysarp.project.service.YapeNotificationParser
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class YapeNotificationParserTest {
    
    @Test
    fun `test parse received payment notification`() {
        // Given
        val notificationText = "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 0.1. El cód. de seguridad es: 608"
        
        // When
        val transaction = YapeNotificationParser.parseYapeNotification(
            notificationText = notificationText,
            businessName = "Test Business"
        )
        
        // Then
        assertNotNull(transaction)
        assertEquals(0.1, transaction.amount)
        assertEquals("PEN", transaction.currency)
        assertEquals("Carlos Orbegoso L.", transaction.senderName)
        assertEquals(TransactionType.RECEIVED, transaction.transactionType)
        assertEquals("Test Business", transaction.businessName)
        assertEquals("608", transaction.securityCode)
        assertEquals(notificationText, transaction.rawNotification)
    }
    
    @Test
    fun `test parse sent payment notification`() {
        // Given
        val notificationText = "Confirmación de Pago Enviaste S/ 50.00 a María García. El cód. de seguridad es: 789"
        
        // When
        val transaction = YapeNotificationParser.parseYapeNotification(
            notificationText = notificationText,
            businessName = "Test Business"
        )
        
        // Then
        assertNotNull(transaction)
        assertEquals(50.0, transaction.amount)
        assertEquals("PEN", transaction.currency)
        assertEquals("María García", transaction.senderName)
        assertEquals(TransactionType.SENT, transaction.transactionType)
        assertEquals("Test Business", transaction.businessName)
        assertEquals("789", transaction.securityCode)
    }
    
    @Test
    fun `test parse notification with different amounts`() {
        // Given
        val testCases = listOf(
            "Confirmación de Pago Juan Pérez te envió un pago por S/ 1.50. El cód. de seguridad es: 111" to 1.5,
            "Confirmación de Pago Ana López te envió un pago por S/ 100.00. El cód. de seguridad es: 222" to 100.0,
            "Confirmación de Pago Pedro Ruiz te envió un pago por S/ 0.01. El cód. de seguridad es: 333" to 0.01
        )
        
        testCases.forEach { (notification, expectedAmount) ->
            // When
            val transaction = YapeNotificationParser.parseYapeNotification(
                notificationText = notification,
                businessName = "Test Business"
            )
            
            // Then
            assertNotNull(transaction, "Failed to parse: $notification")
            assertEquals(expectedAmount, transaction.amount, "Amount mismatch for: $notification")
        }
    }
    
    @Test
    fun `test parse notification with different names`() {
        // Given
        val testCases = listOf(
            "Confirmación de Pago María José García te envió un pago por S/ 10.00. El cód. de seguridad es: 444" to "María José García",
            "Confirmación de Pago J. Carlos Mendoza te envió un pago por S/ 20.00. El cód. de seguridad es: 555" to "J. Carlos Mendoza",
            "Confirmación de Pago Ana te envió un pago por S/ 30.00. El cód. de seguridad es: 666" to "Ana"
        )
        
        testCases.forEach { (notification, expectedName) ->
            // When
            val transaction = YapeNotificationParser.parseYapeNotification(
                notificationText = notification,
                businessName = "Test Business"
            )
            
            // Then
            assertNotNull(transaction, "Failed to parse: $notification")
            assertEquals(expectedName, transaction.senderName, "Name mismatch for: $notification")
        }
    }
    
    @Test
    fun `test invalid notification should return null`() {
        // Given
        val invalidNotifications = listOf(
            "This is not a Yape notification",
            "WhatsApp message",
            "Confirmación de Pago", // Incomplete
            "Te envió un pago por S/ 10.00", // Missing sender name
            "" // Empty
        )
        
        invalidNotifications.forEach { notification ->
            // When
            val transaction = YapeNotificationParser.parseYapeNotification(
                notificationText = notification,
                businessName = "Test Business"
            )
            
            // Then
            assertNull(transaction, "Should return null for invalid notification: $notification")
        }
    }
    
    @Test
    fun `test is yape notification detection`() {
        // Given
        val validNotifications = listOf(
            "Confirmación de Pago Carlos te envió un pago por S/ 10.00. El cód. de seguridad es: 123",
            "Confirmación de Pago Enviaste S/ 20.00 a María. El cód. de seguridad es: 456"
        )
        
        val invalidNotifications = listOf(
            "WhatsApp message",
            "SMS notification",
            "Email notification",
            "Other app notification"
        )
        
        // When & Then
        validNotifications.forEach { notification ->
            assertTrue(
                YapeNotificationParser.isYapeNotification(notification),
                "Should detect as Yape notification: $notification"
            )
        }
        
        invalidNotifications.forEach { notification ->
            assertTrue(
                !YapeNotificationParser.isYapeNotification(notification),
                "Should not detect as Yape notification: $notification"
            )
        }
    }
}
