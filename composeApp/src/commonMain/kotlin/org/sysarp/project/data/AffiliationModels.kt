package org.sysarp.project.data

import kotlinx.serialization.Serializable

// Modelos para códigos QR y afiliación general

@Serializable
data class AffiliationCodeData(
    val affiliationCode: String,
    val expiresAt: String,
    val maxUses: Int,
    val remainingUses: Int,
    val branchId: Int // Assuming this is generally an Int ID
)

@Serializable
data class QRGenerationRequest(
    val type: String = "affiliation",
    val expirationHours: Int = 24,
    val maxUses: Int = 10,
    val branchId: String? = null // Keeping as String? as per original, review if it should be Int?
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
data class QRData(
    val qrId: String,
    val qrCode: String,
    val qrImageUrl: String,
    val expiresAt: String,
    val maxUses: Int,
    val remainingUses: Int,
    val branchId: String? = null // Keeping as String? as per original
)

// Solicitud genérica para un código de afiliación (distinta de la generación por Admin)
@Serializable
data class AffiliationCodeRequest(
    val expirationHours: Int = 72,
    val maxUses: Int = 1,
    val branchId: String? = null, // Keeping as String? as per original
    val notes: String? = null
)

// Respuesta genérica a una solicitud de código de afiliación
@Serializable
data class AffiliationCodeResponse(
    val success: Boolean,
    // Assuming 'message' might be useful here too, like other responses
    val message: String? = null, 
    val data: AffiliationCodeData? = null,
    val error: Boolean = false // Added error for consistency
)
