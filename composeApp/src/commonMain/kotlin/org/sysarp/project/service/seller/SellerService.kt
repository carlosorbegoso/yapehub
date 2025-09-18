package org.sysarp.project.service

import org.sysarp.project.data.*
import org.sysarp.project.service.http.SellerManagementApiClient
import org.sysarp.project.service.http.SellerAuthApiClient
import org.sysarp.project.service.http.SellerRegistrationApiClient
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.utils.Logger

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
            Logger.auth("SELLER_SERVICE", "Iniciando registro de vendedor: $sellerName")
            
            val result = sellerRegistrationApiClient.registerSeller(
                affiliationCode = affiliationCode,
                sellerName = sellerName,
                phone = phone
            )
            
            result.fold(
                onSuccess = { response ->
                    Logger.auth("SELLER_SERVICE", "Registro de vendedor exitoso: $sellerName")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("SELLER_SERVICE", "Error en registro de vendedor: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("SELLER_SERVICE", "Error en registro de vendedor: ${e.message}")
            Result.failure(e)
        }
    }
    
    
    /**
     * Obtener vendedores del administrador con paginación
     */
    suspend fun getMySellers(adminId: Int, page: Int = 1, limit: Int = 30, token: String): Result<org.sysarp.project.data.SellersResponse> {
        return try {
            Logger.auth("SELLER_SERVICE", "Obteniendo vendedores del admin: $adminId, página: $page")

            val result = sellerManagementApiClient.getMySellers(adminId, page, limit, token)

            result.fold(
                onSuccess = { response ->
                    Logger.auth("SELLER_SERVICE", "Vendedores obtenidos: ${response.data?.sellers?.size ?: 0} vendedores")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("SELLER_SERVICE", "Error obteniendo vendedores: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("SELLER_SERVICE", "Error obteniendo vendedores: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateSeller(
        sellerId: Int,
        adminId: Int,
        name: String? = null,
        phone: String? = null,
        isActive: Boolean? = null,
        token: String
    ): Result<MySeller> {
        return try {
            Logger.auth("SELLER_SERVICE", "Actualizando vendedor: $sellerId")

            val result = sellerManagementApiClient.updateSeller(sellerId, adminId, name, phone, isActive, token)

            result.fold(
                onSuccess = { updatedSeller ->
                    Logger.auth("SELLER_SERVICE", "Vendedor actualizado exitosamente: ${updatedSeller.name}")
                    Result.success(updatedSeller)
                },
                onFailure = { error ->
                    Logger.auth("SELLER_SERVICE", "Error actualizando vendedor: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("SELLER_SERVICE", "Error actualizando vendedor: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteSeller(
        sellerId: Int,
        adminId: Int,
        action: String = "pause", // "pause" o "delete"
        token: String
    ): Result<Boolean> {
        return try {
            Logger.auth("SELLER_SERVICE", "Eliminando/pausando vendedor: $sellerId con acción: $action")

            val result = sellerManagementApiClient.deleteSeller(sellerId, adminId, action, token)

            result.fold(
                onSuccess = { success ->
                    Logger.auth("SELLER_SERVICE", "Vendedor $action exitosamente: $sellerId")
                    Result.success(success)
                },
                onFailure = { error ->
                    Logger.auth("SELLER_SERVICE", "Error $action vendedor: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("SELLER_SERVICE", "Error $action vendedor: ${e.message}")
            Result.failure(e)
        }
    }


    /**
     * Login de vendedor por teléfono y código de afiliación
     */
    suspend fun loginSellerByPhone(phone: String, affiliationCode: String): Result<org.sysarp.project.data.SellerLoginByPhoneResponse> {
        return try {
            Logger.auth("SELLER_SERVICE", "Iniciando login de vendedor por teléfono: $phone con código: $affiliationCode")
            
            val result = sellerAuthApiClient.loginSellerByPhone(phone, affiliationCode)
            
            result.fold(
                onSuccess = { response ->
                    Logger.auth("SELLER_SERVICE", "Login de vendedor exitoso por teléfono: $phone")
                    
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
                        
                        Logger.auth("SELLER_SERVICE", "AuthService actualizado para vendedor: ${loginData.sellerId} en sucursal: ${loginData.branchName}")
                    }
                    
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("SELLER_SERVICE", "Error en login de vendedor: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("SELLER_SERVICE", "Error en login de vendedor: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtener solicitudes de desactivación pendientes
     */
    suspend fun getPendingDeactivationRequests(): Result<List<org.sysarp.project.data.DeactivationRequest>> {
        return try {
            Logger.auth("SELLER_SERVICE", "Obteniendo solicitudes de desactivación pendientes")
            
            // Por ahora retornamos una lista vacía ya que no hay API específica para esto
            // En el futuro se puede implementar una llamada a API real
            val requests = emptyList<org.sysarp.project.data.DeactivationRequest>()
            
            Logger.auth("SELLER_SERVICE", "Solicitudes de desactivación obtenidas: ${requests.size} solicitudes")
            Result.success(requests)
        } catch (e: Exception) {
            Logger.auth("SELLER_SERVICE", "Error obteniendo solicitudes de desactivación: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Solicitar desactivación de vendedor
     */
    suspend fun requestDeactivation(reason: String, sellerId: Int): Result<Unit> {
        return try {
            Logger.auth("SELLER_SERVICE", "Solicitando desactivación para vendedor: $sellerId con razón: $reason")
            
            // Por ahora solo logueamos la solicitud ya que no hay API específica para esto
            // En el futuro se puede implementar una llamada a API real para enviar la solicitud
            Logger.auth("SELLER_SERVICE", "Solicitud de desactivación registrada exitosamente")
            
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.auth("SELLER_SERVICE", "Error solicitando desactivación: ${e.message}")
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
            Logger.auth("SELLER_SERVICE", "Obteniendo vendedores conectados para admin: $adminId")
            
            val result = sellerManagementApiClient.getConnectedSellers(
                adminId = adminId,
                accessToken = token
            )
            
            result.fold(
                onSuccess = { response ->
                    Logger.auth("SELLER_SERVICE", "Vendedores conectados obtenidos exitosamente")
                    Result.success(response)
                },
                onFailure = { error ->
                    Logger.auth("SELLER_SERVICE", "Error obteniendo vendedores conectados: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("SELLER_SERVICE", "Error obteniendo vendedores conectados: ${e.message}")
            Result.failure(e)
        }
    }
    
}
