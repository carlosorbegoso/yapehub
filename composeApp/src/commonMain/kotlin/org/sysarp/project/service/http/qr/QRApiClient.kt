package org.sysarp.project.service.http

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.sysarp.project.data.GenerateQRResponse
import org.sysarp.project.data.QRCodeData
import org.sysarp.project.data.QrLoginRequest
import org.sysarp.project.data.QrLoginResponse
import org.sysarp.project.data.SellerLoginData
import org.sysarp.project.data.ValidateAffiliationCodeData
import org.sysarp.project.data.ValidateAffiliationCodeRequest
import org.sysarp.project.data.ValidateAffiliationCodeResponse

class QRApiClient : BaseApiClient() {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // Cliente HTTP específico para QR
    private val qrClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 10000
        }
    }
    
    /**
     * Generar código QR en Base64 desde código de afiliación
     */
    suspend fun generateQRBase64(
        affiliationCode: String,
        accessToken: String
    ): Result<QRCodeData> {
        return try {
            val request = ValidateAffiliationCodeRequest(affiliationCode)
            
            val response: HttpResponse = qrClient.post("$baseUrl/api/generate-qr-base64") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(HttpHeaders.ContentType, "application/json")
                setBody(request)
            }
            
            val responseBody = response.bodyAsText()
            
            if (response.status.isSuccess()) {
                val qrResponse = json.decodeFromString<GenerateQRResponse>(responseBody)
                Result.success(qrResponse.data!!)
            } else {
                val errorMessage = "Error ${response.status.value}: ${response.status.description} - $responseBody"
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validar código de afiliación (endpoint público)
     */
    suspend fun validateAffiliationCode(
        affiliationCode: String
    ): Result<ValidateAffiliationCodeData> {
        return try {
            val request = ValidateAffiliationCodeRequest(affiliationCode)
            
            val response: HttpResponse = qrClient.post("$baseUrl/api/validate-affiliation-code") {
                header(HttpHeaders.ContentType, "application/json")
                setBody(request)
            }
            
            val responseBody = response.bodyAsText()
            
            if (response.status.isSuccess()) {
                val validationResponse = json.decodeFromString<ValidateAffiliationCodeResponse>(responseBody)
                Result.success(validationResponse.data!!)
            } else {
                val errorMessage = "Error ${response.status.value}: ${response.status.description} - $responseBody"
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Login de vendedor usando QR (endpoint público)
     */
    suspend fun loginWithQR(
        qrData: String,
        phone: String
    ): Result<SellerLoginData> {
        return try {
            val request = QrLoginRequest(qrData, phone)
            
            val response: HttpResponse = qrClient.post("$baseUrl/api/login-with-qr") {
                header(HttpHeaders.ContentType, "application/json")
                setBody(request)
            }
            
            val responseBody = response.bodyAsText()
            
            if (response.status.isSuccess()) {
                val loginResponse = json.decodeFromString<QrLoginResponse>(responseBody)
                Result.success(loginResponse.data!!)
            } else {
                val errorMessage = "Error ${response.status.value}: ${response.status.description} - $responseBody"
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
