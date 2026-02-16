package com.medix.mvpmvp.voice.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

class TtsManager(
    context: Context,
    private val onDone: () -> Unit
) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech = TextToSpeech(context, this)
    private var ready = false

    val greetings = listOf(
        "Hola, soy Medix.",
        "Qué gusto saludarte, soy Medix.",
        "Hola, te acompaña Medix."
    )

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            tts.language = Locale("es", "CO")
            if (tts.isLanguageAvailable(Locale("es", "CO")) < 0) {
                tts.language = Locale("es", "ES")
            }
            tts.setSpeechRate(0.95f)
            tts.setPitch(1.0f)
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit
                override fun onError(utteranceId: String?) = onDone()
                override fun onDone(utteranceId: String?) = onDone()
            })
        }
    }

    fun speak(text: String) {
        if (!ready) return
        val utterance = UUID.randomUUID().toString()
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), utterance)
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
