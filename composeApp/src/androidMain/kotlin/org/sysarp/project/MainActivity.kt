package org.sysarp.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.sysarp.project.ui.components.DebugLogManager
import org.sysarp.project.ui.theme.YapeHubTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {
    
    private var isInitializationComplete = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Establecer el contexto global
        ContextProvider.setContext(this)
        
        // Inicializar el manager de lifecycle
        AppLifecycleManager.initialize(application)
        
        // Registrar el lifecycle observer para detectar cuando la app se resume
        lifecycle.addObserver(AppLifecycleManager)
        
        // Cargar Compose con splash mejorado
        setContent {
            YapeHubTheme {
                App()
            }
        }
        
        // Inicialización asíncrona mejorada
        lifecycleScope.launch {
            initializeAppAsync()
        }
    }
    
    /**
     * Inicialización asíncrona mejorada con manejo de errores
     */
    private suspend fun initializeAppAsync() {
        try {
            Timber.tag("MainActivity").d("🚀 Iniciando inicialización asíncrona...")
            
            // 1. Generar device fingerprint (crítico)
            initializeDeviceFingerprint()
            
            // 2. Inicializar servicios críticos
            initializeCriticalServices()
            
            // 3. Solicitar permisos (no bloqueante)
            requestPermissionsAsync()
            
            // 4. Marcar inicialización como completa
            isInitializationComplete = true
            Timber.tag("MainActivity").d("✅ Inicialización completa")
            
        } catch (e: Exception) {
            Timber.tag("MainActivity").e("❌ Error en inicialización: ${e.message}")
            // Continuar con inicialización básica
            isInitializationComplete = true
        }
    }
    
    /**
     * Inicializa el device fingerprint de forma asíncrona
     */
    private suspend fun initializeDeviceFingerprint() {
        try {
            val fingerprint = org.sysarp.project.utils.AndroidDeviceUtils.generateDeviceFingerprint(this@MainActivity)
            Timber.tag("MainActivity").d("🔑 Device fingerprint generado: ${fingerprint.take(20)}...")
            
            // Loggear en el sistema de debug
            DebugLogManager.addLog(
                org.sysarp.project.ui.components.DebugLog(
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    type = org.sysarp.project.ui.components.LogType.PERMISSION,
                    message = "🔑 Device fingerprint generado en MainActivity",
                    details = "Fingerprint: ${fingerprint.take(20)}... (usando Android ID)"
                )
            )
        } catch (e: Exception) {
            Timber.tag("MainActivity").e("Error generando fingerprint: ${e.message}")
        }
    }
    
    /**
     * Inicializa servicios críticos de forma asíncrona
     */
    private suspend fun initializeCriticalServices() {
        try {
            Timber.tag("MainActivity").d("🔧 Inicializando servicios críticos...")
            
            // Inicializar el servicio de captura de notificaciones
            val serviceIntent = android.content.Intent(this@MainActivity, org.sysarp.project.service.AndroidNotificationCaptureService::class.java)
            startService(serviceIntent)
            
            Timber.tag("MainActivity").d("✅ Servicio de notificaciones iniciado")
            
        } catch (e: Exception) {
            Timber.tag("MainActivity").e("❌ Error iniciando servicios: ${e.message}")
        }
    }
    
    /**
     * Solicita permisos de forma crítica para el negocio
     * Los permisos de notificaciones son esenciales para capturar Yape
     */
    private suspend fun requestPermissionsAsync() {
        try {
            Timber.tag("MainActivity").d("🔐 Solicitando permisos críticos para el negocio...")
            
            // Delay mínimo para permitir que la UI se cargue
            kotlinx.coroutines.delay(500)
            
            // Verificar estado actual de permisos
            val hasNotificationPermission = org.sysarp.project.service.AndroidNotificationCaptureService.isNotificationServiceEnabled(this@MainActivity)
            
            if (!hasNotificationPermission) {
                Timber.tag("MainActivity").w("🚨 PERMISO CRÍTICO FALTANTE: Sin permisos de notificaciones")
                Timber.tag("MainActivity").w("🚨 SIN ESTO NO SE PUEDEN CAPTURAR NOTIFICACIONES DE YAPE")
                
                // Loggear en el sistema de debug
                DebugLogManager.addLog(
                    org.sysarp.project.ui.components.DebugLog(
                        timestamp = Clock.System.now().toEpochMilliseconds(),
                        type = org.sysarp.project.ui.components.LogType.ERROR,
                        message = "🚨 PERMISO CRÍTICO FALTANTE",
                        details = "Sin permisos de notificaciones - NO SE PUEDEN CAPTURAR NOTIFICACIONES DE YAPE"
                    )
                )
                
                // Solicitar permiso inmediatamente
                requestNotificationPermission(this@MainActivity)
                
                // Verificar nuevamente después de un delay
                kotlinx.coroutines.delay(2000)
                val stillMissing = !org.sysarp.project.service.AndroidNotificationCaptureService.isNotificationServiceEnabled(this@MainActivity)
                
                if (stillMissing) {
                    Timber.tag("MainActivity").e("🚨 PERMISO SIGUE FALTANDO - FUNCIONALIDAD CRÍTICA COMPROMETIDA")
                    DebugLogManager.addLog(
                        org.sysarp.project.ui.components.DebugLog(
                            timestamp = Clock.System.now().toEpochMilliseconds(),
                            type = org.sysarp.project.ui.components.LogType.ERROR,
                            message = "🚨 PERMISO SIGUE FALTANDO",
                            details = "Funcionalidad crítica comprometida - Captura de Yape no disponible"
                        )
                    )
                }
            } else {
                Timber.tag("MainActivity").d("✅ Permisos críticos OK - Captura de Yape disponible")
                DebugLogManager.addLog(
                    org.sysarp.project.ui.components.DebugLog(
                        timestamp = Clock.System.now().toEpochMilliseconds(),
                        type = org.sysarp.project.ui.components.LogType.API,
                        message = "✅ Permisos críticos OK",
                        details = "Captura de notificaciones de Yape disponible"
                    )
                )
            }
            
        } catch (e: Exception) {
            Timber.tag("MainActivity").e("❌ Error crítico solicitando permisos: ${e.message}")
            DebugLogManager.addLog(
                org.sysarp.project.ui.components.DebugLog(
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    type = org.sysarp.project.ui.components.LogType.ERROR,
                    message = "❌ Error crítico en permisos",
                    details = "Error: ${e.message} - Funcionalidad crítica comprometida"
                )
            )
        }
    }
    
    override fun onResume() {
        super.onResume()
        Timber.tag("MainActivity").d("App resumed - triggering permission check")
        
        // Solo verificar permisos si la inicialización está completa
        if (isInitializationComplete) {
            lifecycleScope.launch {
                kotlinx.coroutines.delay(200)
                requestNotificationPermission(this@MainActivity)
            }
        }
    }
    
    companion object {
        fun requestNotificationPermission(context: android.content.Context) {
            Timber.tag("MainActivity").d("=== VERIFICANDO PERMISO DE NOTIFICACIONES ===")
            
            val hasNotificationPermission = org.sysarp.project.service.AndroidNotificationCaptureService.isNotificationServiceEnabled(context)

            Timber.tag("MainActivity")
                .d("Estado actual - Notificaciones: $hasNotificationPermission")
            
            // Solo abrir configuración si falta el permiso
            if (!hasNotificationPermission) {
                Timber.tag("MainActivity")
                    .d("❌ Falta permiso de notificaciones - Abriendo configuración...")
                org.sysarp.project.service.AndroidNotificationCaptureService.requestNotificationPermission(context)
            } else {
                Timber.tag("MainActivity").d("✅ Permiso de notificaciones ya habilitado")
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    YapeHubTheme {
        App()
    }
}