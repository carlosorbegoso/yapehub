package org.sysarp.project.service.http

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.sysarp.project.data.*
import org.sysarp.project.utils.Logger
import org.sysarp.project.utils.Constants

/**
 * Cliente HTTP para comunicarse con la API de YapeChamo
 */
class ApiClient {
    private val baseUrl = Constants.BASE_URL
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
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
     * Login de administrador
     */
    suspend fun adminLogin(email: String, password: String): Result<LoginResponse> {
        return try {
            Logger.auth("API_CLIENT", "Intentando login de admin: $email")
            
            val response = client.post("$baseUrl/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(
                    email = email, 
                    password = password,
                    deviceFingerprint = "mobile_device_${System.currentTimeMillis()}",
                    role = "ADMIN"
                ))
            }
            
            if (response.status.isSuccess()) {
                val loginResponse = response.body<LoginResponse>()
                Logger.auth("API_CLIENT", "Login exitoso para admin: $email")
                Result.success(loginResponse)
            } else {
                val errorMessage = "Error en login: ${response.status}"
                Logger.auth("API_CLIENT", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.auth("API_CLIENT", "Error en login de admin: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Login de vendedor por teléfono
     */
    suspend fun sellerLoginByPhone(phone: String): Result<SellerLoginByPhoneResponse> {
        return try {
            Logger.auth("API_CLIENT", "Intentando login de vendedor por teléfono: $phone")
            
            val response = client.post("$baseUrl/api/auth/seller/login-by-phone") {
                contentType(ContentType.Application.Json)
                parameter("phone", phone)
            }
            
            if (response.status.isSuccess()) {
                val loginResponse = response.body<SellerLoginByPhoneResponse>()
                Logger.auth("API_CLIENT", "Login exitoso para vendedor: $phone")
                Result.success(loginResponse)
            } else {
                val errorMessage = "Error en login de vendedor: ${response.status}"
                Logger.auth("API_CLIENT", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Logger.auth("API_CLIENT", "Error en login de vendedor: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Registro de administrador
     */
    suspend fun adminRegister(
        businessName: String,
        businessType: String,
        ruc: String,
        email: String,
        password: String,
        phone: String,
        address: String,
        contactName: String
    ): Result<org.sysarp.project.data.AdminRegistrationResponse> {
        return try {
            Logger.auth("API_CLIENT", "Intentando registro de admin: $email")
            
            val requestData = org.sysarp.project.data.AdminRegistrationRequest(
                businessName = businessName,
                businessType = businessType,
                ruc = ruc,
                email = email,
                password = password,
                phone = phone,
                address = address,
                contactName = contactName
            )
            
            Logger.auth("API_CLIENT", "Datos de registro: businessName=$businessName, businessType=$businessType, ruc=$ruc, email=$email, phone=$phone, address=$address, contactName=$contactName")
            
            val response = client.post("$baseUrl/api/auth/admin/register") {
                contentType(ContentType.Application.Json)
                setBody(requestData)
            }
            
            if (response.status.isSuccess()) {
                val registrationResponse = response.body<org.sysarp.project.data.AdminRegistrationResponse>()
                Logger.auth("API_CLIENT", "Registro exitoso para admin: $email")
                Result.success(registrationResponse)
            } else {
                // Intentar parsear el error de validación
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    Logger.auth("API_CLIENT", "Error body: $errorBody")
                    
                    // Intentar parsear como ValidationErrorResponse
                    try {
                        val validationError = kotlinx.serialization.json.Json.decodeFromString<ValidationErrorResponse>(errorBody)
                        val fieldErrors = validationError.details.validationErrors
                        
                        // Construir mensaje de error específico
                        val specificErrors = fieldErrors.map { (field, error) ->
                            val fieldName = when {
                                field.contains("email") -> "Email"
                                field.contains("password") -> "Contraseña"
                                field.contains("businessName") -> "Nombre del negocio"
                                field.contains("businessType") -> "Tipo de negocio"
                                field.contains("ruc") -> "RUC"
                                field.contains("phone") -> "Teléfono"
                                field.contains("address") -> "Dirección"
                                field.contains("contactName") -> "Nombre de contacto"
                                else -> field
                            }
                            "$fieldName: ${error.message}"
                        }
                        
                        if (specificErrors.isNotEmpty()) {
                            specificErrors.joinToString("; ")
                        } else {
                            validationError.message
                        }
                    } catch (e: Exception) {
                        // Si no se puede parsear como ValidationErrorResponse, usar el mensaje original
                        errorBody
                    }
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }
                
                val finalErrorMessage = "Error en registro de admin: ${response.status} - $errorMessage"
                Logger.auth("API_CLIENT", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            Logger.auth("API_CLIENT", "Error en registro de admin: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Obtener vendedores del administrador con paginación
     */
    suspend fun getMySellers(adminId: Int, page: Int = 1, limit: Int = 30, token: String): Result<org.sysarp.project.data.SellersResponse> {
        return try {
            Logger.auth("API_CLIENT", "Obteniendo vendedores del admin: $adminId, página: $page")
            
            val response = client.get("$baseUrl/api/admin/sellers/my-sellers") {
                parameter("adminId", adminId)
                parameter("page", page)
                parameter("limit", limit)
                header("Authorization", "Bearer $token")
            }
            
            if (response.status.isSuccess()) {
                val sellersResponse = response.body<org.sysarp.project.data.SellersResponse>()
                Logger.auth("API_CLIENT", "Vendedores obtenidos exitosamente: ${sellersResponse.data?.sellers?.size ?: 0} vendedores")
                Result.success(sellersResponse)
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    Logger.auth("API_CLIENT", "Error body: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }
                
                val finalErrorMessage = "Error obteniendo vendedores: ${response.status} - $errorMessage"
                Logger.auth("API_CLIENT", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            Logger.auth("API_CLIENT", "Error obteniendo vendedores: ${e.message}")
            Result.failure(e)
        }
    }

}

