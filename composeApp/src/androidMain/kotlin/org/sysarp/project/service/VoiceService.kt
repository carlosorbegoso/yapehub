package org.sysarp.project.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

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
                    tts?.setLanguage(Locale.US)
                }
                
                // Configurar velocidad y tono
                tts?.setSpeechRate(0.8f) // Velocidad ligeramente más lenta
                tts?.setPitch(1.1f) // Tono ligeramente más alto
                
                isInitialized = true
            } else {
            }
        }
        
        // Listener para eventos de reproducción
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
            }
            
            override fun onDone(utteranceId: String?) {
            }
            
            override fun onError(utteranceId: String?) {
            }
        })
    }


    fun speakText(text: String, utteranceId: String = "custom") {
        if (!isInitialized) {
            initializeTTS()
            return
        }
        
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
        }
    }

}
