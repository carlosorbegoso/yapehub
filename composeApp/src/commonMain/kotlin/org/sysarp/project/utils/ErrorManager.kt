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
