package org.sysarp.project.utils

import kotlinx.datetime.Clock

/**
 * Implementación iOS para utilidades de plataforma
 */

actual fun getCurrentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

actual annotation class Volatile

actual fun <T> synchronized(lock: Any, block: () -> T): T {
    return block()
}


