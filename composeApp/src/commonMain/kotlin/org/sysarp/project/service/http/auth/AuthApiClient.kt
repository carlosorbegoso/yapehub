package org.sysarp.project.service.http

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.sysarp.project.data.AdminRegistrationRequest
import org.sysarp.project.data.AdminRegistrationResponse
import org.sysarp.project.data.ForgotPasswordRequest
import org.sysarp.project.data.ForgotPasswordResponse
import org.sysarp.project.data.LoginRequest
import org.sysarp.project.data.LoginResponse
import org.sysarp.project.data.LogoutRequest
import org.sysarp.project.data.LogoutResponse
import org.sysarp.project.data.RefreshTokenRequest
import org.sysarp.project.data.RefreshTokenResponse
import org.sysarp.project.data.SellerLoginByPhoneResponse
import org.sysarp.project.data.SellersResponse
import org.sysarp.project.data.ValidateAffiliationCodeRequest
import org.sysarp.project.data.ValidateAffiliationCodeResponse
import org.sysarp.project.data.ValidationErrorResponse
import org.sysarp.project.utils.DeviceUtils

/**
 * Cliente API especializado para autenticación
 */
class AuthApiClient : BaseApiClient() {
    
    /**
     * Login de administrador
     */
    suspend fun adminLogin(
        email: String, 
        password: String, 
        deviceFingerprint: String? = null, 
        role: String = "ADMIN"
    ): Result<LoginResponse> {
        return try {
            logInfo("AUTH_API", "Intentando login de admin: $email")
            
            // Generar device fingerprint si no se proporciona
            val fingerprint = deviceFingerprint ?: generateDeviceFingerprint()
            logInfo("AUTH_API", "Device fingerprint: ${fingerprint.take(20)}...")
            
            val response = client.post("$baseUrl/api/auth/login") {
                contentType(ContentType.Application.Json)
                val loginRequest = LoginRequest(
                    email = email, 
                    password = password,
                    deviceFingerprint = fingerprint,
                    role = role
                )
                logInfo("AUTH_API", "Enviando request body: ${kotlinx.serialization.json.Json.encodeToString(loginRequest)}")
                setBody(loginRequest)
            }
            
            if (response.status.isSuccess()) {
                val loginResponse = response.body<LoginResponse>()
                logInfo("AUTH_API", "Login exitoso para admin: $email")
                Result.success(loginResponse)
            } else {
                // Intentar parsear el mensaje de error específico del servidor
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("AUTH_API", "Error response body: $errorBody")
                    logError("AUTH_API", "Response status: ${response.status}")
                    logError("AUTH_API", "Response headers: ${response.headers}")
                    
                    // Parsear JSON de error si está disponible
                    val json = kotlinx.serialization.json.Json.parseToJsonElement(errorBody)
                    val message = json.jsonObject["message"]?.jsonPrimitive?.content
                    val code = json.jsonObject["code"]?.jsonPrimitive?.content
                    val details = json.jsonObject["details"]?.jsonObject
                    
                    when (code) {
                        "INVALID_FIELD" -> {
                            val reason = details?.get("reason")?.jsonPrimitive?.content
                            when {
                                reason?.contains("email", ignoreCase = true) == true -> 
                                    "El email ingresado no es válido"
                                reason?.contains("password", ignoreCase = true) == true -> 
                                    "La contraseña ingresada es incorrecta"
                                reason?.contains("credentials", ignoreCase = true) == true -> 
                                    "Email o contraseña incorrectos"
                                else -> message ?: "Credenciales inválidas"
                            }
                        }
                        "ACCOUNT_NOT_FOUND" -> "No existe una cuenta con este email"
                        "ACCOUNT_LOCKED" -> "Tu cuenta está bloqueada. Contacta al soporte"
                        "ACCOUNT_DISABLED" -> "Tu cuenta está deshabilitada"
                        else -> message ?: "Error de autenticación"
                    }
                } catch (e: Exception) {
                    logError("AUTH_API", "Error parseando respuesta de error: ${e.message}")
                    "Error en login: ${response.status}"
                }
                
                logError("AUTH_API", "Error en login: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("AUTH_API", "Error en login de admin: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Login de vendedor por teléfono y código de afiliación
     */
    suspend fun sellerLoginByPhone(phone: String, affiliationCode: String): Result<SellerLoginByPhoneResponse> {
        return try {
            logInfo("AUTH_API", "Intentando login de vendedor por teléfono: $phone con código: $affiliationCode")
            
            val response = client.post("$baseUrl/api/auth/seller/login-by-phone") {
                contentType(ContentType.Application.Json)
                parameter("phone", phone)
                parameter("affiliationCode", affiliationCode)
            }
            
            if (response.status.isSuccess()) {
                val loginResponse = response.body<SellerLoginByPhoneResponse>()
                logInfo("AUTH_API", "Login exitoso para vendedor: $phone")
                Result.success(loginResponse)
            } else {
                val errorMessage = "Error en login de vendedor: ${response.status}"
                logError("AUTH_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("AUTH_API", "Error en login de vendedor: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Refresh token
     */
    suspend fun refreshToken(refreshToken: String): Result<RefreshTokenResponse> {
        return try {
            logInfo("AUTH_API", "Refrescando token")
            
            val response = client.post("$baseUrl/api/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refreshToken = refreshToken))
            }
            
            if (response.status.isSuccess()) {
                val refreshResponse = response.body<RefreshTokenResponse>()
                logInfo("AUTH_API", "Token refrescado exitosamente")
                Result.success(refreshResponse)
            } else {
                val errorMessage = "Error refrescando token: ${response.status}"
                logError("AUTH_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("AUTH_API", "Error refrescando token: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Logout
     */
    suspend fun logout(accessToken: String): Result<LogoutResponse> {
        return try {
            logInfo("AUTH_API", "Cerrando sesión")
            
            val response = client.post("$baseUrl/api/auth/logout") {
                contentType(ContentType.Application.Json)
                setBody(LogoutRequest(accessToken = accessToken))
            }
            
            if (response.status.isSuccess()) {
                val logoutResponse = response.body<LogoutResponse>()
                logInfo("AUTH_API", "Sesión cerrada exitosamente")
                Result.success(logoutResponse)
            } else {
                val errorMessage = "Error cerrando sesión: ${response.status}"
                logError("AUTH_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("AUTH_API", "Error cerrando sesión: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Recuperar contraseña
     */
    suspend fun forgotPassword(email: String): Result<ForgotPasswordResponse> {
        return try {
            logInfo("AUTH_API", "Solicitando recuperación de contraseña para: $email")
            
            val response = client.post("$baseUrl/api/auth/forgot-password") {
                contentType(ContentType.Application.Json)
                setBody(ForgotPasswordRequest(email = email))
            }
            
            if (response.status.isSuccess()) {
                val forgotResponse = response.body<ForgotPasswordResponse>()
                logInfo("AUTH_API", "Solicitud de recuperación enviada exitosamente")
                Result.success(forgotResponse)
            } else {
                val errorMessage = "Error en recuperación de contraseña: ${response.status}"
                logError("AUTH_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("AUTH_API", "Error en recuperación de contraseña: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Validar código de afiliación
     */
    suspend fun validateAffiliationCode(affiliationCode: String): Result<ValidateAffiliationCodeResponse> {
        return try {
            logInfo("AUTH_API", "Validando código de afiliación: $affiliationCode")
            
            val response = client.post("$baseUrl/api/auth/validate-affiliation-code") {
                contentType(ContentType.Application.Json)
                setBody(ValidateAffiliationCodeRequest(affiliationCode = affiliationCode))
            }
            
            if (response.status.isSuccess()) {
                val validateResponse = response.body<ValidateAffiliationCodeResponse>()
                logInfo("AUTH_API", "Código de afiliación validado exitosamente")
                Result.success(validateResponse)
            } else {
                val errorMessage = "Error validando código de afiliación: ${response.status}"
                logError("AUTH_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("AUTH_API", "Error validando código de afiliación: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Genera un fingerprint único del dispositivo
     * En Android usará identificadores reales, en otras plataformas un fallback
     */
    private suspend fun generateDeviceFingerprint(): String {
        return try {
            // Intentar usar el fingerprint real del dispositivo
            DeviceUtils.generateDeviceFingerprint()
        } catch (e: Exception) {
            logError("AUTH_API", "Error generando fingerprint real: ${e.message}")
            DeviceUtils.generateSimpleFingerprint()
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
    ): Result<AdminRegistrationResponse> {
        return try {
            logInfo("AUTH_API", "Intentando registro de admin: $email")
            
            val requestData = AdminRegistrationRequest(
                businessName = businessName,
                businessType = businessType,
                ruc = ruc,
                email = email,
                password = password,
                phone = phone,
                address = address,
                contactName = contactName
            )
            
            logInfo("AUTH_API", "Datos de registro: businessName=$businessName, businessType=$businessType, ruc=$ruc, email=$email, phone=$phone, address=$address, contactName=$contactName")
            
            val response = client.post("$baseUrl/api/auth/admin/register") {
                contentType(ContentType.Application.Json)
                setBody(requestData)
            }
            
            if (response.status.isSuccess()) {
                val registrationResponse = response.body<AdminRegistrationResponse>()
                logInfo("AUTH_API", "Registro exitoso para admin: $email")
                Result.success(registrationResponse)
            } else {
                // Intentar parsear el error de validación
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logInfo("AUTH_API", "Error body: $errorBody")
                    
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
                logError("AUTH_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("AUTH_API", "Error en registro de admin: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Obtener vendedores del administrador con paginación
     */
    suspend fun getMySellers(adminId: Int, page: Int = 1, limit: Int = 30, token: String): Result<SellersResponse> {
        return try {
            logInfo("AUTH_API", "Obteniendo vendedores del admin: $adminId, página: $page")
            
            val response = client.get("$baseUrl/api/admin/sellers/my-sellers") {
                parameter("adminId", adminId)
                parameter("page", page)
                parameter("limit", limit)
                header("Authorization", "Bearer $token")
            }
            
            if (response.status.isSuccess()) {
                val sellersResponse = response.body<SellersResponse>()
                logInfo("AUTH_API", "Vendedores obtenidos exitosamente: ${sellersResponse.data?.sellers?.size ?: 0} vendedores")
                Result.success(sellersResponse)
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logInfo("AUTH_API", "Error body: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }
                
                val finalErrorMessage = "Error obteniendo vendedores: ${response.status} - $errorMessage"
                logError("AUTH_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("AUTH_API", "Error obteniendo vendedores: ${e.message}")
            Result.failure(e)
        }
    }
}
