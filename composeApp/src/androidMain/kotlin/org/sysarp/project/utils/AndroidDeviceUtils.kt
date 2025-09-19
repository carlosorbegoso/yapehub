package org.sysarp.project.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import timber.log.Timber
import java.security.MessageDigest

/**
 * Utilidades Android-específicas para obtener identificadores únicos del dispositivo
 */
object AndroidDeviceUtils {
    
    /**
     * Genera un fingerprint único usando identificadores disponibles del dispositivo Android
     * Sin usar permisos privilegiados del sistema
     */
    suspend fun generateDeviceFingerprint(context: Context): String = withContext(Dispatchers.IO) {
        try {
            Timber.tag("AndroidDeviceUtils")
                .d("Generando device fingerprint usando identificadores disponibles...")
            
            // Obtener identificadores únicos del dispositivo (sin permisos privilegiados)
            val androidId = getAndroidId(context)
            val deviceModel = Build.MODEL
            val deviceBrand = Build.BRAND
            val deviceManufacturer = Build.MANUFACTURER
            val deviceFingerprint = getDeviceFingerprint()

            Timber.tag("AndroidDeviceUtils").d("Android ID: ${androidId.take(10)}...")
            Timber.tag("AndroidDeviceUtils").d("Device: $deviceBrand $deviceModel")
            Timber.tag("AndroidDeviceUtils").d("Manufacturer: $deviceManufacturer")
            Timber.tag("AndroidDeviceUtils").d("Fingerprint: ${deviceFingerprint.take(10)}...")
            
            // Crear fingerprint combinando identificadores únicos disponibles
            val deviceInfo = buildString {
                append("yapechamo_") // Prefijo de la app
                append("${androidId}_") // Android ID (único por dispositivo)
                append("${deviceBrand}_") // Marca del dispositivo
                append("${deviceModel}_") // Modelo del dispositivo
                append("${deviceManufacturer}_") // Fabricante
                append(deviceFingerprint) // Fingerprint del dispositivo
            }
            
            // Crear hash MD5 para hacer el fingerprint más corto y consistente
            val fingerprint = createMD5Hash(deviceInfo)

            Timber.tag("AndroidDeviceUtils")
                .d("Device fingerprint generado: ${fingerprint.take(20)}...")
            fingerprint
            
        } catch (e: Exception) {
            Timber.tag("AndroidDeviceUtils").e("Error generando device fingerprint: ${e.message}")
            // Fallback a un fingerprint básico usando Android ID
            try {
                val androidId = getAndroidId(context)
                "yapechamo_${androidId}_fallback"
            } catch (fallbackError: Exception) {
                Timber.tag("AndroidDeviceUtils").e("Error en fallback: ${fallbackError.message}")
                "yapechamo_unknown_device_${Clock.System.now().toEpochMilliseconds()}"
            }
        }
    }
    
    /**
     * Obtiene el Android ID único del dispositivo
     */
    @SuppressLint("HardwareIds")
    private fun getAndroidId(context: Context): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: "unknown_android_id"
        } catch (e: Exception) {
            Timber.tag("AndroidDeviceUtils").e("Error obteniendo Android ID: ${e.message}")
            "error_android_id"
        }
    }
    
    /**
     * Obtiene el fingerprint del dispositivo (disponible sin permisos especiales)
     */
    private fun getDeviceFingerprint(): String {
        return try {
            Build.FINGERPRINT ?: "unknown_fingerprint"
        } catch (e: Exception) {
            Timber.tag("AndroidDeviceUtils").e("Error obteniendo fingerprint: ${e.message}")
            "error_fingerprint"
        }
    }
    
    /**
     * Crea un hash MD5 de la cadena de entrada
     */
    private fun createMD5Hash(input: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val hashBytes = md.digest(input.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Timber.tag("AndroidDeviceUtils").e("Error creando MD5: ${e.message}")
            // Fallback: usar hashCode
            input.hashCode().toString()
        }
    }
    
    /**
     * Genera un fingerprint más simple usando solo Android ID
     */
    fun generateSimpleFingerprint(context: Context): String {
        return try {
            val androidId = getAndroidId(context)
            "yapechamo_${androidId}_simple"
        } catch (e: Exception) {
            Timber.tag("AndroidDeviceUtils").e("Error en fingerprint simple: ${e.message}")
            "yapechamo_simple_${Clock.System.now().toEpochMilliseconds()}"
        }
    }
    
    /**
     * Valida si un fingerprint tiene el formato correcto
     */
    fun isValidFingerprint(fingerprint: String): Boolean {
        return fingerprint.startsWith("yapechamo_") && fingerprint.length >= 20
    }
}
