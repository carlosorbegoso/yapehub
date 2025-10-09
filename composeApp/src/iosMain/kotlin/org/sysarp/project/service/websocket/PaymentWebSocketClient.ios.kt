package org.sysarp.project.service.websocket

import platform.Foundation.NSDate

/**
 * Implementación iOS de getCurrentTimeMillis usando NSDate
 */
actual fun getCurrentTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}
