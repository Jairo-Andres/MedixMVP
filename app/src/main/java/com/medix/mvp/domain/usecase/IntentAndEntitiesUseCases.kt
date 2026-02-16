package com.medix.mvp.domain.usecase

import com.medix.mvp.core.time.TimeProvider
import com.medix.mvp.core.util.normalizedSpanish
import com.medix.mvp.domain.model.Entities
import com.medix.mvp.domain.model.MedixIntent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DetectIntentUseCase {
    fun execute(text: String): MedixIntent {
        val t = text.normalizedSpanish()
        return when {
            listOf("cambiar usuario", "otra cedula", "cerrar sesion", "terminar").any { t.contains(it) } -> MedixIntent.CAMBIAR_USUARIO
            listOf("reprogramar", "mover", "cambiar").any { t.contains(it) } -> MedixIntent.REPROGRAMAR
            listOf("cancelar", "anular").any { t.contains(it) } -> MedixIntent.CANCELAR
            listOf("confirmar", "si", "dale", "ok", "de acuerdo").any { t.contains(it) } -> MedixIntent.CONFIRMAR
            listOf("agendar", "programar", "sacar cita", "pedir cita", "cita").any { t.contains(it) } -> MedixIntent.AGENDAR
            listOf("ayuda", "que puedo hacer").any { t.contains(it) } -> MedixIntent.AYUDA
            else -> MedixIntent.DESCONOCIDA
        }
    }
}

class ParseEntitiesUseCase(private val timeProvider: TimeProvider) {
    private val monthMap = mapOf(
        "enero" to 1, "febrero" to 2, "marzo" to 3, "abril" to 4,
        "mayo" to 5, "junio" to 6, "julio" to 7, "agosto" to 8,
        "septiembre" to 9, "octubre" to 10, "noviembre" to 11, "diciembre" to 12,
    )

    fun execute(text: String): Entities {
        val normalized = text.normalizedSpanish()
        return Entities(
            fecha = parseDate(normalized),
            hora = parseHour(normalized),
            doctor = parseDoctor(text),
            lugar = parsePlace(text),
        )
    }

    private fun parseDate(text: String): String? {
        val now = timeProvider.nowDate()
        return when {
            "pasado manana" in text -> now.plusDays(2).toString()
            "manana" in text -> now.plusDays(1).toString()
            "hoy" in text -> now.toString()
            else -> {
                val slash = Regex("(\\d{1,2})[/-](\\d{1,2})").find(text)
                if (slash != null) {
                    val d = slash.groupValues[1].toInt()
                    val m = slash.groupValues[2].toInt()
                    return withYear(d, m).toString()
                }
                val textual = Regex("(\\d{1,2})(?: de)? (enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre)")
                    .find(text)
                if (textual != null) {
                    val d = textual.groupValues[1].toInt()
                    val m = monthMap[textual.groupValues[2]] ?: return null
                    return withYear(d, m).format(DateTimeFormatter.ISO_DATE)
                }
                null
            }
        }
    }

    private fun withYear(day: Int, month: Int): LocalDate {
        val now = timeProvider.nowDate()
        var candidate = LocalDate.of(now.year, month, day)
        if (candidate.isBefore(now)) candidate = candidate.plusYears(1)
        return candidate
    }

    private fun parseHour(text: String): String? {
        val hourWithMinutes = Regex("(\\d{1,2}):(\\d{2})").find(text)
        if (hourWithMinutes != null) return "%02d:%02d".format(hourWithMinutes.groupValues[1].toInt(), hourWithMinutes.groupValues[2].toInt())

        val withAmPm = Regex("(\\d{1,2})\\s?(am|pm)").find(text)
        if (withAmPm != null) {
            val base = withAmPm.groupValues[1].toInt()
            val isPm = withAmPm.groupValues[2] == "pm"
            val hour = when {
                isPm && base < 12 -> base + 12
                !isPm && base == 12 -> 0
                else -> base
            }
            return "%02d:00".format(hour)
        }

        val plain = Regex("a las? (\\d{1,2})").find(text)
        if (plain != null) return "%02d:00".format(plain.groupValues[1].toInt())

        if ("manana" in text && !text.contains("pasado")) return "09:00"
        if ("tarde" in text) return "15:00"
        return null
    }

    private fun parseDoctor(text: String): String? {
        val doctorMatch = Regex("(?:doctor|doctora)\\s+([A-Za-zÁÉÍÓÚáéíóúñÑ]+)", RegexOption.IGNORE_CASE).find(text)
        return doctorMatch?.let { "Doctor ${it.groupValues[1].replaceFirstChar { c -> c.titlecase() }}" }
    }

    private fun parsePlace(text: String): String? {
        val placeMatch = Regex("en (sede [A-Za-zÁÉÍÓÚáéíóúñÑ]+|la clinica [A-Za-zÁÉÍÓÚáéíóúñÑ]+|consultorio \\d+)", RegexOption.IGNORE_CASE).find(text)
        return placeMatch?.groupValues?.get(1)?.replaceFirstChar { c -> c.titlecase() }
    }
}
