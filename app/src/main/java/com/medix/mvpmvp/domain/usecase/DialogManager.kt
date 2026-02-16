package com.medix.mvpmvp.domain.usecase

import com.medix.mvpmvp.domain.model.Appointment
import com.medix.mvpmvp.domain.model.DialogState
import com.medix.mvpmvp.domain.model.SchedulingContext
import com.medix.mvpmvp.domain.model.UserIntent
import com.medix.mvpmvp.domain.repository.AppointmentRepository
import java.time.LocalDateTime

class DialogManager(
    private val repository: AppointmentRepository,
    private val intentDetector: DetectIntentUseCase = DetectIntentUseCase()
) {
    var state: DialogState = DialogState.Boot
        private set
    var activeCedula: String? = null
        private set

    fun start(): String {
        state = DialogState.AskingCedula
        return "Hola, soy Medix. Para empezar, dime tu cédula."
    }

    fun process(input: String): String {
        val intent = intentDetector.detect(input)
        return when (state) {
            DialogState.AskingCedula -> handleCedula(intent)
            is DialogState.Scheduling -> handleScheduling(intent)
            DialogState.Ready -> handleReady(intent)
            DialogState.Cancelling -> handleCancelling(intent)
            DialogState.Rescheduling -> handleRescheduling(intent)
            DialogState.Boot -> start()
            is DialogState.ErrorState -> {
                state = DialogState.Ready
                "Recuperado. ¿Qué deseas hacer ahora?"
            }
        }
    }

    fun requiresInput(): Boolean = when (state) {
        DialogState.AskingCedula, DialogState.Ready, is DialogState.Scheduling, DialogState.Cancelling, DialogState.Rescheduling -> true
        DialogState.Boot, is DialogState.ErrorState -> false
    }

    private fun handleCedula(intent: UserIntent): String {
        return if (intent is UserIntent.ProveerCedula) {
            activeCedula = intent.cedula
            state = DialogState.Ready
            "Perfecto, cédula ${intent.cedula} activa. Puedes decir agendar cita, mis citas, cancelar o ayuda."
        } else {
            "Necesito tu cédula para continuar."
        }
    }

    private fun handleReady(intent: UserIntent): String {
        if (activeCedula == null) {
            state = DialogState.AskingCedula
            return "Dime tu cédula por favor."
        }
        return when (intent) {
            is UserIntent.Agendar -> {
                state = DialogState.Scheduling(SchedulingContext(requestedDate = intent.date))
                schedule(intent.date, intent.time)
            }
            UserIntent.Listar -> listMine()
            UserIntent.CambiarCedula -> {
                activeCedula = null
                state = DialogState.AskingCedula
                "Claro, cambiemos de usuario. Dime la nueva cédula."
            }
            UserIntent.Cancelar -> {
                state = DialogState.Cancelling
                "¿Confirmas que deseas cancelar tu próxima cita?"
            }
            is UserIntent.Reprogramar -> {
                state = DialogState.Rescheduling
                reprogram(intent.date, intent.time)
            }
            UserIntent.Ayuda -> "Puedo agendar, listar, cancelar o reprogramar citas. También puedes decir cambiar cédula."
            else -> "No te entendí bien. Puedes decir ayuda para ver opciones."
        }
    }

    private fun schedule(date: java.time.LocalDate?, time: java.time.LocalTime?): String {
        if (date == null) {
            state = DialogState.Scheduling(SchedulingContext())
            return "¿Para qué fecha la necesitas?"
        }
        if (time == null) {
            val options = repository.findAvailableByDate(date).map {
                LocalDateTime.parse(it.dateTimeIso).toLocalTime().toString()
            }
            state = DialogState.Scheduling(SchedulingContext(requestedDate = date, requestedHourOptions = options))
            return if (options.isNotEmpty()) "¿En qué hora la necesitas? Tengo ${options.joinToString(" o ")}." else "¿En qué hora la necesitas?"
        }
        val iso = LocalDateTime.of(date, time).toString()
        val exact = repository.findAvailableExact(iso)
        return if (exact != null) {
            state = DialogState.Scheduling(SchedulingContext(requestedDate = date, pendingAppointmentId = exact.id))
            "¿Confirmas la cita con ${exact.doctorName} en ${exact.location} a las ${time}?"
        } else {
            val dayOptions = repository.findAvailableByDate(date)
            val hourOptions = dayOptions.map { LocalDateTime.parse(it.dateTimeIso).toLocalTime().toString() }
            state = DialogState.Scheduling(SchedulingContext(requestedDate = date, requestedHourOptions = hourOptions))
            if (hourOptions.isEmpty()) "No tengo cupos ese día. ¿Deseas otra fecha?" else "No tengo esa hora. Tengo disponible a las ${hourOptions.joinToString(" o ")}. ¿Cuál prefieres?"
        }
    }

    private fun handleScheduling(intent: UserIntent): String {
        val current = state as DialogState.Scheduling
        return when (intent) {
            is UserIntent.Confirmar -> {
                val id = current.context.pendingAppointmentId
                if (id != null && activeCedula != null) {
                    val booked = repository.book(id, activeCedula!!)
                    state = DialogState.Ready
                    if (booked != null) "Tu cita quedó agendada." else "No pude agendar, intenta de nuevo."
                } else "Aún no tengo una cita para confirmar."
            }
            is UserIntent.Cancelar -> {
                state = DialogState.Ready
                "Listo, no se agenda nada."
            }
            is UserIntent.SeleccionarHora -> {
                schedule(current.context.requestedDate, intent.time)
            }
            is UserIntent.Agendar -> schedule(intent.date ?: current.context.requestedDate, intent.time)
            else -> "Para continuar, dime una hora o confirma."
        }
    }

    private fun handleCancelling(intent: UserIntent): String {
        return if (intent is UserIntent.Confirmar && activeCedula != null) {
            val cancelled = repository.cancelByCedula(activeCedula!!)
            state = DialogState.Ready
            if (cancelled != null) "Tu cita fue cancelada." else "No encontré citas para cancelar."
        } else {
            state = DialogState.Ready
            "Cancelación abortada."
        }
    }

    private fun handleRescheduling(intent: UserIntent): String {
        val reIntent = intent as? UserIntent.Reprogramar ?: return "Dime la nueva fecha y hora para reprogramar."
        if (reIntent.date == null || reIntent.time == null || activeCedula == null) {
            return "Necesito fecha y hora completas para reprogramar."
        }
        val iso = LocalDateTime.of(reIntent.date, reIntent.time).toString()
        val available = repository.findAvailableExact(iso)
        state = DialogState.Ready
        return if (available != null) {
            val result = repository.reprogramByCedula(activeCedula!!, available.id)
            if (result != null) "Listo, tu cita quedó reprogramada." else "No pude reprogramar tu cita."
        } else "No hay disponibilidad exacta en esa hora."
    }

    private fun listMine(): String {
        val cedula = activeCedula ?: return "Primero dime tu cédula."
        val list = repository.getBookedByCedula(cedula)
        if (list.isEmpty()) return "No tienes citas agendadas."
        return list.joinToString(prefix = "Tienes: ", separator = ". ") {
            val dt = LocalDateTime.parse(it.dateTimeIso)
            "${dt.toLocalDate()} a las ${dt.toLocalTime()} con ${it.doctorName} en ${it.location}"
        }
    }

    fun currentAppointments(): List<Appointment> = repository.getAll()
}
