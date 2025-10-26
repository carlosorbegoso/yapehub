package org.sysarp.project.service.affiliation

import org.sysarp.project.data.GenerateAffiliationCodeResponse
import org.sysarp.project.service.http.AffiliationCodeApiClient

class AffiliationService {
    
    private val affiliationCodeApiClient = AffiliationCodeApiClient()
    
    suspend fun generateAffiliationCode(
        adminId: Int,
        branchId: Int,
        expirationHours: Int,
        maxUses: Int,
        notes: String,
        accessToken: String
    ): Result<GenerateAffiliationCodeResponse> {
        return try {
            val result = affiliationCodeApiClient.generateAffiliationCode(
                adminId = adminId,
                branchId = branchId,
                expirationHours = expirationHours,
                maxUses = maxUses,
                notes = notes,
                accessToken = accessToken
            )
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
