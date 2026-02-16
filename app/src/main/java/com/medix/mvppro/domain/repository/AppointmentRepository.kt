package com.medix.mvppro.domain.repository

import com.medix.mvppro.domain.model.AgendaSnapshot

interface AppointmentRepository {
    fun getSnapshot(): AgendaSnapshot
    fun proposeNext(): String
    fun confirmPending(): String
    fun cancelLast(): String
    fun reschedule(): String
}
