package com.medix.mvp.data.mapper

import com.medix.mvp.data.model.AppointmentEntity
import com.medix.mvp.data.model.SlotEntity
import com.medix.mvp.domain.model.Appointment
import com.medix.mvp.domain.model.Slot
import java.time.LocalDate
import java.time.LocalTime

fun SlotEntity.toDomain(): Slot = Slot(
    id = id,
    fecha = LocalDate.parse(fecha),
    hora = LocalTime.parse(hora),
    doctor = doctor,
    lugar = lugar,
)

fun AppointmentEntity.toDomain(): Appointment = Appointment(
    id = id,
    cedula = cedula,
    slotId = slotId,
    fecha = LocalDate.parse(fecha),
    hora = LocalTime.parse(hora),
    doctor = doctor,
    lugar = lugar,
)

fun Appointment.toEntity(): AppointmentEntity = AppointmentEntity(
    id = id,
    cedula = cedula,
    slotId = slotId,
    fecha = fecha.toString(),
    hora = hora.toString(),
    doctor = doctor,
    lugar = lugar,
)
