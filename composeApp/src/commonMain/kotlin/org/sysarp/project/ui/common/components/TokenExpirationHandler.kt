package org.sysarp.project.ui.common.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sysarp.project.data.AuthState
import org.sysarp.project.service.auth.AuthService

/**
 * Componente que maneja automáticamente la expiración de tokens
 * y redirige al login cuando es necesario
 */
@Composable
fun TokenExpirationHandler(
    authService: AuthService,
    onTokenExpired: () -> Unit,
    onSessionExpired: () -> Unit = onTokenExpired,
    checkIntervalMs: Long = 30000L, // Verificar cada 30 segundos
    content: @Composable () -> Unit
) {
    val authState by authService.authState.collectAsState()
    val userProfile by authService.userProfile.collectAsState()
    var lastCheckTime by remember { mutableStateOf(0L) }
    
    // Verificación periódica de tokens
    LaunchedEffect(authState, userProfile) {
        if (authState == AuthState.AUTHENTICATED && userProfile != null) {
            while (true) {
                val currentTime = System.currentTimeMillis()
                
                // Solo verificar si ha pasado el intervalo
                if (currentTime - lastCheckTime >= checkIntervalMs) {
                    lastCheckTime = currentTime
                    
                    val isSessionValid = authService.isSessionValid()
                    
                    if (!isSessionValid) {
                        // Token expirado y no se pudo refrescar
                        onSessionExpired()
                        break
                    }
                }
                
                delay(checkIntervalMs)
            }
        }
    }
    
    // Renderizar contenido
    content()
}

/**
 * Hook para manejar la expiración de tokens en ViewModels o servicios
 */
class TokenExpirationManager(
    private val authService: AuthService,
    private val onTokenExpired: () -> Unit
) {
    
    /**
     * Verifica si el token es válido antes de hacer una operación
     */
    suspend fun ensureValidToken(): Boolean {
        return try {
            val isValid = authService.isSessionValid()
            
            if (!isValid) {
                onTokenExpired()
                false
            } else {
                true
            }
        } catch (e: Exception) {
            onTokenExpired()
            false
        }
    }
    
    /**
     * Ejecuta una operación solo si el token es válido
     */
    suspend fun <T> withValidToken(
        operation: suspend () -> T
    ): Result<T> {
        return try {
            if (ensureValidToken()) {
                Result.success(operation())
            } else {
                Result.failure(TokenExpiredException("Token expirado"))
            }
        } catch (e: Exception) {
            if (isTokenExpiredException(e)) {
                onTokenExpired()
                Result.failure(TokenExpiredException("Token expirado"))
            } else {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Verifica si una excepción indica que el token expiró
     */
    private fun isTokenExpiredException(exception: Throwable): Boolean {
        val message = exception.message?.lowercase() ?: ""
        return message.contains("401") || 
               message.contains("unauthorized") || 
               message.contains("token") && (message.contains("expired") || message.contains("invalid"))
    }
}

/**
 * Excepción personalizada para tokens expirados
 */
class TokenExpiredException(message: String) : Exception(message)

/**
 * Interceptor para respuestas HTTP que detecta tokens expirados
 */
object TokenExpirationInterceptor {
    
    /**
     * Verifica si una respuesta HTTP indica token expirado
     */
    fun isTokenExpiredResponse(
        statusCode: Int,
        responseBody: String? = null
    ): Boolean {
        // Código 401 indica no autorizado
        if (statusCode == 401) {
            return true
        }
        
        // Verificar mensaje en el cuerpo de la respuesta
        val body = responseBody?.lowercase() ?: ""
        return body.contains("token expired") || 
               body.contains("token invalid") || 
               body.contains("unauthorized") ||
               body.contains("session expired")
    }
    
    /**
     * Maneja una respuesta de token expirado
     */
    suspend fun handleTokenExpiredResponse(
        authService: AuthService,
        onTokenExpired: () -> Unit
    ) {
        try {
            // Intentar refrescar el token una vez
            val refreshResult = authService.refreshToken()
            
            if (refreshResult.isFailure) {
                // Si no se puede refrescar, cerrar sesión
                authService.logout()
                onTokenExpired()
            }
        } catch (e: Exception) {
            // Si hay error al refrescar, cerrar sesión
            authService.logout()
            onTokenExpired()
        }
    }
}

/**
 * Extensión para usar el interceptor fácilmente
 */
suspend fun <T> Result<T>.handleTokenExpiration(
    authService: AuthService,
    onTokenExpired: () -> Unit
): Result<T> {
    return this.onFailure { exception ->
        val message = exception.message?.lowercase() ?: ""
        
        if (message.contains("401") || 
            message.contains("unauthorized") || 
            (message.contains("token") && (message.contains("expired") || message.contains("invalid")))) {
            
            TokenExpirationInterceptor.handleTokenExpiredResponse(authService, onTokenExpired)
        }
    }
}

/**
 * Composable que envuelve toda la aplicación para manejar expiración de tokens
 */
@Composable
fun GlobalTokenExpirationHandler(
    authService: AuthService,
    onNavigateToLogin: () -> Unit,
    content: @Composable () -> Unit
) {
    TokenExpirationHandler(
        authService = authService,
        onTokenExpired = {
            // Limpiar sesión y navegar al login
            GlobalScope.launch {
                authService.logout()
                onNavigateToLogin()
            }
        },
        checkIntervalMs = 30000L // Verificar cada 30 segundos
    ) {
        content()
    }
}