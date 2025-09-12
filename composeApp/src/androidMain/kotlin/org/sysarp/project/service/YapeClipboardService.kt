package org.sysarp.project.service

import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sysarp.project.repository.YapeTransactionRepository
import org.sysarp.project.repository.YapeTransactionRepositoryImpl

class YapeClipboardService(private val context: Context) {
    
    private val repository = YapeTransactionRepositoryImpl()
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var isMonitoring = false
    private var lastClipboardContent = ""
    
    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        coroutineScope.launch {
            while (isMonitoring) {
                try {
                    val clipboard = clipboardManager.primaryClip
                    if (clipboard != null && clipboard.itemCount > 0) {
                        val currentContent = clipboard.getItemAt(0).text?.toString() ?: ""
                        
                        if (currentContent != lastClipboardContent && 
                            currentContent.contains("Yape", ignoreCase = true) &&
                            currentContent.contains("te envió", ignoreCase = true)) {
                            
                            Log.d("YapeClipboard", "Contenido de Yape detectado en portapapeles: $currentContent")
                            processYapeContent(currentContent)
                            lastClipboardContent = currentContent
                        }
                    }
                } catch (e: Exception) {
                    Log.e("YapeClipboard", "Error monitoreando portapapeles", e)
                }
                
                delay(1000) // Verificar cada segundo
            }
        }
    }
    
    fun stopMonitoring() {
        isMonitoring = false
    }
    
    private suspend fun processYapeContent(content: String) {
        try {
            if (YapeNotificationParser.isYapeNotification(content)) {
                val transaction = YapeNotificationParser.parseYapeNotification(
                    notificationText = content,
                    businessName = "Negocio Principal"
                )
                
                transaction?.let {
                    repository.insertTransaction(it)
                    Log.d("YapeClipboard", "Transacción guardada desde portapapeles: ${it.amount}")
                }
            }
        } catch (e: Exception) {
            Log.e("YapeClipboard", "Error procesando contenido del portapapeles", e)
        }
    }
}
