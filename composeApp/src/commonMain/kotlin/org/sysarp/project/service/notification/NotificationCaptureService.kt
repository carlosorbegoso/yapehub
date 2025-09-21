package org.sysarp.project.service

interface NotificationCaptureService {
    suspend fun startCapturing()
    suspend fun stopCapturing()
    fun isCapturing(): Boolean
}
