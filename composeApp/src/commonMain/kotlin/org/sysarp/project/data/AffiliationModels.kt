package org.sysarp.project.data

import kotlinx.serialization.Serializable

@Serializable
data class AffiliationCodeRequest(
    val adminId: Int,
    val branchId: Int,
    val expirationHours: Int,
    val maxUses: Int,
    val notes: String
)

@Serializable
data class AffiliationCodeResponse(
    val success: Boolean,
    val message: String,
    val data: AffiliationCodeData?,
    val error: Boolean
)

@Serializable
data class AffiliationCodeData(
    val affiliationCode: String,
    val expiresAt: String,
    val maxUses: Int,
    val remainingUses: Int,
    val branchId: Int
)

@Serializable
data class GenerateQRRequest(
    val affiliationCode: String
)

@Serializable
data class ValidateAffiliationCodeRequest(
    val affiliationCode: String
)

@Serializable
data class ValidateAffiliationCodeResponse(
    val success: Boolean,
    val message: String,
    val data: ValidateAffiliationCodeData?,
    val error: Boolean
)

@Serializable
data class ValidateAffiliationCodeData(
    val isValid: Boolean,
    val affiliationCode: String,
    val expiresAt: String,
    val maxUses: Int,
    val remainingUses: Int,
    val branchId: Int
)

@Serializable
data class GenerateQRResponse(
    val success: Boolean,
    val message: String,
    val data: QRCodeData?,
    val error: Boolean
)

@Serializable
data class QRCodeData(
    val affiliationCode: String,
    val qrBase64: String, // Imagen PNG en Base64
    val expiresAt: String,
    val maxUses: Int,
    val remainingUses: Int,
    val branchName: String,
    val adminName: String
)

@Serializable
data class QrLoginRequest(
    val qrData: String,
    val phone: String
)

@Serializable
data class QrLoginResponse(
    val success: Boolean,
    val message: String,
    val data: SellerLoginData?,
    val error: Boolean
)

@Serializable
data class SellerLoginData(
    val sellerId: Int,
    val sellerName: String,
    val email: String,
    val phone: String,
    val branchId: Int,
    val branchName: String,
    val branchCode: String,
    val affiliationCode: String,
    val accessToken: String,
    val refreshToken: String? = null,
    val affiliationDate: String? = null
)