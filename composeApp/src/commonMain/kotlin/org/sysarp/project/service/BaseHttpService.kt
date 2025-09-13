package org.sysarp.project.service

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.sysarp.project.config.ServerConfig
import org.sysarp.project.data.*
import org.sysarp.project.utils.ErrorHandler

abstract class BaseHttpService {
    
    protected val baseUrl = ServerConfig.BASE_URL
    
    protected val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
        
        install(Logging) {
            level = LogLevel.INFO
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 10000
        }
        
        install(DefaultRequest) {
            url(baseUrl)
        }
    }
    
    // Manejo común de errores
    protected suspend fun handleErrorResponse(response: HttpResponse): Exception {
        val responseText = try {
            response.bodyAsText()
        } catch (e: Exception) {
            "Error de comunicación con el servidor (no se pudo leer el cuerpo de la respuesta)"
        }

        return try {
            val error = Json.decodeFromString<ApiError>(responseText)
            println("📋 [HTTP] Error estructurado del servidor: $error")
            Exception(ErrorHandler.getFriendlyErrorMessage(error))
        } catch (e: Exception) {
            // If it's not an ApiError, use the raw text or a default message
            println("📋 [HTTP] Error no estructurado del servidor: $responseText")
            Exception(ErrorHandler.getFriendlyErrorMessage(responseText))
        }
    }
    
    // Manejo común de excepciones de red
    protected fun handleNetworkException(e: Exception): Exception {
        println("💥 [HTTP] Excepción de red: ${e.javaClass.simpleName}")
        println("💥 [HTTP] Mensaje de error: ${e.message}")
        println("💥 [HTTP] Stack trace: ${e.stackTrace.take(5).joinToString("\n")}")
        
        val errorMessage = when {
            e.message?.contains("EPREM") == true -> "Error de conectividad: No se puede conectar al servidor. Verifica la IP y que el servidor esté corriendo."
            e.message?.contains("Connection refused") == true -> "Conexión rechazada: El servidor no está corriendo o no es accesible."
            e.message?.contains("timeout") == true -> "Timeout: El servidor tardó demasiado en responder."
            e.message?.contains("Network is unreachable") == true -> "Red inalcanzable: Verifica tu conexión a internet."
            e.message?.contains("Socket") == true -> "Error de socket: Problema de conectividad de red."
            e.message?.contains("UnknownHostException") == true -> "Host desconocido: No se puede resolver la dirección del servidor."
            else -> "Error de red: ${e.message}"
        }
        
        println("📝 [HTTP] Mensaje de error amigable: $errorMessage")
        return Exception(errorMessage)
    }
    
    // Función común para manejar llamadas API de manera segura
    protected suspend inline fun <reified T> safeApiCall(crossinline call: suspend () -> HttpResponse): Result<T> = withContext(Dispatchers.IO) {
        try {
            val response = call()
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    val result = response.body<T>()
                    Result.success(result)
                }
                HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden, HttpStatusCode.NotFound, HttpStatusCode.Conflict -> {
                    Result.failure(handleErrorResponse(response))
                }
                else -> {
                    Result.failure(Exception("Error desconocido: ${response.status.value}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(handleNetworkException(e))
        }
    }
    
    fun close() {
        httpClient.close()
    }
}
