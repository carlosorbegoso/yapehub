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
import org.sysarp.project.utils.Constants

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
            
            val response: HttpResponse = httpClient.post("${Constants.BASE_URL}/api/generate-affiliation-code-protected") {
                header(HttpHeaders.Accept, "*/*")
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                parameter("adminId", adminId)
                parameter("branchId", branchId)
                parameter("expirationHours", expirationHours)
                parameter("maxUses", maxUses)
                parameter("notes", notes)
            }
            
            val responseBody = response.bodyAsText()
            
            if (response.status.isSuccess()) {
                val affiliationResponse = json.decodeFromString<AffiliationCodeResponse>(responseBody)
                Result.success(affiliationResponse.data!!)
            } else {
                val errorMessage = "Error ${response.status.value}: ${response.status.description}"
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            println("❌ [AFFILIATION_API] Error generando código: ${e.message}")
            Result.failure(e)
        }
    }
}
