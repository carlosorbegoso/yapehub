package org.sysarp.project.service

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.format

object DebugLogger {
    private val _logs = mutableListOf<LogEntry>()
    private val maxLogs = 1000 // Aumentar límite de logs para mejor debugging
    
    data class LogEntry(
        val timestamp: String,
        val level: String,
        val message: String
    )
    
    fun getLogs(): List<LogEntry> = _logs.toList()
    
    fun clearLogs() {
        _logs.clear()
    }
    
    fun log(level: String, message: String) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val timestamp = "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}:${now.second.toString().padStart(2, '0')}.${now.nanosecond / 1000000}"
        
        _logs.add(LogEntry(timestamp, level, message))
        
        // Mantener solo los últimos maxLogs
        if (_logs.size > maxLogs) {
            _logs.removeAt(0)
        }
        
        // También loggear al sistema (implementado en cada plataforma)
        // logToSystem(level, message) // Temporalmente deshabilitado
    }
    
    fun error(message: String) = log("ERROR", message)
    fun warn(message: String) = log("WARN", message)
    fun info(message: String) = log("INFO", message)
    fun debug(message: String) = log("DEBUG", message)
    
    /**
     * Exporta todos los logs como texto formateado
     */
    fun exportLogsAsText(): String {
        val header = """
            ========================================
            YAPE CHAMO - DEBUG LOGS
            ========================================
            Generado: ${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())}
            Total de logs: ${_logs.size}
            ========================================
            
        """.trimIndent()
        
        val logEntries = _logs.joinToString("\n") { log ->
            "[${log.timestamp}] [${log.level}] ${log.message}"
        }
        
        return header + "\n\n" + logEntries
    }
    
    /**
     * Obtiene estadísticas de los logs
     */
    fun getLogStats(): LogStats {
        val totalLogs = _logs.size
        val errorCount = _logs.count { it.level == "ERROR" }
        val warnCount = _logs.count { it.level == "WARN" }
        val infoCount = _logs.count { it.level == "INFO" }
        val debugCount = _logs.count { it.level == "DEBUG" }
        
        return LogStats(
            totalLogs = totalLogs,
            errorCount = errorCount,
            warnCount = warnCount,
            infoCount = infoCount,
            debugCount = debugCount
        )
    }
    
    data class LogStats(
        val totalLogs: Int,
        val errorCount: Int,
        val warnCount: Int,
        val infoCount: Int,
        val debugCount: Int
    )
}

// Función expect para loggear al sistema (implementada en cada plataforma)
// expect fun logToSystem(level: String, message: String)
