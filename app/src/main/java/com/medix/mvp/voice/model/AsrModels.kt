package com.medix.mvp.voice.model

sealed class AsrError(val message: String) {
    data object Network : AsrError("Problema de red al reconocer voz")
    data object NoMatch : AsrError("No detecté voz clara")
    data object Permission : AsrError("Permiso de micrófono no concedido")
    data object Busy : AsrError("Reconocimiento ocupado")
    data class Unknown(val code: Int) : AsrError("Error ASR código $code")
}
