package org.sysarp.project.service

import org.sysarp.project.data.*
import org.sysarp.project.service.http.SellerApiClient
import org.sysarp.project.utils.Logger

/**
 * Servicio especializado para manejar vendedores
 */
class SellerService {
    
    private val sellerApiClient = SellerApiClient()
    
    /**
     * Registrar vendedor con código de afiliación
     */
    suspend fun registerSeller(
        affiliationCode: String,
        sellerName: String,
        phone: String
    ): Result<org.sysarp.project.data.SellerRegistrationResponse> {
        return try {
            Logger.auth("SELLER_SERVICE", "Iniciando registro de vendedor: $sellerName")
            
            val result = sellerApiClient.registerSeller(
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
            
            val result = sellerApiClient.getMySellers(adminId, page, limit, token)
            
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

    /**
     * Login de vendedor por teléfono
     */
    suspend fun loginSellerByPhone(phone: String): Result<org.sysarp.project.data.SellerLoginByPhoneResponse> {
        return try {
            Logger.auth("SELLER_SERVICE", "Iniciando login de vendedor por teléfono: $phone")
            
            val result = sellerApiClient.loginSellerByPhone(phone)
            
            result.fold(
                onSuccess = { response ->
                    Logger.auth("SELLER_SERVICE", "Login de vendedor exitoso por teléfono: $phone")
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
            // TODO: Implementar llamada a API real
            val requests = emptyList<org.sysarp.project.data.DeactivationRequest>()
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Solicitar desactivación de vendedor
     */
    suspend fun requestDeactivation(reason: String, sellerId: Int): Result<Unit> {
        return try {
            // TODO: Implementar lógica de solicitud
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
