package org.sysarp.project.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.sysarp.project.utils.Logger

/**
 * Utilidades para obtener información única del dispositivo
 */
object DeviceUtils {
    
    /**
     * Genera un fingerprint único del dispositivo
     * Combina múltiples identificadores para crear un ID único
     */
    suspend fun generateDeviceFingerprint(): String = withContext(Dispatchers.IO) {
        try {
            Logger.auth("DEVICE_UTILS", "Generando device fingerprint...")
            
            // Componentes del fingerprint
            val timestamp = Clock.System.now().toEpochMilliseconds()
            val randomSuffix = (1000..9999).random()
            
            // Crear fingerprint basado en características del dispositivo
            val deviceInfo = buildString {
                append("yapechamo_") // Prefijo de la app
                append("${timestamp}_") // Timestamp de creación
                append("${randomSuffix}_") // Número aleatorio
                append("mobile_") // Tipo de dispositivo
                append("${System.currentTimeMillis().hashCode()}") // Hash del tiempo actual
            }
            
            Logger.auth("DEVICE_UTILS", "Device fingerprint generado: ${deviceInfo.take(20)}...")
            return@withContext deviceInfo
            
        } catch (e: Exception) {
            Logger.auth("DEVICE_UTILS", "Error generando device fingerprint: ${e.message}")
            // Fallback a un fingerprint básico
            return@withContext "yapechamo_fallback_${System.currentTimeMillis()}"
        }
    }
    
    /**
     * Genera un fingerprint más simple para casos donde no se necesita complejidad
     */
    fun generateSimpleFingerprint(): String {
        return "yapechamo_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    /**
     * Valida si un fingerprint tiene el formato correcto
     */
    fun isValidFingerprint(fingerprint: String): Boolean {
        return fingerprint.startsWith("yapechamo_") && fingerprint.length > 20
    }
}
