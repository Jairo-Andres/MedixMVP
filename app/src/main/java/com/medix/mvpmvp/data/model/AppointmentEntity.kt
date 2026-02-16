package com.medix.mvpmvp.data.model

data class AppointmentEntity(
    val id: Int,
    val dateTimeIso: String,
    val doctorName: String,
    val location: String,
    val status: String,
    val cedulaOwner: String?
)
