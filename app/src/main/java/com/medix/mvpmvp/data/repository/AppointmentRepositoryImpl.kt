package com.medix.mvpmvp.data.repository

import com.medix.mvpmvp.data.datasource.AppointmentLocalDataSource
import com.medix.mvpmvp.domain.model.Appointment
import com.medix.mvpmvp.domain.model.AppointmentStatus
import com.medix.mvpmvp.domain.repository.AppointmentRepository
import java.time.LocalDate

class AppointmentRepositoryImpl(
    private val dataSource: AppointmentLocalDataSource
) : AppointmentRepository {
    private var appointments: MutableList<Appointment> = dataSource.loadAppointmentsOrBootstrap()

    override fun getAll(): List<Appointment> = appointments.sortedBy { it.dateTimeIso }

    override fun getAvailable(): List<Appointment> = getAll().filter { it.status == AppointmentStatus.AVAILABLE }

    override fun getBookedByCedula(cedula: String): List<Appointment> =
        getAll().filter { it.status == AppointmentStatus.BOOKED && it.cedulaOwner == cedula }

    override fun book(appointmentId: Int, cedula: String): Appointment? {
        val idx = appointments.indexOfFirst { it.id == appointmentId && it.status == AppointmentStatus.AVAILABLE }
        if (idx == -1) return null
        val updated = appointments[idx].copy(status = AppointmentStatus.BOOKED, cedulaOwner = cedula)
        appointments[idx] = updated
        dataSource.saveCedula(cedula)
        persist()
        return updated
    }

    override fun cancelByCedula(cedula: String): Appointment? {
        val idx = appointments.indexOfFirst { it.status == AppointmentStatus.BOOKED && it.cedulaOwner == cedula }
        if (idx == -1) return null
        val updated = appointments[idx].copy(status = AppointmentStatus.AVAILABLE, cedulaOwner = null)
        appointments[idx] = updated
        persist()
        return updated
    }

    override fun reprogramByCedula(cedula: String, newAppointmentId: Int): Appointment? {
        cancelByCedula(cedula)
        return book(newAppointmentId, cedula)
    }

    override fun saveAvailable(available: List<Appointment>) {
        val booked = appointments.filter { it.status != AppointmentStatus.AVAILABLE }
        appointments = (available + booked).toMutableList()
        persist()
    }

    override fun resetFromAssets(): List<Appointment> {
        val booked = appointments.filter { it.status == AppointmentStatus.BOOKED }
        appointments = (dataSource.loadFromAssets() + booked).toMutableList()
        persist()
        return getAvailable()
    }

    override fun findAvailableByDate(date: LocalDate): List<Appointment> =
        getAvailable().filter { dataSource.isDate(it, date) }

    override fun findAvailableExact(iso: String): Appointment? =
        getAvailable().firstOrNull { it.dateTimeIso == iso }

    override fun addAvailable(appointment: Appointment) {
        appointments.add(appointment.copy(status = AppointmentStatus.AVAILABLE, cedulaOwner = null))
        persist()
    }

    override fun updateAvailable(appointment: Appointment) {
        val idx = appointments.indexOfFirst { it.id == appointment.id }
        if (idx >= 0) {
            appointments[idx] = appointment.copy(status = AppointmentStatus.AVAILABLE, cedulaOwner = null)
            persist()
        }
    }

    override fun deleteAvailable(appointmentId: Int) {
        appointments.removeAll { it.id == appointmentId && it.status == AppointmentStatus.AVAILABLE }
        persist()
    }

    private fun persist() = dataSource.saveAppointments(appointments)
}
