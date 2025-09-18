package org.sysarp.project.service.admin

import org.sysarp.project.data.*
import org.sysarp.project.service.http.AdminApiClient
import org.sysarp.project.utils.Logger

class AdminService(
    private val adminApiClient: AdminApiClient
) {
    
    /**
     * Obtener perfil de administrador
     */
    suspend fun getAdminProfile(
        userId: Int,
        token: String
    ): Result<AdminProfileData> {
        return try {
            Logger.auth("ADMIN_SERVICE", "Obteniendo perfil de admin: $userId")

            val result = adminApiClient.getAdminProfile(userId, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("ADMIN_SERVICE", "Perfil de admin obtenido exitosamente")
                    Result.success(response.data!!)
                },
                onFailure = { error ->
                    Logger.auth("ADMIN_SERVICE", "Error obteniendo perfil de admin: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("ADMIN_SERVICE", "Error obteniendo perfil de admin: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar perfil de administrador
     */
    suspend fun updateAdminProfile(
        userId: Int,
        token: String,
        profileData: UpdateAdminProfileRequest
    ): Result<AdminProfileData> {
        return try {
            Logger.auth("ADMIN_SERVICE", "Actualizando perfil de admin: $userId")

            val result = adminApiClient.updateAdminProfile(userId, token, profileData)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("ADMIN_SERVICE", "Perfil de admin actualizado exitosamente")
                    Result.success(response.data!!)
                },
                onFailure = { error ->
                    Logger.auth("ADMIN_SERVICE", "Error actualizando perfil de admin: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("ADMIN_SERVICE", "Error actualizando perfil de admin: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtener estadísticas de notificaciones
     * TODO: Implementar cuando se necesite en la UI
     */
    suspend fun getNotificationStats(
        adminId: Int,
        token: String
    ): Result<NotificationStatsData> {
        return try {
            Logger.auth("ADMIN_SERVICE", "Obteniendo estadísticas de notificaciones para admin: $adminId")

            val result = adminApiClient.getNotificationStats(adminId, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("ADMIN_SERVICE", "Estadísticas de notificaciones obtenidas exitosamente")
                    Result.success(response.data!!)
                },
                onFailure = { error ->
                    Logger.auth("ADMIN_SERVICE", "Error obteniendo estadísticas de notificaciones: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("ADMIN_SERVICE", "Error obteniendo estadísticas de notificaciones: ${e.message}")
            Result.failure(e)
        }
    }
}