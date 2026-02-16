package com.medix.mvppro.voice.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val co = Locale("es", "CO")
                val es = Locale("es", "ES")
                val result = tts?.setLanguage(co) ?: TextToSpeech.LANG_NOT_SUPPORTED
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val fallback = tts?.setLanguage(es)
                    Log.w("TtsManager", "Fallback idioma a es-ES => $fallback")
                }
            }
        }
    }

    fun setProgressListener(
        onStartCallback: () -> Unit,
        onDoneCallback: () -> Unit,
        onErrorCallback: () -> Unit
    ) {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                onStartCallback()
            }

            override fun onDone(utteranceId: String?) {
                onDoneCallback()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onErrorCallback()
            }
        })
    }

    fun speak(text: String, flush: Boolean = true) {
        val queue = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(text, queue, null, "medix-${System.currentTimeMillis()}")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
