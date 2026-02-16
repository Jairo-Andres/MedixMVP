package com.medix.mvp.data.model

data class SlotEntity(
    val id: String,
    val fecha: String,
    val hora: String,
    val doctor: String,
    val lugar: String,
)

data class AppointmentEntity(
    val id: String,
    val cedula: String,
    val slotId: String,
    val fecha: String,
    val hora: String,
    val doctor: String,
    val lugar: String,
)
