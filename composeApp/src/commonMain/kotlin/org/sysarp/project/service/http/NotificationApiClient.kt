package org.sysarp.project.service.http

import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.sysarp.project.data.*

/**
 * Cliente API especializado para notificaciones
 */
class NotificationApiClient : BaseApiClient() {
    
    /**
     * Enviar notificación de Yape
     */
    suspend fun sendYapeNotification(
        adminId: Int,
        encryptedNotification: String,
        deviceFingerprint: String,
        timestamp: Long,
        accessToken: String
    ): Result<YapeNotificationApiResponse> {
        return try {
            logInfo("NOTIFICATION_API", "Enviando notificación de Yape: adminId=$adminId, timestamp=$timestamp")
            
            val response = client.post("$baseUrl/api/notifications/yape-notifications") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                setBody(YapeNotificationRequest(
                    adminId = adminId,
                    encryptedNotification = encryptedNotification,
                    deviceFingerprint = deviceFingerprint,
                    timestamp = timestamp
                ))
            }
            
            if (response.status.isSuccess()) {
                val notificationResponse = response.body<YapeNotificationApiResponse>()
                logInfo("NOTIFICATION_API", "Notificación de Yape enviada exitosamente")
                Result.success(notificationResponse)
            } else {
                val errorMessage = "Error enviando notificación de Yape: ${response.status}"
                logError("NOTIFICATION_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("NOTIFICATION_API", "Error enviando notificación de Yape: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtener notificaciones pendientes
     */
    suspend fun getPendingNotifications(
        sellerId: Int,
        accessToken: String
    ): Result<ApiResponse<List<PaymentNotification>>> {
        return try {
            logInfo("NOTIFICATION_API", "Obteniendo notificaciones pendientes para vendedor: $sellerId")
            
            val response = client.get("$baseUrl/api/notifications/pending") {
                header("Authorization", "Bearer $accessToken")
                parameter("sellerId", sellerId)
            }
            
            if (response.status.isSuccess()) {
                val notificationsResponse = response.body<ApiResponse<List<PaymentNotification>>>()
                logInfo("NOTIFICATION_API", "Notificaciones pendientes obtenidas exitosamente")
                Result.success(notificationsResponse)
            } else {
                val errorMessage = "Error obteniendo notificaciones pendientes: ${response.status}"
                logError("NOTIFICATION_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("NOTIFICATION_API", "Error obteniendo notificaciones pendientes: ${e.message}")
            Result.failure(e)
        }
    }
}

