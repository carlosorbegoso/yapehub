package org.sysarp.project.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.sysarp.project.repository.YapeTransactionRepository
import org.sysarp.project.repository.YapeTransactionRepositoryImpl

class YapeCaptureManager(private val context: Context) {
    
    private val repository = YapeTransactionRepositoryImpl()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    // Servicios de captura
    private lateinit var notificationService: AndroidNotificationCaptureService
    private lateinit var accessibilityService: YapeAccessibilityService
    private lateinit var clipboardService: YapeClipboardService
    private lateinit var overlayService: YapeOverlayService
    private lateinit var logcatService: YapeLogcatService
    
    private var isCapturing = false
    
    fun initializeServices() {
        try {
            // Inicializar todos los servicios
            notificationService = AndroidNotificationCaptureService()
            accessibilityService = YapeAccessibilityService()
            clipboardService = YapeClipboardService(context)
            overlayService = YapeOverlayService()
            logcatService = YapeLogcatService(context)
            
            Log.d("YapeCaptureManager", "Todos los servicios inicializados")
        } catch (e: Exception) {
            Log.e("YapeCaptureManager", "Error inicializando servicios", e)
        }
    }
    
    fun startAllCaptureMethods() {
        if (isCapturing) return
        isCapturing = true
        
        coroutineScope.launch {
            try {
                // Método 1: NotificationListenerService
                if (AndroidNotificationCaptureService.isNotificationServiceEnabled(context)) {
                    Log.d("YapeCaptureManager", "Iniciando captura por notificaciones")
                    // El servicio se inicia automáticamente
                } else {
                    Log.w("YapeCaptureManager", "Servicio de notificaciones no habilitado")
                }
                
                // Método 2: AccessibilityService
                if (YapeAccessibilityService.isAccessibilityServiceEnabled(context)) {
                    Log.d("YapeCaptureManager", "Iniciando captura por accesibilidad")
                    // El servicio se inicia automáticamente
                } else {
                    Log.w("YapeCaptureManager", "Servicio de accesibilidad no habilitado")
                }
                
                // Método 3: Clipboard Monitoring
                Log.d("YapeCaptureManager", "Iniciando monitoreo de portapapeles")
                clipboardService.startMonitoring()
                
                // Método 4: Overlay Detection
                if (YapeOverlayService.canDrawOverlays(context)) {
                    Log.d("YapeCaptureManager", "Iniciando detección de overlays")
                    // El servicio se inicia como servicio
                } else {
                    Log.w("YapeCaptureManager", "Permisos de overlay no habilitados")
                }
                
                // Método 5: Logcat Monitoring
                Log.d("YapeCaptureManager", "Iniciando monitoreo de logcat")
                logcatService.startMonitoring()
                
                Log.d("YapeCaptureManager", "Todos los métodos de captura iniciados")
                
            } catch (e: Exception) {
                Log.e("YapeCaptureManager", "Error iniciando métodos de captura", e)
            }
        }
    }
    
    fun stopAllCaptureMethods() {
        isCapturing = false
        
        try {
            clipboardService.stopMonitoring()
            logcatService.stopMonitoring()
            Log.d("YapeCaptureManager", "Métodos de captura detenidos")
        } catch (e: Exception) {
            Log.e("YapeCaptureManager", "Error deteniendo métodos de captura", e)
        }
    }
    
    fun getCaptureStatus(): Map<String, Boolean> {
        return mapOf(
            "NotificationService" to AndroidNotificationCaptureService.isNotificationServiceEnabled(context),
            "AccessibilityService" to YapeAccessibilityService.isAccessibilityServiceEnabled(context),
            "ClipboardService" to isCapturing,
            "OverlayService" to YapeOverlayService.canDrawOverlays(context),
            "LogcatService" to isCapturing
        )
    }
    
    fun requestAllPermissions() {
        try {
            // Solicitar permisos de notificaciones
            AndroidNotificationCaptureService.requestNotificationPermission(context)
            
            // Solicitar permisos de accesibilidad
            YapeAccessibilityService.requestAccessibilityPermission(context)
            
            // Solicitar permisos de overlay
            YapeOverlayService.requestOverlayPermission(context)
            
            Log.d("YapeCaptureManager", "Solicitando todos los permisos")
        } catch (e: Exception) {
            Log.e("YapeCaptureManager", "Error solicitando permisos", e)
        }
    }
}
