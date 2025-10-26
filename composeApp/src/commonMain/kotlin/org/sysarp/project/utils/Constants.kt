package org.sysarp.project.utils

/**
 * Constantes de la aplicación
 * Evita valores mágicos y facilita el mantenimiento
 */
object Constants {

    // URLs activas (se obtienen de BuildConfig)
    val BASE_URL = BuildConfig.BASE_URL
    val WEBSOCKET_URL = BuildConfig.WEBSOCKET_URL

    // Información del entorno actual
    val ENVIRONMENT = BuildConfig.ENVIRONMENT_NAME
    val IS_DEBUG = BuildConfig.IS_DEBUG
    val IS_PRODUCTION = BuildConfig.IS_PRODUCTION

    // Permisos
    object Permissions {
        const val RECEIVE_YAPE_NOTIFICATIONS = "RECEIVE_YAPE_NOTIFICATIONS"
        const val SEND_PAYMENT_ALERTS = "SEND_PAYMENT_ALERTS"
        const val MANAGE_SELLERS = "MANAGE_SELLERS"
        const val VIEW_ANALYTICS = "VIEW_ANALYTICS"
    }

    // Configuraciones específicas por entorno (delegadas a BuildConfig)
    object Config {
        val TIMEOUT_SECONDS = BuildConfig.config.timeoutSeconds
        val RETRY_ATTEMPTS = BuildConfig.config.retryAttempts
        val LOG_LEVEL = if (IS_PRODUCTION) "ERROR" else "DEBUG"
        val ENABLE_CRASH_REPORTING = IS_PRODUCTION
        val ENABLE_LOGGING = BuildConfig.config.enableLogging
    }

    // Inicialización y validación
    init {
        // Validar configuración al inicializar
        if (!ConfigUtils.validateConfiguration()) {
            throw IllegalStateException("Configuración inválida detectada")
        }
        
        // Imprimir configuración en modo debug
        ConfigUtils.printCurrentConfig()
    }
}
