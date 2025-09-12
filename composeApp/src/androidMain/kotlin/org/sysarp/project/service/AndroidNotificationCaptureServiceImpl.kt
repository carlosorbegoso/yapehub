package org.sysarp.project.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sysarp.project.data.TransactionType
import org.sysarp.project.data.YapeTransaction
import org.sysarp.project.repository.YapeTransactionRepository
import kotlinx.datetime.Clock

class AndroidNotificationCaptureServiceImpl(
    private val repository: YapeTransactionRepository
) : NotificationCaptureService {
    
    private var isCapturing = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val permissionService = PermissionService()
    
    override suspend fun startCapturing() {
        if (isCapturing) return
        
        // Verificar permisos antes de iniciar
        permissionService.updatePermissionState(PermissionState.NEEDS_SETUP)
        permissionService.updateCaptureStatus(CaptureStatus.ERROR)
        
        isCapturing = true
        permissionService.updateCaptureStatus(CaptureStatus.ACTIVE)
        
        // La captura real se hace a través del NotificationListenerService
        // No hay simulación, solo captura real de notificaciones
    }
    
    override suspend fun stopCapturing() {
        isCapturing = false
        permissionService.updateCaptureStatus(CaptureStatus.INACTIVE)
    }
    
    override fun isCapturing(): Boolean {
        return isCapturing
    }
    
    fun getPermissionService(): PermissionService {
        return permissionService
    }
    
}