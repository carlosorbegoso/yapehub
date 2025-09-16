package org.sysarp.project.service.http

import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.sysarp.project.data.*

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
                    deviceFingerprint = "mobile_device_${System.currentTimeMillis()}",
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
}

