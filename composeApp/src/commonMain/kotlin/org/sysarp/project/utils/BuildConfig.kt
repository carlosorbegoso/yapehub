package org.sysarp.project.utils

/**
 * Configuración de build que permite cambiar fácilmente entre entornos
 */
object BuildConfig {
    
    /**
     * Tipo de entorno de la aplicación
     */
    enum class Environment {
        DEVELOPMENT,
        STAGING,
        PRODUCTION
    }
    
    /**
     * Configuración actual del entorno
     * Cambiar este valor para alternar entre entornos
     */
    val CURRENT_ENVIRONMENT = Environment.PRODUCTION
    
    /**
     * Configuraciones por entorno
     */
    private val environmentConfigs = mapOf(
        Environment.DEVELOPMENT to EnvironmentConfig(
            baseUrl = "https://ks9ql0l7-8080.brs.devtunnels.ms",
            websocketUrl = "wss://ks9ql0l7-8080.brs.devtunnels.ms",
            isDebug = true,
            enableLogging = true,
            timeoutSeconds = 60L,
            retryAttempts = 5
        ),
        Environment.STAGING to EnvironmentConfig(
            baseUrl = "http://167.172.117.133:8080",
            websocketUrl = "ws://167.172.117.133:8080",
            isDebug = true,
            enableLogging = true,
            timeoutSeconds = 45L,
            retryAttempts = 4
        ),
        Environment.PRODUCTION to EnvironmentConfig(
            baseUrl = "http://167.172.117.133:8080",
            websocketUrl = "ws://167.172.117.133:8080",
            isDebug = false,
            enableLogging = false,
            timeoutSeconds = 30L,
            retryAttempts = 3
        )
    )
    
    /**
     * Configuración actual basada en el entorno seleccionado
     */
    val config: EnvironmentConfig = environmentConfigs[CURRENT_ENVIRONMENT]!!
    
    /**
     * Propiedades de acceso rápido
     */
    val BASE_URL = config.baseUrl
    val WEBSOCKET_URL = config.websocketUrl
    val IS_DEBUG = config.isDebug
    val IS_PRODUCTION = CURRENT_ENVIRONMENT == Environment.PRODUCTION
    val ENVIRONMENT_NAME = CURRENT_ENVIRONMENT.name
}

/**
 * Configuración específica de un entorno
 */
data class EnvironmentConfig(
    val baseUrl: String,
    val websocketUrl: String,
    val isDebug: Boolean,
    val enableLogging: Boolean,
    val timeoutSeconds: Long,
    val retryAttempts: Int
)

/**
 * Utilidades para configuración
 */
object ConfigUtils {
    
    /**
     * Imprime la configuración actual (solo en debug)
     */
    fun printCurrentConfig() {
        if (BuildConfig.IS_DEBUG) {
            println("=== CONFIGURACIÓN ACTUAL ===")
            println("Entorno: ${BuildConfig.ENVIRONMENT_NAME}")
            println("Base URL: ${BuildConfig.BASE_URL}")
            println("WebSocket URL: ${BuildConfig.WEBSOCKET_URL}")
            println("Debug: ${BuildConfig.IS_DEBUG}")
            println("Timeout: ${BuildConfig.config.timeoutSeconds}s")
            println("Reintentos: ${BuildConfig.config.retryAttempts}")
            println("============================")
        }
    }
    
    /**
     * Valida que las URLs estén correctamente configuradas
     */
    fun validateConfiguration(): Boolean {
        val config = BuildConfig.config
        return config.baseUrl.isNotBlank() && 
               config.websocketUrl.isNotBlank() &&
               config.timeoutSeconds > 0 &&
               config.retryAttempts > 0
    }
}