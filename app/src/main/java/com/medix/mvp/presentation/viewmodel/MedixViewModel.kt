package com.medix.mvp.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medix.mvp.core.logging.MetricsLogger
import com.medix.mvp.core.time.DefaultTimeProvider
import com.medix.mvp.data.datasource.FakeAppointmentDataSource
import com.medix.mvp.data.repository.LocalAppointmentRepository
import com.medix.mvp.domain.model.Appointment
import com.medix.mvp.domain.usecase.ConversationEngine
import com.medix.mvp.domain.usecase.DetectIntentUseCase
import com.medix.mvp.domain.usecase.ParseEntitiesUseCase
import com.medix.mvp.presentation.state.ConversationState
import com.medix.mvp.presentation.state.MedixUiState
import com.medix.mvp.voice.asr.SpeechRecognizerManager
import com.medix.mvp.voice.tts.TtsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MedixViewModel(application: Application) : AndroidViewModel(application) {
    private val timeProvider = DefaultTimeProvider()
    private val repository = LocalAppointmentRepository(
        prefs = application.getSharedPreferences("medix_prefs", 0),
        dataSource = FakeAppointmentDataSource(timeProvider),
    )
    private val engine = ConversationEngine(DetectIntentUseCase(), ParseEntitiesUseCase(timeProvider), repository)
    private val ttsManager = TtsManager(application)
    private val metricsLogger = MetricsLogger(timeProvider)

    private lateinit var asrManager: SpeechRecognizerManager

    private val _uiState = MutableStateFlow<MedixUiState>(MedixUiState.Idle)
    val uiState: StateFlow<MedixUiState> = _uiState.asStateFlow()

    private val _conversationState = MutableStateFlow<ConversationState>(ConversationState.Welcome)
    val conversationState: StateFlow<ConversationState> = _conversationState.asStateFlow()

    private val _assistantText = MutableStateFlow("")
    val assistantText: StateFlow<String> = _assistantText.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    val metrics = metricsLogger.snapshot

    private var hasMicPermission = false

    fun attachAsr() {
        asrManager = SpeechRecognizerManager(
            context = getApplication(),
            onPartial = { _uiState.value = MedixUiState.Listening(it) },
            onFinal = { onUserFinalInput(it) },
            onAsrError = {
                metricsLogger.incrementAsrError()
                _uiState.value = MedixUiState.Error(it.message, "Toca Hablar para intentar otra vez")
            },
        )
    }

    fun onPermissionResult(granted: Boolean) {
        hasMicPermission = granted
        if (!granted) {
            _conversationState.value = ConversationState.ErrorState("Permiso de micrófono denegado")
            _uiState.value = MedixUiState.Error("No tengo permiso de micrófono", "Habilítalo en ajustes")
            return
        }
        if (_assistantText.value.isBlank()) {
            val start = engine.onStart()
            _conversationState.value = start.newState
            speak(start.response, start.expectUserResponse)
        }
    }

    fun onMainButtonClick() {
        when (_uiState.value) {
            is MedixUiState.Listening -> stopListening()
            else -> startListening()
        }
    }

    fun changeUser() {
        val result = engine.processInput(_conversationState.value, "cambiar usuario")
        _conversationState.value = result.newState
        speak(result.response, true)
        refreshAppointments()
    }

    private fun onUserFinalInput(text: String) {
        _uiState.value = MedixUiState.Processing(text)
        val result = engine.processInput(_conversationState.value, text)
        _conversationState.value = result.newState
        if (result.response.contains("¿")) metricsLogger.incrementReprompt()
        if (result.successAction) metricsLogger.incrementSuccess()
        speak(result.response, result.expectUserResponse)
        refreshAppointments()
    }

    private fun speak(text: String, expectResponse: Boolean) {
        _assistantText.value = text
        stopListening()
        _uiState.value = MedixUiState.Speaking(text)
        ttsManager.speak(text) {
            viewModelScope.launch {
                metricsLogger.markTurnCompleted()
                delay(250)
                if (expectResponse && hasMicPermission) {
                    startListening()
                } else {
                    _uiState.value = MedixUiState.Idle
                }
            }
        }
    }

    private fun refreshAppointments() {
        val cedula = repository.getActiveUser() ?: run { _appointments.value = emptyList(); return }
        _appointments.value = repository.getBookedAppointments(cedula)
    }

    fun startListening() {
        if (!hasMicPermission) return
        metricsLogger.markListeningStarted()
        _uiState.value = MedixUiState.Listening("")
        asrManager.startListening()
    }

    fun stopListening() {
        if (this::asrManager.isInitialized) asrManager.stopListening()
        if (_uiState.value is MedixUiState.Listening) _uiState.value = MedixUiState.Idle
    }

    override fun onCleared() {
        if (this::asrManager.isInitialized) asrManager.destroy()
        ttsManager.shutdown()
        super.onCleared()
    }

    fun getActiveUser(): String? = repository.getActiveUser()
}
