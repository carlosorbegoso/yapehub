package org.sysarp.project.utils

/**
 * Constantes de la aplicación
 * Evita valores mágicos y facilita el mantenimiento
 */
object Constants {

    // URLs
    const val BASE_URL = "167.172.117.133:8080"
    const val WEBSOCKET_URL = "wss://167.172.117.133:8080"

    // Permisos
    object Permissions {
        const val RECEIVE_YAPE_NOTIFICATIONS = "RECEIVE_YAPE_NOTIFICATIONS"
        const val SEND_PAYMENT_ALERTS = "SEND_PAYMENT_ALERTS"
        const val MANAGE_SELLERS = "MANAGE_SELLERS"
        const val VIEW_ANALYTICS = "VIEW_ANALYTICS"
    }
}

