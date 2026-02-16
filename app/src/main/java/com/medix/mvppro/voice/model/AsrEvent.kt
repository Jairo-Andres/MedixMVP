package com.medix.mvppro.voice.model

sealed class AsrEvent {
    data class Ready(val message: String) : AsrEvent()
    data class Partial(val text: String) : AsrEvent()
    data class Final(val text: String) : AsrEvent()
    data class State(val listening: Boolean) : AsrEvent()
    data class Error(val asrError: AsrError) : AsrEvent()
}
