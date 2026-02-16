package com.medix.mvppro.data.model

import com.medix.mvppro.domain.model.AppointmentStatus

data class AppointmentEntity(
    val id: String,
    val dateLabel: String,
    val timeLabel: String,
    val status: AppointmentStatus
)
