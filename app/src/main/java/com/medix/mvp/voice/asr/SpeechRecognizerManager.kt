package com.medix.mvp.voice.asr

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.medix.mvp.voice.model.AsrError

class SpeechRecognizerManager(
    context: Context,
    private val onPartial: (String) -> Unit,
    private val onFinal: (String) -> Unit,
    private val onAsrError: (AsrError) -> Unit,
) : RecognitionListener {

    private val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)

    init {
        recognizer.setRecognitionListener(this)
    }

    fun startListening(withBeep: Boolean = true) {
        if (withBeep) toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-CO")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        recognizer.startListening(intent)
    }

    fun stopListening() = recognizer.stopListening()

    fun restartListening() {
        recognizer.cancel()
        startListening()
    }

    fun destroy() {
        recognizer.destroy()
        toneGenerator.release()
    }

    override fun onResults(results: Bundle?) {
        val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
        if (text.isNotBlank()) onFinal(text)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
        if (text.isNotBlank()) onPartial(text)
    }

    override fun onError(error: Int) {
        val mapped = when (error) {
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> AsrError.Network
            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> AsrError.NoMatch
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> AsrError.Permission
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> AsrError.Busy
            else -> AsrError.Unknown(error)
        }
        onAsrError(mapped)
    }

    override fun onReadyForSpeech(params: Bundle?) = Unit
    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onEndOfSpeech() = Unit
    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}
