package org.sysarp.project.service.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sysarp.project.data.*
import org.sysarp.project.data.AuthState
import org.sysarp.project.service.http.ApiClient
import org.sysarp.project.service.http.AuthApiClient
import org.sysarp.project.utils.Logger
import org.sysarp.project.utils.UserProfileFactory

/**
 * Servicio de autenticación simplificado
 * Solo maneja autenticación básica
 */
class AuthService {
    
    private val apiClient = ApiClient()
    
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
            Logger.auth("AUTH_SERVICE", "Iniciando login de admin: $email")
            
            val apiResponse = apiClient.adminLogin(email, password)
            
            apiResponse.fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        val userData = response.data.user
                        val loginData = LoginUserData(
                            id = userData.id,
                            email = userData.email,
                            role = userData.role,
                            businessId = userData.businessId,
                            businessName = userData.businessName,
                            isVerified = userData.isVerified,
                            sellerId = null
                        )
                        
                        _userProfile.value = UserProfileFactory.createAdminProfile(
                            id = loginData.id,
                            email = loginData.email,
                            businessId = loginData.businessId,
                            businessName = loginData.businessName,
                            isVerified = loginData.isVerified
                        )
                        _accessToken.value = response.data.accessToken
                        _authState.value = AuthState.AUTHENTICATED
                        
                        Logger.auth("AUTH_SERVICE", "Login exitoso para admin: $email")
                        Result.success(loginData)
                    } else {
                        Logger.auth("AUTH_SERVICE", "Error en login de admin: ${response.message}")
                        _authState.value = AuthState.UNAUTHENTICATED
                        Result.failure(Exception(response.message))
                    }
                },
                onFailure = { error ->
                    Logger.auth("AUTH_SERVICE", "Error en login de admin: ${error.message}")
                    _authState.value = AuthState.UNAUTHENTICATED
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("AUTH_SERVICE", "Excepción en login de admin: ${e.message}")
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
            Logger.auth("AUTH_SERVICE", "Iniciando login de vendedor por teléfono: $phone")
            
            val apiResponse = apiClient.sellerLoginByPhone(phone)
            
            apiResponse.fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        val userData = response.data.user
                        val loginData = LoginUserData(
                            id = userData.id ?: 0,
                            email = userData.email ?: "",
                            role = userData.role ?: "VENDOR",
                            businessId = userData.branchId ?: 0,
                            businessName = userData.branchName ?: "",
                            isVerified = userData.isVerified,
                            sellerId = userData.sellerId
                        )
                        
                        _userProfile.value = UserProfileFactory.createSellerProfile(
                            id = loginData.id,
                            name = loginData.email,
                            email = loginData.email,
                            role = loginData.role,
                            branchId = loginData.businessId,
                            branchName = loginData.businessName,
                            isVerified = loginData.isVerified,
                            sellerId = loginData.sellerId
                        )
                        _accessToken.value = response.data.accessToken
                        _authState.value = AuthState.AUTHENTICATED
                        
                        Logger.auth("AUTH_SERVICE", "Login exitoso para vendedor: $phone")
                        Result.success(loginData)
                    } else {
                        Logger.auth("AUTH_SERVICE", "Error en login de vendedor: ${response.message}")
                        _authState.value = AuthState.UNAUTHENTICATED
                        Result.failure(Exception(response.message))
                    }
                },
                onFailure = { error ->
                    Logger.auth("AUTH_SERVICE", "Error en login de vendedor: ${error.message}")
                    _authState.value = AuthState.UNAUTHENTICATED
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("AUTH_SERVICE", "Excepción en login de vendedor: ${e.message}")
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
    
    /**
     * Actualizar perfil de usuario (para uso interno)
     */
    fun updateUserProfile(
        id: Int,
        name: String,
        email: String,
        role: String,
        branchId: Int,
        branchName: String,
        isVerified: Boolean,
        sellerId: Int?
    ) {
        _userProfile.value = UserProfileFactory.createSellerProfile(
            id = id,
            name = name,
            email = email,
            role = role,
            branchId = branchId,
            branchName = branchName,
            isVerified = isVerified,
            sellerId = sellerId
        )
    }
    
    /**
     * Establecer token de acceso (para uso interno)
     */
    fun setAccessToken(token: String) {
        _accessToken.value = token
    }
    
    /**
     * Establecer estado de autenticación (para uso interno)
     */
    fun setAuthState(state: AuthState) {
        _authState.value = state
    }
    
    /**
     * Registro de administrador
     */
    suspend fun registerAdmin(
        businessName: String,
        businessType: String,
        ruc: String,
        email: String,
        password: String,
        phone: String,
        address: String,
        contactName: String
    ): Result<LoginUserData> {
        return try {
            Logger.auth("AUTH_SERVICE", "Iniciando registro de admin: $email")
            
            val apiResponse = apiClient.adminRegister(
                businessName = businessName,
                businessType = businessType,
                ruc = ruc,
                email = email,
                password = password,
                phone = phone,
                address = address,
                contactName = contactName
            )
            
            apiResponse.fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        val userData = response.data.user
                        val loginData = LoginUserData(
                            id = userData.id,
                            email = userData.email,
                            role = userData.role,
                            businessId = userData.businessId,
                            businessName = userData.businessName,
                            isVerified = userData.isVerified,
                            sellerId = null
                        )
                        
                        _userProfile.value = UserProfileFactory.createAdminProfile(
                            id = loginData.id,
                            email = loginData.email,
                            businessId = loginData.businessId,
                            businessName = loginData.businessName,
                            isVerified = loginData.isVerified
                        )
                        _accessToken.value = response.data.accessToken
                        _authState.value = AuthState.AUTHENTICATED
                        
                        Logger.auth("AUTH_SERVICE", "Registro exitoso para admin: $email")
                        Result.success(loginData)
                    } else {
                        Logger.auth("AUTH_SERVICE", "Error en registro de admin: ${response.message}")
                        _authState.value = AuthState.UNAUTHENTICATED
                        Result.failure(Exception(response.message))
                    }
                },
                onFailure = { error ->
                    Logger.auth("AUTH_SERVICE", "Error en registro de admin: ${error.message}")
                    _authState.value = AuthState.UNAUTHENTICATED
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("AUTH_SERVICE", "Excepción en registro de admin: ${e.message}")
            _authState.value = AuthState.UNAUTHENTICATED
            Result.failure(e)
        }
    }
    
    // Métodos de compatibilidad para las pantallas existentes
    
    suspend fun login(email: String, password: String, deviceFingerprint: String, role: String): Result<LoginUserData> {
        return loginAdmin(email, password)
    }
    
    suspend fun sellerLoginByPhone(phone: String, password: String): Result<LoginUserData> {
        return loginSellerByPhone(phone, password)
    }
}
