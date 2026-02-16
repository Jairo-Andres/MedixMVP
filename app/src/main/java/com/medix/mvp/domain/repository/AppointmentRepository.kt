package com.medix.mvp.domain.repository

import com.medix.mvp.domain.model.Appointment
import com.medix.mvp.domain.model.Proposal
import com.medix.mvp.domain.model.Slot

interface AppointmentRepository {
    fun setActiveUser(cedula: String)
    fun getActiveUser(): String?
    fun getUsers(): List<String>
    fun clearActiveUser()

    fun getAvailableSlots(fecha: String? = null): List<Slot>
    fun getBookedAppointments(cedula: String): List<Appointment>
    fun findAvailability(fecha: String, hora: String?): List<Slot>
    fun book(slotId: String, doctor: String, lugar: String): Appointment?
    fun cancelLast(cedula: String): Appointment?
    fun reschedule(cedula: String, appointmentId: String, newSlotId: String): Appointment?

    fun getPendingProposal(): Proposal?
    fun setPendingProposal(proposal: Proposal?)
}
