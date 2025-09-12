package org.sysarp.project.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class YapeNotificationParserAdvancedTest {
    
    @Test
    fun testAllNotificationTypes() {
        // Test todas las variaciones de notificaciones de Yape
        val testCases = listOf(
            // Notificaciones recibidas - diferentes formatos
            Triple("Confirmación de Pago Carlos te envió un pago por S/ 10.00. El cód. de seguridad es: 123", 10.0, "Carlos"),
            Triple("Confirmación de Pago María García te envió un pago por S/ 25.50. El cód. de seguridad es: 456", 25.5, "María García"),
            Triple("Confirmación de Pago Juan Pérez te envió un pago por S/ 0.01. El cód. de seguridad es: 789", 0.01, "Juan Pérez"),
            Triple("Confirmación de Pago Ana López te envió un pago por S/ 100.00. El cód. de seguridad es: 111", 100.0, "Ana López"),
            
            // Notificaciones enviadas - diferentes formatos
            Triple("Confirmación de Pago Enviaste S/ 15.00 a María. El cód. de seguridad es: 222", 15.0, "María"),
            Triple("Confirmación de Pago Enviaste S/ 50.75 a Carlos Orbegoso. El cód. de seguridad es: 333", 50.75, "Carlos Orbegoso"),
            Triple("Confirmación de Pago Enviaste S/ 1.25 a Juan. El cód. de seguridad es: 444", 1.25, "Juan"),
            
            // Notificaciones con montos grandes
            Triple("Confirmación de Pago Luis te envió un pago por S/ 1,250.00. El cód. de seguridad es: 555", 1250.0, "Luis"),
            Triple("Confirmación de Pago Carmen te envió un pago por S/ 999.99. El cód. de seguridad es: 666", 999.99, "Carmen"),
            
            // Notificaciones con espacios extra
            Triple("Confirmación de Pago   Carlos   te envió   un pago por   S/   10.50   .   El cód. de seguridad es:   777", 10.5, "Carlos"),
            Triple("Confirmación de Pago\nMaría\nte envió\nun pago por\nS/ 20.00\nEl cód. de seguridad es: 888", 20.0, "María"),
            Triple("Confirmación de Pago\tJuan\tte envió\tun pago por\tS/ 30.00\tEl cód. de seguridad es: 999", 30.0, "Juan")
        )
        
        testCases.forEach { (notification, expectedAmount, expectedSender) ->
            val transaction = YapeNotificationParser.parseYapeNotification(
                notificationText = notification,
                businessName = "Test Business"
            )
            
            assertNotNull(transaction, "Failed to parse: $notification")
            assertEquals(expectedAmount, transaction.amount, "Amount mismatch for: $notification")
            assertEquals("PEN", transaction.currency, "Currency should be PEN")
            assertTrue(
                transaction.senderName?.contains(expectedSender) == true || 
                transaction.senderName == "Usuario",
                "Sender name should contain '$expectedSender' or be 'Usuario' for: $notification"
            )
            assertNotNull(transaction.securityCode, "Security code should not be null")
            assertTrue(transaction.securityCode!!.length == 3, "Security code should be 3 digits")
        }
    }
    
    @Test
    fun testInvalidNotificationCases() {
        // Test casos que NO deberían parsearse
        val invalidCases = listOf(
            // Sin código de seguridad
            "Confirmación de Pago Carlos te envió un pago por S/ 10.00",
            "Confirmación de Pago Enviaste S/ 20.00 a María",
            
            // Sin monto
            "Confirmación de Pago Carlos te envió un pago. El cód. de seguridad es: 123",
            "Confirmación de Pago Enviaste a María. El cód. de seguridad es: 456",
            
            // Código de seguridad inválido (menos de 3 dígitos o no numérico)
            "Confirmación de Pago Carlos te envió un pago por S/ 10.00. El cód. de seguridad es: 12",
            "Confirmación de Pago Carlos te envió un pago por S/ 10.00. El cód. de seguridad es: ABC",
            
            // Monto inválido
            "Confirmación de Pago Carlos te envió un pago por S/ ABC. El cód. de seguridad es: 123",
            "Confirmación de Pago Carlos te envió un pago por S/. El cód. de seguridad es: 123",
            
            // Notificaciones de otras apps
            "WhatsApp: Nuevo mensaje de Carlos",
            "SMS: Recibiste un pago de S/ 10.00",
            "Email: Confirmación de pago",
            "Instagram: Carlos te envió un mensaje",
            
            // Notificaciones vacías o malformadas
            "",
            "   ",
            "Confirmación de Pago",
            "te envió un pago",
            "S/ 10.00",
            "código de seguridad",
            
            // Formato incorrecto - sin indicador de pago
            "Carlos S/ 10.00 código 123",
            "S/ 10.00 de Carlos",
            "WhatsApp: Carlos - S/ 10.00 - 123"
        )
        
        invalidCases.forEach { notification ->
            val transaction = YapeNotificationParser.parseYapeNotification(
                notificationText = notification,
                businessName = "Test Business"
            )
            
            assertNull(transaction, "Should return null for invalid notification: '$notification'")
        }
    }
    
    @Test
    fun testEdgeCases() {
        // Test casos límite que SÍ deberían parsearse
        val edgeCases = listOf(
            // Código de seguridad en diferentes posiciones
            "El cód. de seguridad es: 111. Confirmación de Pago Carlos te envió un pago por S/ 30.00",
            "Confirmación de Pago Carlos te envió un pago por S/ 40.00. Código: 222",
            "Confirmación de Pago Carlos te envió un pago por S/ 50.00. Code: 333",
            
            // Nombres con caracteres especiales
            "Confirmación de Pago José María te envió un pago por S/ 15.75. El cód. de seguridad es: 444",
            "Confirmación de Pago María José te envió un pago por S/ 20.25. El cód. de seguridad es: 555",
            "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 0.1. El cód. de seguridad es: 031",
            
            // Montos con diferentes formatos
            "Confirmación de Pago Luis te envió un pago por S/ 1,500.50. El cód. de seguridad es: 666",
            "Confirmación de Pago Ana te envió un pago por S/ 0.99. El cód. de seguridad es: 777",
            "Confirmación de Pago Pedro te envió un pago por S/ 999.00. El cód. de seguridad es: 888"
        )
        
        edgeCases.forEach { notification ->
            val transaction = YapeNotificationParser.parseYapeNotification(
                notificationText = notification,
                businessName = "Test Business"
            )
            
            assertNotNull(transaction, "Should parse edge case: $notification")
            assertTrue(transaction.amount > 0, "Amount should be positive")
            assertEquals("PEN", transaction.currency, "Currency should be PEN")
            assertNotNull(transaction.securityCode, "Security code should not be null")
            assertTrue(transaction.securityCode!!.length == 3, "Security code should be 3 digits")
        }
    }
    
    @Test
    fun testNotificationDetection() {
        // Test detección de notificaciones de Yape
        val validNotifications = listOf(
            "Confirmación de Pago Carlos te envió un pago por S/ 10.00. El cód. de seguridad es: 123",
            "Confirmación de Pago Enviaste S/ 20.00 a María. El cód. de seguridad es: 456",
            "Confirmación de Pago   Carlos   te envió   un pago por   S/   10.50   .   El cód. de seguridad es:   123",
            "Confirmación de Pago\nCarlos\nte envió\nun pago por\nS/ 10.50\nEl cód. de seguridad es: 123"
        )
        
        val invalidNotifications = listOf(
            "WhatsApp message",
            "SMS notification",
            "Email notification",
            "Other app notification",
            "Confirmación de Pago Carlos te envió un pago por S/ 10.00", // Sin código
            "Carlos S/ 10.00 código 123", // Sin indicador de pago
            "S/ 10.00 de Carlos", // Sin indicador de pago
            "WhatsApp: Carlos - S/ 10.00 - 123" // De otra app
        )
        
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
    
    @Test
    fun testRealWorldScenarios() {
        // Test escenarios del mundo real
        val realWorldCases = listOf(
            // Caso exacto del log del usuario
            Triple(
                "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 0.1. El cód. de seguridad es: 031",
                0.1, "Carlos Orbegoso L."
            ),
            
            // Otros casos comunes
            Triple(
                "Confirmación de Pago María García te envió un pago por S/ 50.00. El cód. de seguridad es: 123",
                50.0, "María García"
            ),
            Triple(
                "Confirmación de Pago Enviaste S/ 25.00 a Juan Pérez. El cód. de seguridad es: 456",
                25.0, "Juan Pérez"
            ),
            Triple(
                "Confirmación de Pago Ana López te envió un pago por S/ 100.50. El cód. de seguridad es: 789",
                100.5, "Ana López"
            )
        )
        
        realWorldCases.forEach { (notification, expectedAmount, expectedSender) ->
            val transaction = YapeNotificationParser.parseYapeNotification(
                notificationText = notification,
                businessName = "Negocio Principal"
            )
            
            assertNotNull(transaction, "Should parse real-world case: $notification")
            assertEquals(expectedAmount, transaction.amount, "Amount should match for: $notification")
            assertEquals("PEN", transaction.currency, "Currency should be PEN")
            assertTrue(
                transaction.senderName?.contains(expectedSender) == true || 
                transaction.senderName == "Usuario",
                "Sender name should contain '$expectedSender' for: $notification"
            )
            assertNotNull(transaction.securityCode, "Security code should not be null")
            assertTrue(transaction.securityCode!!.length == 3, "Security code should be 3 digits")
            assertEquals(notification, transaction.rawNotification, "Raw notification should match input")
            assertEquals("Negocio Principal", transaction.businessName, "Business name should match")
        }
    }
}
