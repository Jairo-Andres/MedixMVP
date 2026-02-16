package com.medix.mvp.data.datasource

import com.medix.mvp.core.time.TimeProvider
import com.medix.mvp.data.model.SlotEntity
import java.time.LocalDate
import java.time.LocalTime

class FakeAppointmentDataSource(timeProvider: TimeProvider) {
    val doctors = listOf("Doctor Pérez", "Doctora Gómez", "Doctor Ruiz", "Doctora Torres")
    val lugares = listOf("Sede Centro", "Sede Norte", "Consultorio 302")

    val slots: List<SlotEntity> = buildList {
        val start = timeProvider.nowDate()
        val hours = listOf(LocalTime.of(9, 0), LocalTime.of(11, 0), LocalTime.of(15, 0))
        (0..6).forEach { offset ->
            val day = start.plusDays(offset.toLong())
            hours.forEachIndexed { index, hour ->
                val doctor = doctors[(offset + index) % doctors.size]
                val lugar = lugares[(offset + index) % lugares.size]
                add(
                    SlotEntity(
                        id = "${day}_$hour_$index",
                        fecha = day.toString(),
                        hora = hour.toString(),
                        doctor = doctor,
                        lugar = lugar,
                    )
                )
            }
        }
    }

    fun byDate(date: LocalDate): List<SlotEntity> = slots.filter { it.fecha == date.toString() }
}
