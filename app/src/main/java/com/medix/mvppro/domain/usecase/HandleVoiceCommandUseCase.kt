package com.medix.mvppro.domain.usecase

import com.medix.mvppro.domain.model.MedixIntent

data class VoiceCommandResult(
    val intent: MedixIntent,
    val responseText: String,
    val actionSuccess: Boolean
)

class HandleVoiceCommandUseCase(
    private val detectIntentUseCase: DetectIntentUseCase,
    private val proposeNextUseCase: ProposeNextUseCase,
    private val confirmPendingUseCase: ConfirmPendingUseCase,
    private val cancelLastUseCase: CancelLastUseCase,
    private val rescheduleUseCase: RescheduleUseCase
) {
    operator fun invoke(text: String): VoiceCommandResult {
        val intent = detectIntentUseCase(text)
        val response = when (intent) {
            MedixIntent.AGENDAR -> proposeNextUseCase(text)
            MedixIntent.CONFIRMAR -> confirmPendingUseCase()
            MedixIntent.CANCELAR -> cancelLastUseCase()
            MedixIntent.REPROGRAMAR -> rescheduleUseCase(text)
            MedixIntent.AYUDA -> "Puedes decir: agendar cita, confirmar, cancelar o reprogramar."
            MedixIntent.DESCONOCIDA -> "No te entendí bien. Di ayuda para escuchar opciones."
        }
        val success = intent != MedixIntent.DESCONOCIDA
        return VoiceCommandResult(intent, response, success)
    }
}
