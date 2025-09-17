package org.sysarp.project.service.qr

import org.sysarp.project.data.QRCodeData
import org.sysarp.project.data.SellerLoginData
import org.sysarp.project.data.ValidateAffiliationCodeData
import org.sysarp.project.service.http.QRApiClient

class QRService {
    
    private val qrApiClient = QRApiClient()
    
    /**
     * Generar código QR desde código de afiliación usando el servidor
     */
    suspend fun generateQRFromAffiliationCode(
        affiliationCode: String,
        accessToken: String
    ): Result<QRCodeData> {
        return try {
            println("🚀 [QR_SERVICE] Generando QR desde código de afiliación: $affiliationCode")
            
            val result = qrApiClient.generateQRBase64(affiliationCode, accessToken)
            
            result.onSuccess { qrData ->
                println("✅ [QR_SERVICE] QR generado exitosamente")
            }.onFailure { error ->
                println("❌ [QR_SERVICE] Error generando QR: ${error.message}")
            }
            
            result
            
        } catch (e: Exception) {
            println("❌ [QR_SERVICE] Excepción en servicio: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Validar código de afiliación
     */
    suspend fun validateAffiliationCode(
        affiliationCode: String
    ): Result<ValidateAffiliationCodeData> {
        return try {
            println("🔍 [QR_SERVICE] Validando código de afiliación: $affiliationCode")
            
            val result = qrApiClient.validateAffiliationCode(affiliationCode)
            
            result.onSuccess { affiliationData ->
                println("✅ [QR_SERVICE] Código de afiliación válido")
            }.onFailure { error ->
                println("❌ [QR_SERVICE] Error validando código: ${error.message}")
            }
            
            result
            
        } catch (e: Exception) {
            println("❌ [QR_SERVICE] Excepción validando código: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Login de vendedor usando QR
     */
    suspend fun loginSellerWithQR(
        qrData: String,
        phone: String
    ): Result<SellerLoginData> {
        return try {
            println("🔐 [QR_SERVICE] Iniciando login con QR para teléfono: $phone")
            
            val result = qrApiClient.loginWithQR(qrData, phone)
            
            result.onSuccess { loginData ->
                println("✅ [QR_SERVICE] Login exitoso para vendedor: ${loginData.sellerName}")
            }.onFailure { error ->
                println("❌ [QR_SERVICE] Error en login con QR: ${error.message}")
            }
            
            result
            
        } catch (e: Exception) {
            println("❌ [QR_SERVICE] Excepción en login con QR: ${e.message}")
            Result.failure(e)
        }
    }
}
