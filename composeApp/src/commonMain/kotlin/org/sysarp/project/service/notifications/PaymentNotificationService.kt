package org.sysarp.project.service.notifications

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.sysarp.project.data.PaymentNotificationData
import org.sysarp.project.data.PaymentResultData
import org.sysarp.project.utils.Logger

/**
 * Servicio para manejar notificaciones locales de pagos
 */
class PaymentNotificationService {
    
    // Flujos de notificaciones
    private val _newPaymentNotification = MutableSharedFlow<PaymentNotificationData>()
    val newPaymentNotification: SharedFlow<PaymentNotificationData> = _newPaymentNotification.asSharedFlow()
    
    private val _paymentResultNotification = MutableSharedFlow<PaymentResultData>()
    val paymentResultNotification: SharedFlow<PaymentResultData> = _paymentResultNotification.asSharedFlow()
    
    // Estado de notificaciones
    private val _isNotificationEnabled = MutableStateFlow(true)
    val isNotificationEnabled: StateFlow<Boolean> = _isNotificationEnabled.asStateFlow()
    
    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()
    
    private val _isVibrationEnabled = MutableStateFlow(true)
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()
    
    /**
     * Procesa una nueva notificación de pago
     */
    suspend fun processNewPayment(notification: PaymentNotificationData) {
        Logger.auth("NOTIFICATION_SERVICE", "Procesando nueva notificación de pago: ${notification.paymentId}")
        
        // Emitir notificación
        _newPaymentNotification.emit(notification)
        
        // Reproducir sonido si está habilitado
        if (_isSoundEnabled.value) {
            playNotificationSound()
        }
        
        // Vibrar si está habilitado
        if (_isVibrationEnabled.value) {
            vibrate()
        }
        
        Logger.auth("NOTIFICATION_SERVICE", "Notificación procesada exitosamente")
    }
    
    /**
     * Procesa un resultado de pago
     */
    suspend fun processPaymentResult(result: PaymentResultData) {
        Logger.auth("NOTIFICATION_SERVICE", "Procesando resultado de pago: ${result.paymentId}")
        
        // Emitir notificación
        _paymentResultNotification.emit(result)
        
        Logger.auth("NOTIFICATION_SERVICE", "Resultado de pago procesado exitosamente")
    }
    
    /**
     * Reproduce sonido de notificación
     */
    private fun playNotificationSound() {
        try {
            // TODO: Implementar reproducción de sonido específica para la plataforma
            Logger.auth("NOTIFICATION_SERVICE", "Reproduciendo sonido de notificación")
            
            // Android: MediaPlayer o SoundPool
            // iOS: AVAudioPlayer
            
        } catch (e: Exception) {
            Logger.auth("NOTIFICATION_SERVICE", "Error reproduciendo sonido: ${e.message}")
        }
    }
    
    /**
     * Vibra el dispositivo
     */
    private fun vibrate() {
        try {
            // TODO: Implementar vibración específica para la plataforma
            Logger.auth("NOTIFICATION_SERVICE", "Vibrando dispositivo")
            
            // Android: Vibrator
            // iOS: UIImpactFeedbackGenerator
            
        } catch (e: Exception) {
            Logger.auth("NOTIFICATION_SERVICE", "Error vibrando: ${e.message}")
        }
    }
    
    /**
     * Habilita/deshabilita notificaciones
     */
    fun setNotificationEnabled(enabled: Boolean) {
        _isNotificationEnabled.value = enabled
        Logger.auth("NOTIFICATION_SERVICE", "Notificaciones ${if (enabled) "habilitadas" else "deshabilitadas"}")
    }
    
    /**
     * Habilita/deshabilita sonido
     */
    fun setSoundEnabled(enabled: Boolean) {
        _isSoundEnabled.value = enabled
        Logger.auth("NOTIFICATION_SERVICE", "Sonido ${if (enabled) "habilitado" else "deshabilitado"}")
    }
    
    /**
     * Habilita/deshabilita vibración
     */
    fun setVibrationEnabled(enabled: Boolean) {
        _isVibrationEnabled.value = enabled
        Logger.auth("NOTIFICATION_SERVICE", "Vibración ${if (enabled) "habilitada" else "deshabilitada"}")
    }
    
    /**
     * Obtiene configuración de notificaciones
     */
    fun getNotificationSettings(): NotificationSettings {
        return NotificationSettings(
            isNotificationEnabled = _isNotificationEnabled.value,
            isSoundEnabled = _isSoundEnabled.value,
            isVibrationEnabled = _isVibrationEnabled.value
        )
    }
}

/**
 * Configuración de notificaciones
 */
data class NotificationSettings(
    val isNotificationEnabled: Boolean,
    val isSoundEnabled: Boolean,
    val isVibrationEnabled: Boolean
)

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
            
            Logger.auth("SELLER_NOTIFICATION_SERVICE", "Obteniendo notificaciones del vendedor")
            
            val result = notificationApiClient.getSellerNotifications(token, page, size)
            
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
