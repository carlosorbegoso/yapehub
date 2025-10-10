package org.sysarp.project.service.auth

import org.sysarp.project.data.LoginUserData
import org.sysarp.project.data.SellerLoginByPhoneResponse
import org.sysarp.project.data.ForgotPasswordData
import org.sysarp.project.data.ValidateAffiliationCodeResponse
import org.sysarp.project.service.http.AuthApiClient
import org.sysarp.project.utils.DeviceUtils

/**
 * Servicio para operaciones de autenticación
 * Responsabilidad única: Ejecutar operaciones de autenticación
 */
class AuthOperations(
    private val authApiClient: AuthApiClient
) {
    
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
            val fingerprint = deviceFingerprint ?: DeviceUtils.generateDeviceFingerprint()
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
                        Result.success(loginData)
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
                businessName, businessType, ruc, email, password, phone, address, contactName
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
                        Result.success(loginData)
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
     * Login de vendedor por teléfono
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
}
