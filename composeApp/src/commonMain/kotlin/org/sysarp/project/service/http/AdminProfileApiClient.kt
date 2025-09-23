package org.sysarp.project.service.http

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import org.sysarp.project.data.AdminProfileResponse
import org.sysarp.project.data.UpdateAdminProfileRequest

/**
 * Cliente API especializado para gestión de perfil de administrador
 */
class AdminProfileApiClient : BaseApiClient() {
    
    /**
     * Obtener perfil de administrador
     */
    suspend fun getAdminProfile(userId: Int, token: String): Result<AdminProfileResponse> {
        return try {
            logInfo("ADMIN_PROFILE_API", "Obteniendo perfil de admin: $userId")
            
            val response = client.get("$baseUrl/api/admin/profile") {
                parameter("userId", userId)
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }
            
            if (response.status.isSuccess()) {
                val profileResponse = response.body<AdminProfileResponse>()
                logInfo("ADMIN_PROFILE_API", "Perfil de admin obtenido exitosamente")
                Result.success(profileResponse)
            } else {
                val errorMessage = "Error obteniendo perfil de admin: ${response.status}"
                logError("ADMIN_PROFILE_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("ADMIN_PROFILE_API", "Error obteniendo perfil de admin: ${e.message}")
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
    ): Result<AdminProfileResponse> {
        return try {
            logInfo("ADMIN_PROFILE_API", "Actualizando perfil de admin: $userId")
            
            val response = client.put("$baseUrl/api/admin/profile") {
                parameter("userId", userId)
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                setBody(profileData)
            }
            
            if (response.status.isSuccess()) {
                val profileResponse = response.body<AdminProfileResponse>()
                logInfo("ADMIN_PROFILE_API", "Perfil de admin actualizado exitosamente")
                Result.success(profileResponse)
            } else {
                val errorMessage = "Error actualizando perfil de admin: ${response.status}"
                logError("ADMIN_PROFILE_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("ADMIN_PROFILE_API", "Error actualizando perfil de admin: ${e.message}")
            Result.failure(e)
        }
    }
}
