package com.medix.mvp.data.repository

import android.content.SharedPreferences
import com.medix.mvp.data.datasource.FakeAppointmentDataSource
import com.medix.mvp.data.mapper.toDomain
import com.medix.mvp.data.mapper.toEntity
import com.medix.mvp.data.model.AppointmentEntity
import com.medix.mvp.domain.model.Appointment
import com.medix.mvp.domain.model.Proposal
import com.medix.mvp.domain.model.Slot
import com.medix.mvp.domain.repository.AppointmentRepository
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class LocalAppointmentRepository(
    private val prefs: SharedPreferences,
    private val dataSource: FakeAppointmentDataSource,
) : AppointmentRepository {

    override fun setActiveUser(cedula: String) {
        val users = getUsers().toMutableSet()
        users.add(cedula)
        prefs.edit().putString(KEY_ACTIVE_USER, cedula)
            .putString(KEY_USERS, JSONArray(users.toList()).toString())
            .apply()
    }

    override fun getActiveUser(): String? = prefs.getString(KEY_ACTIVE_USER, null)

    override fun getUsers(): List<String> {
        val raw = prefs.getString(KEY_USERS, "[]") ?: "[]"
        val array = JSONArray(raw)
        return buildList {
            for (i in 0 until array.length()) add(array.getString(i))
        }
    }

    override fun clearActiveUser() {
        prefs.edit().remove(KEY_ACTIVE_USER).apply()
        setPendingProposal(null)
    }

    override fun getAvailableSlots(fecha: String?): List<Slot> {
        val bookedIds = loadAppointments().map { it.slotId }.toSet()
        return dataSource.slots
            .filter { fecha == null || it.fecha == fecha }
            .filterNot { it.id in bookedIds }
            .map { it.toDomain() }
    }

    override fun getBookedAppointments(cedula: String): List<Appointment> =
        loadAppointments().filter { it.cedula == cedula }.map { it.toDomain() }.sortedBy { it.fecha.toString() + it.hora }

    override fun findAvailability(fecha: String, hora: String?): List<Slot> {
        val slots = getAvailableSlots(fecha)
        return if (hora == null) slots else {
            val time = parseHour(hora)
            val exact = slots.filter { it.hora == time }
            if (exact.isNotEmpty()) exact else slots.sortedBy { kotlin.math.abs(it.hora.hour - time.hour) }.take(2)
        }
    }

    override fun book(slotId: String, doctor: String, lugar: String): Appointment? {
        val cedula = getActiveUser() ?: return null
        val slot = dataSource.slots.firstOrNull { it.id == slotId }?.toDomain() ?: return null
        val appointment = Appointment(
            id = UUID.randomUUID().toString(),
            cedula = cedula,
            slotId = slot.id,
            fecha = slot.fecha,
            hora = slot.hora,
            doctor = doctor.ifBlank { slot.doctor },
            lugar = lugar.ifBlank { slot.lugar },
        )
        val all = loadAppointments().toMutableList()
        all.add(appointment.toEntity())
        saveAppointments(all)
        return appointment
    }

    override fun cancelLast(cedula: String): Appointment? {
        val all = loadAppointments().toMutableList()
        val index = all.indexOfLast { it.cedula == cedula }
        if (index < 0) return null
        val removed = all.removeAt(index)
        saveAppointments(all)
        return removed.toDomain()
    }

    override fun reschedule(cedula: String, appointmentId: String, newSlotId: String): Appointment? {
        val all = loadAppointments().toMutableList()
        val index = all.indexOfFirst { it.id == appointmentId && it.cedula == cedula }
        val slot = dataSource.slots.firstOrNull { it.id == newSlotId }?.toDomain() ?: return null
        if (index < 0) return null
        val updated = all[index].copy(
            slotId = slot.id,
            fecha = slot.fecha.toString(),
            hora = slot.hora.toString(),
            doctor = slot.doctor,
            lugar = slot.lugar,
        )
        all[index] = updated
        saveAppointments(all)
        return updated.toDomain()
    }

    override fun getPendingProposal(): Proposal? {
        val raw = prefs.getString(KEY_PENDING, null) ?: return null
        val obj = JSONObject(raw)
        return Proposal(
            cedula = obj.getString("cedula"),
            slot = Slot(
                id = obj.getString("slotId"),
                fecha = LocalDate.parse(obj.getString("fecha")),
                hora = LocalTime.parse(obj.getString("hora")),
                doctor = obj.getString("doctor"),
                lugar = obj.getString("lugar"),
            )
        )
    }

    override fun setPendingProposal(proposal: Proposal?) {
        if (proposal == null) {
            prefs.edit().remove(KEY_PENDING).apply()
            return
        }
        val obj = JSONObject()
            .put("cedula", proposal.cedula)
            .put("slotId", proposal.slot.id)
            .put("fecha", proposal.slot.fecha.toString())
            .put("hora", proposal.slot.hora.toString())
            .put("doctor", proposal.slot.doctor)
            .put("lugar", proposal.slot.lugar)
        prefs.edit().putString(KEY_PENDING, obj.toString()).apply()
    }

    private fun loadAppointments(): List<AppointmentEntity> {
        val raw = prefs.getString(KEY_APPOINTMENTS, "[]") ?: "[]"
        val array = JSONArray(raw)
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                add(
                    AppointmentEntity(
                        id = obj.getString("id"),
                        cedula = obj.getString("cedula"),
                        slotId = obj.getString("slotId"),
                        fecha = obj.getString("fecha"),
                        hora = obj.getString("hora"),
                        doctor = obj.getString("doctor"),
                        lugar = obj.getString("lugar"),
                    )
                )
            }
        }
    }

    private fun saveAppointments(items: List<AppointmentEntity>) {
        val array = JSONArray()
        items.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id)
                    .put("cedula", it.cedula)
                    .put("slotId", it.slotId)
                    .put("fecha", it.fecha)
                    .put("hora", it.hora)
                    .put("doctor", it.doctor)
                    .put("lugar", it.lugar)
            )
        }
        prefs.edit().putString(KEY_APPOINTMENTS, array.toString()).apply()
    }

    private fun parseHour(raw: String): LocalTime {
        val normalized = raw.replace("am", "").replace("pm", "").trim()
        return when {
            normalized.contains(":") -> LocalTime.parse(normalized.padStart(5, '0'))
            else -> LocalTime.of(normalized.toIntOrNull() ?: 9, 0)
        }
    }

    private companion object {
        const val KEY_ACTIVE_USER = "active_user"
        const val KEY_USERS = "users"
        const val KEY_APPOINTMENTS = "appointments"
        const val KEY_PENDING = "pending_proposal"
    }
}
