package org.sysarp.project.service.http

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.sysarp.project.data.GenerateAffiliationCodeResponse

/**
 * Cliente API especializado para gestión de códigos de afiliación
 */
class AffiliationCodeApiClient : BaseApiClient() {
    
    /**
     * Generar código de afiliación
     */
    suspend fun generateAffiliationCode(
        adminId: Int,
        branchId: Int,
        expirationHours: Int,
        maxUses: Int,
        notes: String,
        accessToken: String
    ): Result<GenerateAffiliationCodeResponse> {
        return try {
            logInfo("AFFILIATION_CODE_API", "Generando código de afiliación para admin: $adminId")
            
            val response = client.post("$baseUrl/api/generate-affiliation-code-protected") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $accessToken")
                parameter("adminId", adminId)
                parameter("branchId", branchId)
                parameter("expirationHours", expirationHours)
                parameter("maxUses", maxUses)
                parameter("notes", notes)
            }
            
            if (response.status.isSuccess()) {
                val affiliationResponse = response.body<GenerateAffiliationCodeResponse>()
                logInfo("AFFILIATION_CODE_API", "Código de afiliación generado exitosamente")
                Result.success(affiliationResponse)
            } else {
                val errorMessage = "Error generando código de afiliación: ${response.status}"
                logError("AFFILIATION_CODE_API", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            logError("AFFILIATION_CODE_API", "Error generando código de afiliación: ${e.message}")
            Result.failure(e)
        }
    }
    
}
