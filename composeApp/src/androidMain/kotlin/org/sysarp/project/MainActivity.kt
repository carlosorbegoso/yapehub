package org.sysarp.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.sysarp.project.ui.theme.YapeHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar Timber Logger
        // TimberLogger.initialize()
        
        // Establecer el contexto global
        ContextProvider.setContext(this)
        
        // Reinicializar el repositorio ahora que tenemos contexto
        RepositorySingleton.reinitializeRepository()
        
        // Generar y loggear el device fingerprint real
        lifecycleScope.launch {
            try {
                val fingerprint = org.sysarp.project.utils.AndroidDeviceUtils.generateDeviceFingerprint(this@MainActivity)
                android.util.Log.d("MainActivity", "🔑 Device fingerprint generado: ${fingerprint.take(20)}...")
                
                // También loggear en el sistema de debug
                org.sysarp.project.ui.components.DebugLogManager.addLog(
                    org.sysarp.project.ui.components.DebugLog(
                        timestamp = System.currentTimeMillis(),
                        type = org.sysarp.project.ui.components.LogType.PERMISSION,
                        message = "🔑 Device fingerprint generado en MainActivity",
                        details = "Fingerprint: ${fingerprint.take(20)}... (usando Android ID)"
                    )
                )
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error generando fingerprint: ${e.message}")
            }
        }
        
        // Inicializar el manager de lifecycle
        AppLifecycleManager.initialize(application)
        
        // Cargar Compose directamente (sin splash nativo)
        setContent {
            YapeHubTheme {
                App()
            }
        }
        
        // Solicitar permisos después de cargar la app
        lifecycleScope.launch {
            android.util.Log.d("MainActivity", "Iniciando solicitud automática de permisos...")
            kotlinx.coroutines.delay(500)
            requestNotificationPermission(this@MainActivity)
        }
    }
    
    override fun onResume() {
        super.onResume()
        android.util.Log.d("MainActivity", "App resumed - triggering permission check")
        
        // Verificar permisos cuando la app regresa del foreground
        lifecycleScope.launch {
            kotlinx.coroutines.delay(200) // Reducido para respuesta más rápida
            requestNotificationPermission(this@MainActivity)
        }
    }
    
    companion object {
        fun requestNotificationPermission(context: android.content.Context) {
            android.util.Log.d("MainActivity", "=== VERIFICANDO PERMISO DE NOTIFICACIONES ===")
            
            val hasNotificationPermission = org.sysarp.project.service.AndroidNotificationCaptureService.isNotificationServiceEnabled(context)
            
            android.util.Log.d("MainActivity", "Estado actual - Notificaciones: $hasNotificationPermission")
            
            // Solo abrir configuración si falta el permiso
            if (!hasNotificationPermission) {
                android.util.Log.d("MainActivity", "❌ Falta permiso de notificaciones - Abriendo configuración...")
                org.sysarp.project.service.AndroidNotificationCaptureService.requestNotificationPermission(context)
            } else {
                android.util.Log.d("MainActivity", "✅ Permiso de notificaciones ya habilitado")
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