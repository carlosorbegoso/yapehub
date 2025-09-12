package org.sysarp.project.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Servicio para manejar el estado de permisos y captura de notificaciones
 */
class PermissionService {
    
    private val _permissionState = MutableStateFlow(PermissionState.UNKNOWN)
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()
    
    private val _captureStatus = MutableStateFlow(CaptureStatus.UNKNOWN)
    val captureStatus: StateFlow<CaptureStatus> = _captureStatus.asStateFlow()
    
    fun updatePermissionState(state: PermissionState) {
        _permissionState.value = state
    }
    
    fun updateCaptureStatus(status: CaptureStatus) {
        _captureStatus.value = status
    }
    
    fun getPermissionMessage(): String {
        return when (_permissionState.value) {
            PermissionState.GRANTED -> "Permisos habilitados - Capturando notificaciones"
            PermissionState.DENIED -> "Permisos denegados - No se pueden capturar notificaciones"
            PermissionState.NEEDS_SETUP -> "Configuración necesaria - Habilita el servicio de notificaciones"
            PermissionState.UNKNOWN -> "Verificando permisos..."
        }
    }
    
    fun getCaptureMessage(): String {
        return when (_captureStatus.value) {
            CaptureStatus.ACTIVE -> "Capturando notificaciones de Yape"
            CaptureStatus.INACTIVE -> "Captura detenida"
            CaptureStatus.ERROR -> "Error en la captura - Verifica los permisos"
            CaptureStatus.UNKNOWN -> "Estado desconocido"
        }
    }
}

enum class PermissionState {
    UNKNOWN,
    GRANTED,
    DENIED,
    NEEDS_SETUP
}

enum class CaptureStatus {
    UNKNOWN,
    ACTIVE,
    INACTIVE,
    ERROR
}
