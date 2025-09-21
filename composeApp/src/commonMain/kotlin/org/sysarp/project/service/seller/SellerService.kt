package org.sysarp.project.service

import org.sysarp.project.data.AuthState
import org.sysarp.project.data.ConnectedSellersResponse
import org.sysarp.project.data.LoginUserData
import org.sysarp.project.data.SellerRegistrationResponse
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.http.SellerAuthApiClient
import org.sysarp.project.service.http.SellerManagementApiClient
import org.sysarp.project.service.http.SellerRegistrationApiClient

/**
 * Servicio especializado para manejar vendedores
 */
class SellerService(private val authService: AuthService) {
    
    private val sellerManagementApiClient = SellerManagementApiClient()
    private val sellerAuthApiClient = SellerAuthApiClient()
    private val sellerRegistrationApiClient = SellerRegistrationApiClient()
    
    /**
     * Registrar vendedor con código de afiliación
     */
    suspend fun registerSeller(
        affiliationCode: String,
        sellerName: String,
        phone: String
    ): Result<SellerRegistrationResponse> {
        return try {
            val result = sellerRegistrationApiClient.registerSeller(
                affiliationCode = affiliationCode,
                sellerName = sellerName,
                phone = phone
            )
            
            result.fold(
                onSuccess = { response ->
                    // Actualizar AuthService con los datos del vendedor registrado y el token
                    if (response.success && response.data != null) {
                        val registrationData = response.data
                        val userData = LoginUserData(
                            id = registrationData.sellerId,
                            email = registrationData.email,
                            role = "SELLER",
                            businessId = registrationData.branchId,
                            businessName = registrationData.branchName,
                            isVerified = true,
                            sellerId = registrationData.sellerId
                        )
                        
                        authService.updateUserProfile(
                            id = userData.id,
                            name = registrationData.name,
                            email = userData.email,
                            role = userData.role,
                            branchId = userData.businessId,
                            branchName = userData.businessName,
                            branchCode = "", // No viene en el registro
                            isVerified = userData.isVerified,
                            sellerId = userData.sellerId,
                            affiliationCode = affiliationCode // Usar el código que se envió
                        )
                        
                        // Usar el token real que viene del servidor
                        authService.setAccessToken(registrationData.token)
                        
                    }
                    
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
     * Obtener vendedores del administrador con paginación
     */
    suspend fun getMySellers(adminId: Int, page: Int = 1, limit: Int = 30, token: String): Result<org.sysarp.project.data.SellersResponse> {
        return try {

            val result = sellerManagementApiClient.getMySellers(adminId, page, limit, token)

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
     * Login de vendedor por teléfono y código de afiliación
     */
    suspend fun loginSellerByPhone(phone: String, affiliationCode: String): Result<org.sysarp.project.data.SellerLoginByPhoneResponse> {
        return try {
            
            val result = sellerAuthApiClient.loginSellerByPhone(phone, affiliationCode)
            
            result.fold(
                onSuccess = { response ->
                    
                    // Actualizar AuthService con los datos del usuario
                    if (response.success && response.data != null) {
                        val loginData = response.data
                        val userData = LoginUserData(
                            id = loginData.sellerId,
                            email = loginData.email,
                            role = "SELLER",
                            businessId = loginData.branchId,
                            businessName = loginData.branchName,
                            isVerified = true,
                            sellerId = loginData.sellerId
                        )
                        
                        authService.updateUserProfile(
                            id = userData.id,
                            name = loginData.sellerName,
                            email = userData.email,
                            role = userData.role,
                            branchId = userData.businessId,
                            branchName = userData.businessName,
                            branchCode = loginData.branchCode,
                            isVerified = userData.isVerified,
                            sellerId = userData.sellerId,
                            affiliationCode = loginData.affiliationCode
                        )
                        authService.setAccessToken(loginData.accessToken)
                        authService.setAuthState(AuthState.AUTHENTICATED)
                        
                    }
                    
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
     * Obtener solicitudes de desactivación pendientes
     */
    fun getPendingDeactivationRequests(): Result<List<org.sysarp.project.data.DeactivationRequest>> {
        return try {
            
            // Por ahora retornamos una lista vacía ya que no hay API específica para esto
            // En el futuro se puede implementar una llamada a API real
            val requests = emptyList<org.sysarp.project.data.DeactivationRequest>()
            
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Solicitar desactivación de vendedor
     */
    fun requestDeactivation(reason: String, sellerId: Int): Result<Unit> {
        return try {
            
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener vendedores conectados
     */
    suspend fun getConnectedSellers(
        adminId: Int,
        token: String
    ): Result<ConnectedSellersResponse> {
        return try {
            
            val result = sellerManagementApiClient.getConnectedSellers(
                adminId = adminId,
                accessToken = token
            )
            
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
    
}
