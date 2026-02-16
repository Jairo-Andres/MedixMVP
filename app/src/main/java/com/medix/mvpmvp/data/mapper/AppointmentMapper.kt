package com.medix.mvpmvp.data.mapper

import com.medix.mvpmvp.data.model.AppointmentEntity
import com.medix.mvpmvp.domain.model.Appointment
import com.medix.mvpmvp.domain.model.AppointmentStatus

object AppointmentMapper {
    fun toDomain(entity: AppointmentEntity): Appointment = Appointment(
        id = entity.id,
        dateTimeIso = entity.dateTimeIso,
        doctorName = entity.doctorName,
        location = entity.location,
        status = AppointmentStatus.valueOf(entity.status),
        cedulaOwner = entity.cedulaOwner
    )

    fun toEntity(domain: Appointment): AppointmentEntity = AppointmentEntity(
        id = domain.id,
        dateTimeIso = domain.dateTimeIso,
        doctorName = domain.doctorName,
        location = domain.location,
        status = domain.status.name,
        cedulaOwner = domain.cedulaOwner
    )
}
