package com.example.medixmvp.data.model

data class Appointment(
    val id: Int,
    val fecha: String,
    val hora: String,
    var estado: String
)
