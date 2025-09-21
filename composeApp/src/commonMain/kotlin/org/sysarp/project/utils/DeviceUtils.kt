package org.sysarp.project.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * Utilidades para obtener información única del dispositivo
 */
object DeviceUtils {
    
    /**
     * Genera un fingerprint único del dispositivo
     * En Android usará identificadores reales, en otras plataformas un fallback
     */
    suspend fun generateDeviceFingerprint(): String = withContext(Dispatchers.IO) {
        try {
            Logger.auth("DEVICE_UTILS", "Generando device fingerprint...")
            
            // Intentar usar AndroidDeviceUtils si está disponible
            val fingerprint = try {
                generateAndroidFingerprint()
            } catch (e: Exception) {
                Logger.auth("DEVICE_UTILS", "No es Android o error: ${e.message}, usando fallback")
                generateFallbackFingerprint()
            }
            
            Logger.auth("DEVICE_UTILS", "Device fingerprint generado: ${fingerprint.take(20)}...")
            return@withContext fingerprint
            
        } catch (e: Exception) {
            Logger.auth("DEVICE_UTILS", "Error generando device fingerprint: ${e.message}")
            return@withContext "yapechamo_fallback_${Clock.System.now().toEpochMilliseconds()}"
        }
    }
    
    /**
     * Genera fingerprint específico para Android
     * Esta función será sobrescrita en androidMain
     */
    private suspend fun generateAndroidFingerprint(): String {
        throw Exception("Not Android platform")
    }
    
    /**
     * Genera fingerprint de fallback para plataformas no-Android
     */
    private suspend fun generateFallbackFingerprint(): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val randomSuffix = (1000..9999).random()
        
        return buildString {
            append("yapechamo_") // Prefijo de la app
            append("${timestamp}_") // Timestamp de creación
            append("${randomSuffix}_") // Número aleatorio
            append("mobile_") // Tipo de dispositivo
            append("${Clock.System.now().toEpochMilliseconds().hashCode()}") // Hash del tiempo actual
        }
    }
    
    /**
     * Genera un fingerprint más simple para casos donde no se necesita complejidad
     */
    fun generateSimpleFingerprint(): String {
        return "yapechamo_${Clock.System.now().toEpochMilliseconds()}_${(1000..9999).random()}"
    }
    
    /**
     * Valida si un fingerprint tiene el formato correcto
     */
    fun isValidFingerprint(fingerprint: String): Boolean {
        return fingerprint.startsWith("yapechamo_") && fingerprint.length > 20
    }
}
