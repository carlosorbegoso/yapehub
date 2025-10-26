package org.sysarp.project.ui.common.components

import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.http.SecurityErrorDetector
import org.sysarp.project.service.http.SecurityErrorType

/**
 * Manejador especializado para errores de la API
 * Detecta específicamente el formato:
 * {"message": "Invalid access token - may be expired or malformed","code": "SECURITY_ERROR",...}
 */
class ApiErrorHandler(
    private val authService: AuthService,
    private val onNavigateToLogin: () -> Unit
) {
    
    /**
     * Maneja un error de respuesta HTTP
     */
    suspend fun handleHttpError(
        statusCode: Int,
        responseBody: String? = null,
        errorMessage: String? = null
    ): ApiErrorResult {
        
        // Si hay cuerpo de respuesta, analizarlo
        if (!responseBody.isNullOrEmpty()) {
            return handleResponseBodyError(statusCode, responseBody)
        }
        
        // Si no hay cuerpo, usar código de estado y mensaje
        return handleStatusCodeError(statusCode, errorMessage)
    }
    
    /**
     * Maneja errores basados en el cuerpo de la respuesta
     */
    private suspend fun handleResponseBodyError(
        statusCode: Int,
        responseBody: String
    ): ApiErrorResult {
        
        val securityError = SecurityErrorDetector.parseSecurityError(responseBody)
        
        return when (securityError.type) {
            SecurityErrorType.TOKEN_EXPIRED -> {
                // Token expirado - intentar refresh o cerrar sesión
                val refreshResult = authService.refreshToken()
                
                if (refreshResult.isSuccess) {
                    ApiErrorResult(
                        shouldRedirectToLogin = false,
                        shouldRetry = true,
                        userMessage = "Sesión renovada automáticamente",
                        errorType = ApiErrorType.TOKEN_REFRESHED
                    )
                } else {
                    authService.logout()
                    onNavigateToLogin()
                    
                    ApiErrorResult(
                        shouldRedirectToLogin = true,
                        shouldRetry = false,
                        userMessage = "Tu sesión ha expirado. Por favor, inicia sesión nuevamente.",
                        errorType = ApiErrorType.SESSION_EXPIRED
                    )
                }
            }
            
            SecurityErrorType.TOKEN_INVALID,
            SecurityErrorType.SECURITY_VIOLATION -> {
                // Token inválido o violación de seguridad - cerrar sesión inmediatamente
                authService.logout()
                onNavigateToLogin()
                
                ApiErrorResult(
                    shouldRedirectToLogin = true,
                    shouldRetry = false,
                    userMessage = "Error de seguridad. Por favor, inicia sesión nuevamente.",
                    errorType = ApiErrorType.SECURITY_ERROR
                )
            }
            
            SecurityErrorType.PARSE_ERROR,
            SecurityErrorType.UNKNOWN -> {
                // Error desconocido - manejar según código de estado
                handleStatusCodeError(statusCode, securityError.originalMessage)
            }
        }
    }
    
    /**
     * Maneja errores basados en código de estado HTTP
     */
    private suspend fun handleStatusCodeError(
        statusCode: Int,
        errorMessage: String?
    ): ApiErrorResult {
        
        return when (statusCode) {
            401 -> {
                // No autorizado - cerrar sesión
                authService.logout()
                onNavigateToLogin()
                
                ApiErrorResult(
                    shouldRedirectToLogin = true,
                    shouldRetry = false,
                    userMessage = "Tu sesión ha expirado. Por favor, inicia sesión nuevamente.",
                    errorType = ApiErrorType.UNAUTHORIZED
                )
            }
            
            403 -> {
                ApiErrorResult(
                    shouldRedirectToLogin = false,
                    shouldRetry = false,
                    userMessage = "No tienes permisos para realizar esta acción.",
                    errorType = ApiErrorType.FORBIDDEN
                )
            }
            
            404 -> {
                ApiErrorResult(
                    shouldRedirectToLogin = false,
                    shouldRetry = false,
                    userMessage = "El recurso solicitado no fue encontrado.",
                    errorType = ApiErrorType.NOT_FOUND
                )
            }
            
            500, 502, 503, 504 -> {
                ApiErrorResult(
                    shouldRedirectToLogin = false,
                    shouldRetry = true,
                    userMessage = "Error del servidor. Intenta nuevamente en unos momentos.",
                    errorType = ApiErrorType.SERVER_ERROR
                )
            }
            
            else -> {
                ApiErrorResult(
                    shouldRedirectToLogin = false,
                    shouldRetry = false,
                    userMessage = errorMessage ?: "Error desconocido. Intenta nuevamente.",
                    errorType = ApiErrorType.UNKNOWN
                )
            }
        }
    }
    
    /**
     * Verifica si un error requiere redirección al login
     */
    fun shouldRedirectToLogin(
        statusCode: Int,
        responseBody: String? = null
    ): Boolean {
        
        if (statusCode == 401) return true
        
        if (!responseBody.isNullOrEmpty()) {
            val securityError = SecurityErrorDetector.parseSecurityError(responseBody)
            return securityError.shouldRedirectToLogin
        }
        
        return false
    }
}

/**
 * Resultado del manejo de errores de API
 */
data class ApiErrorResult(
    val shouldRedirectToLogin: Boolean,
    val shouldRetry: Boolean,
    val userMessage: String,
    val errorType: ApiErrorType
)

/**
 * Tipos de errores de API
 */
enum class ApiErrorType {
    TOKEN_REFRESHED,
    SESSION_EXPIRED,
    SECURITY_ERROR,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    SERVER_ERROR,
    NETWORK_ERROR,
    UNKNOWN
}

/**
 * Extensión para usar el manejador de errores fácilmente
 */
suspend fun <T> Result<T>.handleApiError(
    apiErrorHandler: ApiErrorHandler,
    statusCode: Int? = null,
    responseBody: String? = null
): Result<T> {
    
    return this.onFailure { exception ->
        val errorMessage = exception.message
        
        // Extraer código de estado del mensaje si no se proporciona
        val code = statusCode ?: extractStatusCodeFromMessage(errorMessage)
        
        if (code != null) {
            apiErrorHandler.handleHttpError(code, responseBody, errorMessage)
        }
    }
}

/**
 * Extrae el código de estado HTTP del mensaje de error
 */
private fun extractStatusCodeFromMessage(errorMessage: String?): Int? {
    if (errorMessage == null) return null
    
    return try {
        // Buscar patrones como "401", "HTTP 401", etc.
        val statusRegex = "\\b(\\d{3})\\b".toRegex()
        val match = statusRegex.find(errorMessage)
        match?.value?.toInt()
    } catch (e: Exception) {
        null
    }
}

/**
 * Función de utilidad para crear un manejador de errores
 */
fun createApiErrorHandler(
    authService: AuthService,
    onNavigateToLogin: () -> Unit
): ApiErrorHandler {
    return ApiErrorHandler(authService, onNavigateToLogin)
}