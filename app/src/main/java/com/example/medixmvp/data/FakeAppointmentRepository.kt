package com.example.medixmvp.data

import com.example.medixmvp.data.model.Appointment
import com.example.medixmvp.data.model.AppointmentSlot

class FakeAppointmentRepository {
    private val availableSlots = mutableListOf(
        AppointmentSlot("2026-02-20", "09:00"),
        AppointmentSlot("2026-02-20", "11:30"),
        AppointmentSlot("2026-02-21", "08:30"),
        AppointmentSlot("2026-02-22", "15:00")
    )

    private val scheduledAppointments = mutableListOf<Appointment>()
    private var pendingAction: PendingAction? = null
    private var idCounter: Int = 1

    sealed interface PendingAction {
        data class Schedule(val slot: AppointmentSlot) : PendingAction
        data class Reschedule(
            val originalAppointment: Appointment,
            val newSlot: AppointmentSlot
        ) : PendingAction
    }

    fun getAvailableSlots(): List<AppointmentSlot> = availableSlots.toList()

    fun getScheduledAppointments(): List<Appointment> = scheduledAppointments.toList()

    fun hasPendingConfirmation(): Boolean = pendingAction != null

    fun requestSchedule(): AppointmentSlot? {
        val nextSlot = availableSlots.firstOrNull() ?: return null
        pendingAction = PendingAction.Schedule(nextSlot)
        return nextSlot
    }

    fun requestReschedule(): AppointmentSlot? {
        val current = scheduledAppointments.lastOrNull() ?: return null
        val nextSlot = availableSlots.firstOrNull() ?: return null
        pendingAction = PendingAction.Reschedule(current, nextSlot)
        return nextSlot
    }

    fun confirmPending(): ConfirmResult {
        return when (val action = pendingAction) {
            null -> ConfirmResult.NoPending
            is PendingAction.Schedule -> {
                if (!availableSlots.remove(action.slot)) {
                    pendingAction = null
                    ConfirmResult.NoPending
                } else {
                    val appointment = Appointment(id = idCounter++, slot = action.slot)
                    scheduledAppointments.add(appointment)
                    pendingAction = null
                    ConfirmResult.Confirmed(appointment)
                }
            }

            is PendingAction.Reschedule -> {
                if (!availableSlots.remove(action.newSlot)) {
                    pendingAction = null
                    ConfirmResult.NoPending
                } else {
                    scheduledAppointments.removeAll { it.id == action.originalAppointment.id }
                    availableSlots.add(action.originalAppointment.slot)
                    availableSlots.sortBy { "${it.date} ${it.time}" }

                    val updated = Appointment(id = action.originalAppointment.id, slot = action.newSlot)
                    scheduledAppointments.add(updated)
                    pendingAction = null
                    ConfirmResult.Rescheduled(updated)
                }
            }
        }
    }

    fun cancelLatest(): Appointment? {
        val latest = scheduledAppointments.lastOrNull() ?: return null
        scheduledAppointments.remove(latest)
        availableSlots.add(latest.slot)
        availableSlots.sortBy { "${it.date} ${it.time}" }
        return latest
    }
}

sealed interface ConfirmResult {
    data object NoPending : ConfirmResult
    data class Confirmed(val appointment: Appointment) : ConfirmResult
    data class Rescheduled(val appointment: Appointment) : ConfirmResult
}
