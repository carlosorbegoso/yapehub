package org.sysarp.project.service.http

import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.sysarp.project.data.*

/**
 * Cliente API especializado para administradores
 */
class AdminApiClient : BaseApiClient() {
    
    /**
     * Obtener perfil de administrador
     */
    suspend fun getAdminProfile(userId: Int, token: String): Result<AdminProfileResponse> {
        return try {
            logInfo("ADMIN_API", "Obteniendo perfil de admin: $userId")
            
            val response = client.get("$baseUrl/api/admin/profile") {
                parameter("userId", userId)
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }
            
            if (response.status.isSuccess()) {
                val profileResponse = response.body<AdminProfileResponse>()
                logInfo("ADMIN_API", "Perfil de admin obtenido exitosamente")
                Result.success(profileResponse)
            } else {
                val errorMessage = "Error obteniendo perfil de admin: ${response.status}"
                logError("ADMIN_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("ADMIN_API", "Error obteniendo perfil de admin: ${e.message}")
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
            logInfo("ADMIN_API", "Actualizando perfil de admin: $userId")
            
            val response = client.put("$baseUrl/api/admin/profile") {
                parameter("userId", userId)
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                setBody(profileData)
            }
            
            if (response.status.isSuccess()) {
                val profileResponse = response.body<AdminProfileResponse>()
                logInfo("ADMIN_API", "Perfil de admin actualizado exitosamente")
                Result.success(profileResponse)
            } else {
                val errorMessage = "Error actualizando perfil de admin: ${response.status}"
                logError("ADMIN_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("ADMIN_API", "Error actualizando perfil de admin: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Listar vendedores con filtros
     */
    suspend fun getSellersWithFilters(
        adminId: Int,
        token: String,
        page: Int = 0,
        size: Int = 20,
        search: String? = null,
        status: String? = null,
        sortBy: String? = null,
        sortOrder: String? = null
    ): Result<SellersWithFiltersResponse> {
        return try {
            logInfo("ADMIN_API", "Obteniendo vendedores con filtros para admin: $adminId")
            
            val response = client.get("$baseUrl/api/admin/sellers") {
                parameter("adminId", adminId)
                parameter("page", page)
                parameter("size", size)
                search?.let { parameter("search", it) }
                status?.let { parameter("status", it) }
                sortBy?.let { parameter("sortBy", it) }
                sortOrder?.let { parameter("sortOrder", it) }
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }
            
            if (response.status.isSuccess()) {
                val sellersResponse = response.body<SellersWithFiltersResponse>()
                logInfo("ADMIN_API", "Vendedores con filtros obtenidos exitosamente")
                Result.success(sellersResponse)
            } else {
                val errorMessage = "Error obteniendo vendedores con filtros: ${response.status}"
                logError("ADMIN_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("ADMIN_API", "Error obteniendo vendedores con filtros: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar vendedor
     */
    suspend fun updateSeller(
        sellerId: Int,
        adminId: Int,
        token: String,
        sellerData: UpdateSellerRequest
    ): Result<UpdateSellerResponse> {
        return try {
            logInfo("ADMIN_API", "Actualizando vendedor: $sellerId por admin: $adminId")
            
            val response = client.put("$baseUrl/api/admin/sellers/$sellerId") {
                parameter("adminId", adminId)
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                setBody(sellerData)
            }
            
            if (response.status.isSuccess()) {
                val updateResponse = response.body<UpdateSellerResponse>()
                logInfo("ADMIN_API", "Vendedor actualizado exitosamente")
                Result.success(updateResponse)
            } else {
                val errorMessage = "Error actualizando vendedor: ${response.status}"
                logError("ADMIN_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("ADMIN_API", "Error actualizando vendedor: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Eliminar vendedor
     */
    suspend fun deleteSeller(
        sellerId: Int,
        adminId: Int,
        token: String
    ): Result<DeleteSellerResponse> {
        return try {
            logInfo("ADMIN_API", "Eliminando vendedor: $sellerId por admin: $adminId")
            
            val response = client.delete("$baseUrl/api/admin/sellers/$sellerId") {
                parameter("adminId", adminId)
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }
            
            if (response.status.isSuccess()) {
                val deleteResponse = response.body<DeleteSellerResponse>()
                logInfo("ADMIN_API", "Vendedor eliminado exitosamente")
                Result.success(deleteResponse)
            } else {
                val errorMessage = "Error eliminando vendedor: ${response.status}"
                logError("ADMIN_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("ADMIN_API", "Error eliminando vendedor: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtener estadísticas de notificaciones
     */
    suspend fun getNotificationStats(
        adminId: Int,
        token: String
    ): Result<NotificationStatsResponse> {
        return try {
            logInfo("ADMIN_API", "Obteniendo estadísticas de notificaciones para admin: $adminId")
            
            val response = client.get("$baseUrl/api/payments/notification-stats") {
                parameter("adminId", adminId)
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }
            
            if (response.status.isSuccess()) {
                val statsResponse = response.body<NotificationStatsResponse>()
                logInfo("ADMIN_API", "Estadísticas de notificaciones obtenidas exitosamente")
                Result.success(statsResponse)
            } else {
                val errorMessage = "Error obteniendo estadísticas de notificaciones: ${response.status}"
                logError("ADMIN_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("ADMIN_API", "Error obteniendo estadísticas de notificaciones: ${e.message}")
            Result.failure(e)
        }
    }
}
