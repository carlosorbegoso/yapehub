package org.sysarp.project.service

/**
 * Implementación específica de Android para loggear al sistema
 * Ahora usa Timber para mejor logging
 */
actual fun logToSystem(level: String, message: String) {
    when (level) {
        "ERROR" -> TimberLogger.error(message)
        "WARN" -> TimberLogger.w(message)
        "INFO" -> TimberLogger.i(message)
        "DEBUG" -> TimberLogger.d(message)
        else -> TimberLogger.d(message)
    }
}
