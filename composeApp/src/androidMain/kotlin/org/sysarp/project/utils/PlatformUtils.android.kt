package org.sysarp.project.utils

import java.lang.System
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Implementación Android para utilidades de plataforma
 */

actual fun getCurrentTimeMillis(): Long = System.currentTimeMillis()

actual annotation class Volatile

actual fun <T> synchronized(lock: Any, block: () -> T): T {
    return block()
}


