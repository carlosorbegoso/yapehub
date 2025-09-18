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
            println("🚀 [AFFILIATION_SERVICE] Iniciando generación de código de afiliación")
            println("📋 [AFFILIATION_SERVICE] Parámetros: adminId=$adminId, branchId=$branchId, expirationHours=$expirationHours, maxUses=$maxUses, notes='$notes'")
            
            val result = affiliationCodeApiClient.generateAffiliationCode(
                adminId = adminId,
                branchId = branchId,
                expirationHours = expirationHours,
                maxUses = maxUses,
                notes = notes,
                accessToken = accessToken
            )
            
            result.onSuccess { affiliationData ->
                println("✅ [AFFILIATION_SERVICE] Código generado exitosamente: ${affiliationData.data?.affiliationCode}")
            }.onFailure { error ->
                println("❌ [AFFILIATION_SERVICE] Error generando código: ${error.message}")
            }
            
            result
            
        } catch (e: Exception) {
            println("❌ [AFFILIATION_SERVICE] Excepción en servicio: ${e.message}")
            Result.failure(e)
        }
    }
}
