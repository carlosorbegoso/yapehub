package org.sysarp.project

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import org.sysarp.project.service.AndroidNotificationCaptureService
import org.sysarp.project.service.TimberLogger
import org.sysarp.project.service.PermissionChecker
import org.sysarp.project.ui.theme.YapeHubTheme
import org.sysarp.project.ContextProvider
import org.sysarp.project.AppLifecycleManager
import org.sysarp.project.RepositorySingleton
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar Timber Logger
        TimberLogger.initialize()
        
        // Establecer el contexto global
        ContextProvider.setContext(this)
        
        // Reinicializar el repositorio ahora que tenemos contexto
        RepositorySingleton.reinitializeRepository()
        
        // Inicializar el manager de lifecycle
        AppLifecycleManager.initialize(application)
        
        // Solicitar permisos automáticamente al iniciar
        lifecycleScope.launch {
            android.util.Log.d("MainActivity", "Iniciando solicitud automática de permisos...")
            kotlinx.coroutines.delay(2000) // Esperar más tiempo para que la UI se cargue completamente
            requestAllPermissions(this@MainActivity)
        }
        
        setContent {
            YapeHubTheme {
                App()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        android.util.Log.d("MainActivity", "App resumed - triggering permission check")
        
        // Verificar permisos cuando la app regresa del foreground
        lifecycleScope.launch {
            kotlinx.coroutines.delay(500) // Pequeña pausa para que el contexto esté listo
            requestAllPermissions(this@MainActivity)
        }
    }
    
    companion object {
        fun requestAllPermissions(context: android.content.Context) {
            android.util.Log.d("MainActivity", "=== VERIFICANDO PERMISOS ===")
            
            val hasNotificationPermission = PermissionChecker.isNotificationServiceEnabled(context)
            val hasAccessibilityPermission = PermissionChecker.isAccessibilityServiceEnabled(context)
            
            android.util.Log.d("MainActivity", "Estado actual - Notificaciones: $hasNotificationPermission, Accesibilidad: $hasAccessibilityPermission")
            
            // Solo abrir configuración si faltan permisos
            if (!hasNotificationPermission) {
                android.util.Log.d("MainActivity", "❌ Falta permiso de notificaciones - Abriendo configuración...")
                PermissionChecker.requestNotificationPermission(context)
            } else {
                android.util.Log.d("MainActivity", "✅ Permiso de notificaciones ya habilitado")
            }
            
            if (!hasAccessibilityPermission) {
                android.util.Log.d("MainActivity", "❌ Falta permiso de accesibilidad - Abriendo configuración...")
                // Esperar un poco para no abrir ambas configuraciones al mismo tiempo
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    PermissionChecker.requestAccessibilityPermission(context)
                }, 1500)
            } else {
                android.util.Log.d("MainActivity", "✅ Permiso de accesibilidad ya habilitado")
            }
            
            if (hasNotificationPermission && hasAccessibilityPermission) {
                android.util.Log.d("MainActivity", "🎉 Todos los permisos están habilitados - No se necesita configuración")
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