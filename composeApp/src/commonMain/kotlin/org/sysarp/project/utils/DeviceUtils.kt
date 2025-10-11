package org.sysarp.project.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime

/**
 * Utilidades para obtener información única del dispositivo
 */
@OptIn(ExperimentalTime::class)
object DeviceUtils {
    
    /**
     * Genera un fingerprint único del dispositivo
     * En Android usará identificadores reales, en otras plataformas un fallback
     */
    suspend fun generateDeviceFingerprint(): String = withContext(Dispatchers.Default) {
        try {
            
            // Intentar usar AndroidDeviceUtils si está disponible
            val fingerprint = try {
                generateAndroidFingerprint()
            } catch (e: Exception) {
                generateFallbackFingerprint()
            }
            
            return@withContext fingerprint
            
        } catch (e: Exception) {
            return@withContext "yapehub_fallback_${kotlin.time.Clock.System.now().toEpochMilliseconds()}"
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
        val timestamp = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val randomSuffix = (1000..9999).random()
        
        return buildString {
            append("yapehub_") // Prefijo de la app
            append("${timestamp}_") // Timestamp de creación
            append("${randomSuffix}_") // Número aleatorio
            append("mobile_") // Tipo de dispositivo
            append("${kotlin.time.Clock.System.now().toEpochMilliseconds().hashCode()}") // Hash del tiempo actual
        }
    }
    
    /**
     * Genera un fingerprint más simple para casos donde no se necesita complejidad
     */
    fun generateSimpleFingerprint(): String {
        return "yapehub_${kotlin.time.Clock.System.now().toEpochMilliseconds()}_${(1000..9999).random()}"
    }
    
    /**
     * Valida si un fingerprint tiene el formato correcto
     */
    fun isValidFingerprint(fingerprint: String): Boolean {
        return fingerprint.startsWith("yapehub_") && fingerprint.length > 20
    }
}
