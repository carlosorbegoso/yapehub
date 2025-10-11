package org.sysarp.project.service.seller

import org.sysarp.project.data.SellerLoginByPhoneResponse
import org.sysarp.project.data.SellerRegistrationResponse
import org.sysarp.project.data.AuthState
import org.sysarp.project.service.auth.AuthService
import org.sysarp.project.service.http.SellerAuthApiClient
import org.sysarp.project.service.http.SellerRegistrationApiClient

/**
 * Servicio unificado para autenticación de vendedores
 * Maneja automáticamente tanto login como registro usando código de afiliación
 */
class SellerAuthUnifiedService(
    private val authService: AuthService
) {
    
    private val sellerAuthApiClient = SellerAuthApiClient()
    private val sellerRegistrationApiClient = SellerRegistrationApiClient()
    
    /**
     * Resultado unificado de autenticación de seller
     */
    sealed class SellerAuthResult {
        data class Success(
            val sellerId: Int,
            val sellerName: String,
            val email: String,
            val phone: String,
            val branchId: Int,
            val branchName: String,
            val branchCode: String,
            val affiliationCode: String,
            val accessToken: String,
            val isNewUser: Boolean, // true si fue registro, false si fue login
            val affiliationDate: String? = null
        ) : SellerAuthResult()
        
        data class Error(
            val message: String,
            val isNetworkError: Boolean = false
        ) : SellerAuthResult()
    }
    
    /**
     * Autentica un seller usando teléfono y código de afiliación
     * Intenta login primero, si falla intenta registro automáticamente
     */
    suspend fun authenticateSeller(
        phone: String,
        affiliationCode: String,
        sellerName: String? = null
    ): SellerAuthResult {
        return try {
            // Paso 1: Intentar login primero
            val loginResult = attemptLogin(phone, affiliationCode)
            
            if (loginResult is SellerAuthResult.Success) {
                return loginResult
            }
            
            // Paso 2: Si el login falla, intentar registro
            val registrationResult = attemptRegistration(
                phone = phone,
                affiliationCode = affiliationCode,
                sellerName = sellerName ?: generateDefaultSellerName(phone)
            )
            
            if (registrationResult is SellerAuthResult.Success) {
                return registrationResult
            }
            
            // Paso 3: Si ambos fallan, retornar error
            SellerAuthResult.Error(
                message = "No se pudo autenticar el vendedor. Verifique el código de afiliación y el teléfono.",
                isNetworkError = false
            )
            
        } catch (e: Exception) {
            SellerAuthResult.Error(
                message = "Error de conexión: ${e.message}",
                isNetworkError = true
            )
        }
    }
    
    /**
     * Intenta hacer login del seller
     */
    private suspend fun attemptLogin(
        phone: String,
        affiliationCode: String
    ): SellerAuthResult {
        return try {
            val result = sellerAuthApiClient.loginSellerByPhone(phone, affiliationCode)
            
            result.fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        val loginData = response.data
                        
                        // Actualizar AuthService
                        updateAuthServiceWithSellerData(loginData)
                        
                        SellerAuthResult.Success(
                            sellerId = loginData.sellerId,
                            sellerName = loginData.sellerName,
                            email = loginData.email,
                            phone = loginData.phone,
                            branchId = loginData.branchId,
                            branchName = loginData.branchName,
                            branchCode = loginData.branchCode,
                            affiliationCode = loginData.affiliationCode,
                            accessToken = loginData.accessToken,
                            isNewUser = false,
                            affiliationDate = loginData.affiliationDate
                        )
                    } else {
                        SellerAuthResult.Error(
                            message = response.message ?: "Error en login de vendedor"
                        )
                    }
                },
                onFailure = { error ->
                    SellerAuthResult.Error(
                        message = error.message ?: "Error en login de vendedor",
                        isNetworkError = true
                    )
                }
            )
        } catch (e: Exception) {
            SellerAuthResult.Error(
                message = "Error de conexión en login: ${e.message}",
                isNetworkError = true
            )
        }
    }
    
    /**
     * Intenta registrar un nuevo seller
     */
    private suspend fun attemptRegistration(
        phone: String,
        affiliationCode: String,
        sellerName: String
    ): SellerAuthResult {
        return try {
            val result = sellerRegistrationApiClient.registerSeller(
                affiliationCode = affiliationCode,
                sellerName = sellerName,
                phone = phone
            )
            
            result.fold(
                onSuccess = { response ->
                    if (response.success && response.data != null) {
                        val registrationData = response.data
                        
                        // Actualizar AuthService
                        updateAuthServiceWithRegistrationData(registrationData)
                        
                        SellerAuthResult.Success(
                            sellerId = registrationData.sellerId,
                            sellerName = registrationData.name,
                            email = registrationData.email,
                            phone = registrationData.phone,
                            branchId = registrationData.branchId,
                            branchName = registrationData.branchName,
                            branchCode = "", // No viene en registration
                            affiliationCode = affiliationCode,
                            accessToken = registrationData.token,
                            isNewUser = true,
                            affiliationDate = registrationData.affiliationDate
                        )
                    } else {
                        SellerAuthResult.Error(
                            message = response.message ?: "Error en registro de vendedor"
                        )
                    }
                },
                onFailure = { error ->
                    SellerAuthResult.Error(
                        message = error.message ?: "Error en registro de vendedor",
                        isNetworkError = true
                    )
                }
            )
        } catch (e: Exception) {
            SellerAuthResult.Error(
                message = "Error de conexión en registro: ${e.message}",
                isNetworkError = true
            )
        }
    }
    
    /**
     * Actualiza AuthService con datos de login
     */
    private fun updateAuthServiceWithSellerData(loginData: org.sysarp.project.data.SellerLoginData) {
        val userData = org.sysarp.project.data.LoginUserData(
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
    
    /**
     * Actualiza AuthService con datos de registro
     */
    private fun updateAuthServiceWithRegistrationData(registrationData: org.sysarp.project.data.SellerRegistrationData) {
        val userData = org.sysarp.project.data.LoginUserData(
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
            branchCode = "", // No viene en registration
            isVerified = userData.isVerified,
            sellerId = userData.sellerId,
            affiliationCode = "" // Se puede obtener del contexto
        )
        authService.setAccessToken(registrationData.token)
        authService.setAuthState(AuthState.AUTHENTICATED)
    }
    
    /**
     * Genera un nombre por defecto para el seller basado en el teléfono
     */
    private fun generateDefaultSellerName(phone: String): String {
        return "Vendedor $phone"
    }
    
    /**
     * Valida si un código de afiliación es válido antes de intentar autenticación
     */
    suspend fun validateAffiliationCode(affiliationCode: String): Result<Boolean> {
        return try {
            val result = authService.validateAffiliationCode(affiliationCode)
            result.fold(
                onSuccess = { response ->
                    Result.success(response.success && response.data?.isValid == true)
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
