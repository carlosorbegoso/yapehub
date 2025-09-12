package org.sysarp.project

import org.sysarp.project.service.YapeNotificationParser
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test manual simple para verificar que el parser funciona
 * Este test SÍ se ejecuta y verifica la funcionalidad básica
 */
class ManualTest {
    
    @Test
    fun testYapeNotificationParsing() {
        // Given - Notificación real de Yape
        val yapeNotification = "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 25.50. El cód. de seguridad es: 123"
        
        // When - Parsear la notificación
        val isYape = YapeNotificationParser.isYapeNotification(yapeNotification)
        val transaction = YapeNotificationParser.parseYapeNotification(
            notificationText = yapeNotification,
            businessName = "Test Business"
        )
        
        // Then - Verificar que funciona
        assertTrue(isYape, "Should detect as Yape notification")
        assertNotNull(transaction, "Transaction should be parsed")
        
        println("✅ Test passed! Transaction parsed successfully:")
        println("   - Amount: ${transaction?.amount} ${transaction?.currency}")
        println("   - Sender: ${transaction?.senderName}")
        println("   - Security Code: ${transaction?.securityCode}")
        println("   - Business: ${transaction?.businessName}")
    }
    
    @Test
    fun testDifferentNotificationFormats() {
        val testNotifications = listOf(
            "Confirmación de Pago María García te envió un pago por S/ 10.00. El cód. de seguridad es: 456",
            "Confirmación de Pago Enviaste S/ 50.00 a Juan Pérez. El cód. de seguridad es: 789",
            "Confirmación de Pago Ana López te envió un pago por S/ 0.50. El cód. de seguridad es: 321"
        )
        
        testNotifications.forEach { notification ->
            val isYape = YapeNotificationParser.isYapeNotification(notification)
            val transaction = YapeNotificationParser.parseYapeNotification(
                notificationText = notification,
                businessName = "Test Business"
            )
            
            assertTrue(isYape, "Should detect as Yape: $notification")
            assertNotNull(transaction, "Should parse transaction: $notification")
            
            println("✅ Parsed: ${transaction?.senderName} - ${transaction?.amount} ${transaction?.currency}")
        }
    }
}
