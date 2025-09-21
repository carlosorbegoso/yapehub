package org.sysarp.project.service


/**
 * Implementación de AudioService para iOS
 * Por ahora es una implementación vacía ya que no estamos usando iOS
 */
actual class AudioService {
    
    actual fun playNotificationSound() {
    }
    
    actual fun playPaymentReceivedSound() {
    }
    
    actual fun playMusicalPayment() {
    }
    
    actual fun playBellPayment() {
    }
    
    actual fun playCustomSound(soundType: NotificationSoundType) {
    }
    
    actual fun stopAllSounds() {
    }
    
    actual fun setVolume(volume: Float) {
    }
}
