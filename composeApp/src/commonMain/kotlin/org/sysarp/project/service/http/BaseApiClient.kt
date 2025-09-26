package org.sysarp.project.service.http

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.sysarp.project.utils.Constants

/**
 * Cliente HTTP base con configuración común para todos los servicios
 */
abstract class BaseApiClient {
    
    protected val baseUrl = Constants.BASE_URL
    
    protected val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000 // Aumentado de 30 a 60 segundos
            connectTimeoutMillis = 15000 // Aumentado de 10 a 15 segundos
            socketTimeoutMillis = 15000  // Aumentado de 10 a 15 segundos
        }
    }
    
    /**
     * Cerrar el cliente HTTP
     */
    fun close() {
        client.close()
    }
    
    /**
     * Logging común para todos los servicios
     */
    protected fun logInfo(service: String, message: String) {
        println("[$service] INFO: $message")
    }
    
    protected fun logError(service: String, message: String) {
        println("[$service] ERROR: $message")
    }
    
}
