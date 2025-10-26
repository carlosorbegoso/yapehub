package org.sysarp.project.service

/**
 * Interfaz simple para el servicio de notificaciones
 * Solo para Android por ahora
 */
interface SimpleNotificationService {
    fun startCapture()
    fun stopCapture()
    fun isCapturing(): Boolean
}