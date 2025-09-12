package org.sysarp.project

import org.sysarp.project.repository.YapeTransactionRepository
import org.sysarp.project.repository.YapeTransactionRepositoryImpl
import org.sysarp.project.service.NotificationCaptureService
import org.sysarp.project.service.AndroidNotificationCaptureServiceImpl
import org.sysarp.project.service.YapeCaptureManager
import org.sysarp.project.service.PermissionChecker
import org.sysarp.project.service.TimberLogger
import android.content.Context

actual fun createNotificationService(repository: YapeTransactionRepository): NotificationCaptureService {
    // Implementar servicio real de captura de notificaciones
    return AndroidNotificationCaptureServiceImpl(repository)
}

actual fun createRepository(): YapeTransactionRepository {
    val context = ContextProvider.getContext()
    TimberLogger.database("🔍 [TIMING] Creando repositorio - Contexto disponible: ${context != null}")
    TimberLogger.database("🔍 [TIMING] Stack trace: ${Thread.currentThread().stackTrace.take(5).joinToString("\n")}")
    
    return if (context != null) {
        TimberLogger.database("✅ [SUCCESS] Creando repositorio CON contexto para persistencia")
        YapeTransactionRepositoryImpl(context)
    } else {
        TimberLogger.w("⚠️ [WARNING] Creando repositorio SIN contexto - solo memoria")
        TimberLogger.w("⚠️ [WARNING] Esto significa que el repositorio se creó ANTES de MainActivity.onCreate()")
        YapeTransactionRepositoryImpl()
    }
}

// Función para obtener el manager de captura
fun createYapeCaptureManager(context: Context): YapeCaptureManager {
    return YapeCaptureManager(context)
}

actual fun requestPermissionsAutomatically() {
    try {
        val context = ContextProvider.getContext()
        if (context != null) {
            android.util.Log.d("YapeHub", "Solicitando permisos automáticamente...")
            
            // Solicitar permisos de notificaciones
            if (!PermissionChecker.isNotificationServiceEnabled(context)) {
                PermissionChecker.requestNotificationPermission(context)
            }
            
            // Solicitar permisos de accesibilidad
            if (!PermissionChecker.isAccessibilityServiceEnabled(context)) {
                PermissionChecker.requestAccessibilityPermission(context)
            }
        } else {
            android.util.Log.w("YapeHub", "Contexto no disponible para solicitar permisos")
        }
    } catch (e: Exception) {
        android.util.Log.e("YapeHub", "Error solicitando permisos automáticamente", e)
    }
}

actual suspend fun checkNotificationPermission(): Boolean {
    return try {
        val context = ContextProvider.getContext()
        if (context != null) {
            PermissionChecker.isNotificationServiceEnabled(context)
        } else {
            android.util.Log.w("YapeHub", "Contexto no disponible para verificar permisos de notificaciones")
            false
        }
    } catch (e: Exception) {
        android.util.Log.e("YapeHub", "Error verificando permisos de notificaciones", e)
        false
    }
}

actual suspend fun checkAccessibilityPermission(): Boolean {
    return try {
        val context = ContextProvider.getContext()
        if (context != null) {
            PermissionChecker.isAccessibilityServiceEnabled(context)
        } else {
            android.util.Log.w("YapeHub", "Contexto no disponible para verificar permisos de accesibilidad")
            false
        }
    } catch (e: Exception) {
        android.util.Log.e("YapeHub", "Error verificando permisos de accesibilidad", e)
        false
    }
}

