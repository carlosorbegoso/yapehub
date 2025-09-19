package org.sysarp.project.service.notifications

import org.sysarp.project.utils.Logger

/**
 * Servicio para manejar notificaciones locales de pagos
 * Clase simplificada - funcionalidades específicas implementadas en SellerNotificationService
 */
class PaymentNotificationService {
    // Clase vacía - funcionalidades movidas a SellerNotificationService
    // Mantenida para compatibilidad con código existente
    
    /**
     * Constructor por defecto para evitar errores de NoClassDefFoundError
     */
    init {
        Logger.auth("PAYMENT_NOTIFICATION_SERVICE", "PaymentNotificationService inicializado")
    }
}

/**
 * Servicio extendido para notificaciones del vendedor
 */
class SellerNotificationService(
    private val authService: org.sysarp.project.service.auth.AuthService
) {
    
    private val notificationApiClient = org.sysarp.project.service.http.NotificationApiClient()
    
    /**
     * Obtener notificaciones del vendedor
     */
    suspend fun getSellerNotifications(
        page: Int = 0,
        size: Int = 20
    ): Result<org.sysarp.project.data.SellerNotificationsData> {
        return try {
            val token = authService.accessToken.value ?: throw Exception("Token no disponible")
            val userProfile = authService.userProfile.value ?: throw Exception("Perfil de usuario no disponible")
            val userId = userProfile.id ?: throw Exception("ID de usuario no disponible")
            
            Logger.auth("SELLER_NOTIFICATION_SERVICE", "Obteniendo notificaciones del vendedor")
            
            val result = notificationApiClient.getSellerNotifications(token, userId.toInt(), page, size)
            
            result.fold(
                onSuccess = { response ->
                    Logger.auth("SELLER_NOTIFICATION_SERVICE", "Notificaciones obtenidas exitosamente")
                    Result.success(response.data!!)
                },
                onFailure = { error ->
                    Logger.auth("SELLER_NOTIFICATION_SERVICE", "Error obteniendo notificaciones: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("SELLER_NOTIFICATION_SERVICE", "Error obteniendo notificaciones: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Marcar notificación como leída
     */
    suspend fun markNotificationAsRead(notificationId: Int): Result<org.sysarp.project.data.MarkNotificationReadData> {
        return try {
            val token = authService.accessToken.value ?: throw Exception("Token no disponible")
            
            Logger.auth("SELLER_NOTIFICATION_SERVICE", "Marcando notificación como leída: $notificationId")
            
            val result = notificationApiClient.markNotificationAsRead(notificationId, token)
            
            result.fold(
                onSuccess = { response ->
                    Logger.auth("SELLER_NOTIFICATION_SERVICE", "Notificación marcada como leída exitosamente")
                    Result.success(response.data!!)
                },
                onFailure = { error ->
                    Logger.auth("SELLER_NOTIFICATION_SERVICE", "Error marcando notificación como leída: ${error.message}")
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Logger.auth("SELLER_NOTIFICATION_SERVICE", "Error marcando notificación como leída: ${e.message}")
            Result.failure(e)
        }
    }
}
