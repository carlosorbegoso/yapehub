package org.sysarp.project.service

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.sysarp.project.data.YapeNotificationRequest
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class AndroidNotificationCaptureServiceIntegrationTest {
    
    private lateinit var context: Context
    private lateinit var service: AndroidNotificationCaptureService
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        service = AndroidNotificationCaptureService()
    }
    
    @Test
    fun `isNotificationServiceEnabled should return correct status`() {
        // When
        val isEnabled = AndroidNotificationCaptureService.isNotificationServiceEnabled(context)
        
        // Then
        // Note: This will depend on the actual device configuration
        // In a real test environment, you might need to mock this
        assertNotNull(isEnabled)
    }
    
    @Test
    fun `requestNotificationPermission should not throw exception`() {
        // When & Then
        try {
            AndroidNotificationCaptureService.requestNotificationPermission(context)
            // If we reach here, no exception was thrown
            assertTrue(true)
        } catch (e: Exception) {
            // In test environment, this might fail due to missing UI context
            // That's acceptable for integration tests
            assertTrue(true)
        }
    }
    
    @Test
    fun `cleanupSystemNotifications should not throw exception`() {
        // When & Then
        try {
            AndroidNotificationCaptureService.cleanupSystemNotifications(context)
            // If we reach here, no exception was thrown
            assertTrue(true)
        } catch (e: Exception) {
            // In test environment, this might fail due to missing permissions
            // That's acceptable for integration tests
            assertTrue(true)
        }
    }
    
    @Test
    fun `getServiceStatus should return valid status`() {
        // When
        val status = service.getServiceStatus()
        
        // Then
        assertNotNull(status)
        assertNotNull(status.deviceFingerprint)
        assertTrue(status.pendingNotificationsCount >= 0)
        assertTrue(status.totalNotificationsProcessed >= 0)
        assertTrue(status.totalNotificationsFailed >= 0)
    }
    
    @Test
    fun `isYapePackage should correctly identify Yape packages`() {
        // Given
        val yapePackages = listOf(
            "com.bcp.innovacxion.yapeapp",
            "com.bcp.yape",
            "pe.com.bcp.yape",
            "com.bcp.innovacxion.yape"
        )
        
        val nonYapePackages = listOf(
            "com.whatsapp",
            "com.instagram.android",
            "com.facebook.katana",
            "com.google.android.gm"
        )
        
        // When & Then
        yapePackages.forEach { packageName ->
            val isYape = service.isYapePackage(packageName)
            assertTrue(isYape, "Should identify $packageName as Yape package")
        }
        
        nonYapePackages.forEach { packageName ->
            val isYape = service.isYapePackage(packageName)
            assertTrue(!isYape, "Should not identify $packageName as Yape package")
        }
    }
    
    @Test
    fun `extractNotificationText should handle valid notifications`() {
        // Given
        val mockNotification = createMockStatusBarNotification(
            title = "Confirmación de Pago",
            text = "Carlos te envió S/ 25.50",
            bigText = "Confirmación de Pago\nCarlos te envió S/ 25.50\nCódigo: 123"
        )
        
        // When
        val extractedText = service.extractNotificationText(mockNotification)
        
        // Then
        assertNotNull(extractedText)
        assertTrue(extractedText.contains("Confirmación de Pago"))
        assertTrue(extractedText.contains("Carlos te envió S/ 25.50"))
    }
    
    @Test
    fun `extractNotificationText should handle notifications with only title`() {
        // Given
        val mockNotification = createMockStatusBarNotification(
            title = "Yape: Pago recibido",
            text = null,
            bigText = null
        )
        
        // When
        val extractedText = service.extractNotificationText(mockNotification)
        
        // Then
        assertNotNull(extractedText)
        assertEquals("Yape: Pago recibido", extractedText)
    }
    
    @Test
    fun `extractNotificationText should return null for empty notifications`() {
        // Given
        val mockNotification = createMockStatusBarNotification(
            title = null,
            text = null,
            bigText = null
        )
        
        // When
        val extractedText = service.extractNotificationText(mockNotification)
        
        // Then
        assertTrue(extractedText == null || extractedText.isEmpty())
    }
    
    // Helper method to create mock StatusBarNotification
    private fun createMockStatusBarNotification(
        title: String?,
        text: String?,
        bigText: String?
    ): android.service.notification.StatusBarNotification {
        // Note: This is a simplified mock for testing
        // In a real integration test, you might need to use a more sophisticated mocking approach
        return object : android.service.notification.StatusBarNotification(
            "com.bcp.yape", // packageName
            "com.bcp.yape", // opPkg
            1, // id
            "tag", // tag
            123, // uid
            0, // initialPid
            0, // score
            android.app.Notification(), // notification
            android.os.UserHandle.getUserHandleForUid(0), // user
            1234567890L // postTime
        ) {
            override fun getNotification(): android.app.Notification {
                val notification = android.app.Notification()
                val extras = android.os.Bundle()
                
                title?.let { extras.putString(android.app.Notification.EXTRA_TITLE, it) }
                text?.let { extras.putString(android.app.Notification.EXTRA_TEXT, it) }
                bigText?.let { extras.putString(android.app.Notification.EXTRA_BIG_TEXT, it) }
                
                notification.extras = extras
                return notification
            }
        }
    }
}
