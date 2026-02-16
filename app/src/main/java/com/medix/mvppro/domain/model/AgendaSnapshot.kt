package com.medix.mvppro.domain.model

data class AgendaSnapshot(
    val available: List<Appointment>,
    val booked: List<Appointment>
)
