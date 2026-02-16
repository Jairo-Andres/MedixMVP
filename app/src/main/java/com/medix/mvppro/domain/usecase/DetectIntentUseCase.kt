package com.medix.mvppro.domain.usecase

import com.medix.mvppro.domain.model.MedixIntent

class DetectIntentUseCase {
    operator fun invoke(text: String): MedixIntent {
        val normalized = normalizeText(text)
        return when {
            normalized.contains("ayuda") || normalized.contains("que puedo hacer") -> MedixIntent.AYUDA
            normalized.contains("reprogram") || normalized.contains("cambiar") -> MedixIntent.REPROGRAMAR
            normalized.contains("cancel") -> MedixIntent.CANCELAR
            normalized.contains("confirm") -> MedixIntent.CONFIRMAR
            normalized.contains("agendar") || normalized.contains("cita") || normalized.contains("reserv") -> MedixIntent.AGENDAR
            else -> MedixIntent.DESCONOCIDA
        }
    }
}
