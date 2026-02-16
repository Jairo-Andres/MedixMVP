package com.example.medixmvp.logic

enum class MedixIntent {
    AGENDAR,
    CONFIRMAR,
    CANCELAR,
    REPROGRAMAR,
    DESCONOCIDA
}

fun detectIntent(text: String): MedixIntent {
    val normalized = text.trim().lowercase()
    if (normalized.isBlank()) return MedixIntent.DESCONOCIDA

    val agendarKeywords = listOf("agendar", "programar", "sacar cita", "pedir cita")
    val confirmarKeywords = listOf("confirmar", "confirmo", "sí", "si")
    val cancelarKeywords = listOf("cancelar", "anular")
    val reprogramarKeywords = listOf("reprogramar", "cambiar", "mover")

    return when {
        agendarKeywords.any { normalized.contains(it) } -> MedixIntent.AGENDAR
        confirmarKeywords.any { normalized == it || normalized.contains("$it ") || normalized.contains(" $it") } -> MedixIntent.CONFIRMAR
        cancelarKeywords.any { normalized.contains(it) } -> MedixIntent.CANCELAR
        reprogramarKeywords.any { normalized.contains(it) } -> MedixIntent.REPROGRAMAR
        else -> MedixIntent.DESCONOCIDA
    }
}
