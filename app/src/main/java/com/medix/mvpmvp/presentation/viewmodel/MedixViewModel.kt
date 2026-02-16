package com.medix.mvpmvp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.medix.mvpmvp.core.logging.MetricsLogger
import com.medix.mvpmvp.domain.model.Appointment
import com.medix.mvpmvp.domain.model.AppointmentStatus
import com.medix.mvpmvp.domain.repository.AppointmentRepository
import com.medix.mvpmvp.domain.usecase.DialogManager
import com.medix.mvpmvp.presentation.state.MedixUiState
import com.medix.mvpmvp.voice.model.VoiceUiStatus
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MedixViewModel(
    private val repository: AppointmentRepository,
    private val dialogManager: DialogManager,
    private val metricsLogger: MetricsLogger = MetricsLogger()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedixUiState())
    val uiState: StateFlow<MedixUiState> = _uiState.asStateFlow()

    private val _speakQueue = MutableStateFlow<String?>(null)
    val speakQueue: StateFlow<String?> = _speakQueue.asStateFlow()

    fun boot() {
        val message = dialogManager.start()
        refresh(message = message, status = VoiceUiStatus.Idle)
        queueSpeak(message)
    }

    fun onPermissionDenied() {
        _uiState.value = _uiState.value.copy(
            status = VoiceUiStatus.Error,
            medixText = "No tengo permiso de micrófono. Actívalo en Ajustes para continuar.",
            micPermissionDenied = true
        )
    }

    fun onListeningStart() {
        metricsLogger.markListeningStart()
        _uiState.value = _uiState.value.copy(status = VoiceUiStatus.Listening)
    }

    fun onAsrPartial(text: String) {
        _uiState.value = _uiState.value.copy(userText = text)
    }

    fun onAsrFinal(text: String) {
        metricsLogger.onInteraction()
        val answer = dialogManager.process(text)
        refresh(message = answer, userText = text, status = VoiceUiStatus.Processing)
        queueSpeak(answer)
    }

    fun onAsrError(code: Int) {
        metricsLogger.onAsrError(code.toString())
        _uiState.value = _uiState.value.copy(status = VoiceUiStatus.Error, medixText = "No pude escuchar bien. Intenta de nuevo.")
    }

    fun onTtsDone() {
        metricsLogger.markTtsDone()
        _uiState.value = _uiState.value.copy(status = VoiceUiStatus.Idle, metrics = metricsLogger.summary())
    }

    fun consumeSpeakQueue() {
        _speakQueue.value = null
    }

    fun shouldAutoRestartListening(): Boolean = dialogManager.requiresInput() && !_uiState.value.micPermissionDenied

    fun toggleMetrics() {
        _uiState.value = _uiState.value.copy(expandedMetrics = !_uiState.value.expandedMetrics)
    }

    fun resetAvailableFromFile() {
        repository.resetFromAssets()
        refresh(message = "Lista recargada desde archivo.")
    }

    fun addOrUpdateAvailable(id: Int?, date: String, time: String, doctor: String, location: String) {
        val iso = LocalDateTime.parse("${date}T${time}:00").toString()
        val appt = Appointment(
            id = id ?: ((repository.getAll().maxOfOrNull { it.id } ?: 0) + 1),
            dateTimeIso = iso,
            doctorName = doctor,
            location = location,
            status = AppointmentStatus.AVAILABLE,
            cedulaOwner = null
        )
        if (id == null) repository.addAvailable(appt) else repository.updateAvailable(appt)
        refresh(message = "Cita disponible guardada.")
    }

    fun deleteAvailable(id: Int) {
        repository.deleteAvailable(id)
        refresh(message = "Cita eliminada.")
    }

    private fun refresh(message: String = _uiState.value.medixText, userText: String = _uiState.value.userText, status: VoiceUiStatus = _uiState.value.status) {
        val cedula = dialogManager.activeCedula.orEmpty()
        _uiState.value = _uiState.value.copy(
            status = status,
            medixText = message,
            userText = userText,
            activeCedula = cedula,
            dialogState = dialogManager.state,
            booked = if (cedula.isNotBlank()) repository.getBookedByCedula(cedula) else emptyList(),
            available = repository.getAvailable(),
            metrics = metricsLogger.summary()
        )
    }

    private fun queueSpeak(text: String) {
        _uiState.value = _uiState.value.copy(status = VoiceUiStatus.Speaking)
        _speakQueue.value = text
    }
}
