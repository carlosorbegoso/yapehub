package org.sysarp.project.service.admin

import org.sysarp.project.data.*
import org.sysarp.project.service.http.AdminProfileApiClient
import org.sysarp.project.service.http.AdminSellerApiClient
import org.sysarp.project.service.http.AdminStatsApiClient
import org.sysarp.project.service.http.SellerManagementApiClient

import org.sysarp.project.utils.Logger

class AdminService(
    private val adminProfileApiClient: AdminProfileApiClient,
    private val adminSellerApiClient: AdminSellerApiClient,
    private val sellerManagementApiClient: SellerManagementApiClient,
    private val adminStatsApiClient: AdminStatsApiClient
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

            val result = adminStatsApiClient.getNotificationStats(adminId, token)

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
    
    /**
     * Obtener vendedores del administrador con paginación (método básico)
     */
    suspend fun getMySellers(
        adminId: Int, 
        page: Int = 1, 
        limit: Int = 30, 
        token: String
    ): Result<SellersResponse> {
        return try {
            Logger.auth("ADMIN_SERVICE", "Obteniendo vendedores del admin: $adminId, página: $page")

            val result = sellerManagementApiClient.getMySellers(adminId, page, limit, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("ADMIN_SERVICE", "Vendedores obtenidos: ${response.data?.sellers?.size ?: 0} vendedores")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("ADMIN_SERVICE", "Error obteniendo vendedores: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("ADMIN_SERVICE", "Error obteniendo vendedores: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtener vendedores con filtros avanzados (método específico de admin)
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
            Logger.auth("ADMIN_SERVICE", "Obteniendo vendedores con filtros para admin: $adminId")

            val result = adminSellerApiClient.getSellersWithFilters(
                adminId = adminId,
                token = token,
                page = page,
                size = size,
                search = search,
                status = status,
                sortBy = sortBy,
                sortOrder = sortOrder
            )

            result.fold(
                onSuccess = { response ->
                    Logger.auth("ADMIN_SERVICE", "Vendedores con filtros obtenidos exitosamente")
                    Result.success(response)
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
     * Actualizar vendedor (método específico de admin con UpdateSellerRequest)
     */
    suspend fun updateSeller(
        sellerId: Int,
        adminId: Int,
        token: String,
        sellerData: UpdateSellerRequest
    ): Result<UpdateSellerResponse> {
        return try {
            Logger.auth("ADMIN_SERVICE", "Actualizando vendedor: $sellerId por admin: $adminId")

            val result = adminSellerApiClient.updateSeller(sellerId, adminId, token, sellerData)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("ADMIN_SERVICE", "Vendedor actualizado exitosamente")
                    Result.success(response)
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
     * Actualizar vendedor (método compatible con SellerService)
     */
    suspend fun updateSeller(
        sellerId: Int,
        adminId: Int,
        name: String? = null,
        phone: String? = null,
        isActive: Boolean? = null,
        token: String
    ): Result<org.sysarp.project.data.MySeller> {
        return try {
            Logger.auth("ADMIN_SERVICE", "Actualizando vendedor: $sellerId")

            val result = sellerManagementApiClient.updateSeller(sellerId, adminId, name, phone, isActive, token)

            result.fold(
                onSuccess = { updatedSeller ->
                    Logger.auth("ADMIN_SERVICE", "Vendedor actualizado exitosamente: ${updatedSeller.name}")
                    Result.success(updatedSeller)
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
     * Eliminar vendedor (método específico de admin - eliminación directa)
     */
    suspend fun deleteSeller(
        sellerId: Int,
        adminId: Int,
        token: String
    ): Result<DeleteSellerResponse> {
        return try {
            Logger.auth("ADMIN_SERVICE", "Eliminando vendedor: $sellerId por admin: $adminId")

            val result = adminSellerApiClient.deleteSeller(sellerId, adminId, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("ADMIN_SERVICE", "Vendedor eliminado exitosamente")
                    Result.success(response)
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
     * Eliminar vendedor (método compatible con SellerService)
     */
    suspend fun deleteSeller(
        sellerId: Int,
        adminId: Int,
        action: String = "pause", // "pause" o "delete"
        token: String
    ): Result<Boolean> {
        return try {
            Logger.auth("ADMIN_SERVICE", "Eliminando/pausando vendedor: $sellerId con acción: $action")

            val result = sellerManagementApiClient.deleteSeller(sellerId, adminId, action, token)

            result.fold(
                onSuccess = { success ->
                    Logger.auth("ADMIN_SERVICE", "Vendedor $action exitosamente: $sellerId")
                    Result.success(success)
                },
                onFailure = { error ->
                    Logger.auth("ADMIN_SERVICE", "Error $action vendedor: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("ADMIN_SERVICE", "Error $action vendedor: ${e.message}")
            Result.failure(e)
        }
    }
}