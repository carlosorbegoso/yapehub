package org.sysarp.project.service.admin

import org.sysarp.project.data.AdminProfileData
import org.sysarp.project.data.UpdateAdminProfileRequest
import org.sysarp.project.service.http.AdminProfileApiClient
import org.sysarp.project.utils.Logger

class AdminService(
    private val adminProfileApiClient: AdminProfileApiClient
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

            val result = adminProfileApiClient.getAdminProfile(userId, token)

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

            val result = adminProfileApiClient.updateAdminProfile(userId, token, profileData)

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
}