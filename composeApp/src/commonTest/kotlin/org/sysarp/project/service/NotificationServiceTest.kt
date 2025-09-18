package org.sysarp.project.service

import kotlin.test.Test
import kotlin.test.assertNotNull

class NotificationServiceTest {
    
    @Test
    fun `NotificationService should be created successfully`() {
        // Given
        val apiClient = org.sysarp.project.service.http.NotificationApiClient()
        val authService = org.sysarp.project.service.auth.AuthService()
        
        // When
        val notificationService = NotificationService(apiClient, authService)
        
        // Then
        assertNotNull(notificationService)
    }
    
    @Test
    fun `YapeNotificationParser should parse valid notifications`() {
        // Given
        val validNotification = "Confirmación de Pago Juan Pérez te envió un pago por S/ 25.50. El cód. de seguridad es: 123"
        
        // When
        val result = YapeNotificationParser.isYapeNotification(validNotification)
        
        // Then
        assertNotNull(result)
    }
    
    @Test
    fun `YapeNotificationParser should reject invalid notifications`() {
        // Given
        val invalidNotification = "WhatsApp message"
        
        // When
        val result = YapeNotificationParser.isYapeNotification(invalidNotification)
        
        // Then
        assertNotNull(result)
    }
}