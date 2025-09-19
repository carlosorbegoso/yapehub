package org.sysarp.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.sysarp.project.ui.theme.YapeHubTheme
import timber.log.Timber
import kotlinx.datetime.Clock

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar Timber Logger
        // TimberLogger.initialize()
        
        // Establecer el contexto global
        ContextProvider.setContext(this)
        
        // Sin base de datos local - eliminado RepositorySingleton
        
        // Generar y loggear el device fingerprint real
        lifecycleScope.launch {
            try {
                val fingerprint = org.sysarp.project.utils.AndroidDeviceUtils.generateDeviceFingerprint(this@MainActivity)
                Timber.tag("MainActivity")
                    .d("🔑 Device fingerprint generado: ${fingerprint.take(20)}...")
                
                // También loggear en el sistema de debug
                org.sysarp.project.ui.components.DebugLogManager.addLog(
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
        
        // Inicializar el manager de lifecycle
        AppLifecycleManager.initialize(application)
        
        // Registrar el lifecycle observer para detectar cuando la app se resume
        lifecycle.addObserver(AppLifecycleManager)
        
        // Cargar Compose directamente (sin splash nativo)
        setContent {
            YapeHubTheme {
                App()
            }
        }
        
        // Solicitar permisos después de cargar la app
        lifecycleScope.launch {
            Timber.tag("MainActivity").d("Iniciando solicitud automática de permisos...")
            kotlinx.coroutines.delay(500)
            requestNotificationPermission(this@MainActivity)
            
            // Inicializar el servicio de captura de notificaciones
            try {
                Timber.tag("MainActivity").d("Inicializando AndroidNotificationCaptureService...")
                val serviceIntent = android.content.Intent(this@MainActivity, org.sysarp.project.service.AndroidNotificationCaptureService::class.java)
                startService(serviceIntent)
                Timber.tag("MainActivity").d("✅ Servicio de notificaciones iniciado")
            } catch (e: Exception) {
                Timber.tag("MainActivity").e("❌ Error iniciando servicio: ${e.message}")
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        Timber.tag("MainActivity").d("App resumed - triggering permission check")
        
        // Verificar permisos cuando la app regresa del foreground
        lifecycleScope.launch {
            kotlinx.coroutines.delay(200) // Reducido para respuesta más rápida
            requestNotificationPermission(this@MainActivity)
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