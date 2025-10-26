package org.sysarp.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.sysarp.project.service.ImagePickerService
import org.sysarp.project.ui.theme.YapeHubTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {
    
    private var isInitializationComplete = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ContextProvider.setContext(this)

      ImagePickerService().setContext(this)
        
        AppLifecycleManager.initialize(application)
        
        lifecycle.addObserver(AppLifecycleManager)
        setContent {
            YapeHubTheme {
                App()
            }
        }
        lifecycleScope.launch {
            initializeAppAsync()
        }
    }
    

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
            //Todo: Usar el fingerprint si es necesario
        } catch (e: Exception) {
            // Error handling removed for production
        }
    }
    
    /**
     * Inicializa servicios críticos de forma asíncrona
     */
    private fun initializeCriticalServices() {
        try {
            // Use the service manager for safer service lifecycle management
            org.sysarp.project.service.NotificationServiceManager.startNotificationService(this@MainActivity)
        } catch (e: Exception) {
            Timber.tag("MainActivity").e(e, "Error starting notification service: ${e.message}")
        }
    }

    private suspend fun requestPermissionsAsync() {
        try {
            kotlinx.coroutines.delay(500)
            
            val hasNotificationPermission = org.sysarp.project.service.AndroidNotificationCaptureService.isNotificationServiceEnabled(this@MainActivity)
            
            if (!hasNotificationPermission) {
                requestNotificationPermission(this@MainActivity)
                
                kotlinx.coroutines.delay(2000)
                !org.sysarp.project.service.AndroidNotificationCaptureService.isNotificationServiceEnabled(this@MainActivity)
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