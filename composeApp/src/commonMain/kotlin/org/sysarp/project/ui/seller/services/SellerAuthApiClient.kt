package org.sysarp.project.service.http

import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.sysarp.project.data.SellerLoginByPhoneResponse

/**
 * Cliente API especializado para autenticación de vendedores
 */
class SellerAuthApiClient : BaseApiClient() {
    
    /**
     * Login de vendedor por teléfono y código de afiliación
     */
    suspend fun loginSellerByPhone(phone: String, affiliationCode: String): Result<SellerLoginByPhoneResponse> {
        return try {
            logInfo("SELLER_AUTH_API", "Intentando login de vendedor por teléfono: $phone con código: $affiliationCode")
            
            val response = client.post("$baseUrl/api/auth/seller/login-by-phone") {
                contentType(ContentType.Application.Json)
                parameter("phone", phone)
                parameter("affiliationCode", affiliationCode)
            }
            
            if (response.status.isSuccess()) {
                val loginResponse = response.body<SellerLoginByPhoneResponse>()
                logInfo("SELLER_AUTH_API", "Login de vendedor exitoso por teléfono: $phone")
                Result.success(loginResponse)
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("SELLER_AUTH_API", "Error body: $errorBody")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }
                
                val finalErrorMessage = "Error en login de vendedor: ${response.status} - $errorMessage"
                logError("SELLER_AUTH_API", finalErrorMessage)
                Result.failure(Exception(finalErrorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_AUTH_API", "Error en login de vendedor: ${e.message}")
            Result.failure(e)
        }
    }
}
