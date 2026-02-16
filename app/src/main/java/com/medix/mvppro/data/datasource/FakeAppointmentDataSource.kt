package com.medix.mvppro.data.datasource

import com.medix.mvppro.data.model.AppointmentEntity
import com.medix.mvppro.domain.model.AppointmentStatus
import com.medix.mvppro.domain.usecase.normalizeText

class FakeAppointmentDataSource {
    private val available = mutableListOf(
        AppointmentEntity("1", "Lun 10 Mar", "08:00", AppointmentStatus.AVAILABLE),
        AppointmentEntity("2", "Lun 10 Mar", "10:30", AppointmentStatus.AVAILABLE),
        AppointmentEntity("3", "Mar 11 Mar", "09:00", AppointmentStatus.AVAILABLE),
        AppointmentEntity("4", "Mar 11 Mar", "15:00", AppointmentStatus.AVAILABLE),
        AppointmentEntity("5", "Mié 12 Mar", "11:30", AppointmentStatus.AVAILABLE),
        AppointmentEntity("6", "Jue 13 Mar", "14:00", AppointmentStatus.AVAILABLE)
    )
    private val booked = mutableListOf<AppointmentEntity>()

    private var pendingBook: AppointmentEntity? = null
    private var pendingRescheduleCurrent: AppointmentEntity? = null
    private var pendingRescheduleNew: AppointmentEntity? = null

    fun getAvailable(): List<AppointmentEntity> = available.toList()
    fun getBooked(): List<AppointmentEntity> = booked.toList()

    fun proposeNext(userRequest: String): String {
        if (pendingBook != null) return "Ya hay una cita pendiente por confirmar."
        val candidate = selectSlot(available, userRequest)
            ?: return "No encontré ese horario. Prueba diciendo una fecha u hora disponible."
        pendingBook = candidate.copy(status = AppointmentStatus.PENDING_BOOK)
        return "Te propongo ${candidate.dateLabel} a las ${candidate.timeLabel}. Di confirmar para agendar."
    }

    fun confirmPending(): String {
        pendingBook?.let { pending ->
            available.removeAll { it.id == pending.id }
            booked.add(pending.copy(status = AppointmentStatus.CONFIRMED))
            pendingBook = null
            return "Listo, tu cita quedó confirmada para ${pending.dateLabel} a las ${pending.timeLabel}."
        }

        if (pendingRescheduleCurrent != null && pendingRescheduleNew != null) {
            val current = pendingRescheduleCurrent!!
            val newSlot = pendingRescheduleNew!!
            booked.removeAll { it.id == current.id }
            available.add(current.copy(status = AppointmentStatus.AVAILABLE))
            available.removeAll { it.id == newSlot.id }
            booked.add(newSlot.copy(status = AppointmentStatus.CONFIRMED))
            pendingRescheduleCurrent = null
            pendingRescheduleNew = null
            return "Reprogramación confirmada: ahora tienes cita ${newSlot.dateLabel} a las ${newSlot.timeLabel}."
        }

        return "No hay acciones pendientes para confirmar."
    }

    fun cancelLast(): String {
        val last = booked.removeLastOrNull() ?: return "No tienes citas confirmadas para cancelar."
        available.add(last.copy(status = AppointmentStatus.AVAILABLE))
        pendingRescheduleCurrent = null
        pendingRescheduleNew = null
        return "Se canceló tu última cita de ${last.dateLabel} a las ${last.timeLabel}."
    }

    fun reschedule(userRequest: String): String {
        val current = booked.lastOrNull() ?: return "No tienes una cita confirmada para reprogramar."
        val next = selectSlot(available, userRequest)
            ?: return "No encontré ese horario para reprogramar. Intenta con otra fecha u hora disponible."
        pendingRescheduleCurrent = current.copy(status = AppointmentStatus.PENDING_RESCHEDULE)
        pendingRescheduleNew = next.copy(status = AppointmentStatus.PENDING_RESCHEDULE)
        return "Puedo mover tu cita a ${next.dateLabel} a las ${next.timeLabel}. Di confirmar para aplicar el cambio."
    }

    private fun selectSlot(slots: List<AppointmentEntity>, userRequest: String): AppointmentEntity? {
        val request = normalizeText(userRequest)
        if (request.isBlank()) return slots.firstOrNull()

        val ordinalIndex = when {
            request.contains("segunda") || request.contains("segundo") || Regex("\\b2\\b").containsMatchIn(request) -> 1
            request.contains("tercera") || request.contains("tercero") || Regex("\\b3\\b").containsMatchIn(request) -> 2
            request.contains("cuarta") || request.contains("cuarto") || Regex("\\b4\\b").containsMatchIn(request) -> 3
            else -> null
        }

        val withScore = slots.map { slot ->
            val normalizedDate = normalizeText(slot.dateLabel)
            val normalizedTime = normalizeText(slot.timeLabel)
            var score = 0
            if (request.contains(normalizedDate)) score += 4
            if (request.contains(normalizedTime)) score += 4
            if (request.contains(slot.timeLabel.substringBefore(':'))) score += 2
            if (request.contains(normalizedDate.substringBefore(' '))) score += 1
            if (request.contains(slot.dateLabel.filter { it.isDigit() })) score += 2
            slot to score
        }

        val topScore = withScore.maxOfOrNull { it.second } ?: 0
        if (topScore > 0) {
            return withScore.first { it.second == topScore }.first
        }

        if (ordinalIndex != null) {
            return slots.getOrNull(ordinalIndex)
        }

        return slots.firstOrNull()
    }
}
