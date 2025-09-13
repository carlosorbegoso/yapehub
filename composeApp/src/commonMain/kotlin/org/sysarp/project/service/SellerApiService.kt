package org.sysarp.project.service

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sysarp.project.data.*

class SellerApiService : BaseHttpService() {
    
    // Registro de vendedor con código de afiliación
    suspend fun registerSeller(
        affiliationCode: String,
        sellerName: String,
        phone: String
    ): Result<SellerRegistrationResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [SELLER_API] Iniciando registro de vendedor")
            println("📤 [SELLER_API] AffiliationCode: $affiliationCode")
            println("📤 [SELLER_API] SellerName: $sellerName")
            println("📤 [SELLER_API] Phone: $phone")
            
            val request = SellerRegistrationRequest(
                affiliationCode = affiliationCode,
                sellerName = sellerName,
                phone = phone
            )
            
            val response = httpClient.post("$baseUrl/seller/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            println("📥 [SELLER_API] Respuesta de registro recibida - Status: ${response.status}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [SELLER_API] Vendedor registrado exitosamente")
                    val result = response.body<SellerRegistrationResponse>()
                    Result.success(result)
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [SELLER_API] Error de validación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                HttpStatusCode.Conflict -> {
                    println("❌ [SELLER_API] Error de conflicto - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                else -> {
                    println("❌ [SELLER_API] Error inesperado - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }
    
    // Login de vendedor por teléfono
    suspend fun sellerLoginByPhone(phone: String): Result<SellerLoginByPhoneResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [SELLER_API] Iniciando login de vendedor por teléfono")
            println("📤 [SELLER_API] Phone: $phone")
            
            // Generar identificador único del teléfono
            val phoneFingerprint = generatePhoneFingerprint(phone)
            println("📤 [SELLER_API] Phone Fingerprint: $phoneFingerprint")
            
            val response = httpClient.post("$baseUrl/auth/seller/login-by-phone?phone=$phone") {
                contentType(ContentType.Application.Json)
                header("X-Phone-Fingerprint", phoneFingerprint)
                setBody("{}")
            }
            
            println("📥 [SELLER_API] Respuesta de login recibida - Status: ${response.status}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [SELLER_API] Login de vendedor exitoso")
                    val result = response.body<SellerLoginByPhoneResponse>()
                    Result.success(result)
                }
                HttpStatusCode.Unauthorized -> {
                    println("❌ [SELLER_API] Error de autenticación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [SELLER_API] Error de validación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                else -> {
                    println("❌ [SELLER_API] Error inesperado - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }
    
    // Validar código de afiliación
    suspend fun validateAffiliationCode(affiliationCode: String): Result<ValidateAffiliationCodeResponse> = withContext(Dispatchers.IO) {
        try {
            println("🌐 [SELLER_API] Iniciando validación de código de afiliación")
            println("📤 [SELLER_API] AffiliationCode: $affiliationCode")
            
            val request = ValidateAffiliationCodeRequest(affiliationCode = affiliationCode)
            
            val response = httpClient.post("$baseUrl/auth/validate-affiliation-code") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            println("📥 [SELLER_API] Respuesta de validación recibida - Status: ${response.status}")
            
            when (response.status) {
                HttpStatusCode.OK -> {
                    println("✅ [SELLER_API] Código validado exitosamente")
                    val result = response.body<ValidateAffiliationCodeResponse>()
                    Result.success(result)
                }
                HttpStatusCode.BadRequest -> {
                    println("❌ [SELLER_API] Error de validación - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
                else -> {
                    println("❌ [SELLER_API] Error inesperado - Status: ${response.status}")
                    Result.failure(handleErrorResponse(response))
                }
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }

    private fun generatePhoneFingerprint(phone: String): String {
        // Implementación simple para generar un "fingerprint" del teléfono
        // En un entorno real, esto debería ser más robusto y posiblemente incluir datos del dispositivo
        return "phone_fingerprint_${phone.hashCode()}"
    }
}
