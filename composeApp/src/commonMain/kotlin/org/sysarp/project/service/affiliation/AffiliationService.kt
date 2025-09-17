package org.sysarp.project.service.affiliation

import org.sysarp.project.data.AffiliationCodeData
import org.sysarp.project.service.http.AffiliationApiClient

class AffiliationService(
    private val affiliationApiClient: AffiliationApiClient
) {
    
    suspend fun generateAffiliationCode(
        adminId: Int,
        branchId: Int,
        expirationHours: Int,
        maxUses: Int,
        notes: String,
        accessToken: String
    ): Result<AffiliationCodeData> {
        return try {
            
            val result = affiliationApiClient.generateAffiliationCode(
                adminId = adminId,
                branchId = branchId,
                expirationHours = expirationHours,
                maxUses = maxUses,
                notes = notes,
                accessToken = accessToken
            )
            
            result.onSuccess { affiliationData ->
            }.onFailure { error ->
            }
            
            result
            
        } catch (e: Exception) {
            println("❌ [AFFILIATION_SERVICE] Error en servicio: ${e.message}")
            Result.failure(e)
        }
    }
}
