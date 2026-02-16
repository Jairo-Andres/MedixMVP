package com.medix.mvp.domain.usecase

import com.medix.mvp.domain.model.FlowContext
import com.medix.mvp.domain.model.MedixIntent
import com.medix.mvp.domain.model.Proposal
import com.medix.mvp.domain.model.QuestionSlot
import com.medix.mvp.domain.repository.AppointmentRepository
import com.medix.mvp.presentation.state.ConversationState

data class EngineResult(
    val newState: ConversationState,
    val response: String,
    val expectUserResponse: Boolean,
    val successAction: Boolean = false,
)

class ConversationEngine(
    private val detectIntentUseCase: DetectIntentUseCase,
    private val parseEntitiesUseCase: ParseEntitiesUseCase,
    private val repository: AppointmentRepository,
) {
    private var turn = 0

    fun onStart(): EngineResult {
        val text = ResponseTemplates.pick(ResponseTemplates.saludos, turn++) + " " +
            ResponseTemplates.pick(ResponseTemplates.pedirCedula, turn++)
        return EngineResult(ConversationState.AskCedula, text, true)
    }

    fun processInput(currentState: ConversationState, text: String): EngineResult {
        val intent = detectIntentUseCase.execute(text)
        if (intent == MedixIntent.CAMBIAR_USUARIO) {
            repository.clearActiveUser()
            return EngineResult(
                ConversationState.AskCedula,
                "${ResponseTemplates.pick(ResponseTemplates.despedida, turn++)} Dime la cédula del siguiente usuario.",
                true,
            )
        }
        val cedulaCandidate = Regex("\\d{5,15}").find(text)?.value
        if (currentState is ConversationState.AskCedula || repository.getActiveUser() == null) {
            return if (cedulaCandidate != null) {
                repository.setActiveUser(cedulaCandidate)
                EngineResult(ConversationState.Ready, "Gracias. Usuario activo $cedulaCandidate. ¿Qué deseas hacer?", true)
            } else {
                EngineResult(ConversationState.AskCedula, ResponseTemplates.pick(ResponseTemplates.pedirCedula, turn++), true)
            }
        }

        if (intent == MedixIntent.AYUDA) {
            return EngineResult(currentState, "Puedes decir: agendar cita, cancelar cita, reprogramar o cambiar usuario.", true)
        }

        if (currentState is ConversationState.ConfirmingFlow && intent == MedixIntent.CONFIRMAR) {
            val p = currentState.proposal
            repository.book(p.slot.id, p.slot.doctor, p.slot.lugar)
            repository.setPendingProposal(null)
            return EngineResult(ConversationState.Ready, "Perfecto, tu cita quedó agendada para ${p.slot.fecha} a las ${p.slot.hora} con ${p.slot.doctor} en ${p.slot.lugar}.", true, true)
        }
        if (currentState is ConversationState.ConfirmingFlow && intent == MedixIntent.CANCELAR) {
            return EngineResult(ConversationState.SchedulingFlow(currentState.context), "De acuerdo. ¿Qué dato quieres cambiar: día, hora, doctor o lugar?", true)
        }

        return when (intent) {
            MedixIntent.AGENDAR -> handleScheduling(currentState, text)
            MedixIntent.CANCELAR -> handleCancel()
            MedixIntent.REPROGRAMAR -> handleReschedule(text)
            MedixIntent.CONFIRMAR -> {
                val pending = repository.getPendingProposal()
                if (pending != null) {
                    repository.book(pending.slot.id, pending.slot.doctor, pending.slot.lugar)
                    repository.setPendingProposal(null)
                    EngineResult(ConversationState.Ready, "Listo, confirmé la propuesta y agendé tu cita.", true, true)
                } else {
                    EngineResult(ConversationState.Ready, "No tengo una propuesta pendiente para confirmar.", true)
                }
            }
            else -> EngineResult(currentState, ResponseTemplates.pick(ResponseTemplates.noEntendi, turn++), true)
        }
    }

    private fun handleScheduling(currentState: ConversationState, text: String): EngineResult {
        val entities = parseEntitiesUseCase.execute(text)
        val oldContext = (currentState as? ConversationState.SchedulingFlow)?.context ?: FlowContext(intent = MedixIntent.AGENDAR)
        val ctx = oldContext.copy(
            fecha = entities.fecha ?: oldContext.fecha,
            hora = entities.hora ?: oldContext.hora,
            doctor = entities.doctor ?: oldContext.doctor,
            lugar = entities.lugar ?: oldContext.lugar,
        )
        if (ctx.fecha == null) {
            return EngineResult(ConversationState.SchedulingFlow(ctx.copy(lastQuestionAsked = QuestionSlot.FECHA)), ResponseTemplates.pick(ResponseTemplates.pedirFecha, turn++), true)
        }
        if (ctx.hora == null) {
            return EngineResult(ConversationState.SchedulingFlow(ctx.copy(lastQuestionAsked = QuestionSlot.HORA)), ResponseTemplates.pick(ResponseTemplates.pedirHora, turn++), true)
        }
        if (ctx.doctor == null) {
            return EngineResult(ConversationState.SchedulingFlow(ctx.copy(lastQuestionAsked = QuestionSlot.DOCTOR)), ResponseTemplates.pick(ResponseTemplates.pedirDoctor, turn++), true)
        }
        if (ctx.lugar == null) {
            return EngineResult(ConversationState.SchedulingFlow(ctx.copy(lastQuestionAsked = QuestionSlot.LUGAR)), ResponseTemplates.pick(ResponseTemplates.pedirLugar, turn++), true)
        }

        val options = repository.findAvailability(ctx.fecha, ctx.hora)
            .filter { it.doctor.equals(ctx.doctor, true) || ctx.doctor.contains(it.doctor.substringAfter(" "), true) }
            .filter { it.lugar.equals(ctx.lugar, true) || ctx.lugar.contains(it.lugar.substringAfter(" "), true) }

        val selected = options.firstOrNull() ?: repository.findAvailability(ctx.fecha, ctx.hora).firstOrNull()
        val alternatives = repository.findAvailability(ctx.fecha, null).take(2)

        if (selected == null) {
            return EngineResult(ConversationState.SchedulingFlow(ctx), "No encontré cupos para esa fecha. ¿Quieres intentar otro día?", true)
        }

        val proposal = Proposal(repository.getActiveUser().orEmpty(), selected)
        repository.setPendingProposal(proposal)
        val response = if (options.isNotEmpty()) {
            "Tengo disponibilidad el ${selected.fecha} a las ${selected.hora} con ${selected.doctor} en ${selected.lugar}. ¿Confirmas?"
        } else {
            val alt = alternatives.joinToString(" o ") { "${it.hora}" }
            "Ese horario exacto no está libre. Tengo a las $alt. Propongo ${selected.hora}. ¿Te sirve?"
        }
        return EngineResult(ConversationState.ConfirmingFlow(ctx, proposal), response, true)
    }

    private fun handleCancel(): EngineResult {
        val cedula = repository.getActiveUser().orEmpty()
        val last = repository.getBookedAppointments(cedula).lastOrNull()
            ?: return EngineResult(ConversationState.Ready, "No tienes citas para cancelar.", true)
        repository.cancelLast(cedula)
        return EngineResult(
            ConversationState.Ready,
            "Cancelé tu última cita con ${last.doctor} en ${last.lugar} el ${last.fecha} a las ${last.hora}.",
            true,
            true,
        )
    }

    private fun handleReschedule(text: String): EngineResult {
        val cedula = repository.getActiveUser().orEmpty()
        val appointments = repository.getBookedAppointments(cedula)
        val target = appointments.lastOrNull()
            ?: return EngineResult(ConversationState.Ready, "No tienes citas para reprogramar.", true)
        val entities = parseEntitiesUseCase.execute(text)
        if (entities.fecha == null) return EngineResult(ConversationState.RescheduleFlow(FlowContext(intent = MedixIntent.REPROGRAMAR)), "Claro, ¿para qué día quieres moverla?", true)
        val slots = repository.findAvailability(entities.fecha, entities.hora)
        val selected = slots.firstOrNull() ?: return EngineResult(ConversationState.RescheduleFlow(FlowContext(intent = MedixIntent.REPROGRAMAR)), "No hay cupo en ese horario. ¿Intentamos otra hora?", true)
        repository.reschedule(cedula, target.id, selected.id)
        return EngineResult(ConversationState.Ready, "Listo, moví tu cita para ${selected.fecha} a las ${selected.hora}, ${selected.doctor} en ${selected.lugar}.", true, true)
    }
}
