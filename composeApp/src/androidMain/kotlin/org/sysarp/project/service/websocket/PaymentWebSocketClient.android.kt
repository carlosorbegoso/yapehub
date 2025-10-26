package org.sysarp.project.service.websocket

/**
 * Implementación Android de getCurrentTimeMillis usando System.currentTimeMillis()
 */
actual fun getCurrentTimeMillis(): Long {
    return System.currentTimeMillis()
}
