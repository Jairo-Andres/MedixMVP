package com.medix.mvpmvp.presentation.state

import com.medix.mvpmvp.core.logging.MetricsSummary
import com.medix.mvpmvp.domain.model.Appointment
import com.medix.mvpmvp.domain.model.DialogState
import com.medix.mvpmvp.voice.model.VoiceUiStatus

data class MedixUiState(
    val status: VoiceUiStatus = VoiceUiStatus.Idle,
    val medixText: String = "",
    val userText: String = "",
    val activeCedula: String = "",
    val dialogState: DialogState = DialogState.Boot,
    val booked: List<Appointment> = emptyList(),
    val available: List<Appointment> = emptyList(),
    val metrics: MetricsSummary = MetricsSummary(0, emptyMap(), 0L, emptyList()),
    val expandedMetrics: Boolean = false,
    val micPermissionDenied: Boolean = false
)
