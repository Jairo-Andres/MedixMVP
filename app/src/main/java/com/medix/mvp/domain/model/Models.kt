package com.medix.mvp.domain.model

import java.time.LocalDate
import java.time.LocalTime

enum class MedixIntent {
    AGENDAR,
    CONFIRMAR,
    CANCELAR,
    REPROGRAMAR,
    CAMBIAR_USUARIO,
    AYUDA,
    DESCONOCIDA,
}

data class Slot(
    val id: String,
    val fecha: LocalDate,
    val hora: LocalTime,
    val doctor: String,
    val lugar: String,
)

data class Appointment(
    val id: String,
    val cedula: String,
    val slotId: String,
    val fecha: LocalDate,
    val hora: LocalTime,
    val doctor: String,
    val lugar: String,
)

data class Proposal(
    val cedula: String,
    val slot: Slot,
)

data class Entities(
    val fecha: String? = null,
    val hora: String? = null,
    val doctor: String? = null,
    val lugar: String? = null,
)

enum class QuestionSlot {
    FECHA,
    HORA,
    DOCTOR,
    LUGAR,
    NINGUNO,
}

data class FlowContext(
    val fecha: String? = null,
    val hora: String? = null,
    val doctor: String? = null,
    val lugar: String? = null,
    val intent: MedixIntent = MedixIntent.DESCONOCIDA,
    val lastQuestionAsked: QuestionSlot = QuestionSlot.NINGUNO,
)
