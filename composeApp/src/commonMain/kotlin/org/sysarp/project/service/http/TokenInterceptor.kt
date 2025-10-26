package org.sysarp.project.service.http

import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.sysarp.project.service.auth.AuthService

/**
 * Interceptor que maneja automáticamente la expiración de tokens
 * en todas las respuestas HTTP
 */
object TokenInterceptor {
    
    private val _tokenExpiredEvents = MutableSharedFlow<TokenExpiredEvent>()
    val tokenExpiredEvents: SharedFlow<TokenExpiredEvent> = _tokenExpiredEvents.asSharedFlow()
    
    /**
     * Verifica si una respuesta indica token expirado y maneja la situación
     */
    suspend fun handleResponse(response: HttpResponse, authService: AuthService) {
        if (response.status == HttpStatusCode.Unauthorized || 
            response.status == HttpStatusCode.Forbidden) {
            
            if (isTokenExpiredResponse(response)) {
                // Emitir evento de token expirado
                _tokenExpiredEvents.tryEmit(
                    TokenExpiredEvent(
                        url = response.call.request.url.toString(),
                        statusCode = response.status.value,
                        message = "Token expirado"
                    )
                )
                
                // Manejar la expiración
                handleTokenExpiration(authService)
            }
        }
    }
    
    /**
     * Verifica si una respuesta indica token expirado (versión síncrona)
     */
    private fun isTokenExpiredResponseSync(response: HttpResponse): Boolean {
        return response.status == HttpStatusCode.Unauthorized || 
               response.status == HttpStatusCode.Forbidden
    }
    
    /**
     * Verifica si una respuesta indica token expirado (versión completa con body)
     * Detecta el formato específico de error de la API:
     * {"message": "Invalid access token - may be expired or malformed","code": "SECURITY_ERROR",...}
     */
    private suspend fun isTokenExpiredResponse(response: HttpResponse): Boolean {
        return when {
            // Códigos de estado que indican problemas de autenticación
            response.status == HttpStatusCode.Unauthorized -> true
            response.status == HttpStatusCode.Forbidden -> true
            
            // Para otros códigos, verificar el cuerpo de la respuesta
            else -> {
                try {
                    val body = response.bodyAsText()
                    val bodyLower = body.lowercase()
                    
                    // Detectar el formato específico de tu API
                    val hasSecurityError = bodyLower.contains("\"code\":\"security_error\"") ||
                                         bodyLower.contains("\"code\": \"security_error\"")
                    
                    val hasTokenError = bodyLower.contains("invalid access token") ||
                                      bodyLower.contains("token expired") ||
                                      bodyLower.contains("token invalid") ||
                                      bodyLower.contains("may be expired") ||
                                      bodyLower.contains("malformed") ||
                                      bodyLower.contains("session expired") ||
                                      bodyLower.contains("jwt expired")
                    
                    // Si tiene código SECURITY_ERROR o mensajes relacionados con tokens
                    hasSecurityError || hasTokenError
                } catch (e: Exception) {
                    // Si no se puede leer el body, usar solo el código de estado
                    false
                }
            }
        }
    }
    
    /**
     * Maneja la expiración del token
     */
    private suspend fun handleTokenExpiration(authService: AuthService) {
        try {
            // Intentar refrescar el token
            val refreshResult = authService.refreshToken()
            
            if (refreshResult.isFailure) {
                // Si no se puede refrescar, cerrar sesión
                authService.logout()
                
                // Emitir evento de sesión expirada
                _tokenExpiredEvents.tryEmit(
                    TokenExpiredEvent(
                        url = "",
                        statusCode = 401,
                        message = "Sesión expirada - redirigiendo al login"
                    )
                )
            }
        } catch (e: Exception) {
            // Si hay error, cerrar sesión
            authService.logout()
            
            _tokenExpiredEvents.tryEmit(
                TokenExpiredEvent(
                    url = "",
                    statusCode = 401,
                    message = "Error al refrescar token - redirigiendo al login"
                )
            )
        }
    }
}

/**
 * Evento que se emite cuando un token expira
 */
data class TokenExpiredEvent(
    val url: String,
    val statusCode: Int,
    val message: String,
    val errorCode: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Estructura de error de la API para problemas de seguridad
 */
data class SecurityErrorResponse(
    val message: String,
    val code: String,
    val details: Map<String, Any>? = null,
    val timestamp: String
)

/**
 * Utilidades para detectar errores de seguridad específicos de la API
 */
object SecurityErrorDetector {
    
    /**
     * Detecta si un cuerpo de respuesta contiene un error de seguridad
     */
    fun isSecurityError(responseBody: String): Boolean {
        val bodyLower = responseBody.lowercase()
        
        return bodyLower.contains("\"code\":\"security_error\"") ||
               bodyLower.contains("\"code\": \"security_error\"") ||
               bodyLower.contains("security_error")
    }
    
    /**
     * Detecta si un mensaje indica token expirado o inválido
     */
    fun isTokenError(responseBody: String): Boolean {
        val bodyLower = responseBody.lowercase()
        
        return bodyLower.contains("invalid access token") ||
               bodyLower.contains("token expired") ||
               bodyLower.contains("token invalid") ||
               bodyLower.contains("may be expired") ||
               bodyLower.contains("malformed") ||
               bodyLower.contains("session expired") ||
               bodyLower.contains("jwt expired")
    }
    
    /**
     * Extrae información específica del error de seguridad
     */
    fun parseSecurityError(responseBody: String): SecurityErrorInfo {
        return try {
            val bodyLower = responseBody.lowercase()
            
            when {
                isSecurityError(responseBody) && isTokenError(responseBody) -> {
                    SecurityErrorInfo(
                        type = SecurityErrorType.TOKEN_EXPIRED,
                        message = "Tu sesión ha expirado",
                        originalMessage = extractMessage(responseBody),
                        shouldRedirectToLogin = true
                    )
                }
                isSecurityError(responseBody) -> {
                    SecurityErrorInfo(
                        type = SecurityErrorType.SECURITY_VIOLATION,
                        message = "Error de seguridad",
                        originalMessage = extractMessage(responseBody),
                        shouldRedirectToLogin = true
                    )
                }
                isTokenError(responseBody) -> {
                    SecurityErrorInfo(
                        type = SecurityErrorType.TOKEN_INVALID,
                        message = "Token inválido",
                        originalMessage = extractMessage(responseBody),
                        shouldRedirectToLogin = true
                    )
                }
                else -> {
                    SecurityErrorInfo(
                        type = SecurityErrorType.UNKNOWN,
                        message = "Error desconocido",
                        originalMessage = responseBody,
                        shouldRedirectToLogin = false
                    )
                }
            }
        } catch (e: Exception) {
            SecurityErrorInfo(
                type = SecurityErrorType.PARSE_ERROR,
                message = "Error al procesar respuesta",
                originalMessage = responseBody,
                shouldRedirectToLogin = false
            )
        }
    }
    
    /**
     * Extrae el mensaje del JSON de error
     */
    private fun extractMessage(responseBody: String): String {
        return try {
            // Buscar el patrón "message":"..." en el JSON
            val messageRegex = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            val matchResult = messageRegex.find(responseBody)
            matchResult?.groupValues?.get(1) ?: responseBody
        } catch (e: Exception) {
            responseBody
        }
    }
}

/**
 * Información sobre errores de seguridad
 */
data class SecurityErrorInfo(
    val type: SecurityErrorType,
    val message: String,
    val originalMessage: String,
    val shouldRedirectToLogin: Boolean
)

/**
 * Tipos de errores de seguridad
 */
enum class SecurityErrorType {
    TOKEN_EXPIRED,
    TOKEN_INVALID,
    SECURITY_VIOLATION,
    PARSE_ERROR,
    UNKNOWN
}

/**
 * Extensión para HttpResponse que verifica automáticamente tokens expirados
 */
suspend fun HttpResponse.checkTokenExpiration(authService: AuthService) {
    TokenInterceptor.handleResponse(this, authService)
}

/**
 * Manejador de eventos de token expirado para la UI
 */
class TokenExpirationUIHandler(
    private val onNavigateToLogin: () -> Unit
) {
    
    /**
     * Inicia la escucha de eventos de token expirado
     */
    suspend fun startListening() {
        TokenInterceptor.tokenExpiredEvents.collect { event ->
            when {
                event.message.contains("redirigiendo al login") -> {
                    onNavigateToLogin()
                }
                event.statusCode == 401 -> {
                    // Token expirado, navegar al login
                    onNavigateToLogin()
                }
            }
        }
    }
}