package com.medix.mvp.voice.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

class TtsManager(context: Context) {
    private var isInitialized = false
    private val tts: TextToSpeech = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            val co = tts.setLanguage(Locale("es", "CO"))
            if (co == TextToSpeech.LANG_MISSING_DATA || co == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.language = Locale("es", "ES")
            }
            isInitialized = true
        }
    }

    fun speak(text: String, onDone: () -> Unit) {
        if (!isInitialized) return
        val utteranceId = UUID.randomUUID().toString()
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) = onDone()
            override fun onError(utteranceId: String?) = onDone()
        })
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
