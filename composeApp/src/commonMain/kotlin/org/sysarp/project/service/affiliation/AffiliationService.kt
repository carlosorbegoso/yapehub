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
            println("🔐 [AFFILIATION_SERVICE] Generando código de afiliación para admin: $adminId")
            
            val result = affiliationApiClient.generateAffiliationCode(
                adminId = adminId,
                branchId = branchId,
                expirationHours = expirationHours,
                maxUses = maxUses,
                notes = notes,
                accessToken = accessToken
            )
            
            result.onSuccess { affiliationData ->
                println("🔐 [AFFILIATION_SERVICE] Código generado exitosamente: ${affiliationData.affiliationCode}")
            }.onFailure { error ->
                println("🔐 [AFFILIATION_SERVICE] Error generando código: ${error.message}")
            }
            
            result
            
        } catch (e: Exception) {
            println("🔐 [AFFILIATION_SERVICE] ERROR: Error en servicio de afiliación: ${e.message}")
            Result.failure(e)
        }
    }
}
