package org.sysarp.project.service.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sysarp.project.utils.Logger

/**
 * Gestor especializado para tokens de autenticación
 * Responsabilidad única: Manejo de tokens
 */
class TokenManager {
    
    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()
    
    private val _refreshToken = MutableStateFlow<String?>(null)
    val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()
    
    private val _sessionExpiryTime = MutableStateFlow<Long?>(null)
    val sessionExpiryTime: StateFlow<Long?> = _sessionExpiryTime.asStateFlow()
    
    private val _lastActivityTime = MutableStateFlow<Long?>(null)
    val lastActivityTime: StateFlow<Long?> = _lastActivityTime.asStateFlow()
    
    /**
     * Guarda los tokens de autenticación
     */
    fun saveTokens(
        accessToken: String,
        refreshToken: String,
        expiresInSeconds: Int
    ) {
        Logger.auth("TOKEN_MANAGER", "Guardando tokens de autenticación")
        
        _accessToken.value = accessToken
        _refreshToken.value = refreshToken
        
        val expiryTime = System.currentTimeMillis() + (expiresInSeconds * 1000L)
        _sessionExpiryTime.value = expiryTime
        _lastActivityTime.value = System.currentTimeMillis()
        
        Logger.success("TOKEN_MANAGER", "Tokens guardados exitosamente")
        Logger.debug("TOKEN_MANAGER", "Expira en: ${expiryTime - System.currentTimeMillis()}ms")
    }
    
    /**
     * Obtiene el token de acceso actual
     */
    fun getAccessToken(): String? {
        val token = _accessToken.value
        if (token != null) {
            _lastActivityTime.value = System.currentTimeMillis()
        }
        return token
    }
    
    /**
     * Obtiene el refresh token actual
     */
    fun getRefreshToken(): String? {
        return _refreshToken.value
    }
    
    /**
     * Verifica si el token está expirado
     */
    fun isTokenExpired(): Boolean {
        val expiryTime = _sessionExpiryTime.value
        return expiryTime != null && System.currentTimeMillis() >= expiryTime
    }
    
    /**
     * Verifica si el token está próximo a expirar (5 minutos)
     */
    fun isTokenNearExpiry(): Boolean {
        val expiryTime = _sessionExpiryTime.value
        if (expiryTime == null) return false
        
        val fiveMinutes = 5 * 60 * 1000L
        return System.currentTimeMillis() >= (expiryTime - fiveMinutes)
    }
    
    /**
     * Limpia todos los tokens
     */
    fun clearTokens() {
        Logger.auth("TOKEN_MANAGER", "Limpiando tokens de autenticación")
        
        _accessToken.value = null
        _refreshToken.value = null
        _sessionExpiryTime.value = null
        _lastActivityTime.value = null
        
        Logger.success("TOKEN_MANAGER", "Tokens limpiados exitosamente")
    }
    
    /**
     * Actualiza el token de acceso
     */
    fun updateAccessToken(newAccessToken: String, expiresInSeconds: Int) {
        Logger.auth("TOKEN_MANAGER", "Actualizando token de acceso")
        
        _accessToken.value = newAccessToken
        
        val expiryTime = System.currentTimeMillis() + (expiresInSeconds * 1000L)
        _sessionExpiryTime.value = expiryTime
        _lastActivityTime.value = System.currentTimeMillis()
        
        Logger.success("TOKEN_MANAGER", "Token de acceso actualizado")
    }
}
