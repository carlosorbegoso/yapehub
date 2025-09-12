package org.sysarp.project.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.random.Random

data class QRCodeData(
    val id: String,
    val businessId: String,
    val businessName: String,
    val branchCode: String,
    val branchName: String,
    val expiresAt: Instant,
    val createdAt: Instant,
    val isActive: Boolean = true
) {
    fun toQRString(): String {
        return "YAPE_AFFILIATION:${id}:${businessId}:${branchCode}:${expiresAt.epochSeconds}"
    }
    
    fun isExpired(): Boolean {
        return Clock.System.now() > expiresAt
    }
    
    fun getTimeRemaining(): String {
        val now = Clock.System.now()
        val remaining = expiresAt - now
        
        return if (remaining.inWholeSeconds <= 0) {
            "Expirado"
        } else {
            val minutes = remaining.inWholeMinutes
            val seconds = remaining.inWholeSeconds % 60
            "${minutes}m ${seconds}s"
        }
    }
}

class QRService {
    private val _activeQRCode = MutableStateFlow<QRCodeData?>(null)
    val activeQRCode: StateFlow<QRCodeData?> = _activeQRCode.asStateFlow()
    
    private val _qrHistory = MutableStateFlow<List<QRCodeData>>(emptyList())
    val qrHistory: StateFlow<List<QRCodeData>> = _qrHistory.asStateFlow()
    
    fun generateQRCode(
        businessId: String,
        businessName: String,
        branchCode: String,
        branchName: String
    ): QRCodeData {
        // Invalidar QR anterior si existe
        _activeQRCode.value?.let { oldQR ->
            _activeQRCode.value = oldQR.copy(isActive = false)
        }
        
        val now = Clock.System.now()
        val expiresAt = now.plus(kotlin.time.Duration.parse("5m")) // Expira en 5 minutos
        
        val newQR = QRCodeData(
            id = "qr_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}",
            businessId = businessId,
            businessName = businessName,
            branchCode = branchCode,
            branchName = branchName,
            expiresAt = expiresAt,
            createdAt = now
        )
        
        _activeQRCode.value = newQR
        _qrHistory.value = listOf(newQR) + _qrHistory.value.take(9) // Mantener solo los últimos 10
        
        return newQR
    }
    
    fun invalidateQRCode() {
        _activeQRCode.value?.let { qr ->
            _activeQRCode.value = qr.copy(isActive = false)
        }
    }
    
    fun getQRCodeStatus(): String {
        val qr = _activeQRCode.value
        return when {
            qr == null -> "No hay código QR activo"
            !qr.isActive -> "Código QR inactivo"
            qr.isExpired() -> "Código QR expirado"
            else -> "Código QR activo - ${qr.getTimeRemaining()} restantes"
        }
    }
    
    fun canGenerateNewQR(): Boolean {
        val qr = _activeQRCode.value
        return when {
            qr == null -> true
            !qr.isActive -> true
            qr.isExpired() -> true
            else -> false
        }
    }
}
