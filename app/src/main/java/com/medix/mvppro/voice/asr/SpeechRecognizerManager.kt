package com.medix.mvppro.voice.asr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.medix.mvppro.voice.model.AsrError
import com.medix.mvppro.voice.model.AsrErrorType
import com.medix.mvppro.voice.model.AsrEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.random.Random

class SpeechRecognizerManager(private val context: Context) {
    private val events = MutableSharedFlow<AsrEvent>(extraBufferCapacity = 64)
    val eventFlow = events.asSharedFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var hasRetried = false

    init {
        createRecognizer("es-CO")
    }

    private fun createRecognizer(languageTag: String) {
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    events.tryEmit(AsrEvent.Ready("Escuchando..."))
                    events.tryEmit(AsrEvent.State(true))
                }

                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() {
                    events.tryEmit(AsrEvent.State(false))
                }

                override fun onError(error: Int) {
                    val mapped = mapError(error)
                    events.tryEmit(AsrEvent.Error(mapped))
                    if (!hasRetried && (mapped.type == AsrErrorType.TIMEOUT || mapped.type == AsrErrorType.NO_MATCH)) {
                        hasRetried = true
                        val backoff = Random.nextLong(400, 601)
                        mainHandler.postDelayed({ startListening() }, backoff)
                    }
                    if (mapped.type == AsrErrorType.SERVER && languageTag == "es-CO") {
                        createRecognizer("es-ES")
                    }
                }

                override fun onResults(results: Bundle?) {
                    hasRetried = false
                    val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                    events.tryEmit(AsrEvent.Final(text))
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                    events.tryEmit(AsrEvent.Partial(text))
                }

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
        }
        startListening(languageTag)
        stopListening()
    }

    fun startListening(languageTag: String = "es-CO") {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun mapError(code: Int): AsrError {
        return when (code) {
            SpeechRecognizer.ERROR_NO_MATCH -> AsrError(code, AsrErrorType.NO_MATCH, "No entendí lo que dijiste.", "Intenta hablar más claro y cerca del micrófono.")
            SpeechRecognizer.ERROR_NETWORK -> AsrError(code, AsrErrorType.NETWORK, "No hay conexión para el reconocimiento.", "Conéctate a internet o intenta nuevamente.")
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> AsrError(code, AsrErrorType.PERMISSION, "No tengo permiso para usar el micrófono.", "Habilita el permiso de micrófono en ajustes.")
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> AsrError(code, AsrErrorType.BUSY, "El micrófono está ocupado.", "Cierra otras apps de voz y reintenta.")
            SpeechRecognizer.ERROR_CLIENT -> AsrError(code, AsrErrorType.CLIENT, "Error del sistema de voz.", "Reinicia la app.")
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> AsrError(code, AsrErrorType.TIMEOUT, "Se agotó el tiempo de escucha.", "Habla justo después de presionar escuchar.")
            SpeechRecognizer.ERROR_SERVER -> AsrError(code, AsrErrorType.SERVER, "El servicio de voz no responde.", "Intenta de nuevo en unos segundos.")
            else -> AsrError(code, AsrErrorType.UNKNOWN, "Ocurrió un error inesperado.", "Intenta nuevamente.")
        }
    }
}
