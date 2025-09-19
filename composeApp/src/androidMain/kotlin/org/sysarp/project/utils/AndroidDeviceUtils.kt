package org.sysarp.project.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.MessageDigest
import java.util.*
import kotlinx.datetime.Clock

/**
 * Utilidades Android-específicas para obtener identificadores únicos del dispositivo
 */
object AndroidDeviceUtils {
    
    /**
     * Genera un fingerprint único usando identificadores reales del dispositivo Android
     */
    suspend fun generateDeviceFingerprint(context: Context): String = withContext(Dispatchers.IO) @androidx.annotation.RequiresPermission(
        "android.permission.READ_PRIVILEGED_PHONE_STATE"
    ) {
        try {
            Timber.tag("AndroidDeviceUtils")
                .d("Generando device fingerprint usando identificadores reales...")
            
            // Obtener identificadores únicos del dispositivo
            val androidId = getAndroidId(context)
            val deviceModel = Build.MODEL
            val deviceBrand = Build.BRAND
            val deviceManufacturer = Build.MANUFACTURER
            val deviceSerial = getDeviceSerial()

            Timber.tag("AndroidDeviceUtils").d("Android ID: ${androidId.take(10)}...")
            Timber.tag("AndroidDeviceUtils").d("Device: $deviceBrand $deviceModel")
            Timber.tag("AndroidDeviceUtils").d("Manufacturer: $deviceManufacturer")
            Timber.tag("AndroidDeviceUtils").d("Serial: ${deviceSerial.take(10)}...")
            
            // Crear fingerprint combinando identificadores únicos
            val deviceInfo = buildString {
                append("yapechamo_") // Prefijo de la app
                append("${androidId}_") // Android ID (único por dispositivo)
                append("${deviceBrand}_") // Marca del dispositivo
                append("${deviceModel}_") // Modelo del dispositivo
                append("${deviceManufacturer}_") // Fabricante
                append(deviceSerial) // Serial del dispositivo
            }
            
            // Crear hash MD5 para hacer el fingerprint más corto y consistente
            val fingerprint = createMD5Hash(deviceInfo)

            Timber.tag("AndroidDeviceUtils")
                .d("Device fingerprint generado: ${fingerprint.take(20)}...")
            return@withContext fingerprint
            
        } catch (e: Exception) {
            Timber.tag("AndroidDeviceUtils").e("Error generando device fingerprint: ${e.message}")
            // Fallback a un fingerprint básico usando Android ID
            return@withContext try {
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
     * Obtiene el serial del dispositivo (si está disponible)
     */
    @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    private fun getDeviceSerial(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Build.getSerial() ?: "unknown_serial"
            } else {
                @Suppress("DEPRECATION")
                Build.SERIAL ?: "unknown_serial"
            }
        } catch (e: Exception) {
            Timber.tag("AndroidDeviceUtils").e("Error obteniendo serial: ${e.message}")
            "error_serial"
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
