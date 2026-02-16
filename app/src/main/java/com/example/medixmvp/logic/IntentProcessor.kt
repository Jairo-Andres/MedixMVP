package com.example.medixmvp.logic

enum class MedixIntent {
    AGENDAR,
    CONFIRMAR,
    CANCELAR,
    REPROGRAMAR,
    DESCONOCIDA
}

object IntentProcessor {
    private val agendarKeywords = listOf("agendar", "programar", "sacar cita", "pedir cita")
    private val confirmarKeywords = listOf("confirmar", "sí confirmo", "si confirmo", "confirmo")
    private val cancelarKeywords = listOf("cancelar", "anular")
    private val reprogramarKeywords = listOf("reprogramar", "cambiar", "mover la cita")

    fun parse(text: String): MedixIntent {
        val normalized = text.trim().lowercase()
        if (normalized.isBlank()) return MedixIntent.DESCONOCIDA

        return when {
            reprogramarKeywords.any { normalized.contains(it) } -> MedixIntent.REPROGRAMAR
            cancelarKeywords.any { normalized.contains(it) } -> MedixIntent.CANCELAR
            confirmarKeywords.any { normalized.contains(it) } -> MedixIntent.CONFIRMAR
            agendarKeywords.any { normalized.contains(it) } -> MedixIntent.AGENDAR
            else -> MedixIntent.DESCONOCIDA
        }
    }
}
