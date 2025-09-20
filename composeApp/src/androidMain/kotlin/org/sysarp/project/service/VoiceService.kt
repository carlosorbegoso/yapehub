package org.sysarp.project.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import org.sysarp.project.utils.Logger
import java.util.*

/**
 * Servicio para generar sonidos de voz usando Text-to-Speech
 */
class VoiceService(private val context: Context) {
    
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    
    init {
        initializeTTS()
    }
    
    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Configurar idioma español
                val result = tts?.setLanguage(Locale("es", "ES"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Logger.auth("VOICE_SERVICE", "⚠️ Idioma español no soportado, usando inglés")
                    tts?.setLanguage(Locale.US)
                }
                
                // Configurar velocidad y tono
                tts?.setSpeechRate(0.8f) // Velocidad ligeramente más lenta
                tts?.setPitch(1.1f) // Tono ligeramente más alto
                
                isInitialized = true
                Logger.auth("VOICE_SERVICE", "✅ Text-to-Speech inicializado correctamente")
            } else {
                Logger.auth("VOICE_SERVICE", "❌ Error inicializando Text-to-Speech")
            }
        }
        
        // Listener para eventos de reproducción
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Logger.auth("VOICE_SERVICE", "🔊 Iniciando reproducción de voz: $utteranceId")
            }
            
            override fun onDone(utteranceId: String?) {
                Logger.auth("VOICE_SERVICE", "✅ Reproducción de voz completada: $utteranceId")
            }
            
            override fun onError(utteranceId: String?) {
                Logger.auth("VOICE_SERVICE", "❌ Error en reproducción de voz: $utteranceId")
            }
        })
    }
    
    /**
     * Reproduce "Pago Recibido" usando TTS
     */
    fun speakPaymentReceived() {
        speakText("Pago recibido", "payment_received")
    }
    
    /**
     * Reproduce "Nuevo Pago" usando TTS
     */
    fun speakNewPayment() {
        speakText("Nuevo pago", "new_payment")
    }
    
    /**
     * Reproduce "Dinero Recibido" usando TTS
     */
    fun speakMoneyReceived() {
        speakText("Dinero recibido", "money_received")
    }
    
    /**
     * Reproduce texto personalizado usando TTS
     */
    fun speakText(text: String, utteranceId: String = "custom") {
        if (!isInitialized) {
            Logger.auth("VOICE_SERVICE", "⚠️ TTS no inicializado, intentando inicializar...")
            initializeTTS()
            return
        }
        
        try {
            // Usar método moderno de TTS
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            Logger.auth("VOICE_SERVICE", "🔊 Reproduciendo: '$text'")
        } catch (e: Exception) {
            Logger.auth("VOICE_SERVICE", "❌ Error reproduciendo texto: ${e.message}")
        }
    }
    
    /**
     * Detiene la reproducción actual
     */
    fun stopSpeaking() {
        tts?.stop()
        Logger.auth("VOICE_SERVICE", "🔇 Deteniendo reproducción de voz")
    }
    
    /**
     * Libera recursos del TTS
     */
    fun release() {
        tts?.stop()
        tts?.shutdown()
        Logger.auth("VOICE_SERVICE", "🔇 TTS liberado")
    }
    
    /**
     * Verifica si TTS está disponible
     */
    fun isAvailable(): Boolean {
        return isInitialized && tts != null
    }
}
