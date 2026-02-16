package com.example.medixmvp.viewmodel

import android.app.Application
import android.speech.SpeechRecognizer
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.medixmvp.data.FakeAppointmentRepository
import com.example.medixmvp.data.model.Appointment
import com.example.medixmvp.logic.MedixIntent
import com.example.medixmvp.logic.detectIntent
import com.example.medixmvp.voice.SpeechRecognizerManager
import com.example.medixmvp.voice.TtsSpeaker

class MedixViewModel(application: Application) : AndroidViewModel(application),
    SpeechRecognizerManager.Listener {

    private val repository = FakeAppointmentRepository()
    private val ttsSpeaker = TtsSpeaker(application)
    private val speechManager = SpeechRecognizerManager(application, this)

    val recognizedText: MutableState<String> = mutableStateOf("")
    val responseText: MutableState<String> = mutableStateOf(
        "Bienvenido a Medix MVP. Presione escuchar para gestionar su cita."
    )
    val isListening: MutableState<Boolean> = mutableStateOf(false)
    val availableAppointments: MutableState<List<Appointment>> = mutableStateOf(emptyList())
    val bookedAppointments: MutableState<List<Appointment>> = mutableStateOf(emptyList())

    init {
        refreshAppointments()
        ttsSpeaker.speak(responseText.value)
    }

    fun startListening() {
        isListening.value = true
        speechManager.startListening()
    }

    fun stopListening() {
        speechManager.stopListening()
        isListening.value = false
    }

    fun processText(text: String) {
        recognizedText.value = text
        val intent = detectIntent(text)
        handleIntent(intent)
    }

    fun handleIntent(intent: MedixIntent) {
        val reply = when (intent) {
            MedixIntent.AGENDAR -> {
                val pending = repository.proposeNextAvailable()
                if (pending == null) {
                    "No tengo horarios disponibles para agendar en este momento."
                } else {
                    "Le propongo el ${pending.fecha} a las ${pending.hora}. Diga confirmar para agendar."
                }
            }

            MedixIntent.CONFIRMAR -> {
                val confirmed = repository.confirmPending()
                if (confirmed == null) {
                    "No hay una cita pendiente para confirmar."
                } else {
                    "Cita confirmada para el ${confirmed.fecha} a las ${confirmed.hora}."
                }
            }

            MedixIntent.CANCELAR -> {
                val canceled = repository.cancelLast()
                if (canceled == null) {
                    "No hay citas agendadas para cancelar."
                } else {
                    "Su cita del ${canceled.fecha} a las ${canceled.hora} fue cancelada."
                }
            }

            MedixIntent.REPROGRAMAR -> {
                val pending = repository.reschedule()
                if (pending == null) {
                    "No pude reprogramar porque no hay citas agendadas o no hay horarios disponibles."
                } else {
                    "Reprogramación sugerida para el ${pending.fecha} a las ${pending.hora}. Diga confirmar para aceptar."
                }
            }

            MedixIntent.DESCONOCIDA -> {
                "No entendí su solicitud. Puede decir: agendar, confirmar, cancelar o reprogramar."
            }
        }

        responseText.value = reply
        refreshAppointments()
        ttsSpeaker.speak(reply)
    }

    private fun refreshAppointments() {
        availableAppointments.value = repository.getAvailable()
        bookedAppointments.value = repository.getBooked()
    }

    override fun onReadyForSpeech() {
        responseText.value = "Le escucho, puede hablar ahora."
        ttsSpeaker.speak(responseText.value)
    }

    override fun onBeginningOfSpeech() {
        responseText.value = "Procesando su solicitud..."
    }

    override fun onResults(text: String) {
        isListening.value = false
        if (text.isBlank()) {
            responseText.value = "No pude escuchar una frase válida."
            ttsSpeaker.speak(responseText.value)
        } else {
            processText(text)
        }
    }

    override fun onError(error: Int) {
        isListening.value = false
        val message = when (error) {
            SpeechRecognizer.ERROR_NETWORK -> "Hay un problema de red. Intente nuevamente."
            SpeechRecognizer.ERROR_NO_MATCH -> "No encontré coincidencias en su voz."
            SpeechRecognizer.ERROR_CLIENT -> "Hubo un problema del cliente de voz."
            else -> "Ocurrió un error de reconocimiento: $error"
        }
        responseText.value = message
        ttsSpeaker.speak(message)
    }

    override fun onEndOfSpeech() {
        isListening.value = false
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.release()
        ttsSpeaker.release()
    }
}
