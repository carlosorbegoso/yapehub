package org.sysarp.project.utils

/**
 * Constantes de la aplicación
 * Evita valores mágicos y facilita el mantenimiento
 */
object Constants {
    
    // URLs
    const val BASE_URL = "http://10.0.2.2:8080"
    //const val BASE_URL = "https://ks9ql0l7-8080.brs.devtunnels.ms"
    const val WEBSOCKET_URL = "wss://ks9ql0l7-8080.brs.devtunnels.ms"
    
    // Endpoints
    object Endpoints {
        const val LOGIN = "/api/auth/login"
        const val REFRESH_TOKEN = "/api/auth/refresh"
        const val LOGOUT = "/api/auth/logout"
        const val PENDING_PAYMENTS = "/api/payments/pending"
        const val CLAIM_PAYMENT = "/api/payments/claim"
        const val WEBSOCKET_PAYMENTS = "/ws/payments"
    }
    
    // Roles
    object Roles {
        const val ADMIN = "ADMIN"
        const val SELLER = "SELLER"
        const val VENDOR = "VENDOR"
    }
    
    // Estados de pago
    object PaymentStatus {
        const val PENDING = "PENDING"
        const val CONFIRMED = "CONFIRMED"
        const val CANCELLED = "CANCELLED"
    }
    
    // Monedas
    object Currency {
        const val PEN = "PEN"
        const val USD = "USD"
    }
    
    // Timeouts
    object Timeouts {
        const val CONNECT_TIMEOUT_MS = 10000L
        const val READ_TIMEOUT_MS = 30000L
        const val WRITE_TIMEOUT_MS = 30000L
    }
    
    // Permisos
    object Permissions {
        const val RECEIVE_YAPE_NOTIFICATIONS = "RECEIVE_YAPE_NOTIFICATIONS"
        const val SEND_PAYMENT_ALERTS = "SEND_PAYMENT_ALERTS"
        const val MANAGE_SELLERS = "MANAGE_SELLERS"
        const val VIEW_ANALYTICS = "VIEW_ANALYTICS"
    }
}
