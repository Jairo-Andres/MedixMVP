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
    val responseText: String = "Hola, soy Medix, su asistente de citas médicas. Para comenzar, por favor ingrese su cédula.",
    val patientId: String = "",
    val availableSlots: List<String> = emptyList(),
    val scheduledAppointments: List<String> = emptyList(),
    val pendingConfirm: Boolean = false,
    val awaitingScheduleDate: Boolean = false
)

class MedixViewModel(
    private val repository: FakeAppointmentRepository = FakeAppointmentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedixUiState())
    val uiState: StateFlow<MedixUiState> = _uiState.asStateFlow()

    init {
        refreshUi()
    }

    fun onPatientIdChange(value: String) {
        val sanitized = value.filter { it.isDigit() }
        _uiState.value = _uiState.value.copy(patientId = sanitized)
    }

    fun onSpeechRecognized(text: String) {
        val normalizedInput = normalizeForComparison(text)
        val normalizedResponse = normalizeForComparison(_uiState.value.responseText)
        if (normalizedInput.isBlank() || normalizedInput == normalizedResponse) {
            return
        }

        val currentPatientId = _uiState.value.patientId
        if (currentPatientId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                recognizedText = text,
                responseText = "Antes de gestionar citas, necesito su cédula para guardar la información del usuario."
            )
            return
        }

        val requestedDate = extractRequestedDate(text)
        val intent = IntentProcessor.parse(text)
        val wasAwaitingDate = _uiState.value.awaitingScheduleDate

        val response = when {
            wasAwaitingDate && requestedDate != null -> handleSchedule(requestedDate)
            wasAwaitingDate -> {
                _uiState.value = _uiState.value.copy(awaitingScheduleDate = true)
                "Indíqueme el día exacto para revisar disponibilidad. Puede decir una fecha como 2026-02-21 o 21/02/2026."
            }

            intent == MedixIntent.AGENDAR && requestedDate != null -> handleSchedule(requestedDate)
            intent == MedixIntent.AGENDAR -> {
                _uiState.value = _uiState.value.copy(awaitingScheduleDate = true)
                "Perfecto. ¿Para qué día desea la cita?"
            }

            intent == MedixIntent.CONFIRMAR -> handleConfirm(currentPatientId)
            intent == MedixIntent.CANCELAR -> handleCancel()
            intent == MedixIntent.REPROGRAMAR -> handleReschedule()
            else -> HELP_TEXT
        }

        _uiState.value = _uiState.value.copy(
            recognizedText = text,
            responseText = response
        )
        refreshUi(keepResponse = true)
    }

    private fun handleSchedule(requestedDate: String): String {
        val slot = repository.requestScheduleForDate(requestedDate)
        return if (slot == null) {
            _uiState.value = _uiState.value.copy(awaitingScheduleDate = true)
            "No tengo horarios disponibles para el $requestedDate. Si desea, indíqueme otra fecha y la reviso."
        } else {
            _uiState.value = _uiState.value.copy(awaitingScheduleDate = false)
            "Tengo disponible el ${slot.date} a las ${slot.time} con ${slot.doctorName} en ${slot.location}. ¿Desea confirmar la cita?"
        }
    }

    private fun handleConfirm(patientId: String): String {
        return when (val result = repository.confirmPending(patientId)) {
            ConfirmResult.NoPending -> "No tengo una cita pendiente por confirmar."
            is ConfirmResult.Confirmed -> "Cita confirmada para el ${result.appointment.slot.date} a las ${result.appointment.slot.time} con ${result.appointment.slot.doctorName} en ${result.appointment.slot.location} (cédula: ${result.appointment.patientId})."
            is ConfirmResult.Rescheduled -> "Cita reprogramada y confirmada para el ${result.appointment.slot.date} a las ${result.appointment.slot.time} con ${result.appointment.slot.doctorName} en ${result.appointment.slot.location} (cédula: ${result.appointment.patientId})."
        }
    }

    private fun handleCancel(): String {
        val canceled = repository.cancelLatest()
        return if (canceled == null) {
            "No tiene citas agendadas para cancelar."
        } else {
            "Se canceló su cita del ${canceled.slot.date} a las ${canceled.slot.time} con ${canceled.slot.doctorName} en ${canceled.slot.location} (cédula: ${canceled.patientId})."
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
            "Puedo mover su cita al ${newSlot.date} a las ${newSlot.time} con ${newSlot.doctorName} en ${newSlot.location}. ¿Desea confirmar la cita?"
        }
    }

    private fun refreshUi(keepResponse: Boolean = false) {
        val previous = _uiState.value
        val next = previous.copy(
            availableSlots = repository.getAvailableSlots().map {
                "${it.date} - ${it.time} | ${it.doctorName} | ${it.location}"
            },
            scheduledAppointments = repository.getScheduledAppointments().map {
                "${it.slot.date} - ${it.slot.time} | ${it.slot.doctorName} | ${it.slot.location} (C.C.: ${it.patientId})"
            },
            pendingConfirm = repository.hasPendingConfirmation()
        )
        _uiState.value = if (keepResponse) next.copy(responseText = previous.responseText) else next
    }

    private fun normalizeForComparison(text: String): String {
        return text.lowercase().replace("[^a-z0-9áéíóúüñ ]".toRegex(), "").trim()
    }

    private fun extractRequestedDate(text: String): String? {
        val normalized = text.lowercase()
        val isoDateRegex = Regex("\\b(\\d{4})-(\\d{2})-(\\d{2})\\b")
        isoDateRegex.find(normalized)?.let {
            return "${it.groupValues[1]}-${it.groupValues[2]}-${it.groupValues[3]}"
        }

        val slashDateRegex = Regex("\\b(\\d{1,2})/(\\d{1,2})(?:/(\\d{2,4}))?\\b")
        slashDateRegex.find(normalized)?.let {
            val day = it.groupValues[1].padStart(2, '0')
            val month = it.groupValues[2].padStart(2, '0')
            val yearRaw = it.groupValues.getOrNull(3).orEmpty()
            val year = when {
                yearRaw.isBlank() -> DEFAULT_YEAR
                yearRaw.length == 2 -> "20$yearRaw"
                else -> yearRaw
            }
            return "$year-$month-$day"
        }

        val monthNames = mapOf(
            "enero" to "01",
            "febrero" to "02",
            "marzo" to "03",
            "abril" to "04",
            "mayo" to "05",
            "junio" to "06",
            "julio" to "07",
            "agosto" to "08",
            "septiembre" to "09",
            "setiembre" to "09",
            "octubre" to "10",
            "noviembre" to "11",
            "diciembre" to "12"
        )
        val longDateRegex = Regex("\\b(\\d{1,2})\\s+de\\s+([a-záéíóúñ]+)(?:\\s+de\\s+(\\d{4}))?\\b")
        val longDateMatch = longDateRegex.find(normalized) ?: return null
        val day = longDateMatch.groupValues[1].padStart(2, '0')
        val month = monthNames[longDateMatch.groupValues[2]] ?: return null
        val year = longDateMatch.groupValues.getOrNull(3).takeUnless { it.isNullOrBlank() } ?: DEFAULT_YEAR
        return "$year-$month-$day"
    }

    companion object {
        private const val DEFAULT_YEAR = "2026"
        private const val HELP_TEXT =
            "Puedo agendar, confirmar, cancelar o reprogramar una cita. Si desea agendar, dígame también el día que prefiere."
    }
}
