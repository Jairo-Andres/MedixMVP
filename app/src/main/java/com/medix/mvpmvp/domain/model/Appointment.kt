package com.medix.mvpmvp.domain.model

data class Appointment(
    val id: Int,
    val dateTimeIso: String,
    val doctorName: String,
    val location: String,
    val status: AppointmentStatus,
    val cedulaOwner: String?
)

enum class AppointmentStatus {
    AVAILABLE,
    BOOKED,
    PENDING_CONFIRM,
    CANCELLED
}
