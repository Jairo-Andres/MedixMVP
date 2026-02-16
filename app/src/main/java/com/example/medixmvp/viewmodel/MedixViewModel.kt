package com.example.medixmvp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.medixmvp.data.ConfirmResult
import com.example.medixmvp.data.FakeAppointmentRepository
import com.example.medixmvp.logic.IntentProcessor
import com.example.medixmvp.logic.MedixIntent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MedixUiState(
    val recognizedText: String = "",
    val responseText: String = "Bienvenido a Medix. Presione hablar para comenzar.",
    val availableSlots: List<String> = emptyList(),
    val scheduledAppointments: List<String> = emptyList(),
    val pendingConfirm: Boolean = false
)

class MedixViewModel(
    private val repository: FakeAppointmentRepository = FakeAppointmentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedixUiState())
    val uiState: StateFlow<MedixUiState> = _uiState.asStateFlow()

    init {
        refreshUi()
    }

    fun onSpeechRecognized(text: String) {
        val intent = IntentProcessor.parse(text)
        val response = when (intent) {
            MedixIntent.AGENDAR -> handleSchedule()
            MedixIntent.CONFIRMAR -> handleConfirm()
            MedixIntent.CANCELAR -> handleCancel()
            MedixIntent.REPROGRAMAR -> handleReschedule()
            MedixIntent.DESCONOCIDA -> HELP_TEXT
        }

        _uiState.value = _uiState.value.copy(
            recognizedText = text,
            responseText = response
        )
        refreshUi(keepResponse = true)
    }

    private fun handleSchedule(): String {
        val slot = repository.requestSchedule()
        return if (slot == null) {
            "En este momento no tengo horarios disponibles para agendar."
        } else {
            "Tengo disponible el ${slot.date} a las ${slot.time}. ¿Desea confirmar la cita para ${slot.date} a las ${slot.time}?"
        }
    }

    private fun handleConfirm(): String {
        return when (val result = repository.confirmPending()) {
            ConfirmResult.NoPending -> "No tengo una cita pendiente por confirmar."
            is ConfirmResult.Confirmed -> "Cita confirmada para el ${result.appointment.slot.date} a las ${result.appointment.slot.time}."
            is ConfirmResult.Rescheduled -> "Cita reprogramada y confirmada para el ${result.appointment.slot.date} a las ${result.appointment.slot.time}."
        }
    }

    private fun handleCancel(): String {
        val canceled = repository.cancelLatest()
        return if (canceled == null) {
            "No tiene citas agendadas para cancelar."
        } else {
            "Se canceló su cita del ${canceled.slot.date} a las ${canceled.slot.time}."
        }
    }

    private fun handleReschedule(): String {
        val hasAnyScheduled = repository.getScheduledAppointments().isNotEmpty()
        if (!hasAnyScheduled) {
            return "No tiene citas agendadas para reprogramar."
        }

        val newSlot = repository.requestReschedule()
        return if (newSlot == null) {
            "No hay horarios disponibles para reprogramar su cita."
        } else {
            "Puedo mover su cita al ${newSlot.date} a las ${newSlot.time}. ¿Desea confirmar la cita para ${newSlot.date} a las ${newSlot.time}?"
        }
    }

    private fun refreshUi(keepResponse: Boolean = false) {
        val previous = _uiState.value
        val next = previous.copy(
            availableSlots = repository.getAvailableSlots().map { "${it.date} - ${it.time}" },
            scheduledAppointments = repository.getScheduledAppointments().map { "${it.slot.date} - ${it.slot.time}" },
            pendingConfirm = repository.hasPendingConfirmation()
        )
        _uiState.value = if (keepResponse) next.copy(responseText = previous.responseText) else next
    }

    companion object {
        private const val HELP_TEXT =
            "Puedo agendar, confirmar, cancelar o reprogramar una cita. ¿Qué desea hacer?"
    }
}
