package org.sysarp.project.service

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sysarp.project.data.*

class AuthApiService : BaseHttpService() {
    
    // Registro de administrador
    suspend fun registerAdmin(
        businessName: String,
        businessType: String,
        ruc: String,
        email: String,
        password: String,
        phone: String,
        address: String,
        contactName: String
    ): Result<AdminRegistrationResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [AUTH_API] Iniciando registro de administrador")
            
            val request = AdminRegistrationRequest(
                businessName = businessName,
                businessType = businessType,
                ruc = ruc,
                email = email,
                password = password,
                phone = phone,
                address = address,
                contactName = contactName
            )
            
            val response = httpClient.post("$baseUrl/auth/admin/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            println("📥 [AUTH_API] Respuesta de registro recibida - Status: ${response.status}")
            
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    println("✅ [AUTH_API] Administrador registrado exitosamente")
                    val result = response.body<AdminRegistrationResponse>()
                    Result.success(result)
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [AUTH_API] Error de validación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                else -> {
                    println("❌ [AUTH_API] Error inesperado - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }
    
    // Login de administrador
    suspend fun login(
        email: String,
        password: String,
        deviceFingerprint: String,
        role: String
    ): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [AUTH_API] Iniciando login")
            
            val request = LoginRequest(
                email = email,
                password = password,
                deviceFingerprint = deviceFingerprint,
                role = role
            )
            
            val response = httpClient.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            println("📥 [AUTH_API] Respuesta de login recibida - Status: ${response.status}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [AUTH_API] Login exitoso")
                    val result = response.body<LoginResponse>()
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    println("❌ [AUTH_API] Error de autenticación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [AUTH_API] Error de validación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                else -> {
                    println("❌ [AUTH_API] Error inesperado - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }
    
    // Logout
    suspend fun logout(accessToken: String): Result<LogoutResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [AUTH_API] Iniciando logout")
            
            val response = httpClient.post("$baseUrl/auth/logout") {
                contentType(ContentType.Application.Json)
                header("X-Auth-Token", accessToken)
                setBody("{}")
            }
            
            println("📥 [AUTH_API] Respuesta de logout recibida - Status: ${response.status}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [AUTH_API] Logout exitoso")
                    val result = response.body<LogoutResponse>()
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    println("❌ [AUTH_API] Error de autenticación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                else -> {
                    println("❌ [AUTH_API] Error inesperado - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }
    
    // Refresh token
    suspend fun refreshToken(refreshToken: String): Result<RefreshTokenResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [AUTH_API] Iniciando refresh token")
            
            val response = httpClient.post("$baseUrl/auth/refresh") {
                contentType(ContentType.Application.Json)
                header("X-Auth-Token", refreshToken)
                setBody("{}")
            }
            
            println("📥 [AUTH_API] Respuesta de refresh recibida - Status: ${response.status}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [AUTH_API] Token refrescado exitosamente")
                    val result = response.body<RefreshTokenResponse>()
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    println("❌ [AUTH_API] Token de refresh inválido - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [AUTH_API] Error de validación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                else -> {
                    println("❌ [AUTH_API] Error inesperado - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }
}
