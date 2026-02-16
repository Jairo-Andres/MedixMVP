package com.medix.mvp.core.time

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

interface TimeProvider {
    fun nowDateTime(): LocalDateTime
    fun nowDate(): LocalDate
    fun nowTime(): LocalTime
}

class DefaultTimeProvider : TimeProvider {
    override fun nowDateTime(): LocalDateTime = LocalDateTime.now()
    override fun nowDate(): LocalDate = LocalDate.now()
    override fun nowTime(): LocalTime = LocalTime.now()
}
