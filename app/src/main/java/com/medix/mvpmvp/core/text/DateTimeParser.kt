package com.medix.mvpmvp.core.text

import com.medix.mvpmvp.core.time.TimeProvider
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DateTimeParser(private val timeProvider: TimeProvider = TimeProvider()) {
    private val monthMap = mapOf(
        "enero" to 1, "febrero" to 2, "marzo" to 3, "abril" to 4,
        "mayo" to 5, "junio" to 6, "julio" to 7, "agosto" to 8,
        "septiembre" to 9, "setiembre" to 9, "octubre" to 10,
        "noviembre" to 11, "diciembre" to 12
    )

    fun parseDate(text: String): LocalDate? {
        val cleaned = text.lowercase().trim()
        runCatching { return LocalDate.parse(cleaned, DateTimeFormatter.ISO_LOCAL_DATE) }

        Regex("(\\d{1,2})/(\\d{1,2})/(\\d{4})").find(cleaned)?.let {
            val (d, m, y) = it.destructured
            return LocalDate.of(y.toInt(), m.toInt(), d.toInt())
        }

        Regex("(\\d{1,2})\\s+de\\s+([a-záéíóú]+)").find(cleaned)?.let {
            val day = it.groupValues[1].toInt()
            val month = monthMap[it.groupValues[2]] ?: return null
            return LocalDate.of(timeProvider.today().year, month, day)
        }

        Regex("\\bel\\s+(\\d{1,2})\\b").find(cleaned)?.let {
            val day = it.groupValues[1].toInt()
            val now = timeProvider.today()
            return runCatching { LocalDate.of(now.year, now.monthValue, day) }.getOrNull()
        }

        if (cleaned.contains("mañana")) return timeProvider.today().plusDays(1)
        return null
    }

    fun parseTime(text: String): LocalTime? {
        val cleaned = text.lowercase().trim()
        Regex("(?:a\\s+las\\s+)?(\\d{1,2}):(\\d{2})").find(cleaned)?.let {
            val hour = it.groupValues[1].toInt()
            val minute = it.groupValues[2].toInt()
            return LocalTime.of(hour, minute)
        }
        Regex("a\\s+las\\s+(\\d{1,2})\\b").find(cleaned)?.let {
            return LocalTime.of(it.groupValues[1].toInt(), 0)
        }
        return null
    }
}
