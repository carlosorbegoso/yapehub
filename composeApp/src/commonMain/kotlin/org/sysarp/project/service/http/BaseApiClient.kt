package org.sysarp.project.service.http

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.sysarp.project.service.getHttpClientEngine
import org.sysarp.project.utils.Constants
import org.sysarp.project.utils.getFormattedTimestamp
import org.sysarp.project.utils.calculateDurationMs
import org.sysarp.project.utils.getCurrentTimestampMs

/**
 * Cliente HTTP base con configuración común para todos los servicios
 */
abstract class BaseApiClient {
    
    protected val baseUrl = Constants.BASE_URL
    
    protected val client = HttpClient(getHttpClientEngine()) {
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
     * Obtiene el timestamp actual formateado usando DateUtils
     */
    private fun getCurrentTimestamp(): String {
        return getFormattedTimestamp()
    }
    
    /**
     * Enmascara datos sensibles en logs
     */
    private fun maskSensitiveData(data: String): String {
        return data.replace(Regex("""(phone|teléfono|telefono)\s*[:=]\s*(\d+)""", RegexOption.IGNORE_CASE)) { matchResult ->
            val phone = matchResult.groupValues[2]
            val maskedPhone = if (phone.length > 4) {
                "${phone.take(3)}***${phone.takeLast(2)}"
            } else {
                "***"
            }
            "${matchResult.groupValues[1]}: $maskedPhone"
        }.replace(Regex("""(token|accessToken|authToken)\s*[:=]\s*([A-Za-z0-9._-]+)""", RegexOption.IGNORE_CASE)) { matchResult ->
            val token = matchResult.groupValues[2]
            val maskedToken = if (token.length > 20) {
                "${token.take(10)}...${token.takeLast(10)}"
            } else {
                "***"
            }
            "${matchResult.groupValues[1]}: $maskedToken"
        }.replace(Regex("""(email|correo)\s*[:=]\s*([A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,})""", RegexOption.IGNORE_CASE)) { matchResult ->
            val email = matchResult.groupValues[2]
            val parts = email.split("@")
            val maskedEmail = if (parts[0].length > 2) {
                "${parts[0].take(2)}***@${parts[1]}"
            } else {
                "***@${parts[1]}"
            }
            "${matchResult.groupValues[1]}: $maskedEmail"
        }
    }
    
    /**
     * Logging común para todos los servicios - INFO level
     */
    protected fun logInfo(service: String, message: String) {
        val timestamp = getCurrentTimestamp()
        val maskedMessage = maskSensitiveData(message)
        println("[$timestamp] [$service] INFO: $maskedMessage")
    }
    
    /**
     * Logging común para todos los servicios - ERROR level
     */
    protected fun logError(service: String, message: String) {
        val timestamp = getCurrentTimestamp()
        val maskedMessage = maskSensitiveData(message)
        println("[$timestamp] [$service] ERROR: $maskedMessage")
    }
    
    /**
     * Logging común para todos los servicios - DEBUG level
     */
    protected fun logDebug(service: String, message: String) {
        val timestamp = getCurrentTimestamp()
        val maskedMessage = maskSensitiveData(message)
        println("[$timestamp] [$service] DEBUG: $maskedMessage")
    }
    
    /**
     * Logging común para todos los servicios - WARNING level
     */
    protected fun logWarning(service: String, message: String) {
        val timestamp = getCurrentTimestamp()
        val maskedMessage = maskSensitiveData(message)
        println("[$timestamp] [$service] WARNING: $maskedMessage")
    }
    
    /**
     * Logging de inicio de operación con métricas de tiempo
     */
    protected fun logOperationStart(service: String, operation: String, details: String? = null) {
        val message = "Iniciando $operation" + if (details != null) " - $details" else ""
        logInfo(service, message)
    }
    
    /**
     * Logging de finalización de operación con métricas de tiempo
     */
    protected fun logOperationEnd(service: String, operation: String, success: Boolean, durationMs: Long? = null) {
        val status = if (success) "exitoso" else "fallido"
        val duration = if (durationMs != null) " en ${durationMs}ms" else ""
        val message = "$operation $status$duration"
        
        if (success) {
            logInfo(service, message)
        } else {
            logError(service, message)
        }
    }
    
}