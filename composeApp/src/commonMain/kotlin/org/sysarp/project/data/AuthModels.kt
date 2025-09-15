package org.sysarp.project.data

import kotlinx.serialization.Serializable

// Modelos para autenticación y login
@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val deviceFingerprint: String,
    val role: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData? = null,
    val error: Boolean = false
)

@Serializable
data class LoginData(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val user: LoginUserData
)

@Serializable
data class LoginUserData(
    val id: Int,
    val email: String,
    val role: String,
    val businessId: Int,
    val businessName: String,
    val isVerified: Boolean,
    val sellerId: Int? = null
)

// Modelos para refresh token
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class RefreshTokenResponse(
    val success: Boolean,
    val message: String,
    val data: RefreshTokenData? = null,
    val error: Boolean = false
)

@Serializable
data class RefreshTokenData(
    val accessToken: String,
    val expiresIn: Int,
    val user: RefreshUserData
)

@Serializable
data class RefreshUserData(
    val id: Int,
    val email: String,
    val role: String,
    val businessId: Int?,
    val businessName: String?,
    val isVerified: Boolean
)

// Modelos para logout
@Serializable
data class LogoutRequest(
    val accessToken: String
)

@Serializable
data class LogoutResponse(
    val success: Boolean,
    val message: String
)

// Modelos para errores de validación
@Serializable
data class ValidationErrorResponse(
    val message: String,
    val code: String,
    val details: ValidationDetails,
    val timestamp: String
)

@Serializable
data class ValidationDetails(
    val validationErrors: Map<String, FieldValidationError>
)

@Serializable
data class FieldValidationError(
    val message: String,
    val invalidValue: String
)

// Modelos para errores específicos de vendedor
@Serializable
data class SellerErrorResponse(
    val message: String,
    val code: String,
    val details: SellerErrorDetails,
    val timestamp: String
)

@Serializable
data class SellerErrorDetails(
    val field: String,
    val value: String,
    val reason: String
)



