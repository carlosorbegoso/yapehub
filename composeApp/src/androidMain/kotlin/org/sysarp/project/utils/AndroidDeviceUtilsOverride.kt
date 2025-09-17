package org.sysarp.project.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sysarp.project.utils.Logger

/**
 * Sobrescribe DeviceUtils para Android usando identificadores reales del dispositivo
 */
object AndroidDeviceUtilsOverride {
    
    /**
     * Genera fingerprint usando identificadores reales de Android
     * Esta función sobrescribe la función de commonMain
     */
    suspend fun generateAndroidFingerprint(context: Context): String = withContext(Dispatchers.IO) {
        try {
            Logger.auth("ANDROID_DEVICE_UTILS", "Generando fingerprint con identificadores reales de Android...")
            
            // Usar AndroidDeviceUtils para obtener identificadores reales
            val fingerprint = AndroidDeviceUtils.generateDeviceFingerprint(context)
            
            Logger.auth("ANDROID_DEVICE_UTILS", "Fingerprint Android generado: ${fingerprint.take(20)}...")
            return@withContext fingerprint
            
        } catch (e: Exception) {
            Logger.auth("ANDROID_DEVICE_UTILS", "Error generando fingerprint Android: ${e.message}")
            // Fallback a fingerprint simple
            return@withContext AndroidDeviceUtils.generateSimpleFingerprint(context)
        }
    }
}
