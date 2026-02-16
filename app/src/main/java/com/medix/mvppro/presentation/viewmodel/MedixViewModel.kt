package com.medix.mvppro.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medix.mvppro.core.logging.MetricsLogger
import com.medix.mvppro.data.datasource.FakeAppointmentDataSource
import com.medix.mvppro.data.repository.AppointmentRepositoryImpl
import com.medix.mvppro.domain.model.MedixIntent
import com.medix.mvppro.domain.usecase.CancelLastUseCase
import com.medix.mvppro.domain.usecase.ConfirmPendingUseCase
import com.medix.mvppro.domain.usecase.DetectIntentUseCase
import com.medix.mvppro.domain.usecase.GetAppointmentsUseCase
import com.medix.mvppro.domain.usecase.HandleVoiceCommandUseCase
import com.medix.mvppro.domain.usecase.ProposeNextUseCase
import com.medix.mvppro.domain.usecase.RescheduleUseCase
import com.medix.mvppro.presentation.state.MedixUiState
import com.medix.mvppro.presentation.state.MedixViewData
import com.medix.mvppro.voice.asr.SpeechRecognizerManager
import com.medix.mvppro.voice.model.AsrEvent
import com.medix.mvppro.voice.tts.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MedixViewModel(application: Application) : AndroidViewModel(application) {
    private val logger = MetricsLogger()
    private val repository = AppointmentRepositoryImpl(FakeAppointmentDataSource())
    private val getAppointmentsUseCase = GetAppointmentsUseCase(repository)
    private val handleVoiceCommandUseCase = HandleVoiceCommandUseCase(
        detectIntentUseCase = DetectIntentUseCase(),
        proposeNextUseCase = ProposeNextUseCase(repository),
        confirmPendingUseCase = ConfirmPendingUseCase(repository),
        cancelLastUseCase = CancelLastUseCase(repository),
        rescheduleUseCase = RescheduleUseCase(repository)
    )

    private val speechRecognizerManager = SpeechRecognizerManager(application.applicationContext)
    private val ttsManager = TtsManager(application.applicationContext)

    private val _viewData = MutableStateFlow(MedixViewData())
    val viewData: StateFlow<MedixViewData> = _viewData.asStateFlow()

    init {
        logger.sessionStarted()
        refreshAppointments()
        observeAsr()
    }

    fun onPermissionResult(granted: Boolean) {
        logger.permission(granted)
        if (!granted) {
            _viewData.value = _viewData.value.copy(
                uiState = MedixUiState.Error(
                    errorCode = -1,
                    message = "Se necesita permiso de micrófono.",
                    suggestion = "Habilítalo desde ajustes y presiona reintentar."
                )
            )
        }
        updateMetrics()
    }

    fun startListening() {
        logger.listeningStarted()
        _viewData.value = _viewData.value.copy(
            uiState = MedixUiState.Listening(message = "Habla cuando quieras"),
            recognizedText = ""
        )
        speechRecognizerManager.startListening()
        updateMetrics()
    }

    fun stopListening() {
        logger.listeningStopped()
        speechRecognizerManager.stopListening()
        _viewData.value = _viewData.value.copy(uiState = MedixUiState.Idle)
        updateMetrics()
    }

    fun retry() {
        _viewData.value = _viewData.value.copy(uiState = MedixUiState.Idle)
        startListening()
    }

    private fun observeAsr() {
        viewModelScope.launch {
            speechRecognizerManager.eventFlow.collect { event ->
                when (event) {
                    is AsrEvent.Ready -> {
                        _viewData.value = _viewData.value.copy(
                            uiState = MedixUiState.Listening(
                                partialText = _viewData.value.recognizedText,
                                message = event.message
                            )
                        )
                    }

                    is AsrEvent.Partial -> {
                        logger.asrPartialReceived()
                        _viewData.value = _viewData.value.copy(
                            recognizedText = event.text,
                            uiState = MedixUiState.Listening(partialText = event.text)
                        )
                    }

                    is AsrEvent.Final -> {
                        logger.asrFinalReceived(event.text)
                        processFinalText(event.text)
                    }

                    is AsrEvent.State -> {
                        if (!event.listening && _viewData.value.uiState is MedixUiState.Listening) {
                            _viewData.value = _viewData.value.copy(uiState = MedixUiState.Processing(_viewData.value.recognizedText))
                        }
                    }

                    is AsrEvent.Error -> {
                        logger.errorOccurred(event.asrError.type.name, event.asrError.code)
                        _viewData.value = _viewData.value.copy(
                            uiState = MedixUiState.Error(
                                errorCode = event.asrError.code,
                                message = event.asrError.userMessage,
                                suggestion = event.asrError.suggestion
                            )
                        )
                    }
                }
                updateMetrics()
            }
        }
    }

    private fun processFinalText(text: String) {
        _viewData.value = _viewData.value.copy(uiState = MedixUiState.Processing(text), recognizedText = text)
        val result = handleVoiceCommandUseCase(text)
        logger.intentDetected(result.intent.name)
        logger.actionExecuted(result.intent.name, result.actionSuccess && result.intent != MedixIntent.DESCONOCIDA)
        ttsManager.speak(result.responseText)
        logger.ttsSpoken(result.responseText.length)
        refreshAppointments()
        _viewData.value = _viewData.value.copy(
            uiState = MedixUiState.Idle,
            responseText = result.responseText
        )
        updateMetrics()
    }

    private fun refreshAppointments() {
        val snapshot = getAppointmentsUseCase()
        _viewData.value = _viewData.value.copy(
            availableAppointments = snapshot.available,
            bookedAppointments = snapshot.booked
        )
    }

    private fun updateMetrics() {
        _viewData.value = _viewData.value.copy(metricsSummary = logger.summary())
    }

    override fun onCleared() {
        speechRecognizerManager.destroy()
        ttsManager.shutdown()
        super.onCleared()
    }
}
