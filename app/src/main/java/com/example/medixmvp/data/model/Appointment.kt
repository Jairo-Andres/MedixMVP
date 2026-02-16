package com.example.medixmvp.data.model

data class AppointmentSlot(
    val date: String,
    val time: String
)

data class Appointment(
    val id: Int,
    val slot: AppointmentSlot,
    val createdAt: Long = System.currentTimeMillis()
)
