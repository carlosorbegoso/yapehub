package org.sysarp.project.service.http

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.serialization.json.Json
import org.sysarp.project.data.AffiliationCodeData
import org.sysarp.project.data.AffiliationCodeRequest
import org.sysarp.project.data.AffiliationCodeResponse
import org.sysarp.project.utils.Constants

class AffiliationApiClient : BaseApiClient() {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // Cliente HTTP específico sin ContentNegotiation para evitar Content-Type automático
    private val affiliationClient = HttpClient {
        install(Logging) {
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 10000
        }
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
            val response: HttpResponse = affiliationClient.post("$baseUrl/api/generate-affiliation-code-protected") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(HttpHeaders.ContentType, "application/json")
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
                val errorMessage = "Error ${response.status.value}: ${response.status.description} - $responseBody"
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
