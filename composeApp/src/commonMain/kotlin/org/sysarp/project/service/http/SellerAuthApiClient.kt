package org.sysarp.project.service.http

import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.sysarp.project.data.SellerLoginByPhoneResponse
import org.sysarp.project.utils.getCurrentTimestampMs
import org.sysarp.project.utils.calculateDurationMs

/**
 * Cliente API especializado para autenticación de vendedores
 */
class SellerAuthApiClient : BaseApiClient() {
    
    /**
     * Login de vendedor por teléfono y código de afiliación
     */
    suspend fun loginSellerByPhone(phone: String, affiliationCode: String): Result<SellerLoginByPhoneResponse> {
        val startTime = getCurrentTimestampMs()
        val serviceName = "SELLER_AUTH_API"
        
        return try {
            val response = client.post("$baseUrl/api/auth/seller/login-by-phone") {
                contentType(ContentType.Application.Json)
                parameter("phone", phone)
                parameter("affiliationCode", affiliationCode)
            }
            
            val duration = calculateDurationMs(startTime)
            
            if (response.status.isSuccess()) {
                val loginResponse = response.body<SellerLoginByPhoneResponse>()
                
                if (loginResponse.success && loginResponse.data != null) {
                    val sellerData = loginResponse.data
                    logInfo(serviceName, "Login exitoso - Seller ID: ${sellerData.sellerId} (${duration}ms)")
                } else {
                    logWarning(serviceName, "Login falló - ${loginResponse.message}")
                }
                
                Result.success(loginResponse)
            } else {
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError(serviceName, "Error HTTP ${response.status}")
                    errorBody
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }
                
                Result.failure(Exception("Error en login de vendedor: ${response.status} - $errorMessage"))
            }
        } catch (e: Exception) {
            val duration = calculateDurationMs(startTime)
            logError(serviceName, "Excepción en login: ${e.message} (${duration}ms)")
            Result.failure(e)
        }
    }
}
