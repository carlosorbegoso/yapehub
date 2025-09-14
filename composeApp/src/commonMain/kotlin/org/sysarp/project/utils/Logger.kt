package org.sysarp.project.utils

/**
 * Utilidad centralizada para logging
 * Elimina duplicación de patrones de logging
 */
object Logger {
    
    // Emojis para diferentes tipos de logs
    private const val INFO = "ℹ️"
    private const val SUCCESS = "✅"
    private const val ERROR = "❌"
    private const val WARNING = "⚠️"
    private const val DEBUG = "🔍"
    private const val CONNECTION = "🔌"
    private const val PAYMENT = "💰"
    private const val NOTIFICATION = "📨"
    private const val API = "📤"
    private const val RESPONSE = "📥"
    private const val WEBSOCKET = "🔌"
    private const val AUTH = "🔐"
    private const val DATA = "📋"
    
    /**
     * Log de información general
     */
    fun info(service: String, message: String) {
        println("$INFO [$service] $message")
    }
    
    /**
     * Log de éxito
     */
    fun success(service: String, message: String) {
        println("$SUCCESS [$service] $message")
    }
    
    /**
     * Log de error
     */
    fun error(service: String, message: String) {
        println("$ERROR [$service] $message")
    }
    
    /**
     * Log de advertencia
     */
    fun warning(service: String, message: String) {
        println("$WARNING [$service] $message")
    }
    
    /**
     * Log de debug
     */
    fun debug(service: String, message: String) {
        println("$DEBUG [$service] $message")
    }
    
    /**
     * Log de conexión
     */
    fun connection(service: String, message: String) {
        println("$CONNECTION [$service] $message")
    }
    
    /**
     * Log de pago
     */
    fun payment(service: String, message: String) {
        println("$PAYMENT [$service] $message")
    }
    
    /**
     * Log de notificación
     */
    fun notification(service: String, message: String) {
        println("$NOTIFICATION [$service] $message")
    }
    
    /**
     * Log de API request
     */
    fun apiRequest(service: String, message: String) {
        println("$API [$service] $message")
    }
    
    /**
     * Log de API response
     */
    fun apiResponse(service: String, message: String) {
        println("$RESPONSE [$service] $message")
    }
    
    /**
     * Log de WebSocket
     */
    fun websocket(service: String, message: String) {
        println("$WEBSOCKET [$service] $message")
    }
    
    /**
     * Log de autenticación
     */
    fun auth(service: String, message: String) {
        println("$AUTH [$service] $message")
    }
    
    /**
     * Log de datos
     */
    fun data(service: String, message: String) {
        println("$DATA [$service] $message")
    }
    
    /**
     * Log con separador visual
     */
    fun section(service: String, title: String) {
        println("$INFO [$service] ===== $title =====")
    }
    
    /**
     * Log de error con separador
     */
    fun errorSection(service: String, title: String) {
        println("$ERROR [$service] ===== $title =====")
    }
    
    /**
     * Log de éxito con separador
     */
    fun successSection(service: String, title: String) {
        println("$SUCCESS [$service] ===== $title =====")
    }
}
