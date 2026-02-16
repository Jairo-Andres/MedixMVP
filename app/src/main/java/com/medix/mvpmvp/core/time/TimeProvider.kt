package com.medix.mvpmvp.core.time

import java.time.LocalDate
import java.time.LocalDateTime

class TimeProvider {
    fun now(): LocalDateTime = LocalDateTime.now()
    fun today(): LocalDate = LocalDate.now()
}
