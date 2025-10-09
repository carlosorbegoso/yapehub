package org.sysarp.project.service.notification

/**
 * Implementación Android de getCurrentTimeMillis usando System.currentTimeMillis()
 */
actual fun getCurrentTimeMillis(): Long {
    return System.currentTimeMillis()
}
