package org.sysarp.project.ui.common.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sysarp.project.data.AuthState
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.http.TokenExpirationUIHandler
import org.sysarp.project.service.http.TokenInterceptor

/**
 * Gestor de sesión que maneja automáticamente:
 * - Expiración de tokens
 * - Redirección al login
 * - Verificación periódica de sesión
 * - Interceptación de respuestas HTTP 401
 */
@Composable
fun SessionManager(
    authService: AuthService,
    onNavigateToLogin: () -> Unit,
    onSessionExpired: (String) -> Unit = { reason ->
        // Mostrar mensaje de sesión expirada
        println("Sesión expirada: $reason")
        onNavigateToLogin()
    },
    checkIntervalSeconds: Long = 30,
    content: @Composable () -> Unit
) {
    val authState by authService.authState.collectAsState()
    val userProfile by authService.userProfile.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    var sessionCheckEnabled by remember { mutableStateOf(false) }
    
    // Configurar el manejador de eventos de token expirado
    LaunchedEffect(Unit) {
        val uiHandler = TokenExpirationUIHandler(onNavigateToLogin)
        
        // Iniciar escucha de eventos de token expirado
        coroutineScope.launch {
            uiHandler.startListening()
        }
    }
    
    // Verificación periódica de sesión
    LaunchedEffect(authState, userProfile) {
        sessionCheckEnabled = authState == AuthState.AUTHENTICATED && userProfile != null
        
        if (sessionCheckEnabled) {
            while (sessionCheckEnabled) {
                try {
                    val isSessionValid = authService.isSessionValid()
                    
                    if (!isSessionValid) {
                        sessionCheckEnabled = false
                        onSessionExpired("Token expirado y no se pudo renovar")
                        break
                    }
                    
                    // Verificar si el token está próximo a expirar
                    if (authService.shouldRefreshToken()) {
                        val refreshResult = authService.refreshToken()
                        
                        if (refreshResult.isFailure) {
                            sessionCheckEnabled = false
                            onSessionExpired("No se pudo renovar el token")
                            break
                        }
                    }
                    
                } catch (e: Exception) {
                    sessionCheckEnabled = false
                    onSessionExpired("Error al verificar sesión: ${e.message}")
                    break
                }
                
                delay(checkIntervalSeconds * 1000)
            }
        }
    }
    
    // Renderizar contenido
    content()
}

/**
 * Composable simplificado para manejar sesiones
 */
@Composable
fun AutoSessionHandler(
    authService: AuthService,
    onNavigateToLogin: () -> Unit,
    content: @Composable () -> Unit
) {
    SessionManager(
        authService = authService,
        onNavigateToLogin = onNavigateToLogin,
        onSessionExpired = { reason ->
            // Log del motivo de expiración
            println("🔒 Sesión expirada: $reason")
            
            // Limpiar sesión y navegar
            kotlinx.coroutines.GlobalScope.launch {
                authService.logout()
                onNavigateToLogin()
            }
        },
        checkIntervalSeconds = 30
    ) {
        content()
    }
}

/**
 * Hook para usar en ViewModels o servicios
 */
class SessionValidator(
    private val authService: AuthService,
    private val onSessionExpired: () -> Unit
) {
    
    /**
     * Valida que la sesión sea válida antes de ejecutar una operación
     */
    suspend fun validateSession(): Boolean {
        return try {
            val isValid = authService.isSessionValid()
            
            if (!isValid) {
                onSessionExpired()
                false
            } else {
                true
            }
        } catch (e: Exception) {
            onSessionExpired()
            false
        }
    }
    
    /**
     * Ejecuta una operación solo si la sesión es válida
     */
    suspend fun <T> withValidSession(
        operation: suspend () -> T
    ): Result<T> {
        return try {
            if (validateSession()) {
                Result.success(operation())
            } else {
                Result.failure(SessionExpiredException("Sesión expirada"))
            }
        } catch (e: Exception) {
            if (isSessionExpiredException(e)) {
                onSessionExpired()
                Result.failure(SessionExpiredException("Sesión expirada"))
            } else {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Verifica si una excepción indica sesión expirada
     */
    private fun isSessionExpiredException(exception: Throwable): Boolean {
        val message = exception.message?.lowercase() ?: ""
        return message.contains("401") || 
               message.contains("unauthorized") || 
               message.contains("token") && (message.contains("expired") || message.contains("invalid")) ||
               message.contains("session") && message.contains("expired")
    }
}

/**
 * Excepción para sesiones expiradas
 */
class SessionExpiredException(message: String) : Exception(message)

/**
 * Extensión para Result que maneja automáticamente sesiones expiradas
 */
suspend fun <T> Result<T>.handleSessionExpiration(
    authService: AuthService,
    onSessionExpired: () -> Unit
): Result<T> {
    return this.onFailure { exception ->
        val message = exception.message?.lowercase() ?: ""
        
        if (message.contains("401") || 
            message.contains("unauthorized") || 
            message.contains("session expired") ||
            (message.contains("token") && (message.contains("expired") || message.contains("invalid")))) {
            
            // Cerrar sesión y notificar
            kotlinx.coroutines.GlobalScope.launch {
                authService.logout()
                onSessionExpired()
            }
        }
    }
}

/**
 * Utilidades para manejar sesiones
 */
object SessionUtils {
    
    /**
     * Verifica si un error HTTP indica sesión expirada
     * Incluye detección del formato específico de la API
     */
    fun isSessionExpiredError(
        statusCode: Int? = null,
        errorMessage: String? = null,
        responseBody: String? = null
    ): Boolean {
        // Verificar código de estado
        if (statusCode == 401 || statusCode == 403) {
            return true
        }
        
        // Verificar el cuerpo de la respuesta si está disponible
        if (!responseBody.isNullOrEmpty()) {
            val securityError = org.sysarp.project.service.http.SecurityErrorDetector.parseSecurityError(responseBody)
            return securityError.shouldRedirectToLogin
        }
        
        // Verificar mensaje de error como fallback
        val message = errorMessage?.lowercase() ?: ""
        return message.contains("token expired") ||
               message.contains("token invalid") ||
               message.contains("session expired") ||
               message.contains("unauthorized") ||
               message.contains("jwt expired") ||
               message.contains("invalid access token") ||
               message.contains("security_error") ||
               message.contains("may be expired") ||
               message.contains("malformed")
    }
    
    /**
     * Extrae información útil de un error de sesión
     */
    fun parseSessionError(exception: Throwable): SessionErrorInfo {
        val message = exception.message ?: ""
        
        return when {
            message.contains("401") -> SessionErrorInfo(
                type = SessionErrorType.TOKEN_EXPIRED,
                message = "Tu sesión ha expirado",
                shouldRedirectToLogin = true
            )
            message.contains("403") -> SessionErrorInfo(
                type = SessionErrorType.PERMISSION_DENIED,
                message = "No tienes permisos para realizar esta acción",
                shouldRedirectToLogin = false
            )
            message.contains("network") -> SessionErrorInfo(
                type = SessionErrorType.NETWORK_ERROR,
                message = "Error de conexión",
                shouldRedirectToLogin = false
            )
            else -> SessionErrorInfo(
                type = SessionErrorType.UNKNOWN,
                message = "Error desconocido",
                shouldRedirectToLogin = false
            )
        }
    }
}

/**
 * Información sobre errores de sesión
 */
data class SessionErrorInfo(
    val type: SessionErrorType,
    val message: String,
    val shouldRedirectToLogin: Boolean
)

/**
 * Tipos de errores de sesión
 */
enum class SessionErrorType {
    TOKEN_EXPIRED,
    PERMISSION_DENIED,
    NETWORK_ERROR,
    UNKNOWN
}