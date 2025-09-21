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
            val result = qrApiClient.generateQRBase64(affiliationCode, accessToken)
            result
        } catch (e: Exception) {
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
            val result = qrApiClient.validateAffiliationCode(affiliationCode)
            result
        } catch (e: Exception) {
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
            val result = qrApiClient.loginWithQR(qrData, phone)
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
