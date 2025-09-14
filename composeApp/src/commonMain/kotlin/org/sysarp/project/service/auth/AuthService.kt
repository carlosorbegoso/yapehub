package org.sysarp.project.service.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sysarp.project.data.*
import org.sysarp.project.data.AuthState

/**
 * Servicio de autenticación simplificado
 * Solo maneja autenticación básica
 */
class AuthService {
    
    // Estados básicos
    private val _authState = MutableStateFlow(AuthState.LOADING)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()
    
    /**
     * Verificar si la sesión es válida
     */
    fun isSessionValid(): Boolean {
        return _accessToken.value != null
    }
    
    /**
     * Login de administrador
     */
    suspend fun loginAdmin(
        email: String,
        password: String
    ): Result<LoginUserData> {
        return try {
            // TODO: Implementar llamada a API real
            val loginData = LoginUserData(
                id = 1,
                email = email,
                role = "ADMIN",
                businessId = null,
                businessName = null,
                isVerified = true,
                sellerId = null
            )
            _accessToken.value = "fake_token_${System.currentTimeMillis()}"
            _userProfile.value = null // TODO: Crear UserProfile desde LoginUserData
            _authState.value = AuthState.AUTHENTICATED
            Result.success(loginData)
        } catch (e: Exception) {
            _authState.value = AuthState.UNAUTHENTICATED
            Result.failure(e)
        }
    }
    
    /**
     * Login de vendedor por teléfono
     */
    suspend fun loginSellerByPhone(
        phone: String,
        password: String
    ): Result<LoginUserData> {
        return try {
            // TODO: Implementar llamada a API real
            val sellerData = LoginUserData(
                id = 2,
                email = "seller@test.com",
                role = "SELLER",
                businessId = 1,
                businessName = "Business Test",
                isVerified = true,
                sellerId = 1
            )
            _accessToken.value = "fake_seller_token_${System.currentTimeMillis()}"
            _userProfile.value = null // TODO: Crear UserProfile desde LoginUserData
            _authState.value = AuthState.AUTHENTICATED
            Result.success(sellerData)
        } catch (e: Exception) {
            _authState.value = AuthState.UNAUTHENTICATED
            Result.failure(e)
        }
    }
    
    /**
     * Logout
     */
    suspend fun logout(): Result<Unit> {
        _accessToken.value = null
        _userProfile.value = null
        _authState.value = AuthState.UNAUTHENTICATED
        return Result.success(Unit)
    }
    
    /**
     * Actualizar actividad del usuario
     */
    fun updateActivity() {
        // TODO: Implementar cuando sea necesario
    }
    
    // Métodos de compatibilidad para las pantallas existentes
    
    suspend fun login(email: String, password: String, deviceFingerprint: String, role: String): Result<LoginUserData> {
        return loginAdmin(email, password)
    }
    
    suspend fun sellerLoginByPhone(phone: String, password: String): Result<LoginUserData> {
        return loginSellerByPhone(phone, password)
    }
}
