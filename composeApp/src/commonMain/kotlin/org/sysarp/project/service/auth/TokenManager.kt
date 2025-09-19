package org.sysarp.project.service.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

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
    private val _lastActivityTime = MutableStateFlow<Long?>(null)
    
    /**
     * Guarda los tokens de autenticación
     */
    fun saveTokens(
        accessToken: String,
        refreshToken: String,
        expiresInSeconds: Int
    ) {
        _accessToken.value = accessToken
        _refreshToken.value = refreshToken
        
        val expiryTime = Clock.System.now().toEpochMilliseconds() + (expiresInSeconds * 1000L)
        _sessionExpiryTime.value = expiryTime
        _lastActivityTime.value = Clock.System.now().toEpochMilliseconds()

    }
    
    /**
     * Obtiene el token de acceso actual
     */
    fun getAccessToken(): String? {
        val token = _accessToken.value
        if (token != null) {
            _lastActivityTime.value = Clock.System.now().toEpochMilliseconds()
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
        return expiryTime != null && Clock.System.now().toEpochMilliseconds() >= expiryTime
    }
    
    /**
     * Verifica si el token está próximo a expirar (5 minutos)
     */
    fun isTokenNearExpiry(): Boolean {
        val expiryTime = _sessionExpiryTime.value
        if (expiryTime == null) return false
        
        val fiveMinutes = 5 * 60 * 1000L
        return Clock.System.now().toEpochMilliseconds() >= (expiryTime - fiveMinutes)
    }
    
    /**
     * Limpia todos los tokens
     */
    fun clearTokens() {
        _accessToken.value = null
        _refreshToken.value = null
        _sessionExpiryTime.value = null
        _lastActivityTime.value = null
    }
    
    /**
     * Actualiza el token de acceso
     */
    fun updateAccessToken(newAccessToken: String, expiresInSeconds: Int) {
        _accessToken.value = newAccessToken
        
        val expiryTime = Clock.System.now().toEpochMilliseconds() + (expiresInSeconds * 1000L)
        _sessionExpiryTime.value = expiryTime
        _lastActivityTime.value = Clock.System.now().toEpochMilliseconds()
    }
}
