package com.medix.mvppro.domain.model

enum class AppointmentStatus {
    AVAILABLE,
    PENDING_BOOK,
    PENDING_RESCHEDULE,
    CONFIRMED
}

data class Appointment(
    val id: String,
    val dateLabel: String,
    val timeLabel: String,
    val status: AppointmentStatus
)
