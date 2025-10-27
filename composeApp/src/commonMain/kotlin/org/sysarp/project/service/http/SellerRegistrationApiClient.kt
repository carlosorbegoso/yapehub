package org.sysarp.project.service.http

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.sysarp.project.data.ApiError
import org.sysarp.project.data.SellerRegistrationRequest
import org.sysarp.project.data.SellerRegistrationResponse
import org.sysarp.project.utils.calculateDurationMs
import org.sysarp.project.utils.getCurrentTimestampMs

/**
 * Cliente API especializado para registro de vendedores
 */
class SellerRegistrationApiClient : BaseApiClient() {
    

    suspend fun registerSeller(
        affiliationCode: String,
        sellerName: String,
        phone: String
    ): Result<SellerRegistrationResponse> {
        val startTime = getCurrentTimestampMs()
        val serviceName = "SELLER_REGISTRATION_API"
        
        return try {
            val requestData = SellerRegistrationRequest(
                affiliationCode = affiliationCode,
                sellerName = sellerName,
                phone = phone
            )
            
            val response = client.post("$baseUrl/api/seller/register") {
                contentType(ContentType.Application.Json)
                setBody(requestData)
            }
            
            val duration = calculateDurationMs(startTime)
            
            if (response.status.isSuccess()) {
                val registrationResponse = response.body<SellerRegistrationResponse>()
                
                if (registrationResponse.success && registrationResponse.data != null) {
                    val sellerData = registrationResponse.data
                    logInfo(serviceName, "Registro exitoso - Seller ID: ${sellerData.sellerId} (${duration}ms)")
                } else {
                    logWarning(serviceName, "Registro falló - ${registrationResponse.message}")
                }
                
                Result.success(registrationResponse)
            } else {
                // Manejar errores específicos de vendedor
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError(serviceName, "Error HTTP ${response.status}")
                    
                    try {
                        // Intentar parsear como ApiError primero
                        val apiError = kotlinx.serialization.json.Json.decodeFromString<ApiError>(errorBody)
                        
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
                        errorBody
                    }
                } catch (e: Exception) {
                    "Error desconocido: ${e.message}"
                }
                
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            val duration = calculateDurationMs(startTime)
            logError(serviceName, "Excepción en registro: ${e.message} (${duration}ms)")
            Result.failure(e)
        }
    }
    
    /**
     * Devuelve el mensaje original del servidor ya que viene detallado
     */
    private fun getFriendlyMessage(message: String): String {
        return message
    }
}
