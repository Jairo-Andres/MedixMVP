package com.medix.mvpmvp.data.datasource

import android.content.Context
import com.medix.mvpmvp.core.storage.JsonStorage
import com.medix.mvpmvp.data.mapper.AppointmentMapper
import com.medix.mvpmvp.domain.model.Appointment
import com.medix.mvpmvp.domain.model.AppointmentStatus
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime

class AppointmentLocalDataSource(private val context: Context, private val storage: JsonStorage) {
    private val keyAppointments = "appointments"
    private val keyCedulas = "known_cedulas"

    fun loadAppointmentsOrBootstrap(): MutableList<Appointment> {
        val existing = storage.getArray(keyAppointments)
        if (existing != null) return parseAppointments(existing).toMutableList()
        val fromAssets = loadFromAssets()
        saveAppointments(fromAssets)
        return fromAssets.toMutableList()
    }

    fun loadFromAssets(): List<Appointment> {
        val lines = context.assets.open("available_appointments.txt").bufferedReader().readLines()
        return lines.filter { it.isNotBlank() }.mapIndexed { index, line ->
            val parts = line.split("|")
            val iso = LocalDateTime.parse("${parts[0]}T${parts[1]}:00").toString()
            Appointment(
                id = index + 1,
                dateTimeIso = iso,
                doctorName = parts.getOrElse(2) { "Doctor" },
                location = parts.getOrElse(3) { "Sede" },
                status = AppointmentStatus.AVAILABLE,
                cedulaOwner = null
            )
        }
    }

    fun saveAppointments(appointments: List<Appointment>) {
        val arr = JSONArray()
        appointments.forEach {
            val entity = AppointmentMapper.toEntity(it)
            arr.put(JSONObject().apply {
                put("id", entity.id)
                put("dateTimeIso", entity.dateTimeIso)
                put("doctorName", entity.doctorName)
                put("location", entity.location)
                put("status", entity.status)
                put("cedulaOwner", entity.cedulaOwner)
            })
        }
        storage.putArray(keyAppointments, arr)
    }

    fun saveCedula(cedula: String) {
        val arr = storage.getArray(keyCedulas) ?: JSONArray()
        val exists = (0 until arr.length()).any { arr.getString(it) == cedula }
        if (!exists) {
            arr.put(cedula)
            storage.putArray(keyCedulas, arr)
        }
    }

    fun parseAppointments(arr: JSONArray): List<Appointment> = buildList {
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val appointment = Appointment(
                id = obj.getInt("id"),
                dateTimeIso = obj.getString("dateTimeIso"),
                doctorName = obj.getString("doctorName"),
                location = obj.getString("location"),
                status = AppointmentStatus.valueOf(obj.getString("status")),
                cedulaOwner = obj.optString("cedulaOwner").takeIf { it.isNotBlank() && it != "null" }
            )
            add(appointment)
        }
    }

    fun isDate(appointment: Appointment, date: LocalDate): Boolean =
        LocalDateTime.parse(appointment.dateTimeIso).toLocalDate() == date
}
