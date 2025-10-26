package org.sysarp.project.service.http

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import org.sysarp.project.data.ConnectedSellersResponse
import org.sysarp.project.data.MySeller
import org.sysarp.project.data.SellersResponse
import org.sysarp.project.data.SellersStatusResponse
import org.sysarp.project.data.UpdateSellerResponse

/**
 * Cliente API especializado para gestión básica de vendedores
 */
class SellerManagementApiClient : BaseApiClient() {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // Cliente HTTP específico sin ContentNegotiation para evitar Content-Type automático
    private val sellerClient = HttpClient {
        install(Logging) {
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 10000
        }
    }
    
    /**
     * Obtener vendedores del administrador con paginación
     */
    suspend fun getMySellers(adminId: Int, page: Int = 1, limit: Int = 30, token: String): Result<SellersResponse> {
        return try {
            // logInfo("SELLER_MANAGEMENT_API", "Obteniendo vendedores del admin: $adminId, página: $page")

            val response = client.get("$baseUrl/api/admin/sellers/my-sellers") {
                parameter("adminId", adminId)
                parameter("page", page)
                parameter("limit", limit)
                header("Authorization", "Bearer $token")
            }

            if (response.status.isSuccess()) {
                val sellersResponse = response.body<SellersResponse>()
                // logInfo("SELLER_MANAGEMENT_API", "Vendedores obtenidos exitosamente: ${sellersResponse.data?.sellers?.size ?: 0} vendedores")
                Result.success(sellersResponse)
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("SELLER_MANAGEMENT_API", "Error body: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }

                val finalErrorMessage = "Error obteniendo vendedores: ${response.status} - $errorMessage"
                logError("SELLER_MANAGEMENT_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_MANAGEMENT_API", "Error obteniendo vendedores: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Actualizar vendedor (método básico con parámetros individuales)
     */
    suspend fun updateSeller(
        sellerId: Int,
        adminId: Int,
        name: String? = null,
        phone: String? = null,
        isActive: Boolean? = null,
        token: String
    ): Result<MySeller> {
        return try {
            // logInfo("SELLER_MANAGEMENT_API", "Actualizando vendedor: $sellerId")

            val response = sellerClient.put("$baseUrl/api/admin/sellers/$sellerId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, "application/json")
                parameter("adminId", adminId)
                name?.let { parameter("name", it) }
                phone?.let { parameter("phone", it) }
                isActive?.let { parameter("isActive", it.toString()) }
            }

            val responseBody = response.bodyAsText()
            // logInfo("SELLER_MANAGEMENT_API", "Respuesta del servidor: $responseBody")

            if (response.status.isSuccess()) {
                try {
                    // Intentar parsear como MySeller primero
                    val updatedSeller = json.decodeFromString<MySeller>(responseBody)
                    // logInfo("SELLER_MANAGEMENT_API", "Vendedor actualizado exitosamente: ${updatedSeller.name}")
                    Result.success(updatedSeller)
                } catch (e: Exception) {
                    logError("SELLER_MANAGEMENT_API", "Error parseando respuesta como MySeller: ${e.message}")
                    // Si falla, intentar parsear como UpdateSellerResponse
                    try {
                        val updateResponse = json.decodeFromString<UpdateSellerResponse>(responseBody)
                        // logInfo("SELLER_MANAGEMENT_API", "Vendedor actualizado exitosamente (UpdateSellerResponse)")
                        // Convertir UpdateSellerResponse a MySeller
                        val mySeller = MySeller(
                            sellerId = updateResponse.data?.sellerId ?: sellerId,
                            name = updateResponse.data?.name ?: "",
                            email = updateResponse.data?.email ?: "",
                            phone = updateResponse.data?.phone ?: "",
                            branchId = updateResponse.data?.branchId ?: 0,
                            branchName = updateResponse.data?.branchName ?: "",
                            isActive = updateResponse.data?.isActive ?: true,
                            isOnline = false, // Valor por defecto
                            totalPayments = 0, // Valor por defecto
                            totalAmount = 0.0, // Valor por defecto
                            lastPayment = null,
                            affiliationDate = null
                        )
                        Result.success(mySeller)
                    } catch (e2: Exception) {
                        logError("SELLER_MANAGEMENT_API", "Error parseando respuesta como UpdateSellerResponse: ${e2.message}")
                        Result.failure(Exception("Error parseando respuesta del servidor: ${e.message}"))
                    }
                }
            } else {
                val errorMessage = "Error actualizando vendedor: ${response.status.value}: ${response.status.description} - $responseBody"
                logError("SELLER_MANAGEMENT_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_MANAGEMENT_API", "Error actualizando vendedor: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Eliminar vendedor (método básico con parámetro action)
     */
    suspend fun deleteSeller(
        sellerId: Int,
        adminId: Int,
        action: String = "pause", // "pause" o "delete"
        token: String
    ): Result<Boolean> {
        return try {
            // logInfo("SELLER_MANAGEMENT_API", "Eliminando/pausando vendedor: $sellerId con acción: $action")

            val response = sellerClient.delete("$baseUrl/api/admin/sellers/$sellerId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, "application/json")
                parameter("action", action)
                parameter("adminId", adminId)
            }

            val responseBody = response.bodyAsText()

            if (response.status.isSuccess()) {
                // logInfo("SELLER_MANAGEMENT_API", "Vendedor $action exitosamente: $sellerId")
                Result.success(true)
            } else {
                val errorMessage = "Error $action vendedor: ${response.status.value}: ${response.status.description} - $responseBody"
                logError("SELLER_MANAGEMENT_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_MANAGEMENT_API", "Error $action vendedor: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtener vendedores conectados
     */
    suspend fun getConnectedSellers(
        adminId: Int,
        accessToken: String
    ): Result<ConnectedSellersResponse> {
        return try {
            // logInfo("SELLER_MANAGEMENT_API", "Obteniendo vendedores conectados para admin: $adminId")
            
            val response = client.get("$baseUrl/api/payments/admin/connected-sellers") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                parameter("adminId", adminId)
            }
            
            if (response.status.isSuccess()) {
                val connectedSellersResponse = response.body<ConnectedSellersResponse>()
                // logInfo("SELLER_MANAGEMENT_API", "Vendedores conectados obtenidos: ${connectedSellersResponse.data?.totalConnected} conectados")
                Result.success(connectedSellersResponse)
            } else {
                val errorMessage = "Error obteniendo vendedores conectados: ${response.status}"
                logError("SELLER_MANAGEMENT_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_MANAGEMENT_API", "Error obteniendo vendedores conectados: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtener estado de todos los vendedores
     */
    suspend fun getSellersStatus(
        adminId: Int,
        accessToken: String
    ): Result<SellersStatusResponse> {
        return try {
            // logInfo("SELLER_MANAGEMENT_API", "Obteniendo estado de vendedores para admin: $adminId")
            
            val response = client.get("$baseUrl/api/payments/admin/sellers-status") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                parameter("adminId", adminId)
            }
            
            if (response.status.isSuccess()) {
                val sellersStatusResponse = response.body<SellersStatusResponse>()
                // logInfo("SELLER_MANAGEMENT_API", "Estado de vendedores obtenido: ${sellersStatusResponse.data?.totalSellers} total")
                Result.success(sellersStatusResponse)
            } else {
                val errorMessage = "Error obteniendo estado de vendedores: ${response.status}"
                logError("SELLER_MANAGEMENT_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_MANAGEMENT_API", "Error obteniendo estado de vendedores: ${e.message}")
            Result.failure(e)
        }
    }
}
