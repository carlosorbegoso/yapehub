package org.sysarp.project.utils

/**
 * Utilidades multiplataforma para funcionalidades específicas de plataforma
 */

/**
 * Obtiene el tiempo actual en milisegundos
 */
expect fun getCurrentTimeMillis(): Long

/**
 * Anotación para campos volátiles (thread-safe)
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
expect annotation class Volatile()

/**
 * Función para sincronización thread-safe
 */
expect fun <T> synchronized(lock: Any, block: () -> T): T


