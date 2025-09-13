package org.sysarp.project.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sysarp.project.data.*

@Serializable
data class AuthRequest(
    val deviceId: String,
    val deviceFingerprint: String,
    val deviceName: String,
    val businessName: String? = null,
    val ownerName: String? = null,
    val phoneNumber: String? = null,
    val activationCode: String? = null,
    val adminId: String? = null,
    val qrData: String? = null,
    val qrSignature: String? = null,
    val sellerName: String? = null,
    val branchCode: String? = null,
    val branchName: String? = null
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val adminId: String? = null,
    val sellerId: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val publicKey: String? = null,
    val privateKey: String? = null,
    val message: String? = null
)

@Serializable
data class RefreshRequest(
    val deviceFingerprint: String
)

@Serializable
data class RefreshResponse(
    val accessToken: String,
    val expiresIn: Int
)

@Serializable
data class UserProfile(
    val deviceId: String,
    val role: String, // ADMIN, SELLER
    val adminId: String? = null,
    val businessName: String? = null,
    val sellerName: String? = null,
    val branchCode: String? = null,
    val branchName: String? = null,
    val permissions: List<String> = emptyList(),
    val subscriptionPlan: String? = null,
    val subscriptionStatus: String? = null,
    val registeredAt: String? = null,
    val lastSyncAt: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class QRGenerationResponse(
    val qrData: String,
    val qrSignature: String,
    val qrImageBase64: String,
    val expiresAt: String,
    val affiliationUrl: String
)

@Serializable
data class DeactivationRequest(
    val sellerId: String,
    val sellerName: String,
    val reason: String,
    val requestedAt: String,
    val status: String = "PENDING" // PENDING, APPROVED, REJECTED
)

@Serializable
data class DeactivationResponse(
    val success: Boolean,
    val message: String? = null
)

class AuthService {
    private val _authState = MutableStateFlow<AuthState>(AuthState.NotAuthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    // Almacenamiento de tokens y sesión
    private var _accessToken: String? = null
    private var _refreshToken: String? = null
    private var _sessionExpiryTime: Long? = null // Timestamp de expiración
    private var _lastActivityTime: Long? = null // Última actividad del usuario
    
    private val json = Json { ignoreUnknownKeys = true }
    private val httpService = HttpService()
    
    sealed class AuthState {
        object NotAuthenticated : AuthState()
        object Loading : AuthState()
        data class Authenticated(val user: UserProfile) : AuthState()
        data class Error(val message: String) : AuthState()
    }
    
    suspend fun registerAdmin(
        businessName: String,
        businessType: String,
        ruc: String,
        email: String,
        password: String,
        phone: String,
        address: String,
        contactName: String
    ): Result<AdminRegistrationResponse> = withContext(Dispatchers.IO) {
        try {
            println("🚀 [AUTH] Iniciando registro de administrador")
            println("📋 [AUTH] Datos recibidos:")
            println("   - businessName: '$businessName'")
            println("   - businessType: '$businessType'")
            println("   - ruc: '$ruc'")
            println("   - email: '$email'")
            println("   - phone: '$phone'")
            println("   - address: '$address'")
            println("   - contactName: '$contactName'")
            
            _authState.value = AuthState.Loading
            
            val request = AdminRegistrationRequest(
                businessName = businessName,
                businessType = businessType,
                ruc = ruc,
                email = email,
                password = password,
                phone = phone,
                address = address,
                contactName = contactName
            )
            
            val response = httpService.registerAdmin(request)
            
            response.fold(
                onSuccess = { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        val data = apiResponse.data
                        val user = data.user
                        
                        // Crear perfil de usuario
                        val profile = UserProfile(
                            deviceId = user.id.toString(),
                            role = user.role,
                            adminId = user.id.toString(),
                            businessName = user.businessName,
                            permissions = listOf(
                                "RECEIVE_YAPE_NOTIFICATIONS",
                                "SEND_PAYMENT_ALERTS",
                                "MANAGE_SELLERS",
                                "VIEW_ANALYTICS"
                            ),
                            subscriptionPlan = "PROFESSIONAL",
                            subscriptionStatus = "ACTIVE"
                        )
                        
                        // Guardar tokens con tiempo de expiración
                        saveTokens(data.accessToken, data.refreshToken, data.expiresIn)
                        
                        _userProfile.value = profile
                        _authState.value = AuthState.Authenticated(profile)
                        
                        Result.success(apiResponse)
                    } else {
                        _authState.value = AuthState.Error(apiResponse.message)
                        Result.failure(Exception(apiResponse.message))
                    }
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error("Error al registrar admin: ${error.message}")
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Error al registrar admin: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun login(
        email: String,
        password: String,
        deviceFingerprint: String,
        role: String
    ): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            println("🚀 [AUTH] Iniciando login")
            println("📋 [AUTH] Datos recibidos:")
            println("   - email: '$email'")
            println("   - role: '$role'")
            println("   - deviceFingerprint: '$deviceFingerprint'")
            
            _authState.value = AuthState.Loading
            
            val request = LoginRequest(
                email = email,
                password = password,
                deviceFingerprint = deviceFingerprint,
                role = role
            )
            
            val response = httpService.login(request)
            
            response.fold(
                onSuccess = { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        val data = apiResponse.data
                        val user = data.user
                        
                        // Crear perfil de usuario
                        val profile = UserProfile(
                            deviceId = user.id.toString(),
                            role = user.role,
                            adminId = user.id.toString(),
                            businessName = user.businessName,
                            permissions = listOf(
                                "RECEIVE_YAPE_NOTIFICATIONS",
                                "SEND_PAYMENT_ALERTS",
                                "MANAGE_SELLERS",
                                "VIEW_ANALYTICS"
                            ),
                            subscriptionPlan = "PROFESSIONAL",
                            subscriptionStatus = "ACTIVE"
                        )
                        
                        // Guardar tokens con tiempo de expiración
                        saveTokens(data.accessToken, data.refreshToken, data.expiresIn)
                        
                        _userProfile.value = profile
                        _authState.value = AuthState.Authenticated(profile)
                        
                        Result.success(apiResponse)
                    } else {
                        _authState.value = AuthState.Error(apiResponse.message)
                        Result.failure(Exception(apiResponse.message))
                    }
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error("Error al hacer login: ${error.message}")
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Error al hacer login: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun affiliateSeller(
        deviceId: String,
        deviceFingerprint: String,
        adminId: String,
        qrData: String,
        qrSignature: String,
        sellerName: String,
        branchCode: String,
        branchName: String
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            _authState.value = AuthState.Loading
            
            val request = AuthRequest(
                deviceId = deviceId,
                deviceFingerprint = deviceFingerprint,
                deviceName = "Android Device",
                adminId = adminId,
                qrData = qrData,
                qrSignature = qrSignature,
                sellerName = sellerName,
                branchCode = branchCode,
                branchName = branchName
            )
            
            // TODO: Implementar llamada HTTP real al backend
            // val response = httpClient.post("$baseUrl/auth/affiliate-seller") { ... }
            
            // Simulación temporal
            val mockResponse = AuthResponse(
                success = true,
                sellerId = deviceId,
                adminId = adminId,
                accessToken = "mock_seller_token_${System.currentTimeMillis()}",
                refreshToken = "mock_seller_refresh_${System.currentTimeMillis()}",
                message = "Afiliación exitosa"
            )
            
            if (mockResponse.success) {
                val profile = UserProfile(
                    deviceId = deviceId,
                    role = "SELLER",
                    adminId = adminId,
                    sellerName = sellerName,
                    branchCode = branchCode,
                    branchName = branchName,
                    permissions = listOf(
                        "RECEIVE_PAYMENT_ALERTS",
                        "CLAIM_PAYMENTS",
                        "VIEW_PAYMENT_HISTORY"
                    ),
                    subscriptionPlan = "BASIC",
                    subscriptionStatus = "ACTIVE"
                )
                
                _userProfile.value = profile
                _authState.value = AuthState.Authenticated(profile)
                
                // Guardar tokens localmente
                saveTokens(mockResponse.accessToken!!, mockResponse.refreshToken!!)
            }
            
            Result.success(mockResponse)
            
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Error al afiliar vendedor: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun refreshToken(): Result<RefreshResponse> = withContext(Dispatchers.IO) {
        try {
            val refreshToken = getRefreshToken()
            if (refreshToken == null) {
                return@withContext Result.failure(Exception("No hay refresh token disponible"))
            }
            
            val deviceFingerprint = generateDeviceFingerprint()
            val request = RefreshRequest(deviceFingerprint)
            
            // TODO: Implementar llamada HTTP real al backend
            // val response = httpClient.post("$baseUrl/auth/refresh") { ... }
            
            // Simulación temporal
            val mockResponse = RefreshResponse(
                accessToken = "new_access_token_${System.currentTimeMillis()}",
                expiresIn = 900 // 15 minutos
            )
            
            // Guardar nuevo token
            saveAccessToken(mockResponse.accessToken)
            
            Result.success(mockResponse)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<LogoutResponse> = withContext(Dispatchers.IO) {
        try {
            println("🚀 [AUTH] Iniciando logout")
            
            val accessToken = getAccessToken()
            if (accessToken == null) {
                println("❌ [AUTH] No hay access token disponible")
                clearTokens()
                _userProfile.value = null
                _authState.value = AuthState.NotAuthenticated
                return@withContext Result.failure(Exception("No hay token de acceso"))
            }
            
            println("📋 [AUTH] AccessToken para logout: ${accessToken.take(20)}...")
            
            _authState.value = AuthState.Loading
            
            val response = httpService.logout(accessToken)
            
            response.fold(
                onSuccess = { apiResponse ->
                    if (apiResponse.success) {
                        // Limpiar tokens locales
                        clearTokens()
                        _userProfile.value = null
                        _authState.value = AuthState.NotAuthenticated
                        
                        println("✅ [AUTH] Logout exitoso")
                        Result.success(apiResponse)
                    } else {
                        _authState.value = AuthState.Error(apiResponse.message)
                        Result.failure(Exception(apiResponse.message))
                    }
                },
                onFailure = { error ->
                    // Aún así limpiar tokens locales en caso de error de red
                    clearTokens()
                    _userProfile.value = null
                    _authState.value = AuthState.NotAuthenticated
                    
                    println("❌ [AUTH] Error en logout: ${error.message}")
                    Result.failure(error)
                }
            )
            
        } catch (e: Exception) {
            // Aún así limpiar tokens locales en caso de error
            clearTokens()
            _userProfile.value = null
            _authState.value = AuthState.NotAuthenticated
            
            println("💥 [AUTH] Excepción en logout: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun generateAffiliationQR(): Result<QRGenerationResponse> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            if (accessToken == null) {
                return@withContext Result.failure(Exception("No hay token de acceso"))
            }
            
            // TODO: Implementar llamada HTTP real al backend
            // val response = httpClient.get("$baseUrl/sellers/generate-qr") { ... }
            
            // Simulación temporal
            val mockResponse = QRGenerationResponse(
                qrData = "mock_qr_data_${System.currentTimeMillis()}",
                qrSignature = "mock_qr_signature_${System.currentTimeMillis()}",
                qrImageBase64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==",
                expiresAt = "2025-09-12T14:35:00Z",
                affiliationUrl = "https://yourapp.com/affiliate?token=mock_token_123"
            )
            
            Result.success(mockResponse)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            if (accessToken == null) {
                return@withContext Result.failure(Exception("No hay token de acceso"))
            }
            
            // TODO: Implementar llamada HTTP real al backend
            // val response = httpClient.get("$baseUrl/profile") { ... }
            
            // Simulación temporal - usar perfil actual
            val profile = _userProfile.value
            if (profile != null) {
                Result.success(profile)
            } else {
                Result.failure(Exception("No hay perfil disponible"))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Funciones de utilidad para manejo de tokens
    private fun saveTokens(accessToken: String, refreshToken: String, expiresInSeconds: Int = 3600) {
        _accessToken = accessToken
        _refreshToken = refreshToken
        
        // Calcular tiempo de expiración (24 horas máximo, o expiresIn si es menor)
        val maxSessionDuration = 24 * 60 * 60 * 1000L // 24 horas en millisegundos
        val tokenExpiryDuration = expiresInSeconds * 1000L // expiresIn en millisegundos
        val sessionDuration = minOf(maxSessionDuration, tokenExpiryDuration)
        
        _sessionExpiryTime = System.currentTimeMillis() + sessionDuration
        _lastActivityTime = System.currentTimeMillis()
        
        println("🔐 [AUTH] Tokens guardados:")
        println("   - AccessToken: ${accessToken.take(20)}...")
        println("   - RefreshToken: ${refreshToken.take(20)}...")
        println("   - Expira en: ${sessionDuration / (60 * 60 * 1000)} horas")
        println("   - Timestamp expiración: $_sessionExpiryTime")
    }
    
    private fun saveAccessToken(accessToken: String) {
        _accessToken = accessToken
        println("🔐 [AUTH] Access token guardado: ${accessToken.take(20)}...")
    }
    
    private fun getAccessToken(): String? {
        return _accessToken
    }
    
    private fun getRefreshToken(): String? {
        return _refreshToken
    }
    
    private fun clearTokens() {
        _accessToken = null
        _refreshToken = null
        _sessionExpiryTime = null
        _lastActivityTime = null
        println("🔐 [AUTH] Tokens y sesión limpiados")
    }
    
    // Funciones de verificación de sesión
    fun isSessionValid(): Boolean {
        val currentTime = System.currentTimeMillis()
        val expiryTime = _sessionExpiryTime
        
        if (expiryTime == null) {
            println("⏰ [AUTH] No hay sesión activa")
            return false
        }
        
        val isValid = currentTime < expiryTime
        val remainingTime = expiryTime - currentTime
        
        if (isValid) {
            println("⏰ [AUTH] Sesión válida - Tiempo restante: ${remainingTime / (60 * 1000)} minutos")
            _lastActivityTime = currentTime // Actualizar última actividad
        } else {
            println("⏰ [AUTH] Sesión expirada - Tiempo excedido: ${-remainingTime / (60 * 1000)} minutos")
        }
        
        return isValid
    }
    
    fun updateActivity() {
        _lastActivityTime = System.currentTimeMillis()
        println("⏰ [AUTH] Actividad actualizada: $_lastActivityTime")
    }
    
    suspend fun refreshSessionIfNeeded(): Boolean {
        if (!isSessionValid()) {
            println("🔄 [AUTH] Sesión expirada, intentando renovar...")
            return refreshTokens()
        }
        return true
    }
    
    private suspend fun refreshTokens(): Boolean {
        val refreshToken = getRefreshToken()
        if (refreshToken == null) {
            println("❌ [AUTH] No hay refresh token disponible")
            clearTokens()
            _authState.value = AuthState.NotAuthenticated
            return false
        }
        
        try {
            println("🔄 [AUTH] Renovando tokens...")
            val response = httpService.refreshToken(refreshToken)
            
            response.fold(
                onSuccess = { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        val data = apiResponse.data
                        
                        // Guardar nuevos tokens
                        saveTokens(data.accessToken, data.refreshToken, data.expiresIn)
                        
                        println("✅ [AUTH] Tokens renovados exitosamente")
                        return@fold true
                    } else {
                        println("❌ [AUTH] Error al renovar tokens: ${apiResponse.message}")
                        clearTokens()
                        _authState.value = AuthState.NotAuthenticated
                        return@fold false
                    }
                },
                onFailure = { error ->
                    println("❌ [AUTH] Error al renovar tokens: ${error.message}")
                    clearTokens()
                    _authState.value = AuthState.NotAuthenticated
                    return@fold false
                }
            )
            
            return response.isSuccess
        } catch (e: Exception) {
            println("💥 [AUTH] Excepción al renovar tokens: ${e.message}")
            clearTokens()
            _authState.value = AuthState.NotAuthenticated
            return false
        }
    }
    
    fun generateDeviceFingerprint(): String {
        // TODO: Implementar generación de fingerprint único del dispositivo
        // Incluir: Android ID, Build info, etc.
        return "device_fingerprint_${System.currentTimeMillis()}"
    }
    
    fun generateDeviceId(): String {
        // TODO: Implementar generación de ID único del dispositivo
        return "device_${System.currentTimeMillis()}"
    }
    
    fun getDeviceName(): String {
        // TODO: Implementar obtención del nombre del dispositivo
        return "Android Device"
    }
    
    // Métodos para manejar solicitudes de baja
    private val _deactivationRequests = MutableStateFlow<List<DeactivationRequest>>(emptyList())
    val deactivationRequests: StateFlow<List<DeactivationRequest>> = _deactivationRequests.asStateFlow()
    
    suspend fun requestDeactivation(reason: String): DeactivationResponse {
        val currentProfile = _userProfile.value
        return if (currentProfile?.role == "SELLER" && currentProfile.isActive) {
            val request = DeactivationRequest(
                sellerId = currentProfile.adminId ?: "unknown",
                sellerName = currentProfile.sellerName ?: "Unknown Seller",
                reason = reason,
                requestedAt = java.time.Instant.now().toString()
            )
            
            val currentRequests = _deactivationRequests.value.toMutableList()
            currentRequests.add(request)
            _deactivationRequests.value = currentRequests
            
            DeactivationResponse(success = true, message = "Solicitud de baja enviada correctamente")
        } else {
            DeactivationResponse(success = false, message = "Solo los vendedores activos pueden solicitar baja")
        }
    }
    
    suspend fun approveDeactivation(sellerId: String): DeactivationResponse {
        val currentRequests = _deactivationRequests.value.toMutableList()
        val requestIndex = currentRequests.indexOfFirst { it.sellerId == sellerId && it.status == "PENDING" }
        
        return if (requestIndex != -1) {
            val updatedRequest = currentRequests[requestIndex].copy(status = "APPROVED")
            currentRequests[requestIndex] = updatedRequest
            _deactivationRequests.value = currentRequests
            
            // Simular desactivación del vendedor
            val currentProfile = _userProfile.value
            if (currentProfile?.adminId == sellerId) {
                _userProfile.value = currentProfile.copy(isActive = false)
            }
            
            DeactivationResponse(success = true, message = "Baja aprobada correctamente")
        } else {
            DeactivationResponse(success = false, message = "Solicitud no encontrada")
        }
    }
    
    suspend fun rejectDeactivation(sellerId: String): DeactivationResponse {
        val currentRequests = _deactivationRequests.value.toMutableList()
        val requestIndex = currentRequests.indexOfFirst { it.sellerId == sellerId && it.status == "PENDING" }
        
        return if (requestIndex != -1) {
            val updatedRequest = currentRequests[requestIndex].copy(status = "REJECTED")
            currentRequests[requestIndex] = updatedRequest
            _deactivationRequests.value = currentRequests
            
            DeactivationResponse(success = true, message = "Solicitud de baja rechazada")
        } else {
            DeactivationResponse(success = false, message = "Solicitud no encontrada")
        }
    }
    
    fun getPendingDeactivationRequests(): List<DeactivationRequest> {
        return _deactivationRequests.value.filter { it.status == "PENDING" }
    }
}
