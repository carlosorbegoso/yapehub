package org.sysarp.project.service.admin

import org.sysarp.project.data.AdminProfileData
import org.sysarp.project.data.UpdateAdminProfileRequest
import org.sysarp.project.service.http.AdminProfileApiClient

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

            val result = adminProfileApiClient.getAdminProfile(userId, token)

            result.fold(
                onSuccess = { response ->
                    Result.success(response.data!!)
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
     * Actualizar perfil de administrador
     */
    suspend fun updateAdminProfile(
        userId: Int,
        token: String,
        profileData: UpdateAdminProfileRequest
    ): Result<AdminProfileData> {
        return try {

            val result = adminProfileApiClient.updateAdminProfile(userId, token, profileData)

            result.fold(
                onSuccess = { response ->
                    Result.success(response.data!!)
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