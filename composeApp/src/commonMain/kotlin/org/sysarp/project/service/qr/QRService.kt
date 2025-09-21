package org.sysarp.project.service.qr

import org.sysarp.project.data.QRCodeData
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

}
