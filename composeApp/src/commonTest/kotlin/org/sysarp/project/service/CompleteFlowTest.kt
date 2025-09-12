package org.sysarp.project.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CompleteFlowTest {
    
    @Test
    fun testCompleteNotificationProcessingFlow() {
        // Given - Notificación real del usuario
        val notificationText = "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 0.1. El cód. de seguridad es: 031"
        
        // When - Procesar la notificación completa
        val isYapeNotification = YapeNotificationParser.isYapeNotification(notificationText)
        val transaction = YapeNotificationParser.parseYapeNotification(
            notificationText = notificationText,
            businessName = "Negocio Principal"
        )
        
        // Then - Verificar que todo el flujo funcionó
        assertTrue(isYapeNotification, "Should detect as Yape notification")
        assertNotNull(transaction, "Should parse transaction successfully")
        
        // Verificar datos de la transacción
        assertEquals(0.1, transaction.amount)
        assertEquals("PEN", transaction.currency)
        assertEquals("Carlos Orbegoso L.", transaction.senderName)
        assertEquals("031", transaction.securityCode)
        assertEquals("Negocio Principal", transaction.businessName)
        assertEquals(notificationText, transaction.rawNotification)
    }
    
    @Test
    fun testMultipleNotificationTypes() {
        // Given - Diferentes tipos de notificaciones
        val notifications = listOf(
            "Confirmación de Pago Carlos te envió un pago por S/ 10.00. El cód. de seguridad es: 123",
            "Confirmación de Pago Enviaste S/ 20.00 a María. El cód. de seguridad es: 456",
            "Confirmación de Pago Juan te envió un pago por S/ 1,250.00. El cód. de seguridad es: 789",
            "Confirmación de Pago Ana te envió un pago por S/ 0.99. El cód. de seguridad es: 111"
        )
        
        // When & Then - Procesar cada notificación
        notifications.forEach { notificationText ->
            val isYapeNotification = YapeNotificationParser.isYapeNotification(notificationText)
            val transaction = YapeNotificationParser.parseYapeNotification(
                notificationText = notificationText,
                businessName = "Test Business"
            )
            
            assertTrue(isYapeNotification, "Should detect as Yape notification: $notificationText")
            assertNotNull(transaction, "Should parse transaction: $notificationText")
            assertTrue(transaction.amount > 0, "Amount should be positive")
            assertEquals("PEN", transaction.currency, "Currency should be PEN")
            assertNotNull(transaction.securityCode, "Security code should not be null")
            assertTrue(transaction.securityCode!!.length >= 3, "Security code should be at least 3 digits")
        }
    }
    
    @Test
    fun testInvalidNotificationHandling() {
        // Given - Notificaciones inválidas
        val invalidNotifications = listOf(
            "WhatsApp: Nuevo mensaje",
            "Confirmación de Pago Carlos te envió un pago por S/ 10.00", // Sin código
            "WhatsApp: Carlos S/ 10.00 código 123", // De otra app
            "Confirmación de Pago Carlos te envió un pago por S/ ABC. El cód. de seguridad es: 123" // Monto inválido
        )
        
        // When & Then - Verificar que se rechazan correctamente
        invalidNotifications.forEach { notificationText ->
            val isYapeNotification = YapeNotificationParser.isYapeNotification(notificationText)
            val transaction = YapeNotificationParser.parseYapeNotification(
                notificationText = notificationText,
                businessName = "Test Business"
            )
            
            // Al menos uno de los dos debería fallar
            assertTrue(
                !isYapeNotification || transaction == null,
                "Should reject invalid notification: $notificationText"
            )
        }
    }
    
    @Test
    fun testEdgeCases() {
        // Given - Casos límite que SÍ deberían funcionar
        val edgeCases = listOf(
            "Confirmación de Pago   Carlos   te envió   un pago por   S/   10.50   .   El cód. de seguridad es:   123",
            "Confirmación de Pago\nMaría\nte envió\nun pago por\nS/ 20.00\nEl cód. de seguridad es: 456",
            "Confirmación de Pago\tJuan\tte envió\tun pago por\tS/ 30.00\tEl cód. de seguridad es: 789",
            "Confirmación de Pago José María te envió un pago por S/ 15.75. El cód. de seguridad es: 111",
            "Confirmación de Pago Luis te envió un pago por S/ 1,500.50. El cód. de seguridad es: 222"
        )
        
        // When & Then - Verificar que se procesan correctamente
        edgeCases.forEach { notificationText ->
            val isYapeNotification = YapeNotificationParser.isYapeNotification(notificationText)
            val transaction = YapeNotificationParser.parseYapeNotification(
                notificationText = notificationText,
                businessName = "Test Business"
            )
            
            assertTrue(isYapeNotification, "Should detect as Yape notification: $notificationText")
            assertNotNull(transaction, "Should parse edge case: $notificationText")
            assertTrue(transaction.amount > 0, "Amount should be positive")
            assertEquals("PEN", transaction.currency, "Currency should be PEN")
            assertNotNull(transaction.securityCode, "Security code should not be null")
            assertTrue(transaction.securityCode!!.length >= 3, "Security code should be at least 3 digits")
        }
    }
    
    @Test
    fun testDifferentSecurityCodeLengths() {
        // Given - Notificaciones con códigos de seguridad de diferentes longitudes
        val notifications = listOf(
            "Confirmación de Pago Carlos te envió un pago por S/ 10.00. El cód. de seguridad es: 123", // 3 dígitos
            "Confirmación de Pago María te envió un pago por S/ 20.00. El cód. de seguridad es: 1234", // 4 dígitos
            "Confirmación de Pago Juan te envió un pago por S/ 30.00. El cód. de seguridad es: 12345", // 5 dígitos
            "Confirmación de Pago Ana te envió un pago por S/ 40.00. El cód. de seguridad es: 123456" // 6 dígitos
        )
        
        // When & Then - Verificar que se procesan correctamente
        notifications.forEach { notificationText ->
            val isYapeNotification = YapeNotificationParser.isYapeNotification(notificationText)
            val transaction = YapeNotificationParser.parseYapeNotification(
                notificationText = notificationText,
                businessName = "Test Business"
            )
            
            assertTrue(isYapeNotification, "Should detect as Yape notification: $notificationText")
            assertNotNull(transaction, "Should parse transaction: $notificationText")
            assertTrue(transaction.amount > 0, "Amount should be positive")
            assertEquals("PEN", transaction.currency, "Currency should be PEN")
            assertNotNull(transaction.securityCode, "Security code should not be null")
            assertTrue(transaction.securityCode!!.length >= 3, "Security code should be at least 3 digits")
        }
    }
    
    @Test
    fun testRealWorldScenarios() {
        // Given - Escenarios del mundo real
        val realWorldCases = listOf(
            "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 0.1. El cód. de seguridad es: 031",
            "Confirmación de Pago María García te envió un pago por S/ 50.00. El cód. de seguridad es: 123",
            "Confirmación de Pago Enviaste S/ 25.00 a Juan Pérez. El cód. de seguridad es: 456",
            "Confirmación de Pago Ana López te envió un pago por S/ 100.50. El cód. de seguridad es: 789",
            "Confirmación de Pago Luis te envió un pago por S/ 999.99. El cód. de seguridad es: 111"
        )
        
        // When & Then - Verificar que se procesan correctamente
        realWorldCases.forEach { notificationText ->
            val isYapeNotification = YapeNotificationParser.isYapeNotification(notificationText)
            val transaction = YapeNotificationParser.parseYapeNotification(
                notificationText = notificationText,
                businessName = "Negocio Principal"
            )
            
            assertTrue(isYapeNotification, "Should detect as Yape notification: $notificationText")
            assertNotNull(transaction, "Should parse real-world case: $notificationText")
            assertTrue(transaction.amount > 0, "Amount should be positive")
            assertEquals("PEN", transaction.currency, "Currency should be PEN")
            assertNotNull(transaction.securityCode, "Security code should not be null")
            assertTrue(transaction.securityCode!!.length >= 3, "Security code should be at least 3 digits")
            assertEquals("Negocio Principal", transaction.businessName, "Business name should match")
        }
    }
    
    @Test
    fun testPerformanceWithMultipleNotifications() {
        // Given - Muchas notificaciones para probar rendimiento
        val notifications = (1..100).map { i ->
            val amount = (1..1000).random() / 100.0
            val securityCode = (100..999).random()
            val senderName = listOf("Carlos", "María", "Juan", "Ana", "Luis", "Carmen", "Pedro", "Laura")[i % 8]
            
            "Confirmación de Pago $senderName te envió un pago por S/ $amount. El cód. de seguridad es: $securityCode"
        }
        
        // When - Procesar todas las notificaciones
        val results = notifications.map { notificationText ->
            val isYapeNotification = YapeNotificationParser.isYapeNotification(notificationText)
            val transaction = YapeNotificationParser.parseYapeNotification(
                notificationText = notificationText,
                businessName = "Test Business"
            )
            isYapeNotification && transaction != null
        }
        
        // Then - Verificar que todas se procesaron correctamente
        val successCount = results.count { it }
        assertEquals(100, successCount, "All notifications should be processed successfully")
    }
}
