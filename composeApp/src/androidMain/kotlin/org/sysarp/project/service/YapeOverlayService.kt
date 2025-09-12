package org.sysarp.project.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sysarp.project.R
import org.sysarp.project.repository.YapeTransactionRepository
import org.sysarp.project.repository.YapeTransactionRepositoryImpl

class YapeOverlayService : Service() {
    
    private lateinit var repository: YapeTransactionRepository
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var isMonitoring = false
    
    override fun onCreate() {
        super.onCreate()
        repository = YapeTransactionRepositoryImpl()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createOverlay()
    }
    
    private fun createOverlay() {
        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.yape_overlay, null)
        
        val layoutParams = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = PixelFormat.TRANSLUCENT
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP
            y = 0
        }
        
        windowManager.addView(overlayView, layoutParams)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startMonitoring()
        return START_STICKY
    }
    
    private fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        coroutineScope.launch {
            while (isMonitoring) {
                try {
                    // Monitorear cambios en la pantalla
                    checkForYapeNotifications()
                    delay(2000) // Verificar cada 2 segundos
                } catch (e: Exception) {
                    Log.e("YapeOverlay", "Error en monitoreo", e)
                }
            }
        }
    }
    
    private suspend fun checkForYapeNotifications() {
        // Este método puede ser expandido para detectar overlays de Yape
        // Por ahora, solo registramos que el servicio está activo
        Log.d("YapeOverlay", "Monitoreando overlays de Yape...")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isMonitoring = false
        try {
            windowManager.removeView(overlayView)
        } catch (e: Exception) {
            Log.e("YapeOverlay", "Error removiendo overlay", e)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    companion object {
        fun canDrawOverlays(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }
        
        fun requestOverlayPermission(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        }
    }
}
