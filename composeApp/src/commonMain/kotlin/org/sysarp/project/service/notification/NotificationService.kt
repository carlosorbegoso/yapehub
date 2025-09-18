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

    /**
     * Obtener notificaciones de un vendedor
     */
    suspend fun getSellerNotifications(
        sellerId: Int,
        page: Int = 0,
        limit: Int = 20
    ): Result<org.sysarp.project.data.SellerNotificationsResponse> {
        return try {
            Logger.auth("NOTIFICATION_SERVICE", "Obteniendo notificaciones del vendedor: $sellerId, página: $page")
            
            val token = authService.accessToken.value
            if (token.isNullOrBlank()) {
                Logger.auth("NOTIFICATION_SERVICE", "❌ No hay token de autenticación disponible")
                return Result.failure(Exception("Token de autenticación no disponible"))
            }
            
            val result = notificationApiClient.getSellerNotifications(token, sellerId, page, limit)
            
            result.fold(
                onSuccess = { response ->
                    Logger.auth("NOTIFICATION_SERVICE", "✅ Notificaciones obtenidas: ${response.data?.notifications?.size ?: 0} notificaciones")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("NOTIFICATION_SERVICE", "❌ Error obteniendo notificaciones: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("NOTIFICATION_SERVICE", "💥 Excepción obteniendo notificaciones: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Marcar notificación como leída
     */
    suspend fun markNotificationAsRead(
        notificationId: Int
    ): Result<org.sysarp.project.data.MarkNotificationReadResponse> {
        return try {
            Logger.auth("NOTIFICATION_SERVICE", "Marcando notificación como leída: $notificationId")
            
            val token = authService.accessToken.value
            if (token.isNullOrBlank()) {
                Logger.auth("NOTIFICATION_SERVICE", "❌ No hay token de autenticación disponible")
                return Result.failure(Exception("Token de autenticación no disponible"))
            }
            
            val result = notificationApiClient.markNotificationAsRead(notificationId, token)
            
            result.fold(
                onSuccess = { response ->
                    Logger.auth("NOTIFICATION_SERVICE", "✅ Notificación marcada como leída exitosamente")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("NOTIFICATION_SERVICE", "❌ Error marcando notificación como leída: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("NOTIFICATION_SERVICE", "💥 Excepción marcando notificación como leída: ${e.message}")
            Result.failure(e)
        }
    }

}
