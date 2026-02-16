package com.medix.mvppro.data.mapper

import com.medix.mvppro.data.model.AppointmentEntity
import com.medix.mvppro.domain.model.Appointment

object AppointmentMapper {
    fun toDomain(entity: AppointmentEntity): Appointment = Appointment(
        id = entity.id,
        dateLabel = entity.dateLabel,
        timeLabel = entity.timeLabel,
        status = entity.status
    )
}
