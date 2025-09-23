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

/**
 * Cliente API especializado para registro de vendedores
 */
class SellerRegistrationApiClient : BaseApiClient() {
    
    /**
     * Registro de vendedor con código de afiliación
     */
    suspend fun registerSeller(
        affiliationCode: String,
        sellerName: String,
        phone: String
    ): Result<SellerRegistrationResponse> {
        return try {
            logInfo("SELLER_REGISTRATION_API", "Intentando registro de vendedor con código: $affiliationCode")
            
            val requestData = SellerRegistrationRequest(
                affiliationCode = affiliationCode,
                sellerName = sellerName,
                phone = phone
            )
            
            logInfo("SELLER_REGISTRATION_API", "Datos de registro: affiliationCode=$affiliationCode, sellerName=$sellerName, phone=$phone")
            
            val response = client.post("$baseUrl/api/seller/register") {
                contentType(ContentType.Application.Json)
                setBody(requestData)
            }
            
            if (response.status.isSuccess()) {
                val registrationResponse = response.body<SellerRegistrationResponse>()
                logInfo("SELLER_REGISTRATION_API", "Registro de vendedor exitoso con código: $affiliationCode")
                Result.success(registrationResponse)
            } else {
                // Manejar errores específicos de vendedor
                val errorMessage = try {
                    val errorBody = response.body<String>()
                    logError("SELLER_REGISTRATION_API", "Error body: $errorBody")
                    
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
                
                logError("SELLER_REGISTRATION_API", "Error en registro de vendedor: ${response.status} - $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("SELLER_REGISTRATION_API", "Error en registro de vendedor: ${e.message}")
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
