package org.sysarp.project.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
// import org.sysarp.project.ContextProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    
    private val json = Json { ignoreUnknownKeys = true }
    
    // TODO: Configurar URL base del backend
    private val baseUrl = "https://your-backend-url.com/api"
    
    sealed class AuthState {
        object NotAuthenticated : AuthState()
        object Loading : AuthState()
        data class Authenticated(val user: UserProfile) : AuthState()
        data class Error(val message: String) : AuthState()
    }
    
    suspend fun registerAdmin(
        deviceId: String,
        deviceFingerprint: String,
        deviceName: String,
        businessName: String,
        ownerName: String,
        phoneNumber: String,
        activationCode: String? = null
    ): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            _authState.value = AuthState.Loading
            
            val request = AuthRequest(
                deviceId = deviceId,
                deviceFingerprint = deviceFingerprint,
                deviceName = deviceName,
                businessName = businessName,
                ownerName = ownerName,
                phoneNumber = phoneNumber,
                activationCode = activationCode
            )
            
            // TODO: Implementar llamada HTTP real al backend
            // val response = httpClient.post("$baseUrl/auth/register-admin") { ... }
            
            // Simulación temporal
            val mockResponse = AuthResponse(
                success = true,
                adminId = deviceId,
                accessToken = "mock_access_token_${System.currentTimeMillis()}",
                refreshToken = "mock_refresh_token_${System.currentTimeMillis()}",
                message = "Admin registrado exitosamente"
            )
            
            if (mockResponse.success) {
                val profile = UserProfile(
                    deviceId = deviceId,
                    role = "ADMIN",
                    adminId = deviceId,
                    businessName = businessName,
                    permissions = listOf(
                        "RECEIVE_YAPE_NOTIFICATIONS",
                        "SEND_PAYMENT_ALERTS",
                        "MANAGE_SELLERS",
                        "VIEW_ANALYTICS"
                    ),
                    subscriptionPlan = "PROFESSIONAL",
                    subscriptionStatus = "ACTIVE"
                )
                
                _userProfile.value = profile
                _authState.value = AuthState.Authenticated(profile)
                
                // Guardar tokens localmente
                saveTokens(mockResponse.accessToken!!, mockResponse.refreshToken!!)
            }
            
            Result.success(mockResponse)
            
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Error al registrar admin: ${e.message}")
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
    
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            if (accessToken != null) {
                // TODO: Implementar llamada HTTP real al backend
                // httpClient.post("$baseUrl/auth/logout") { ... }
            }
            
            // Limpiar tokens locales
            clearTokens()
            _userProfile.value = null
            _authState.value = AuthState.NotAuthenticated
            
            Result.success(Unit)
            
        } catch (e: Exception) {
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
    private fun saveTokens(accessToken: String, refreshToken: String) {
        // TODO: Implementar almacenamiento seguro de tokens
        // Usar Android Keystore o iOS Keychain
        android.util.Log.d("AuthService", "Guardando tokens: $accessToken")
    }
    
    private fun saveAccessToken(accessToken: String) {
        // TODO: Implementar almacenamiento seguro
        android.util.Log.d("AuthService", "Guardando access token: $accessToken")
    }
    
    private fun getAccessToken(): String? {
        // TODO: Implementar recuperación de token
        return "mock_access_token"
    }
    
    private fun getRefreshToken(): String? {
        // TODO: Implementar recuperación de refresh token
        return "mock_refresh_token"
    }
    
    private fun clearTokens() {
        // TODO: Implementar limpieza de tokens
        android.util.Log.d("AuthService", "Limpiando tokens")
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
