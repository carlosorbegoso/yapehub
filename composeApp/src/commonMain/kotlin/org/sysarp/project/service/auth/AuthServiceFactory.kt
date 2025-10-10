package org.sysarp.project.service.auth

import org.sysarp.project.service.http.AuthApiClient

/**
 * Factory para crear servicios de autenticación refactorizados
 * Implementa el patrón Factory para crear instancias de los servicios
 */
object AuthServiceFactory {
    
    /**
     * Crear AuthCoordinator con todas sus dependencias
     */
    fun createAuthCoordinator(): AuthCoordinator {
        val authApiClient = AuthApiClient()
        val authStateManager = AuthStateManager()
        val authOperations = AuthOperations(authApiClient)
        val tokenManager = TokenManager()
        
        return AuthCoordinator(
            authStateManager = authStateManager,
            authOperations = authOperations,
            tokenManager = tokenManager
        )
    }
    
    /**
     * Crear AuthStateManager independiente
     */
    fun createAuthStateManager(): AuthStateManager {
        return AuthStateManager()
    }
    
    /**
     * Crear AuthOperations independiente
     */
    fun createAuthOperations(): AuthOperations {
        val authApiClient = AuthApiClient()
        return AuthOperations(authApiClient)
    }
    
    /**
     * Crear TokenManager independiente
     */
    fun createTokenManager(): TokenManager {
        return TokenManager()
    }
}
