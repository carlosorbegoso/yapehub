package org.sysarp.project.service.http

import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.sysarp.project.data.*

/**
 * Cliente API especializado para vendedores
 */
class SellerApiClient : BaseApiClient() {
    
    /**
     * Generar código de afiliación
     */
    suspend fun generateAffiliationCode(
        adminId: Int,
        branchId: Int,
        expirationHours: Int,
        maxUses: Int,
        notes: String,
        accessToken: String
    ): Result<GenerateAffiliationCodeResponse> {
        return try {
            logInfo("SELLER_API", "Generando código de afiliación para admin: $adminId")
            
            val response = client.post("$baseUrl/api/generate-affiliation-code-protected") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                parameter("adminId", adminId)
                parameter("branchId", branchId)
                parameter("expirationHours", expirationHours)
                parameter("maxUses", maxUses)
                parameter("notes", notes)
            }
            
            if (response.status.isSuccess()) {
                val affiliationResponse = response.body<GenerateAffiliationCodeResponse>()
                logInfo("SELLER_API", "Código de afiliación generado exitosamente")
                Result.success(affiliationResponse)
            } else {
                val errorMessage = "Error generando código de afiliación: ${response.status}"
                logError("SELLER_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_API", "Error generando código de afiliación: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Registro de vendedor con código de afiliación
     */
    suspend fun registerSeller(
        affiliationCode: String,
        sellerName: String,
        phone: String
    ): Result<org.sysarp.project.data.SellerRegistrationResponse> {
        return try {
            logInfo("SELLER_API", "Intentando registro de vendedor con código: $affiliationCode")
            
            val requestData = org.sysarp.project.data.SellerRegistrationRequest(
                affiliationCode = affiliationCode,
                sellerName = sellerName,
                phone = phone
            )
            
            logInfo("SELLER_API", "Datos de registro: affiliationCode=$affiliationCode, sellerName=$sellerName, phone=$phone")
            
            val response = client.post("$baseUrl/api/seller/register") {
                contentType(ContentType.Application.Json)
                setBody(requestData)
            }
            
            if (response.status.isSuccess()) {
                val registrationResponse = response.body<org.sysarp.project.data.SellerRegistrationResponse>()
                logInfo("SELLER_API", "Registro de vendedor exitoso con código: $affiliationCode")
                Result.success(registrationResponse)
            } else {
                // Manejar errores específicos de vendedor
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("SELLER_API", "Error body: $errorBody")
                    
                    try {
                        val sellerError = kotlinx.serialization.json.Json.decodeFromString<SellerErrorResponse>(errorBody)
                        val fieldName = when (sellerError.details.field) {
                            "affiliationCode" -> "Código de afiliación"
                            "sellerName" -> "Nombre del vendedor"
                            "phone" -> "Teléfono"
                            else -> sellerError.details.field
                        }
                        "$fieldName: ${sellerError.details.reason}"
                    } catch (e: Exception) {
                        errorBody
                    }
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }
                
                val finalErrorMessage = "Error en registro de vendedor: ${response.status} - $errorMessage"
                logError("SELLER_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_API", "Error en registro de vendedor: ${e.message}")
            Result.failure(e)
        }
    }
    
    
    /**
     * Obtener vendedores del administrador con paginación
     */
    suspend fun getMySellers(adminId: Int, page: Int = 1, limit: Int = 30, token: String): Result<org.sysarp.project.data.SellersResponse> {
        return try {
            logInfo("SELLER_API", "Obteniendo vendedores del admin: $adminId, página: $page")
            
            val response = client.get("$baseUrl/api/admin/sellers/my-sellers") {
                parameter("adminId", adminId)
                parameter("page", page)
                parameter("limit", limit)
                header("Authorization", "Bearer $token")
            }
            
            if (response.status.isSuccess()) {
                val sellersResponse = response.body<org.sysarp.project.data.SellersResponse>()
                logInfo("SELLER_API", "Vendedores obtenidos exitosamente: ${sellersResponse.data?.sellers?.size ?: 0} vendedores")
                Result.success(sellersResponse)
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("SELLER_API", "Error body: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }
                
                val finalErrorMessage = "Error obteniendo vendedores: ${response.status} - $errorMessage"
                logError("SELLER_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_API", "Error obteniendo vendedores: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Login de vendedor por teléfono
     */
    suspend fun loginSellerByPhone(phone: String): Result<org.sysarp.project.data.SellerLoginByPhoneResponse> {
        return try {
            logInfo("SELLER_API", "Intentando login de vendedor por teléfono: $phone")
            
            val response = client.post("$baseUrl/api/auth/seller/login-by-phone") {
                contentType(ContentType.Application.Json)
                parameter("phone", phone)
            }
            
            if (response.status.isSuccess()) {
                val loginResponse = response.body<org.sysarp.project.data.SellerLoginByPhoneResponse>()
                logInfo("SELLER_API", "Login de vendedor exitoso por teléfono: $phone")
                Result.success(loginResponse)
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("SELLER_API", "Error body: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }
                
                val finalErrorMessage = "Error en login de vendedor: ${response.status} - $errorMessage"
                logError("SELLER_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_API", "Error en login de vendedor: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtener vendedores del administrador
     */
    suspend fun getSellers(adminId: Int, accessToken: String): Result<GetSellersResponse> {
        return try {
            logInfo("SELLER_API", "Obteniendo vendedores para admin: $adminId")
            
            val response = client.get("$baseUrl/api/admin/sellers") {
                header("Authorization", "Bearer $accessToken")
                parameter("adminId", adminId)
            }
            
            if (response.status.isSuccess()) {
                val sellersResponse = response.body<GetSellersResponse>()
                logInfo("SELLER_API", "Vendedores obtenidos exitosamente")
                Result.success(sellersResponse)
            } else {
                val errorMessage = "Error obteniendo vendedores: ${response.status}"
                logError("SELLER_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_API", "Error obteniendo vendedores: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar vendedor
     */
    suspend fun updateSeller(
        sellerId: Int,
        adminId: Int,
        name: String,
        phone: String,
        isActive: Boolean,
        accessToken: String
    ): Result<UpdateSellerResponse> {
        return try {
            logInfo("SELLER_API", "Actualizando vendedor: $sellerId")
            
            val response = client.put("$baseUrl/api/admin/sellers/$sellerId") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                setBody(UpdateSellerRequest(
                    name = name,
                    phone = phone,
                    isActive = isActive
                ))
            }
            
            if (response.status.isSuccess()) {
                val updateResponse = response.body<UpdateSellerResponse>()
                logInfo("SELLER_API", "Vendedor actualizado exitosamente")
                Result.success(updateResponse)
            } else {
                val errorMessage = "Error actualizando vendedor: ${response.status}"
                logError("SELLER_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_API", "Error actualizando vendedor: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Eliminar/pausar vendedor
     */
    suspend fun deleteSeller(
        adminId: Int,
        action: String,
        sellerId: Int,
        accessToken: String
    ): Result<DeleteSellerResponse> {
        return try {
            logInfo("SELLER_API", "Eliminando/pausando vendedor: $sellerId")
            
            val response = client.delete("$baseUrl/api/admin/sellers/$sellerId") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                parameter("action", action)
            }
            
            if (response.status.isSuccess()) {
                val deleteResponse = response.body<DeleteSellerResponse>()
                logInfo("SELLER_API", "Vendedor eliminado/pausado exitosamente")
                Result.success(deleteResponse)
            } else {
                val errorMessage = "Error eliminando/pausando vendedor: ${response.status}"
                logError("SELLER_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_API", "Error eliminando/pausando vendedor: ${e.message}")
            Result.failure(e)
        }
    }
}

