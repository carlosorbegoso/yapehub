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
    val businessId: Int?,
    val businessName: String?,
    val isVerified: Boolean
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

// Modelos adicionales para AuthService
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
