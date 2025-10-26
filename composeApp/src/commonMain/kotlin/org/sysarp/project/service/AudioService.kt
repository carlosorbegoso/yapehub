package org.sysarp.project.service
/**
 * Servicio para manejar la reproducción de sonidos de notificación
 * Implementación multiplataforma usando expect/actual
 */
expect class AudioService {
    /**
     * Reproduce un sonido de notificación general
     */
    fun playNotificationSound()
    
    /**
     * Reproduce un sonido específico para pagos recibidos
     */
    fun playPaymentReceivedSound()
    
    /**
     * Reproduce un sonido musical profesional (Do-Mi-Sol)
     */
    fun playMusicalPayment()
    
    /**
     * Reproduce un sonido de campana elegante
     */
    fun playBellPayment()
    
    /**
     * Reproduce un sonido personalizado
     * @param soundType Tipo de sonido a reproducir
     */
    fun playCustomSound(soundType: NotificationSoundType)
    
    /**
     * Detiene todos los sonidos en reproducción
     */
    fun stopAllSounds()
    
    /**
     * Configura el volumen de los sonidos
     * @param volume Volumen entre 0.0 y 1.0
     */
    fun setVolume(volume: Float)
}

/**
 * Tipos de sonidos de notificación disponibles
 */
enum class NotificationSoundType {
    GENERAL,           // Notificación general
    PAYMENT_RECEIVED,  // Pago recibido (melodía)
    MUSICAL_PAYMENT,   // Musical: Pago recibido (Do-Mi-Sol)
    BELL_PAYMENT,      // Campana: Pago recibido
    YAPE_REALISTIC,    // Yape realista (melodía ascendente)
    ERROR              // Error
}

/**
 * Configuración de audio para notificaciones
 */
data class AudioConfig(
    val enabled: Boolean = true,
    val volume: Float = 0.7f,
    val vibrationEnabled: Boolean = true,
    val soundType: NotificationSoundType = NotificationSoundType.GENERAL
)
