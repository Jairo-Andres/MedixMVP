package com.example.medixmvp.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class VoiceInteractor(
    context: Context,
    private val defaultLocale: Locale = Locale("es", "ES")
) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isReady = false
    private var onSpeechCompleted: (() -> Unit)? = null

    override fun onInit(status: Int) {
        isReady = status == TextToSpeech.SUCCESS
        if (!isReady) return

        textToSpeech?.language = defaultLocale
        textToSpeech?.setSpeechRate(0.95f)
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit

            override fun onDone(utteranceId: String?) {
                onSpeechCompleted?.invoke()
            }

            override fun onError(utteranceId: String?) {
                onSpeechCompleted?.invoke()
            }
        })
    }

    fun setOnSpeechCompletedListener(listener: (() -> Unit)?) {
        onSpeechCompleted = listener
    }

    fun speak(text: String) {
        if (!isReady || text.isBlank()) return
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "medix_response")
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isReady = false
    }
}
