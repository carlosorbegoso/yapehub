package org.sysarp.project.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Implementación de AudioService para Android
 */
actual class AudioService {
    
    private var context: Context? = null
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    private var volume: Float = 0.7f
    private var voiceService: VoiceService? = null
    
    /**
     * Inicializa el servicio de audio con el contexto de Android
     */
    fun initialize(context: Context) {
        this.context = context
        this.vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        this.audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        this.voiceService = VoiceService(context)
    }
    
    actual fun playNotificationSound() {
        playCustomSound(NotificationSoundType.GENERAL)
    }
    
    actual fun playPaymentReceivedSound() {
        playCustomSound(NotificationSoundType.PAYMENT_RECEIVED)
    }
    
    actual fun playMusicalPayment() {
        playCustomSound(NotificationSoundType.MUSICAL_PAYMENT)
    }
    
    actual fun playBellPayment() {
        playCustomSound(NotificationSoundType.BELL_PAYMENT)
    }
    
    /**
     * Reproduce voz usando TTS (opcional, más claro que síntesis)
     */
    fun playVoiceWithTTS(text: String) {
        voiceService?.speakText(text)
    }
    
    actual fun playCustomSound(soundType: NotificationSoundType) {
        try {
            
            // Detener sonido anterior si está reproduciéndose
            stopAllSounds()
            
            // Crear nuevo MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                // Configurar atributos de audio
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                
                // Configurar volumen
                setVolume(volume, volume)
                
                // Cargar el archivo de sonido según el tipo
                val soundResource = when (soundType) {
                    NotificationSoundType.GENERAL -> "notification_general"
                    NotificationSoundType.PAYMENT_RECEIVED -> "payment_received"
                    NotificationSoundType.MUSICAL_PAYMENT -> "musical_payment_received"
                    NotificationSoundType.BELL_PAYMENT -> "bell_payment_received"
                    NotificationSoundType.YAPE_REALISTIC -> "yape_realistic"
                    NotificationSoundType.ERROR -> "notification_short"
                }
                
                // Obtener el ID del recurso
                val resourceId = context?.resources?.getIdentifier(
                    soundResource, 
                    "raw", 
                    context?.packageName
                ) ?: 0
                
                if (resourceId != 0) {
                    setDataSource(context?.resources?.openRawResourceFd(resourceId)?.fileDescriptor)
                    prepare()
                    start()
                    
                    // Vibrar si está habilitado
                    vibrateIfEnabled()
                    
                } else {
                }
                
                // Liberar recursos cuando termine la reproducción
                setOnCompletionListener {
                    release()
                    mediaPlayer = null
                }
                
                // Manejar errores
                setOnErrorListener { _, what, extra ->
                    release()
                    mediaPlayer = null
                    true
                }
            }
            
        } catch (e: Exception) {
        }
    }
    
    actual fun stopAllSounds() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
        }
    }
    
    actual fun setVolume(volume: Float) {
        this.volume = volume.coerceIn(0.0f, 1.0f)
    }
    
    /**
     * Vibra el dispositivo si está habilitado
     */
    private fun vibrateIfEnabled() {
        try {
            vibrator?.let { vib ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val vibrationEffect = VibrationEffect.createOneShot(
                        200, // 200ms
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                    vib.vibrate(vibrationEffect)
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(200)
                }
            }
        } catch (e: Exception) {
        }
    }
}

/**
 * Composable para obtener el AudioService con contexto
 */
@Composable
fun rememberAudioService(): AudioService {
    val context = LocalContext.current
    val audioService = remember { AudioService() }
    
    LaunchedEffect(context) {
        audioService.initialize(context)
    }
    
    return audioService
}
