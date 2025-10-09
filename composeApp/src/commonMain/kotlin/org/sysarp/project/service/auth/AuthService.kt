package org.sysarp.project.service.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sysarp.project.data.*
import org.sysarp.project.service.http.AuthApiClient
import org.sysarp.project.utils.ErrorInfo
import org.sysarp.project.utils.ErrorManager
import org.sysarp.project.utils.UserProfileFactory
import org.sysarp.project.utils.Volatile
import org.sysarp.project.utils.synchronized

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
    
    private val _authState = MutableStateFlow(AuthState.LOADING)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    val accessToken: StateFlow<String?> = tokenManager.accessToken
    
    /**
     * Verificar si la sesión es válida
     */
    suspend fun isSessionValid(): Boolean {
        val accessToken = tokenManager.getAccessToken()
        
        if (accessToken == null) {
            return false
        }
        
        if (tokenManager.isTokenExpired()) {
            
            val refreshResult = refreshToken()
            return refreshResult.isSuccess
        }
        
        if (shouldRefreshToken()) {
            
            val refreshResult = refreshToken()
            if (refreshResult.isFailure) {
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
            
            val fingerprint = deviceFingerprint ?: org.sysarp.project.utils.DeviceUtils.generateDeviceFingerprint()
            
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
                        
                        Result.success(loginData)
                    } else {
                        _authState.value = AuthState.UNAUTHENTICATED
                        Result.failure(Exception(response.message))
                    }
                },
                onFailure = { error ->
                    _authState.value = AuthState.UNAUTHENTICATED
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
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
                return Result.failure(Exception("No hay refresh token disponible"))
            }
            
            
            val apiResponse = authApiClient.refreshToken(refreshToken)
            
            apiResponse.fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        // Actualizar token en TokenManager
                        tokenManager.updateAccessToken(
                            response.data.accessToken,
                            response.data.expiresIn
                        )
                        
                        Result.success(true)
                    } else {
                        Result.failure(Exception(response.message))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
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
                val apiResponse = authApiClient.logout(accessToken)
                
                apiResponse.fold(
                    onSuccess = { response ->
                    },
                    onFailure = { error ->
                        // Continuar con logout local aunque falle el servidor
                    }
                )
            }
            
            // Limpiar datos locales
            tokenManager.clearTokens()
            _userProfile.value = null
            _authState.value = AuthState.UNAUTHENTICATED
            
            Result.success(Unit)
        } catch (e: Exception) {
            
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
    }
    
    /**
     * Verificar y refrescar token si es necesario
     * Este método debe ser llamado periódicamente para mantener la sesión activa
     */
    suspend fun checkAndRefreshTokenIfNeeded(): Boolean {
        return try {
            if (shouldRefreshToken()) {
                val refreshResult = refreshToken()
                
                if (refreshResult.isSuccess) {
                    true
                } else {
                    false
                }
            } else {
                true
            }
        } catch (e: Exception) {
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
                        
                        Result.success(loginData)
                    } else {
                        _authState.value = AuthState.UNAUTHENTICATED
                        Result.failure(Exception(response.message))
                    }
                },
                onFailure = { error ->
                    _authState.value = AuthState.UNAUTHENTICATED
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            _authState.value = AuthState.UNAUTHENTICATED
            Result.failure(e)
        }
    }
    
    
    
    /**
     * Recuperar contraseña
     */
    suspend fun forgotPassword(email: String): Result<ForgotPasswordData> {
        return try {
            
            val result = authApiClient.forgotPassword(email)
            
            result.fold(
                onSuccess = { response ->
                    Result.success(response.data!!)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
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
            
            val result = authApiClient.sellerLoginByPhone(phone, affiliationCode)
            
            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
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
            
            val result = authApiClient.validateAffiliationCode(affiliationCode)
            
            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
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
            
            val result = authApiClient.getMySellers(adminId, page, limit, token)
            
            result.fold(
                onSuccess = { response ->
                    Result.success(response)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    
    /**
     * Login de administrador con manejo elegante de errores
     */
    suspend fun loginAdminWithErrorHandling(
        email: String,
        password: String,
        deviceFingerprint: String? = null,
        role: String = "ADMIN"
    ): Pair<LoginUserData?, ErrorInfo?> {
        return try {
            
            val result = loginAdmin(email, password, deviceFingerprint, role)
            result.fold(
                onSuccess = { loginData ->
                    Pair(loginData, null)
                },
                onFailure = { exception ->
                    
                    // Parsear el error específico del login
                    val errorInfo = when {
                        exception.message?.contains("Email o contraseña incorrectos") == true -> 
                            ErrorInfo(
                                type = org.sysarp.project.ui.components.ErrorType.VALIDATION,
                                title = "Credenciales Incorrectas",
                                message = "El email o la contraseña que ingresaste no son correctos",
                                details = "Verifica que hayas escrito correctamente tu email y contraseña",
                                canRetry = true
                            )
                        exception.message?.contains("El email ingresado no es válido") == true -> 
                            ErrorInfo(
                                type = org.sysarp.project.ui.components.ErrorType.VALIDATION,
                                title = "Email Inválido",
                                message = "El formato del email no es válido",
                                details = "Asegúrate de escribir un email válido (ejemplo@dominio.com)",
                                canRetry = true
                            )
                        exception.message?.contains("No existe una cuenta con este email") == true -> 
                            ErrorInfo(
                                type = org.sysarp.project.ui.components.ErrorType.VALIDATION,
                                title = "Cuenta No Encontrada",
                                message = "No existe una cuenta registrada con este email",
                                details = "Verifica el email o regístrate si es tu primera vez",
                                canRetry = true
                            )
                        exception.message?.contains("Tu cuenta está bloqueada") == true -> 
                            ErrorInfo(
                                type = org.sysarp.project.ui.components.ErrorType.PERMISSION,
                                title = "Cuenta Bloqueada",
                                message = "Tu cuenta ha sido bloqueada por seguridad",
                                details = "Contacta al soporte técnico para desbloquear tu cuenta",
                                canRetry = false
                            )
                        exception.message?.contains("network", ignoreCase = true) == true -> 
                            ErrorManager.parseException(exception)
                        else -> 
                            ErrorManager.parseException(exception)
                    }
                    
                    Pair(null, errorInfo)
                }
            )
        } catch (e: Exception) {
            val errorInfo = ErrorManager.parseException(e)
            Pair(null, errorInfo)
        }
    }
    
}
