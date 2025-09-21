package org.sysarp.project.service

import org.sysarp.project.data.YapeNotificationApiResponse
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.http.NotificationApiClient

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
            
            // Obtener token de autenticación
            val token = authService.accessToken.value
            if (token.isNullOrBlank()) {
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
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
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
            
            val token = authService.accessToken.value
            if (token.isNullOrBlank()) {
                return Result.failure(Exception("Token de autenticación no disponible"))
            }
            
            val result = notificationApiClient.getSellerNotifications(token, sellerId, page, limit)
            
            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
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
            
            val token = authService.accessToken.value
            if (token.isNullOrBlank()) {
                return Result.failure(Exception("Token de autenticación no disponible"))
            }
            
            val result = notificationApiClient.markNotificationAsRead(notificationId, token)
            
            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
