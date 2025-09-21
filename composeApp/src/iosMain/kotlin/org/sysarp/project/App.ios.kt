package org.sysarp.project

import org.sysarp.project.repository.YapeTransactionRepository
import org.sysarp.project.repository.YapeTransactionRepositoryImpl
import org.sysarp.project.service.NotificationCaptureService

actual fun createNotificationService(repository: YapeTransactionRepository): NotificationCaptureService {
    // iOS no soporta captura de notificaciones de otras apps
    return object : NotificationCaptureService {
        override suspend fun startCapturing() {
            // iOS no soporta captura de notificaciones
        }
        
        override suspend fun stopCapturing() {
            // iOS no soporta captura de notificaciones
        }
        
        override fun isCapturing(): Boolean {
            return false
        }
    }
}

actual fun createRepository(): YapeTransactionRepository {
    return YapeTransactionRepositoryImpl()
}

actual fun requestPermissionsAutomatically() {
    // iOS no soporta captura de notificaciones de otras apps
    // No se solicitan permisos
}

actual suspend fun checkNotificationPermission(): Boolean {
    // iOS no soporta captura de notificaciones de otras apps
    return false
}

actual suspend fun checkAccessibilityPermission(): Boolean {
    // iOS no soporta captura de notificaciones de otras apps
    return false
}
