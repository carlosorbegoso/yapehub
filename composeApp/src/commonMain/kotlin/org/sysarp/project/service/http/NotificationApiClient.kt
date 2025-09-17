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
    
    /**
     * Listar notificaciones del vendedor
     */
    suspend fun getSellerNotifications(
        token: String,
        page: Int = 0,
        size: Int = 20
    ): Result<SellerNotificationsResponse> {
        return try {
            logInfo("NOTIFICATION_API", "Obteniendo notificaciones del vendedor")
            
            val response = client.get("$baseUrl/api/notifications") {
                parameter("page", page)
                parameter("size", size)
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }
            
            if (response.status.isSuccess()) {
                val notificationsResponse = response.body<SellerNotificationsResponse>()
                logInfo("NOTIFICATION_API", "Notificaciones del vendedor obtenidas exitosamente")
                Result.success(notificationsResponse)
            } else {
                val errorMessage = "Error obteniendo notificaciones del vendedor: ${response.status}"
                logError("NOTIFICATION_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("NOTIFICATION_API", "Error obteniendo notificaciones del vendedor: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Marcar notificación como leída
     */
    suspend fun markNotificationAsRead(
        notificationId: Int,
        token: String
    ): Result<MarkNotificationReadResponse> {
        return try {
            logInfo("NOTIFICATION_API", "Marcando notificación como leída: $notificationId")
            
            val response = client.post("$baseUrl/api/notifications/$notificationId/read") {
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }
            
            if (response.status.isSuccess()) {
                val markResponse = response.body<MarkNotificationReadResponse>()
                logInfo("NOTIFICATION_API", "Notificación marcada como leída exitosamente")
                Result.success(markResponse)
            } else {
                val errorMessage = "Error marcando notificación como leída: ${response.status}"
                logError("NOTIFICATION_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("NOTIFICATION_API", "Error marcando notificación como leída: ${e.message}")
            Result.failure(e)
        }
    }
}

