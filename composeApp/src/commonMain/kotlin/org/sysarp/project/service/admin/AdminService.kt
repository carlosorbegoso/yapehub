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
    ): Result<SellersWithFiltersData> {
        return try {
            Logger.auth("ADMIN_SERVICE", "Obteniendo vendedores con filtros para admin: $adminId")

            val result = adminApiClient.getSellersWithFilters(
                adminId, token, page, size, search, status, sortBy, sortOrder
            )

            result.fold(
                onSuccess = { response ->
                    Logger.auth("ADMIN_SERVICE", "Vendedores con filtros obtenidos exitosamente")
                    Result.success(response.data!!)
                },
                onFailure = { error ->
                    Logger.auth("ADMIN_SERVICE", "Error obteniendo vendedores con filtros: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("ADMIN_SERVICE", "Error obteniendo vendedores con filtros: ${e.message}")
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
    ): Result<SellerInfo> {
        return try {
            Logger.auth("ADMIN_SERVICE", "Actualizando vendedor: $sellerId por admin: $adminId")

            val result = adminApiClient.updateSeller(sellerId, adminId, token, sellerData)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("ADMIN_SERVICE", "Vendedor actualizado exitosamente")
                    Result.success(response.data!!)
                },
                onFailure = { error ->
                    Logger.auth("ADMIN_SERVICE", "Error actualizando vendedor: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("ADMIN_SERVICE", "Error actualizando vendedor: ${e.message}")
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
    ): Result<DeleteSellerData> {
        return try {
            Logger.auth("ADMIN_SERVICE", "Eliminando vendedor: $sellerId por admin: $adminId")

            val result = adminApiClient.deleteSeller(sellerId, adminId, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("ADMIN_SERVICE", "Vendedor eliminado exitosamente")
                    Result.success(response.data!!)
                },
                onFailure = { error ->
                    Logger.auth("ADMIN_SERVICE", "Error eliminando vendedor: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("ADMIN_SERVICE", "Error eliminando vendedor: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtener estadísticas de notificaciones
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