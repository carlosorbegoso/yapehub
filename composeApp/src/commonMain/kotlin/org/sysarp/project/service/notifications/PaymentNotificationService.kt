package org.sysarp.project.service.notifications
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
            
            
            val result = notificationApiClient.getSellerNotifications(token, userId.toInt(), page, size)
            
            result.fold(
                onSuccess = { response ->
                    Result.success(response.data!!)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Marcar notificación como leída
     */
    suspend fun markNotificationAsRead(notificationId: Int): Result<org.sysarp.project.data.MarkNotificationReadData> {
        return try {
            val token = authService.accessToken.value ?: throw Exception("Token no disponible")
            
            
            val result = notificationApiClient.markNotificationAsRead(notificationId, token)
            
            result.fold(
                onSuccess = { response ->
                    Result.success(response.data!!)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
