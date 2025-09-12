package org.sysarp.project.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Servicio para exportar logs a archivos
 */
object LogExportService {
    
    /**
     * Exporta los logs a un archivo y devuelve el URI para compartir
     */
    suspend fun exportLogsToFile(context: Context, logsText: String): Uri? = withContext(Dispatchers.IO) {
        try {
            DebugLogger.info("📁 Iniciando exportación de logs...")
            
            // Crear directorio de logs si no existe
            val logsDir = File(context.getExternalFilesDir(null), "logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
                DebugLogger.info("📁 Directorio de logs creado: ${logsDir.absolutePath}")
            }
            
            // Generar nombre de archivo con timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "yapechamo_logs_$timestamp.txt"
            val logFile = File(logsDir, fileName)
            
            // Escribir logs al archivo
            FileWriter(logFile).use { writer ->
                writer.write(logsText)
            }
            
            DebugLogger.info("✅ Logs exportados exitosamente: ${logFile.absolutePath}")
            DebugLogger.info("📊 Tamaño del archivo: ${logFile.length()} bytes")
            
            // Crear URI para compartir usando FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                logFile
            )
            
            DebugLogger.info("🔗 URI generado para compartir: $uri")
            uri
            
        } catch (e: Exception) {
            DebugLogger.error("❌ Error exportando logs: ${e.message}")
            DebugLogger.error("📊 Stack trace: ${e.stackTrace.joinToString("\n")}")
            null
        }
    }
    
    /**
     * Comparte los logs usando un Intent
     */
    fun shareLogs(context: Context, logsText: String) {
        try {
            DebugLogger.info("📤 Iniciando compartir logs...")
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, logsText)
                    putExtra(Intent.EXTRA_SUBJECT, "YapeHub Debug Logs")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooser = Intent.createChooser(intent, "Compartir logs de YapeHub")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            
            DebugLogger.info("✅ Intent de compartir iniciado")
            
        } catch (e: Exception) {
            DebugLogger.error("❌ Error compartiendo logs: ${e.message}")
        }
    }
    
    /**
     * Comparte los logs como archivo
     */
    suspend fun shareLogsAsFile(context: Context, logsText: String) = withContext(Dispatchers.IO) {
        try {
            DebugLogger.info("📤 Iniciando compartir logs como archivo...")
            
            val uri = exportLogsToFile(context, logsText)
            if (uri != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "YapeHub Debug Logs")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val chooser = Intent.createChooser(intent, "Compartir logs de YapeHub")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                
                DebugLogger.info("✅ Logs compartidos como archivo")
            } else {
                DebugLogger.warn("⚠️ No se pudo crear el archivo de logs, compartiendo como texto")
                shareLogs(context, logsText)
            }
            
        } catch (e: Exception) {
            DebugLogger.error("❌ Error compartiendo logs como archivo: ${e.message}")
        }
    }

    /**
     * Comparte contenido como archivo con nombre personalizado
     */
    suspend fun shareLogsAsFile(context: Context, content: String, fileName: String) = withContext(Dispatchers.IO) {
        try {
            DebugLogger.info("📤 Iniciando compartir archivo personalizado: $fileName")
            
            // Crear directorio de logs si no existe
            val logsDir = File(context.getExternalFilesDir(null), "logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
                DebugLogger.info("📁 Directorio de logs creado: ${logsDir.absolutePath}")
            }
            
            // Crear archivo con nombre personalizado
            val customFile = File(logsDir, fileName)

            // Escribir contenido al archivo
            FileWriter(customFile).use { writer ->
                writer.write(content)
            }
            
            DebugLogger.info("✅ Archivo creado exitosamente: ${customFile.absolutePath}")
            DebugLogger.info("📊 Tamaño del archivo: ${customFile.length()} bytes")

            // Crear URI para compartir usando FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                customFile
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "YapeHub Database Export")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooser = Intent.createChooser(intent, "Compartir archivo de YapeHub")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            
            DebugLogger.info("✅ Archivo personalizado compartido exitosamente")
            
        } catch (e: Exception) {
            DebugLogger.error("❌ Error compartiendo archivo personalizado: ${e.message}")
        }
    }
}
