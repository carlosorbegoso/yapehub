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
import org.sysarp.project.service.YapeAccessibilityService
import org.sysarp.project.service.TimberLogger
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
        
        setContent {
            YapeHubTheme {
                App()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        android.util.Log.d("MainActivity", "App resumed - triggering permission check")
        // Notificar que la app regresó del foreground
        // Esto se manejará en el ViewModel
    }
    
    companion object {
        fun requestAllPermissions(context: android.content.Context) {
            // Solicitar permisos de notificaciones
            if (!AndroidNotificationCaptureService.isNotificationServiceEnabled(context)) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
            
            // Solicitar permisos de accesibilidad
            if (!YapeAccessibilityService.isAccessibilityServiceEnabled(context)) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
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