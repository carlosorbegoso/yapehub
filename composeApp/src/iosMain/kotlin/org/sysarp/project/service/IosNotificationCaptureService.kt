package org.sysarp.project.service

import org.sysarp.project.repository.YapeTransactionRepository

class IosNotificationCaptureService(
    private val repository: YapeTransactionRepository
) : NotificationCaptureService {
    
    private var isCapturing = false
    
    override suspend fun startCapturing() {
        if (isCapturing) return
        
        // En iOS, la captura de notificaciones es más limitada
        // Se requeriría usar UserNotifications framework y extensiones
        // Por ahora, marcamos como capturando pero no implementamos la funcionalidad real
        isCapturing = true
    }
    
    override suspend fun stopCapturing() {
        isCapturing = false
    }
    
    override fun isCapturing(): Boolean {
        return isCapturing
    }
}
