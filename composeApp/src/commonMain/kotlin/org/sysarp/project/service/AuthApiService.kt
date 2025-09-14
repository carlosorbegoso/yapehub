package org.sysarp.project.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.sysarp.project.data.ApiResponse
import org.sysarp.project.data.LoginUserData
import org.sysarp.project.data.SellerUserData

class AuthApiService {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    private val baseUrl = "https://ks9ql0l7-8080.brs.devtunnels.ms/api"
    
    suspend fun loginAdmin(email: String, password: String, deviceFingerprint: String): ApiResponse<LoginUserData> {
        return try {
            val response = httpClient.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "email" to email,
                    "password" to password,
                    "deviceFingerprint" to deviceFingerprint,
                    "role" to "ADMIN"
                ))
            }
            
            if (response.status.isSuccess()) {
                val loginData = response.body<LoginUserData>()
                ApiResponse(success = true, data = loginData, message = "Login exitoso")
            } else {
                ApiResponse(success = false, message = "Error en login")
            }
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Error desconocido")
        }
    }
    
    suspend fun loginSellerByPhone(phone: String, password: String, deviceFingerprint: String): ApiResponse<SellerUserData> {
        return try {
            val response = httpClient.post("$baseUrl/auth/seller-login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "phone" to phone,
                    "password" to password,
                    "deviceFingerprint" to deviceFingerprint
                ))
            }
            
            if (response.status.isSuccess()) {
                val sellerData = response.body<SellerUserData>()
                ApiResponse(success = true, data = sellerData, message = "Login exitoso")
            } else {
                ApiResponse(success = false, message = "Error en login")
            }
        } catch (e: Exception) {
            ApiResponse(success = false, message = e.message ?: "Error desconocido")
        }
    }
}
