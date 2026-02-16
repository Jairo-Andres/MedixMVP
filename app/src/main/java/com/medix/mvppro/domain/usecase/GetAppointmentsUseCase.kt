package com.medix.mvppro.domain.usecase

import com.medix.mvppro.domain.model.AgendaSnapshot
import com.medix.mvppro.domain.repository.AppointmentRepository

class GetAppointmentsUseCase(private val repository: AppointmentRepository) {
    operator fun invoke(): AgendaSnapshot = repository.getSnapshot()
}
