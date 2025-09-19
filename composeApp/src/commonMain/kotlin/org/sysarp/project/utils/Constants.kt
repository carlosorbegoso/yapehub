package org.sysarp.project.utils

/**
 * Constantes de la aplicación
 * Evita valores mágicos y facilita el mantenimiento
 */
object Constants {
    
    // URLs
    const val BASE_URL = "https://ks9ql0l7-8080.brs.devtunnels.ms"
    const val WEBSOCKET_URL = "wss://ks9ql0l7-8080.brs.devtunnels.ms"

    // Permisos
    object Permissions {
        const val RECEIVE_YAPE_NOTIFICATIONS = "RECEIVE_YAPE_NOTIFICATIONS"
        const val SEND_PAYMENT_ALERTS = "SEND_PAYMENT_ALERTS"
        const val MANAGE_SELLERS = "MANAGE_SELLERS"
        const val VIEW_ANALYTICS = "VIEW_ANALYTICS"
    }
}
