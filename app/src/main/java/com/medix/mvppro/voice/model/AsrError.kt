package com.medix.mvppro.voice.model

enum class AsrErrorType {
    NETWORK,
    NO_MATCH,
    PERMISSION,
    BUSY,
    CLIENT,
    TIMEOUT,
    SERVER,
    UNKNOWN
}

data class AsrError(
    val code: Int,
    val type: AsrErrorType,
    val userMessage: String,
    val suggestion: String
)
