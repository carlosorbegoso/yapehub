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