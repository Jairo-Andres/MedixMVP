package com.medix.mvppro.domain.usecase

import com.medix.mvppro.domain.repository.AppointmentRepository

class RescheduleUseCase(private val repository: AppointmentRepository) {
    operator fun invoke(): String = repository.reschedule()
}
