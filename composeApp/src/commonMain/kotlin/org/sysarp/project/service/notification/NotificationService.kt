package org.sysarp.project.service

import org.sysarp.project.data.YapeNotificationApiResponse
import org.sysarp.project.service.http.NotificationApiClient
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.utils.Logger

/**
 * Servicio especializado para manejar notificaciones de Yape
 */
class NotificationService(
    private val notificationApiClient: NotificationApiClient,
    private val authService: AuthService
) {
    
    /**
     * Enviar notificación de Yape a la API
     */
    suspend fun sendYapeNotification(
        adminId: Int,
        encryptedNotification: String,
        deviceFingerprint: String,
        timestamp: Long
    ): Result<YapeNotificationApiResponse> {
        return try {
            Logger.auth("NOTIFICATION_SERVICE", "🚀 Enviando notificación real a la API")
            Logger.auth("NOTIFICATION_SERVICE", "📱 AdminId: $adminId")
            Logger.auth("NOTIFICATION_SERVICE", "📄 Notification: ${encryptedNotification.take(50)}...")
            Logger.auth("NOTIFICATION_SERVICE", "🔑 DeviceFingerprint: ${deviceFingerprint.take(20)}...")
            Logger.auth("NOTIFICATION_SERVICE", "⏰ Timestamp: $timestamp")
            
            // Obtener token de autenticación
            val token = authService.accessToken.value
            if (token.isNullOrBlank()) {
                Logger.auth("NOTIFICATION_SERVICE", "❌ No hay token de autenticación disponible")
                return Result.failure(Exception("Token de autenticación no disponible"))
            }
            
            // Hacer llamada real a la API
            val result = notificationApiClient.sendYapeNotification(
                adminId = adminId,
                encryptedNotification = encryptedNotification,
                deviceFingerprint = deviceFingerprint,
                timestamp = timestamp,
                accessToken = token
            )
            
            result.fold(
                onSuccess = { response ->
                    Logger.auth("NOTIFICATION_SERVICE", "✅ Notificación enviada exitosamente: ${response.message}")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("NOTIFICATION_SERVICE", "❌ Error enviando notificación: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("NOTIFICATION_SERVICE", "💥 Excepción enviando notificación: ${e.message}")
            Result.failure(e)
        }
    }

}
