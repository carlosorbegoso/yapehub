package org.sysarp.project.service.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sysarp.project.data.*
import org.sysarp.project.data.AuthState
import org.sysarp.project.service.http.AuthApiClient
import org.sysarp.project.utils.Logger
import org.sysarp.project.utils.UserProfileFactory

/**
 * Servicio de autenticación simplificado
 * Solo maneja autenticación básica
 * 
 * TEMPORAL: Convertido a Singleton para compartir estado entre App y NotificationService
 */
class AuthService {
    
    companion object {
        @Volatile
        private var INSTANCE: AuthService? = null
        
        fun getInstance(): AuthService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthService().also { INSTANCE = it }
            }
        }
    }
    
    private val authApiClient = AuthApiClient()
    private val tokenManager = TokenManager()
    
    // Estados básicos
    private val _authState = MutableStateFlow(AuthState.LOADING)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    // Delegar al TokenManager
    val accessToken: StateFlow<String?> = tokenManager.accessToken
    
    /**
     * Verificar si la sesión es válida
     */
    suspend fun isSessionValid(): Boolean {
        val accessToken = tokenManager.getAccessToken()
        
        // Si no hay token, la sesión no es válida
        if (accessToken == null) {
            return false
        }
        
        // Si el token está expirado, intentar refrescarlo
        if (tokenManager.isTokenExpired()) {
            Logger.auth("AUTH_SERVICE", "Token expirado, intentando refrescar...")
            
            val refreshResult = refreshToken()
            return refreshResult.isSuccess
        }
        
        // Si el token está próximo a expirar, refrescarlo preventivamente
        if (shouldRefreshToken()) {
            Logger.auth("AUTH_SERVICE", "Token próximo a expirar, refrescando preventivamente...")
            
            val refreshResult = refreshToken()
            if (refreshResult.isFailure) {
                Logger.auth("AUTH_SERVICE", "Error refrescando token preventivamente: ${refreshResult.exceptionOrNull()?.message}")
                // No fallar la sesión por error en refresh preventivo
            }
        }
        
        return true
    }
    
    /**
     * Login de administrador
     */
    suspend fun loginAdmin(
        email: String,
        password: String,
        deviceFingerprint: String? = null,
        role: String = "ADMIN"
    ): Result<LoginUserData> {
        return try {
            Logger.auth("AUTH_SERVICE", "Iniciando login de admin: $email")
            
            // Generar device fingerprint si no se proporciona
            val fingerprint = deviceFingerprint ?: org.sysarp.project.utils.DeviceUtils.generateDeviceFingerprint()
            Logger.auth("AUTH_SERVICE", "Device fingerprint: ${fingerprint.take(20)}...")
            
            val apiResponse = authApiClient.adminLogin(email, password, fingerprint, role)
            
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
                        
                        // Guardar tokens en TokenManager
                        tokenManager.saveTokens(
                            response.data.accessToken,
                            response.data.refreshToken,
                            response.data.expiresIn
                        )
                        
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
     * Refrescar token usando refresh token
     */
    suspend fun refreshToken(): Result<Boolean> {
        return try {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken == null) {
                Logger.auth("AUTH_SERVICE", "No hay refresh token disponible")
                return Result.failure(Exception("No hay refresh token disponible"))
            }
            
            Logger.auth("AUTH_SERVICE", "Refrescando token...")
            
            val apiResponse = authApiClient.refreshToken(refreshToken)
            
            apiResponse.fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        // Actualizar token en TokenManager
                        tokenManager.updateAccessToken(
                            response.data.accessToken,
                            response.data.expiresIn
                        )
                        
                        Logger.auth("AUTH_SERVICE", "Token refrescado exitosamente")
                        Result.success(true)
                    } else {
                        Logger.auth("AUTH_SERVICE", "Error refrescando token: ${response.message}")
                        Result.failure(Exception(response.message))
                    }
                },
                onFailure = { error ->
                    Logger.auth("AUTH_SERVICE", "Error refrescando token: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("AUTH_SERVICE", "Excepción refrescando token: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Verificar si el token necesita ser refrescado
     */
    fun shouldRefreshToken(): Boolean {
        return tokenManager.isTokenNearExpiry()
    }
    
    /**
     * Logout
     */
    suspend fun logout(): Result<Unit> {
        return try {
            val accessToken = tokenManager.getAccessToken()
            
            // Intentar cerrar sesión en el servidor si hay token
            if (accessToken != null) {
                Logger.auth("AUTH_SERVICE", "Cerrando sesión en el servidor...")
                val apiResponse = authApiClient.logout(accessToken)
                
                apiResponse.fold(
                    onSuccess = { response ->
                        Logger.auth("AUTH_SERVICE", "Sesión cerrada exitosamente en el servidor")
                    },
                    onFailure = { error ->
                        Logger.auth("AUTH_SERVICE", "Error cerrando sesión en el servidor: ${error.message}")
                        // Continuar con logout local aunque falle el servidor
                    }
                )
            }
            
            // Limpiar datos locales
            tokenManager.clearTokens()
            _userProfile.value = null
            _authState.value = AuthState.UNAUTHENTICATED
            
            Logger.auth("AUTH_SERVICE", "Usuario ha cerrado sesión localmente")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.auth("AUTH_SERVICE", "Excepción en logout: ${e.message}")
            
            // Limpiar datos locales aunque haya error
            tokenManager.clearTokens()
            _userProfile.value = null
            _authState.value = AuthState.UNAUTHENTICATED
            
            Result.failure(e)
        }
    }
    
    /**
     * Actualizar actividad del usuario
     */
    fun updateActivity() {
        // Actualizar timestamp de última actividad en TokenManager
        tokenManager.getAccessToken() // Esto actualiza automáticamente lastActivityTime
        Logger.auth("AUTH_SERVICE", "Actividad del usuario actualizada")
    }
    
    /**
     * Verificar y refrescar token si es necesario
     * Este método debe ser llamado periódicamente para mantener la sesión activa
     */
    suspend fun checkAndRefreshTokenIfNeeded(): Boolean {
        return try {
            // Solo refrescar si es necesario
            if (shouldRefreshToken()) {
                Logger.auth("AUTH_SERVICE", "Token necesita refresh, refrescando...")
                val refreshResult = refreshToken()
                
                if (refreshResult.isSuccess) {
                    Logger.auth("AUTH_SERVICE", "Token refrescado exitosamente en verificación periódica")
                    true
                } else {
                    Logger.auth("AUTH_SERVICE", "Error refrescando token en verificación periódica: ${refreshResult.exceptionOrNull()?.message}")
                    false
                }
            } else {
                Logger.auth("AUTH_SERVICE", "Token no necesita refresh")
                true
            }
        } catch (e: Exception) {
            Logger.auth("AUTH_SERVICE", "Excepción en verificación periódica de token: ${e.message}")
            false
        }
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
        branchCode: String? = null,
        isVerified: Boolean,
        sellerId: Int?,
        affiliationCode: String? = null
    ) {
        _userProfile.value = UserProfileFactory.createSellerProfile(
            id = id,
            name = name,
            email = email,
            role = role,
            branchId = branchId,
            branchName = branchName,
            branchCode = branchCode,
            isVerified = isVerified,
            sellerId = sellerId,
            affiliationCode = affiliationCode
        )
    }
    
    /**
     * Establecer token de acceso (para uso interno)
     */
    fun setAccessToken(token: String) {
        // Actualizar solo el access token en TokenManager
        tokenManager.updateAccessToken(token, 3600) // 1 hora por defecto
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
            
            val apiResponse = authApiClient.adminRegister(
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
                        
                        // Guardar tokens en TokenManager
                        tokenManager.saveTokens(
                            response.data.accessToken,
                            response.data.refreshToken,
                            response.data.expiresIn
                        )
                        
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
    
    
    
    /**
     * Recuperar contraseña
     */
    suspend fun forgotPassword(email: String): Result<ForgotPasswordData> {
        return try {
            Logger.auth("AUTH_SERVICE", "Solicitando recuperación de contraseña para: $email")
            
            val result = authApiClient.forgotPassword(email)
            
            result.fold(
                onSuccess = { response ->
                    Logger.auth("AUTH_SERVICE", "Solicitud de recuperación enviada exitosamente")
                    Result.success(response.data!!)
                },
                onFailure = { error ->
                    Logger.auth("AUTH_SERVICE", "Error en recuperación de contraseña: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("AUTH_SERVICE", "Excepción en recuperación de contraseña: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Login de vendedor por teléfono y código de afiliación
     */
    suspend fun sellerLoginByPhone(
        phone: String,
        affiliationCode: String
    ): Result<SellerLoginByPhoneResponse> {
        return try {
            Logger.auth("AUTH_SERVICE", "Intentando login de vendedor por teléfono: $phone")
            
            val result = authApiClient.sellerLoginByPhone(phone, affiliationCode)
            
            result.fold(
                onSuccess = { response ->
                    Logger.auth("AUTH_SERVICE", "Login de vendedor exitoso por teléfono: $phone")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("AUTH_SERVICE", "Error en login de vendedor por teléfono: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("AUTH_SERVICE", "Excepción en login de vendedor por teléfono: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Validar código de afiliación
     */
    suspend fun validateAffiliationCode(
        affiliationCode: String
    ): Result<ValidateAffiliationCodeResponse> {
        return try {
            Logger.auth("AUTH_SERVICE", "Validando código de afiliación: ${affiliationCode.take(10)}...")
            
            val result = authApiClient.validateAffiliationCode(affiliationCode)
            
            result.fold(
                onSuccess = { response ->
                    Logger.auth("AUTH_SERVICE", "Código de afiliación validado exitosamente")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("AUTH_SERVICE", "Error validando código de afiliación: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("AUTH_SERVICE", "Excepción validando código de afiliación: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Obtener vendedores del administrador
     */
    suspend fun getMySellers(
        adminId: Int,
        page: Int = 1,
        limit: Int = 30,
        token: String
    ): Result<org.sysarp.project.data.SellersResponse> {
        return try {
            Logger.auth("AUTH_SERVICE", "Obteniendo vendedores del admin: $adminId, página: $page")
            
            val result = authApiClient.getMySellers(adminId, page, limit, token)
            
            result.fold(
                onSuccess = { response ->
                    Logger.auth("AUTH_SERVICE", "Vendedores obtenidos: ${response.data?.sellers?.size ?: 0} vendedores")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("AUTH_SERVICE", "Error obteniendo vendedores: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("AUTH_SERVICE", "Excepción obteniendo vendedores: ${e.message}")
            Result.failure(e)
        }
    }
    
}
