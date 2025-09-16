package org.sysarp.project.service

import android.content.Context
import android.util.Log

/**
 * Servicio seguro para manejar notificaciones sin causar crashes
 */
class SafeNotificationService(private val context: Context) {
    
    companion object {
        private const val TAG = "SafeNotificationService"
    }
    
    /**
     * Verifica si el servicio de notificaciones está disponible
     */
    fun isNotificationServiceAvailable(): Boolean {
        return try {
            // Verificar si el servicio está registrado correctamente
            val packageManager = context.packageManager
            val serviceInfo = packageManager.getServiceInfo(
                android.content.ComponentName(context, AndroidNotificationCaptureService::class.java),
                0
            )
            serviceInfo != null
        } catch (e: Exception) {
            Log.w(TAG, "Servicio de notificaciones no disponible: ${e.message}")
            false
        }
    }
    
    /**
     * Inicializa el servicio de notificaciones de manera segura
     */
    fun initializeNotificationService(): Boolean {
        return try {
            Log.d(TAG, "Inicializando servicio de notificaciones...")
            
            // Verificar disponibilidad
            if (!isNotificationServiceAvailable()) {
                Log.w(TAG, "Servicio de notificaciones no disponible")
                return false
            }
            
            Log.d(TAG, "Servicio de notificaciones inicializado correctamente")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando servicio de notificaciones: ${e.message}")
            false
        }
    }
    
    /**
     * Procesa una notificación de manera segura
     */
    fun processNotificationSafely(
        packageName: String,
        title: String,
        text: String
    ): Boolean {
        return try {
            Log.d(TAG, "Procesando notificación de manera segura")
            Log.d(TAG, "Package: $packageName")
            Log.d(TAG, "Title: $title")
            Log.d(TAG, "Text: $text")
            
            // Verificar si es una notificación de Yape
            if (isYapeNotification(packageName, title, text)) {
                Log.d(TAG, "Notificación de Yape detectada")
                // Aquí se puede procesar la notificación
                return true
            }
            
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error procesando notificación: ${e.message}")
            false
        }
    }
    
    /**
     * Verifica si una notificación es de Yape
     */
    private fun isYapeNotification(packageName: String, title: String, text: String): Boolean {
        return try {
            // Verificar package name
            if (!packageName.contains("yape", ignoreCase = true)) {
                return false
            }
            
            // Verificar contenido típico de Yape
            val yapeKeywords = listOf(
                "yape", "pago", "recibiste", "envió", "soles", "s/", "código"
            )
            
            val fullText = "$title $text".lowercase()
            yapeKeywords.any { keyword -> fullText.contains(keyword) }
        } catch (e: Exception) {
            Log.e(TAG, "Error verificando notificación de Yape: ${e.message}")
            false
        }
    }
}
