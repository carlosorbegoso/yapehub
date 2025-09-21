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
    
    private var isInitializationComplete = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Establecer el contexto global
        ContextProvider.setContext(this)
        
        // Inicializar el servicio de selección de imágenes
        org.sysarp.project.service.ImagePickerService().setContext(this)
        
        AppLifecycleManager.initialize(application)
        
        lifecycle.addObserver(AppLifecycleManager)
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
            initializeDeviceFingerprint()
            initializeCriticalServices()
            requestPermissionsAsync()
            isInitializationComplete = true
            
        } catch (e: Exception) {
            isInitializationComplete = true
        }
    }
    
    /**
     * Inicializa el device fingerprint de forma asíncrona
     */
    private suspend fun initializeDeviceFingerprint() {
        try {
            val fingerprint = org.sysarp.project.utils.AndroidDeviceUtils.generateDeviceFingerprint(this@MainActivity)
        } catch (e: Exception) {
            // Error handling removed for production
        }
    }
    
    /**
     * Inicializa servicios críticos de forma asíncrona
     */
    private suspend fun initializeCriticalServices() {
        try {
            val serviceIntent = android.content.Intent(this@MainActivity, org.sysarp.project.service.AndroidNotificationCaptureService::class.java)
            startService(serviceIntent)
            
        } catch (e: Exception) {
            // Error handling removed for production
        }
    }
    
    /**
     * Solicita permisos de forma crítica para el negocio
     * Los permisos de notificaciones son esenciales para capturar Yape
     */
    private suspend fun requestPermissionsAsync() {
        try {
            // Delay mínimo para permitir que la UI se cargue
            kotlinx.coroutines.delay(500)
            
            val hasNotificationPermission = org.sysarp.project.service.AndroidNotificationCaptureService.isNotificationServiceEnabled(this@MainActivity)
            
            if (!hasNotificationPermission) {
                // Solicitar permiso inmediatamente
                requestNotificationPermission(this@MainActivity)
                
                kotlinx.coroutines.delay(2000)
                val stillMissing = !org.sysarp.project.service.AndroidNotificationCaptureService.isNotificationServiceEnabled(this@MainActivity)
            }
            
        } catch (e: Exception) {
            // Error handling removed for production
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        if (isInitializationComplete) {
            lifecycleScope.launch {
                kotlinx.coroutines.delay(200)
                requestNotificationPermission(this@MainActivity)
            }
        }
    }
    
    companion object {
        fun requestNotificationPermission(context: android.content.Context) {
            val hasNotificationPermission = org.sysarp.project.service.AndroidNotificationCaptureService.isNotificationServiceEnabled(context)
            
            // Solo abrir configuración si falta el permiso
            if (!hasNotificationPermission) {
                org.sysarp.project.service.AndroidNotificationCaptureService.requestNotificationPermission(context)
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