package org.sysarp.project.service.http

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import org.sysarp.project.data.AffiliationCodeData
import org.sysarp.project.data.AffiliationCodeRequest
import org.sysarp.project.data.AffiliationCodeResponse

class AffiliationApiClient(private val httpClient: HttpClient) {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    suspend fun generateAffiliationCode(
        adminId: Int,
        branchId: Int,
        expirationHours: Int,
        maxUses: Int,
        notes: String,
        accessToken: String
    ): Result<AffiliationCodeData> {
        return try {
            println("🔐 [AFFILIATION_API] Generando código de afiliación para admin: $adminId, branch: $branchId")
            
            val response: HttpResponse = httpClient.post("http://localhost:8080/api/generate-affiliation-code-protected") {
                header(HttpHeaders.Accept, "*/*")
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                parameter("adminId", adminId)
                parameter("branchId", branchId)
                parameter("expirationHours", expirationHours)
                parameter("maxUses", maxUses)
                parameter("notes", notes)
            }
            
            val responseBody = response.bodyAsText()
            println("🔐 [AFFILIATION_API] Respuesta raw: $responseBody")
            
            if (response.status.isSuccess()) {
                val affiliationResponse = json.decodeFromString<AffiliationCodeResponse>(responseBody)
                println("🔐 [AFFILIATION_API] Código de afiliación generado exitosamente: ${affiliationResponse.data?.affiliationCode}")
                Result.success(affiliationResponse.data!!)
            } else {
                val errorMessage = "Error ${response.status.value}: ${response.status.description}"
                println("🔐 [AFFILIATION_API] ERROR: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            println("🔐 [AFFILIATION_API] ERROR: Error generando código de afiliación: ${e.message}")
            Result.failure(e)
        }
    }
}
