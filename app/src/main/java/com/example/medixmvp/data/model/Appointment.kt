package com.example.medixmvp.data.model

data class AppointmentSlot(
    val date: String,
    val time: String,
    val doctorName: String,
    val location: String
)

data class Appointment(
    val id: Int,
    val patientId: String,
    val slot: AppointmentSlot,
    val createdAt: Long = System.currentTimeMillis()
)
