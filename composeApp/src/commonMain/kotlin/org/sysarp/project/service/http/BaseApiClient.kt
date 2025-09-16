package org.sysarp.project.service.http

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.sysarp.project.utils.Logger
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
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 10000
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
        Logger.auth(service, message)
    }
    
    protected fun logError(service: String, message: String) {
        Logger.auth(service, "ERROR: $message")
    }
}

