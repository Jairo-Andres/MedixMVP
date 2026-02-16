package com.medix.mvppro.presentation.state

import com.medix.mvppro.core.logging.MetricsSummary
import com.medix.mvppro.domain.model.Appointment

sealed class MedixUiState {
    object Idle : MedixUiState()
    data class Listening(
        val partialText: String = "",
        val volumeLevel: Float? = null,
        val message: String = "Te escucho"
    ) : MedixUiState()

    data class Processing(val finalText: String) : MedixUiState()

    data class Error(
        val errorCode: Int,
        val message: String,
        val suggestion: String
    ) : MedixUiState()
}

data class MedixViewData(
    val uiState: MedixUiState = MedixUiState.Idle,
    val recognizedText: String = "",
    val responseText: String = "Hola, soy Medix. Presiona escuchar para empezar.",
    val availableAppointments: List<Appointment> = emptyList(),
    val bookedAppointments: List<Appointment> = emptyList(),
    val metricsSummary: MetricsSummary = MetricsSummary(0, 0, 0, 0, emptyList())
)
