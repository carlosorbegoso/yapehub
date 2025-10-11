package org.sysarp.project.service.auth

import kotlinx.coroutines.flow.StateFlow
import org.sysarp.project.data.AuthState
import org.sysarp.project.data.ForgotPasswordData
import org.sysarp.project.data.LoginUserData
import org.sysarp.project.data.SellerLoginByPhoneResponse
import org.sysarp.project.data.UserProfile
import org.sysarp.project.data.ValidateAffiliationCodeResponse

/**
 * Coordinador principal de autenticación
 * Responsabilidad única: Coordinar los diferentes servicios de autenticación
 */
class AuthCoordinator(
    private val authStateManager: AuthStateManager,
    private val authOperations: AuthOperations,
    private val tokenManager: TokenManager
) {
    
    // Exponer estados del AuthStateManager
    val authState: StateFlow<AuthState> = authStateManager.authState
    val userProfile: StateFlow<UserProfile?> = authStateManager.userProfile
    val accessToken: StateFlow<String?> = tokenManager.accessToken
    
    /**
     * Login de administrador completo
     */
    suspend fun loginAdmin(
        email: String,
        password: String,
        deviceFingerprint: String? = null,
        role: String = "ADMIN"
    ): Result<LoginUserData> {
        return try {
            authStateManager.setAuthState(AuthState.LOADING)
            
            val result = authOperations.loginAdmin(email, password, deviceFingerprint, role)
            
            result.fold(
                onSuccess = { loginData ->
                    // Crear perfil de administrador
                    authStateManager.createAdminProfile(
                        id = loginData.id,
                        email = loginData.email,
                        businessId = loginData.businessId,
                        businessName = loginData.businessName,
                        isVerified = loginData.isVerified
                    )
                    
                    // Guardar tokens (esto se haría con la respuesta real)
                    // tokenManager.saveTokens(accessToken, refreshToken, expiresIn)
                    
                    authStateManager.setAuthState(AuthState.AUTHENTICATED)
                    Result.success(loginData)
                },
                onFailure = { error ->
                    authStateManager.setAuthState(AuthState.UNAUTHENTICATED)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            authStateManager.setAuthState(AuthState.UNAUTHENTICATED)
            Result.failure(e)
        }
    }
    
    /**
     * Registro de administrador completo
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
            authStateManager.setAuthState(AuthState.LOADING)
            
            val result = authOperations.registerAdmin(
                businessName, businessType, ruc, email, password, phone, address, contactName
            )
            
            result.fold(
                onSuccess = { loginData ->
                    authStateManager.createAdminProfile(
                        id = loginData.id,
                        email = loginData.email,
                        businessId = loginData.businessId,
                        businessName = loginData.businessName,
                        isVerified = loginData.isVerified
                    )
                    
                    authStateManager.setAuthState(AuthState.AUTHENTICATED)
                    Result.success(loginData)
                },
                onFailure = { error ->
                    authStateManager.setAuthState(AuthState.UNAUTHENTICATED)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            authStateManager.setAuthState(AuthState.UNAUTHENTICATED)
            Result.failure(e)
        }
    }
    
    /**
     * Login de vendedor por teléfono completo
     */
    suspend fun sellerLoginByPhone(
        phone: String,
        affiliationCode: String
    ): Result<SellerLoginByPhoneResponse> {
        return try {
            authStateManager.setAuthState(AuthState.LOADING)
            
            val result = authOperations.sellerLoginByPhone(phone, affiliationCode)
            
            result.fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        val loginData = response.data
                        
                        authStateManager.updateUserProfile(
                            id = loginData.sellerId,
                            name = loginData.sellerName,
                            email = loginData.email,
                            role = "SELLER",
                            branchId = loginData.branchId,
                            branchName = loginData.branchName,
                            branchCode = loginData.branchCode,
                            isVerified = true,
                            sellerId = loginData.sellerId,
                            affiliationCode = loginData.affiliationCode
                        )
                        
                        tokenManager.updateAccessToken(loginData.accessToken, 3600)
                        authStateManager.setAuthState(AuthState.AUTHENTICATED)
                    }
                    
                    Result.success(response)
                },
                onFailure = { error ->
                    authStateManager.setAuthState(AuthState.UNAUTHENTICATED)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            authStateManager.setAuthState(AuthState.UNAUTHENTICATED)
            Result.failure(e)
        }
    }
    
    /**
     * Recuperar contraseña
     */
    suspend fun forgotPassword(email: String): Result<ForgotPasswordData> {
        return authOperations.forgotPassword(email)
    }
    
    /**
     * Validar código de afiliación
     */
    suspend fun validateAffiliationCode(
        affiliationCode: String
    ): Result<ValidateAffiliationCodeResponse> {
        return authOperations.validateAffiliationCode(affiliationCode)
    }
    
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
                // Manejar error de refresh
            }
        }
        
        return true
    }
    
    /**
     * Refrescar token
     */
    private suspend fun refreshToken(): Result<String> {
        // Implementar lógica de refresh token
        return Result.success("new_token")
    }
    
    /**
     * Verificar si debe refrescar el token
     */
    private fun shouldRefreshToken(): Boolean {
        // Implementar lógica de verificación
        return false
    }
    
    /**
     * Logout
     */
    fun logout() {
        authStateManager.clearUserData()
        tokenManager.clearTokens()
    }
    
    /**
     * Establecer token de acceso (para uso interno)
     */
    fun setAccessToken(token: String) {
        tokenManager.updateAccessToken(token, 3600)
    }
}
