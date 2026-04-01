package com.medix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medix.app.audio.AudioPlayer
import com.medix.app.audio.AudioRecorder
import com.medix.app.data.repository.VoiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.util.UUID

enum class ConversationStatus {
    IDLE,
    LISTENING,
    PROCESSING,
    RESPONDING,
    ERROR,
}

data class VoiceUiState(
    val sessionId: String = UUID.randomUUID().toString(),
    val userText: String = "",
    val assistantText: String = "Presiona y mantén el botón para hablar.",
    val status: ConversationStatus = ConversationStatus.IDLE,
    val isLoading: Boolean = false,
    val completed: Boolean = false,
    val wsConnected: Boolean = false,
    val errorMessage: String? = null,
)

class VoiceViewModel(
    private val repository: VoiceRepository,
    private val recorder: AudioRecorder,
    private val player: AudioPlayer,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private var currentAudioFile: File? = null

    init {
        connectWebSocket()
    }

    fun startRecording() {
        runCatching {
            currentAudioFile = recorder.start()
            _uiState.update {
                it.copy(
                    status = ConversationStatus.LISTENING,
                    errorMessage = null,
                )
            }
        }.onFailure { throwable ->
            showError("No se pudo iniciar la grabación: ${throwable.localizedMessage}")
        }
    }

    fun stopRecordingAndSend() {
        val audioFile = recorder.stop() ?: currentAudioFile

        if (audioFile == null) {
            showError("No se detectó audio para enviar.")
            return
        }

        _uiState.update {
            it.copy(
                status = ConversationStatus.PROCESSING,
                isLoading = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            runCatching {
                val text = repository.transcribeAudio(audioFile)
                _uiState.update { state -> state.copy(userText = text) }

                if (text.isBlank()) {
                    throw IllegalArgumentException("El texto transcrito está vacío.")
                }

                val response = repository.sendConversationMessage(text, _uiState.value.sessionId)
                response
            }.onSuccess { response ->
                updateAssistantResponse(response.response, response.completed)
            }.onFailure { throwable ->
                showError("Error procesando la solicitud: ${throwable.localizedMessage}")
            }
        }
    }

    fun sendTextMessage(text: String) {
        if (text.isBlank()) return

        _uiState.update {
            it.copy(
                userText = text,
                status = ConversationStatus.PROCESSING,
                isLoading = true,
            )
        }

        viewModelScope.launch {
            runCatching {
                repository.sendConversationMessage(text, _uiState.value.sessionId)
            }.onSuccess { response ->
                updateAssistantResponse(response.response, response.completed)
            }.onFailure { throwable ->
                showError("No se pudo enviar el texto: ${throwable.localizedMessage}")
            }
        }
    }

    private fun connectWebSocket() {
        repository.connectWebSocket(
            sessionId = _uiState.value.sessionId,
            onMessage = { payload -> handleWebSocketPayload(payload) },
            onStateChanged = { isConnected ->
                _uiState.update { state -> state.copy(wsConnected = isConnected) }
            },
        )
    }

    private fun handleWebSocketPayload(payload: String) {
        runCatching {
            val json = JSONObject(payload)
            val response = json.optString("response")
            val state = json.optString("state")
            val completed = json.optBoolean("completed", false)

            val status = when (state.lowercase()) {
                "listening" -> ConversationStatus.LISTENING
                "processing" -> ConversationStatus.PROCESSING
                "responding" -> ConversationStatus.RESPONDING
                else -> _uiState.value.status
            }

            if (response.isNotBlank()) {
                updateAssistantResponse(response, completed)
            } else {
                _uiState.update { it.copy(status = status) }
            }
        }
    }

    fun sendTextByWebSocket(text: String) {
        if (text.isBlank()) return
        repository.sendWebSocketMessage(text, _uiState.value.sessionId)
        _uiState.update {
            it.copy(
                userText = text,
                status = ConversationStatus.PROCESSING,
                isLoading = true,
            )
        }
    }

    private fun updateAssistantResponse(response: String, completed: Boolean) {
        _uiState.update {
            it.copy(
                assistantText = response,
                status = if (completed) ConversationStatus.IDLE else ConversationStatus.RESPONDING,
                isLoading = false,
                completed = completed,
                errorMessage = null,
            )
        }
        player.speak(response)
    }

    private fun showError(message: String) {
        _uiState.update {
            it.copy(
                status = ConversationStatus.ERROR,
                isLoading = false,
                errorMessage = message,
            )
        }
    }

    override fun onCleared() {
        repository.closeWebSocket()
        player.release()
        super.onCleared()
    }
}

class VoiceViewModelFactory(
    private val repository: VoiceRepository,
    private val recorder: AudioRecorder,
    private val player: AudioPlayer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VoiceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VoiceViewModel(repository, recorder, player) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
