package com.medix.mvpmvp.domain.model

import java.time.LocalDate

data class SchedulingContext(
    val requestedDate: LocalDate? = null,
    val requestedHourOptions: List<String> = emptyList(),
    val pendingAppointmentId: Int? = null
)

sealed class DialogState {
    data object Boot : DialogState()
    data object AskingCedula : DialogState()
    data object Ready : DialogState()
    data class Scheduling(val context: SchedulingContext = SchedulingContext()) : DialogState()
    data object Cancelling : DialogState()
    data object Rescheduling : DialogState()
    data class ErrorState(val message: String) : DialogState()
}
