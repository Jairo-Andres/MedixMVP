package com.medix.mvpmvp.domain.usecase

import com.medix.mvpmvp.core.text.DateTimeParser
import com.medix.mvpmvp.domain.model.UserIntent

class DetectIntentUseCase(private val parser: DateTimeParser = DateTimeParser()) {
    fun detect(text: String): UserIntent {
        val cleaned = text.trim().lowercase()

        Regex("\\b(\\d{6,12})\\b").find(cleaned)?.let { return UserIntent.ProveerCedula(it.groupValues[1]) }

        if (listOf("cambiar cédula", "cambiar cedula", "otra cédula", "otra cedula").any { cleaned.contains(it) }) {
            return UserIntent.CambiarCedula
        }
        if (cleaned in listOf("sí", "si", "confirmar", "ok", "de acuerdo")) return UserIntent.Confirmar
        if (cleaned.contains("cancelar") || cleaned.contains("anular")) return UserIntent.Cancelar
        if (cleaned.contains("reprogram") || cleaned.contains("mover") || cleaned.contains("cambiar cita")) {
            return UserIntent.Reprogramar(parser.parseDate(cleaned), parser.parseTime(cleaned))
        }
        if (cleaned.contains("mis citas") || cleaned.contains("qué citas") || cleaned.contains("que citas") || cleaned.contains("ver citas")) {
            return UserIntent.Listar
        }
        if (cleaned.contains("ayuda") || cleaned.contains("qué puedes") || cleaned.contains("que puedes")) {
            return UserIntent.Ayuda
        }
        if (cleaned.contains("agendar") || cleaned.contains("cita para") || cleaned.contains("quiero cita")) {
            return UserIntent.Agendar(parser.parseDate(cleaned), parser.parseTime(cleaned))
        }
        parser.parseTime(cleaned)?.let { return UserIntent.SeleccionarHora(it) }
        return UserIntent.Desconocido
    }
}
