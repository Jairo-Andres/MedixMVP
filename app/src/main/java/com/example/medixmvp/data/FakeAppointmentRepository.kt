package com.example.medixmvp.data

import com.example.medixmvp.data.model.Appointment

class FakeAppointmentRepository {

    private val availableSlots = mutableListOf(
        Appointment(id = 0, fecha = "2026-02-20", hora = "09:00", estado = "DISPONIBLE"),
        Appointment(id = 0, fecha = "2026-02-20", hora = "11:30", estado = "DISPONIBLE"),
        Appointment(id = 0, fecha = "2026-02-21", hora = "08:30", estado = "DISPONIBLE"),
        Appointment(id = 0, fecha = "2026-02-22", hora = "15:00", estado = "DISPONIBLE")
    )

    private val bookedAppointments = mutableListOf<Appointment>()
    var pendingConfirmation: Appointment? = null
        private set

    private var nextId = 1

    fun getAvailable(): List<Appointment> = availableSlots.toList()

    fun getBooked(): List<Appointment> = bookedAppointments.toList()

    fun proposeNextAvailable(): Appointment? {
        val next = availableSlots.firstOrNull() ?: return null
        pendingConfirmation = next.copy(id = nextId, estado = "PENDIENTE")
        return pendingConfirmation
    }

    fun confirmPending(): Appointment? {
        val pending = pendingConfirmation ?: return null
        val originalIndex = availableSlots.indexOfFirst {
            it.fecha == pending.fecha && it.hora == pending.hora
        }
        if (originalIndex == -1) {
            pendingConfirmation = null
            return null
        }

        availableSlots.removeAt(originalIndex)
        val confirmed = pending.copy(estado = "AGENDADA")
        bookedAppointments.add(confirmed)
        pendingConfirmation = null
        nextId += 1
        return confirmed
    }

    fun cancelLast(): Appointment? {
        val last = bookedAppointments.lastOrNull() ?: return null
        bookedAppointments.remove(last)
        val backToAvailable = last.copy(id = 0, estado = "DISPONIBLE")
        availableSlots.add(backToAvailable)
        availableSlots.sortWith(compareBy({ it.fecha }, { it.hora }))
        return last.copy(estado = "CANCELADA")
    }

    fun reschedule(): Appointment? {
        if (bookedAppointments.isEmpty()) return null

        cancelLast() ?: return null
        return proposeNextAvailable()
    }
}
