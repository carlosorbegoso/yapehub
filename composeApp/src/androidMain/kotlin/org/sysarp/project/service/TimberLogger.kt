package org.sysarp.project.service

import timber.log.Timber

/**
 * Logger mejorado usando Timber para YapeHub
 * Proporciona logging estructurado y mejor debugging
 */
object TimberLogger {
    
    private var isInitialized = false
    
    fun initialize() {
        if (!isInitialized) {
            Timber.plant(Timber.DebugTree())
            isInitialized = true
            Timber.d("🌲 Timber Logger inicializado para YapeHub")
        }
    }
    
    fun d(message: String, vararg args: Any?) {
        Timber.d(message, *args)
    }
    
    fun i(message: String, vararg args: Any?) {
        Timber.i(message, *args)
    }
    
    fun w(message: String, vararg args: Any?) {
        Timber.w(message, *args)
    }
    
    fun e(message: String, vararg args: Any?) {
        Timber.e(message, *args)
    }
    
    fun e(throwable: Throwable?, message: String, vararg args: Any?) {
        Timber.e(throwable, message, *args)
    }
    
    // Métodos específicos para YapeHub
    fun transaction(message: String, vararg args: Any?) {
        Timber.tag("YapeHub-Transaction").d(message, *args)
    }
    
    fun notification(message: String, vararg args: Any?) {
        Timber.tag("YapeHub-Notification").d(message, *args)
    }
    
    fun database(message: String, vararg args: Any?) {
        Timber.tag("YapeHub-Database").d(message, *args)
    }
    
    fun permission(message: String, vararg args: Any?) {
        Timber.tag("YapeHub-Permission").d(message, *args)
    }
    
    fun error(message: String, vararg args: Any?) {
        Timber.tag("YapeHub-Error").e(message, *args)
    }
    
    fun error(throwable: Throwable?, message: String, vararg args: Any?) {
        Timber.tag("YapeHub-Error").e(throwable, message, *args)
    }
}
