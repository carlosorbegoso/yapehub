package org.sysarp.project.utils

import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import org.sysarp.project.ui.components.ErrorType

/**
 * Manager centralizado para manejar errores HTTP y de aplicación
 */
object ErrorManager {
    
    /**
     * Parsea un error HTTP y retorna información estructurada
     */
    fun parseHttpError(
        httpStatusCode: Int,
        responseBody: String? = null,
        exception: Throwable? = null
    ): ErrorInfo {
        val errorType = ErrorType.fromHttpCode(httpStatusCode)
        
        return when (errorType) {
            ErrorType.NETWORK -> ErrorInfo(
                type = ErrorType.NETWORK,
                title = "Sin Conexión",
                message = "No se pudo conectar al servidor",
                details = exception?.message ?: "Verifica tu conexión a internet",
                canRetry = true
            )
            
            ErrorType.SERVER -> ErrorInfo(
                type = ErrorType.SERVER,
                title = "Error del Servidor",
                message = "El servidor está experimentando problemas",
                details = parseServerErrorDetails(responseBody, httpStatusCode),
                canRetry = true
            )
            
            ErrorType.VALIDATION -> ErrorInfo(
                type = ErrorType.VALIDATION,
                title = "Datos Inválidos",
                message = "Los datos enviados no son válidos",
                details = parseValidationErrorDetails(responseBody),
                canRetry = false
            )
            
            ErrorType.AUTHENTICATION -> ErrorInfo(
                type = ErrorType.AUTHENTICATION,
                title = "Sesión Expirada",
                message = "Tu sesión ha expirado, inicia sesión nuevamente",
                details = parseAuthErrorDetails(responseBody),
                canRetry = false
            )
            
            ErrorType.PERMISSION -> ErrorInfo(
                type = ErrorType.PERMISSION,
                title = "Sin Permisos",
                message = "No tienes permisos para realizar esta acción",
                details = parsePermissionErrorDetails(responseBody),
                canRetry = false
            )
            
            ErrorType.UNKNOWN -> ErrorInfo(
                type = ErrorType.UNKNOWN,
                title = "Error Inesperado",
                message = "Ha ocurrido un error inesperado",
                details = parseUnknownErrorDetails(responseBody, exception),
                canRetry = true
            )
        }
    }
    
    /**
     * Parsea errores de servidor (500, 502, 503, 504)
     */
    private fun parseServerErrorDetails(responseBody: String?, statusCode: Int): String {
        return try {
            val errorMessage = when (statusCode) {
                500 -> "Error interno del servidor"
                502 -> "Servidor no disponible (Bad Gateway)"
                503 -> "Servicio temporalmente no disponible"
                504 -> "Tiempo de espera agotado (Gateway Timeout)"
                else -> "Error del servidor"
            }
            
            responseBody?.let { body ->
                try {
                    val json = Json.parseToJsonElement(body)
                    val message = json.jsonObject["message"]?.jsonPrimitive?.content
                    if (!message.isNullOrBlank()) {
                        "$errorMessage: $message"
                    } else {
                        errorMessage
                    }
                } catch (e: Exception) {
                    errorMessage
                }
            } ?: errorMessage
        } catch (e: Exception) {
            "Error del servidor (Código: $statusCode)"
        }
    }
    
    /**
     * Parsea errores de validación (400, 422)
     */
    private fun parseValidationErrorDetails(responseBody: String?): String {
        return try {
            responseBody?.let { body ->
                try {
                    val json = Json.parseToJsonElement(body)
                    val message = json.jsonObject["message"]?.jsonPrimitive?.content
                    val errors = json.jsonObject["errors"]?.jsonArray
                    
                    val details = mutableListOf<String>()
                    
                    message?.let { details.add(it) }
                    
                    errors?.forEach { error ->
                        val field = error.jsonObject["field"]?.jsonPrimitive?.content
                        val msg = error.jsonObject["message"]?.jsonPrimitive?.content
                        if (field != null && msg != null) {
                            details.add("$field: $msg")
                        }
                    }
                    
                    if (details.isNotEmpty()) {
                        details.joinToString("\n")
                    } else {
                        "Datos inválidos"
                    }
                } catch (e: Exception) {
                    "Datos inválidos"
                }
            } ?: "Datos inválidos"
        } catch (e: Exception) {
            "Error de validación"
        }
    }
    
    /**
     * Parsea errores de autenticación (401)
     */
    private fun parseAuthErrorDetails(responseBody: String?): String {
        return try {
            responseBody?.let { body ->
                try {
                    val json = Json.parseToJsonElement(body)
                    val message = json.jsonObject["message"]?.jsonPrimitive?.content
                    message ?: "Token inválido o expirado"
                } catch (e: Exception) {
                    "Token inválido o expirado"
                }
            } ?: "Token inválido o expirado"
        } catch (e: Exception) {
            "Error de autenticación"
        }
    }
    
    /**
     * Parsea errores de permisos (403)
     */
    private fun parsePermissionErrorDetails(responseBody: String?): String {
        return try {
            responseBody?.let { body ->
                try {
                    val json = Json.parseToJsonElement(body)
                    val message = json.jsonObject["message"]?.jsonPrimitive?.content
                    message ?: "Acceso denegado"
                } catch (e: Exception) {
                    "Acceso denegado"
                }
            } ?: "Acceso denegado"
        } catch (e: Exception) {
            "Error de permisos"
        }
    }
    
    /**
     * Parsea errores desconocidos
     */
    private fun parseUnknownErrorDetails(responseBody: String?, exception: Throwable?): String {
        return try {
            val details = mutableListOf<String>()
            
            responseBody?.let { body ->
                try {
                    val json = Json.parseToJsonElement(body)
                    val message = json.jsonObject["message"]?.jsonPrimitive?.content
                    message?.let { details.add(it) }
                } catch (e: Exception) {
                    // Ignorar errores de parsing
                }
            }
            
            exception?.message?.let { details.add(it) }
            
            if (details.isNotEmpty()) {
                details.joinToString("\n")
            } else {
                "Error desconocido"
            }
        } catch (e: Exception) {
            "Error inesperado"
        }
    }
    
    /**
     * Maneja excepciones generales
     */
    fun parseException(exception: Throwable): ErrorInfo {
        return when (exception) {
            is java.net.UnknownHostException -> ErrorInfo(
                type = ErrorType.NETWORK,
                title = "Sin Conexión",
                message = "No se pudo conectar al servidor",
                details = "Verifica tu conexión a internet",
                canRetry = true
            )
            
            is java.net.SocketTimeoutException -> ErrorInfo(
                type = ErrorType.NETWORK,
                title = "Tiempo Agotado",
                message = "La conexión tardó demasiado",
                details = "Intenta nuevamente en unos momentos",
                canRetry = true
            )
            
            is java.net.ConnectException -> ErrorInfo(
                type = ErrorType.NETWORK,
                title = "Sin Conexión",
                message = "No se pudo conectar al servidor",
                details = "Verifica tu conexión a internet",
                canRetry = true
            )
            
            else -> ErrorInfo(
                type = ErrorType.UNKNOWN,
                title = "Error Inesperado",
                message = "Ha ocurrido un error inesperado",
                details = exception.message ?: "Error desconocido",
                canRetry = true
            )
        }
    }
}

/**
 * Información estructurada de un error
 */
data class ErrorInfo(
    val type: ErrorType,
    val title: String,
    val message: String,
    val details: String? = null,
    val canRetry: Boolean = false,
    val httpCode: Int? = null
)
