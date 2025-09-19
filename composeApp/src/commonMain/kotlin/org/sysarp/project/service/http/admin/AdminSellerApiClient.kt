package org.sysarp.project.service.http

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import org.sysarp.project.data.DeleteSellerResponse
import org.sysarp.project.data.SellersWithFiltersResponse
import org.sysarp.project.data.UpdateSellerRequest
import org.sysarp.project.data.UpdateSellerResponse

/**
 * Cliente API especializado para gestión de vendedores por administrador
 */
class AdminSellerApiClient : BaseApiClient() {
    
    /**
     * Listar vendedores con filtros avanzados
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
            logInfo("ADMIN_SELLER_API", "Obteniendo vendedores con filtros para admin: $adminId")
            
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
                logInfo("ADMIN_SELLER_API", "Vendedores con filtros obtenidos exitosamente")
                Result.success(sellersResponse)
            } else {
                val errorMessage = "Error obteniendo vendedores con filtros: ${response.status}"
                logError("ADMIN_SELLER_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("ADMIN_SELLER_API", "Error obteniendo vendedores con filtros: ${e.message}")
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
            logInfo("ADMIN_SELLER_API", "Actualizando vendedor: $sellerId por admin: $adminId")
            
            val response = client.put("$baseUrl/api/admin/sellers/$sellerId") {
                parameter("adminId", adminId)
                header("Authorization", "Bearer $token")
                header("Content-Type", "application/json")
                setBody(sellerData)
            }
            
            if (response.status.isSuccess()) {
                val updateResponse = response.body<UpdateSellerResponse>()
                logInfo("ADMIN_SELLER_API", "Vendedor actualizado exitosamente")
                Result.success(updateResponse)
            } else {
                val errorMessage = "Error actualizando vendedor: ${response.status}"
                logError("ADMIN_SELLER_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("ADMIN_SELLER_API", "Error actualizando vendedor: ${e.message}")
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
            logInfo("ADMIN_SELLER_API", "Eliminando vendedor: $sellerId por admin: $adminId")
            
            val response = client.delete("$baseUrl/api/admin/sellers/$sellerId") {
                parameter("adminId", adminId)
                header("Authorization", "Bearer $token")
                header("accept", "application/json")
            }
            
            if (response.status.isSuccess()) {
                val deleteResponse = response.body<DeleteSellerResponse>()
                logInfo("ADMIN_SELLER_API", "Vendedor eliminado exitosamente")
                Result.success(deleteResponse)
            } else {
                val errorMessage = "Error eliminando vendedor: ${response.status}"
                logError("ADMIN_SELLER_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("ADMIN_SELLER_API", "Error eliminando vendedor: ${e.message}")
            Result.failure(e)
        }
    }
}
