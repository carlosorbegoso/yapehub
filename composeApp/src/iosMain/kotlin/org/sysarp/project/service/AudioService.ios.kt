package org.sysarp.project.service

import org.sysarp.project.utils.Logger

/**
 * Implementación de AudioService para iOS
 * Por ahora es una implementación vacía ya que no estamos usando iOS
 */
actual class AudioService {
    
    actual fun playNotificationSound() {
        Logger.auth("AUDIO_SERVICE", "🔊 Reproduciendo sonido de notificación (iOS - no implementado)")
    }
    
    actual fun playPaymentReceivedSound() {
        Logger.auth("AUDIO_SERVICE", "🔊 Reproduciendo sonido de pago recibido (iOS - no implementado)")
    }
    
    actual fun playMusicalPayment() {
        Logger.auth("AUDIO_SERVICE", "🔊 Reproduciendo sonido musical (iOS - no implementado)")
    }
    
    actual fun playBellPayment() {
        Logger.auth("AUDIO_SERVICE", "🔊 Reproduciendo sonido de campana (iOS - no implementado)")
    }
    
    actual fun playCustomSound(soundType: NotificationSoundType) {
        Logger.auth("AUDIO_SERVICE", "🔊 Reproduciendo sonido personalizado: $soundType (iOS - no implementado)")
    }
    
    actual fun stopAllSounds() {
        Logger.auth("AUDIO_SERVICE", "🔇 Deteniendo sonidos (iOS - no implementado)")
    }
    
    actual fun setVolume(volume: Float) {
        Logger.auth("AUDIO_SERVICE", "🔊 Configurando volumen: $volume (iOS - no implementado)")
    }
}
