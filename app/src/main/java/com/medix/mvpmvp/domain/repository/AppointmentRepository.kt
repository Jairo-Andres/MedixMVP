package com.medix.mvpmvp.domain.repository

import com.medix.mvpmvp.domain.model.Appointment
import java.time.LocalDate

interface AppointmentRepository {
    fun getAll(): List<Appointment>
    fun getAvailable(): List<Appointment>
    fun getBookedByCedula(cedula: String): List<Appointment>
    fun book(appointmentId: Int, cedula: String): Appointment?
    fun cancelByCedula(cedula: String): Appointment?
    fun reprogramByCedula(cedula: String, newAppointmentId: Int): Appointment?
    fun saveAvailable(available: List<Appointment>)
    fun resetFromAssets(): List<Appointment>
    fun findAvailableByDate(date: LocalDate): List<Appointment>
    fun findAvailableExact(iso: String): Appointment?
    fun addAvailable(appointment: Appointment)
    fun updateAvailable(appointment: Appointment)
    fun deleteAvailable(appointmentId: Int)
}
