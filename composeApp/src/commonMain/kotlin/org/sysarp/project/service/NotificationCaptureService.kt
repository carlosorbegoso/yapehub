package org.sysarp.project.service

import kotlinx.datetime.Instant
import org.sysarp.project.data.TransactionType
import org.sysarp.project.data.YapeTransaction
import org.sysarp.project.data.YapeNotification

interface NotificationCaptureService {
    suspend fun startCapturing()
    suspend fun stopCapturing()
    fun isCapturing(): Boolean
}

