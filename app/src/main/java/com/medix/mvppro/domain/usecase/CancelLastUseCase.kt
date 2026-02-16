package com.medix.mvppro.domain.usecase

import com.medix.mvppro.domain.repository.AppointmentRepository

class CancelLastUseCase(private val repository: AppointmentRepository) {
    operator fun invoke(): String = repository.cancelLast()
}
