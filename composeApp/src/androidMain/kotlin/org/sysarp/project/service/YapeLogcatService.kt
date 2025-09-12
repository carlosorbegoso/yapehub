package org.sysarp.project.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sysarp.project.repository.YapeTransactionRepository
import org.sysarp.project.repository.YapeTransactionRepositoryImpl
import java.io.BufferedReader
import java.io.InputStreamReader

class YapeLogcatService(private val context: Context) {
    
    private val repository = YapeTransactionRepositoryImpl()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var isMonitoring = false
    
    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        coroutineScope.launch {
            try {
                val process = Runtime.getRuntime().exec("logcat -s Yape:*")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                
                while (isMonitoring) {
                    val line = reader.readLine()
                    if (line != null && line.contains("te envió", ignoreCase = true)) {
                        Log.d("YapeLogcat", "Yape detectado en logcat: $line")
                        processYapeLog(line)
                    }
                }
            } catch (e: Exception) {
                Log.e("YapeLogcat", "Error monitoreando logcat", e)
            }
        }
    }
    
    fun stopMonitoring() {
        isMonitoring = false
    }
    
    private suspend fun processYapeLog(logLine: String) {
        try {
            // Extraer información de Yape del log
            val yapePattern = Regex(".*Yape.*te envió.*pago.*S/([0-9,]+(?:\\.[0-9]{2})?).*")
            val match = yapePattern.find(logLine)
            
            if (match != null) {
                val amount = match.groupValues[1].replace(",", "").toDoubleOrNull() ?: 0.0
                
                // Crear transacción basada en el log
                val transaction = org.sysarp.project.data.YapeTransaction(
                    id = System.currentTimeMillis(),
                    transactionId = "YAPE_LOG_${System.currentTimeMillis()}",
                    amount = amount,
                    currency = "PEN",
                    senderName = "Usuario Yape",
                    senderPhone = null,
                    message = "Detectado desde logcat",
                    transactionType = org.sysarp.project.data.TransactionType.RECEIVED,
                    businessName = "Negocio Principal",
                    createdAt = kotlinx.datetime.Clock.System.now(),
                    processedAt = null,
                    isProcessed = false,
                    rawNotification = logLine,
                    securityCode = null
                )
                
                repository.insertTransaction(transaction)
                Log.d("YapeLogcat", "Transacción guardada desde logcat: $amount")
            }
        } catch (e: Exception) {
            Log.e("YapeLogcat", "Error procesando log de Yape", e)
        }
    }
}
