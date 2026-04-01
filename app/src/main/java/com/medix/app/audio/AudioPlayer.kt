package com.medix.app.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class AudioPlayer(context: Context) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech = TextToSpeech(context, this)
    private var isReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale("es", "ES")
            textToSpeech.setSpeechRate(0.95f)
            isReady = true
        }
    }

    fun speak(text: String) {
        if (!isReady) return
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "MEDIX_TTS")
    }

    fun release() {
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}
