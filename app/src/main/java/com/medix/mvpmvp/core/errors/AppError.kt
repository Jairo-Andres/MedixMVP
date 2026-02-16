package com.medix.mvpmvp.core.errors

sealed class AppError(val message: String) {
    data object MissingMicrophonePermission : AppError("No tengo permiso de micrófono.")
    data class AsrError(val code: Int) : AppError("Error de reconocimiento de voz: $code")
    data object Unknown : AppError("Ha ocurrido un error inesperado.")
}
