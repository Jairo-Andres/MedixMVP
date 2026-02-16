package com.medix.mvppro.domain.usecase

import com.medix.mvppro.domain.repository.AppointmentRepository

class ConfirmPendingUseCase(private val repository: AppointmentRepository) {
    operator fun invoke(): String = repository.confirmPending()
}
