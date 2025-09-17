package org.sysarp.project.service.http

import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.sysarp.project.data.*

/**
 * Cliente API especializado para vendedores
 */
class SellerApiClient : BaseApiClient() {
    
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
                        // Intentar parsear como ApiError primero
                        val apiError = kotlinx.serialization.json.Json.decodeFromString<org.sysarp.project.data.ApiError>(errorBody)
                        
                        // Si hay errores de validación específicos, mostrarlos
                        val validationErrors = apiError.details?.validationErrors
                        if (validationErrors != null && validationErrors.isNotEmpty()) {
                            val friendlyErrors = validationErrors.map { (field, error) ->
                                val fieldName = when {
                                    field.contains("phone", ignoreCase = true) -> "Teléfono"
                                    field.contains("email", ignoreCase = true) -> "Email"
                                    field.contains("name", ignoreCase = true) -> "Nombre"
                                    field.contains("affiliation", ignoreCase = true) -> "Código de afiliación"
                                    else -> field
                                }
                                "$fieldName: ${getFriendlyMessage(error.message)}"
                            }
                            friendlyErrors.joinToString("; ")
                        } else {
                            apiError.message
                        }
                    } catch (e: Exception) {
                        // Fallback al mensaje original si no se puede parsear
                        errorBody
                    }
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }
                
                logError("SELLER_API", "Error en registro de vendedor: ${response.status} - $errorMessage")
                Result.failure(Exception(errorMessage))
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

    suspend fun updateSeller(
        sellerId: Int,
        adminId: Int,
        name: String? = null,
        phone: String? = null,
        isActive: Boolean? = null,
        token: String
    ): Result<org.sysarp.project.data.MySeller> {
        return try {
            logInfo("SELLER_API", "Actualizando vendedor: $sellerId")

            val response = sellerClient.put("$baseUrl/api/admin/sellers/$sellerId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, "application/json")
                parameter("adminId", adminId)
                name?.let { parameter("name", it) }
                phone?.let { parameter("phone", it) }
                isActive?.let { parameter("isActive", it.toString()) }
            }

            val responseBody = response.bodyAsText()
            logInfo("SELLER_API", "Respuesta del servidor: $responseBody")

            if (response.status.isSuccess()) {
                try {
                    // Intentar parsear como MySeller primero
                    val updatedSeller = json.decodeFromString<org.sysarp.project.data.MySeller>(responseBody)
                    logInfo("SELLER_API", "Vendedor actualizado exitosamente: ${updatedSeller.name}")
                    Result.success(updatedSeller)
                } catch (e: Exception) {
                    logError("SELLER_API", "Error parseando respuesta como MySeller: ${e.message}")
                    // Si falla, intentar parsear como UpdateSellerResponse
                    try {
                        val updateResponse = json.decodeFromString<org.sysarp.project.data.UpdateSellerResponse>(responseBody)
                        logInfo("SELLER_API", "Vendedor actualizado exitosamente (UpdateSellerResponse)")
                        // Convertir UpdateSellerResponse a MySeller
                        val mySeller = org.sysarp.project.data.MySeller(
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
                        logError("SELLER_API", "Error parseando respuesta como UpdateSellerResponse: ${e2.message}")
                        Result.failure(Exception("Error parseando respuesta del servidor: ${e.message}"))
                    }
                }
            } else {
                val errorMessage = "Error actualizando vendedor: ${response.status.value}: ${response.status.description} - $responseBody"
                logError("SELLER_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_API", "Error actualizando vendedor: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteSeller(
        sellerId: Int,
        adminId: Int,
        action: String = "pause", // "pause" o "delete"
        token: String
    ): Result<Boolean> {
        return try {
            logInfo("SELLER_API", "Eliminando/pausando vendedor: $sellerId con acción: $action")

            val response = sellerClient.delete("$baseUrl/api/admin/sellers/$sellerId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                header(HttpHeaders.ContentType, "application/json")
                parameter("action", action)
                parameter("adminId", adminId)
            }

            val responseBody = response.bodyAsText()

            if (response.status.isSuccess()) {
                logInfo("SELLER_API", "Vendedor $action exitosamente: $sellerId")
                Result.success(true)
            } else {
                val errorMessage = "Error $action vendedor: ${response.status.value}: ${response.status.description} - $responseBody"
                logError("SELLER_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_API", "Error $action vendedor: ${e.message}")
            Result.failure(e)
        }
    }


    /**
     * Login de vendedor por teléfono y código de afiliación
     */
    suspend fun loginSellerByPhone(phone: String, affiliationCode: String): Result<org.sysarp.project.data.SellerLoginByPhoneResponse> {
        return try {
            logInfo("SELLER_API", "Intentando login de vendedor por teléfono: $phone con código: $affiliationCode")
            
            val response = client.post("$baseUrl/api/auth/seller/login-by-phone") {
                contentType(ContentType.Application.Json)
                parameter("phone", phone)
                parameter("affiliationCode", affiliationCode)
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
     * Obtener vendedores conectados
     */
    suspend fun getConnectedSellers(
        adminId: Int,
        accessToken: String
    ): Result<ConnectedSellersResponse> {
        return try {
            logInfo("SELLER_API", "Obteniendo vendedores conectados para admin: $adminId")
            
            val response = client.get("$baseUrl/api/payments/admin/connected-sellers") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                parameter("adminId", adminId)
            }
            
            if (response.status.isSuccess()) {
                val connectedSellersResponse = response.body<ConnectedSellersResponse>()
                logInfo("SELLER_API", "Vendedores conectados obtenidos: ${connectedSellersResponse.data?.totalConnected} conectados")
                Result.success(connectedSellersResponse)
            } else {
                val errorMessage = "Error obteniendo vendedores conectados: ${response.status}"
                logError("SELLER_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_API", "Error obteniendo vendedores conectados: ${e.message}")
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
            logInfo("SELLER_API", "Obteniendo estado de vendedores para admin: $adminId")
            
            val response = client.get("$baseUrl/api/payments/admin/sellers-status") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                parameter("adminId", adminId)
            }
            
            if (response.status.isSuccess()) {
                val sellersStatusResponse = response.body<SellersStatusResponse>()
                logInfo("SELLER_API", "Estado de vendedores obtenido: ${sellersStatusResponse.data?.totalSellers} total")
                Result.success(sellersStatusResponse)
            } else {
                val errorMessage = "Error obteniendo estado de vendedores: ${response.status}"
                logError("SELLER_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_API", "Error obteniendo estado de vendedores: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Convierte mensajes técnicos a mensajes amigables
     */
    private fun getFriendlyMessage(message: String): String {
        return when {
            message.contains("Invalid phone number format", ignoreCase = true) -> 
                "El formato del teléfono no es válido. Debe contener solo números"
            message.contains("Invalid email format", ignoreCase = true) -> 
                "El formato del email no es válido"
            message.contains("Required field", ignoreCase = true) -> 
                "Este campo es obligatorio"
            message.contains("Too short", ignoreCase = true) -> 
                "El texto es muy corto"
            message.contains("Too long", ignoreCase = true) -> 
                "El texto es muy largo"
            message.contains("Invalid format", ignoreCase = true) -> 
                "El formato no es válido"
            else -> message
        }
    }
}

