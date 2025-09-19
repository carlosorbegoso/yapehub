package org.sysarp.project.service.http

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess
import org.sysarp.project.data.NotificationStatsResponse

/**
 * Cliente API especializado para estadísticas de administrador
 */
class AdminStatsApiClient : BaseApiClient() {
    
    /**
     * Obtener estadísticas de notificaciones
     */
    suspend fun getNotificationStats(
        adminId: Int,
        token: String
    ): Result<NotificationStatsResponse> {
        return try {
            logInfo("ADMIN_STATS_API", "Obteniendo estadísticas de notificaciones para admin: $adminId")
            
            val response = client.get("$baseUrl/api/payments/notification-stats") {
                parameter("adminId", adminId)
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }
            
            if (response.status.isSuccess()) {
                val statsResponse = response.body<NotificationStatsResponse>()
                logInfo("ADMIN_STATS_API", "Estadísticas de notificaciones obtenidas exitosamente")
                Result.success(statsResponse)
            } else {
                val errorMessage = "Error obteniendo estadísticas de notificaciones: ${response.status}"
                logError("ADMIN_STATS_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("ADMIN_STATS_API", "Error obteniendo estadísticas de notificaciones: ${e.message}")
            Result.failure(e)
        }
    }
}
