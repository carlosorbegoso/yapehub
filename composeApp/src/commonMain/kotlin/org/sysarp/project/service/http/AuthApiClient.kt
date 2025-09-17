package org.sysarp.project.service.http

import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.sysarp.project.data.*
import org.sysarp.project.utils.DeviceUtils

/**
 * Cliente API especializado para autenticación
 */
class AuthApiClient : BaseApiClient() {
    
    /**
     * Login de administrador
     */
    suspend fun adminLogin(email: String, password: String): Result<LoginResponse> {
        return try {
            logInfo("AUTH_API", "Intentando login de admin: $email")
            
            val response = client.post("$baseUrl/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(
                    email = email, 
                    password = password,
                    deviceFingerprint = generateDeviceFingerprint(),
                    role = "ADMIN"
                ))
            }
            
            if (response.status.isSuccess()) {
                val loginResponse = response.body<LoginResponse>()
                logInfo("AUTH_API", "Login exitoso para admin: $email")
                Result.success(loginResponse)
            } else {
                val errorMessage = "Error en login: ${response.status}"
                logError("AUTH_API", errorMessage)
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
            // Fallback a fingerprint simple
            DeviceUtils.generateSimpleFingerprint()
        }
    }
}

