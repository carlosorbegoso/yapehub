package org.sysarp.project.service.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sysarp.project.data.UserProfile
import org.sysarp.project.utils.Logger

/**
 * Gestor especializado para el estado de autenticación
 * Responsabilidad única: Manejo del estado de autenticación
 */
class AuthenticationStateManager {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.NotAuthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    /**
     * Cambia el estado a autenticado
     */
    fun setAuthenticated(user: UserProfile) {
        Logger.auth("AUTH_STATE_MANAGER", "Usuario autenticado: ${user.email}")
        
        _authState.value = AuthState.Authenticated(user)
        
        Logger.success("AUTH_STATE_MANAGER", "Estado cambiado a autenticado")
    }
    
    /**
     * Cambia el estado a no autenticado
     */
    fun setNotAuthenticated() {
        Logger.auth("AUTH_STATE_MANAGER", "Usuario no autenticado")
        
        _authState.value = AuthState.NotAuthenticated
        
        Logger.success("AUTH_STATE_MANAGER", "Estado cambiado a no autenticado")
    }
    
    /**
     * Cambia el estado a cargando
     */
    fun setLoading() {
        Logger.auth("AUTH_STATE_MANAGER", "Estado de carga")
        
        _authState.value = AuthState.Loading
        
        Logger.debug("AUTH_STATE_MANAGER", "Estado cambiado a cargando")
    }
    
    /**
     * Cambia el estado a error
     */
    fun setError(message: String) {
        Logger.error("AUTH_STATE_MANAGER", "Error de autenticación: $message")
        
        _authState.value = AuthState.Error(message)
        
        Logger.error("AUTH_STATE_MANAGER", "Estado cambiado a error")
    }
    
    /**
     * Verifica si el usuario está autenticado
     */
    fun isAuthenticated(): Boolean {
        return _authState.value is AuthState.Authenticated
    }
    
    /**
     * Verifica si está en estado de carga
     */
    fun isLoading(): Boolean {
        return _authState.value is AuthState.Loading
    }
    
    /**
     * Verifica si hay un error
     */
    fun hasError(): Boolean {
        return _authState.value is AuthState.Error
    }
    
    /**
     * Obtiene el usuario autenticado actual
     */
    fun getAuthenticatedUser(): UserProfile? {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.user
            else -> null
        }
    }
    
    /**
     * Obtiene el mensaje de error actual
     */
    fun getErrorMessage(): String? {
        return when (val state = _authState.value) {
            is AuthState.Error -> state.message
            else -> null
        }
    }
}

/**
 * Estados de autenticación
 */
sealed class AuthState {
    object NotAuthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: UserProfile) : AuthState()
    data class Error(val message: String) : AuthState()
}
