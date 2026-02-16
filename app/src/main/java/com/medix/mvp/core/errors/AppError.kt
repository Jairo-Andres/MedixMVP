package com.medix.mvp.core.errors

sealed class AppError(open val message: String) {
    data class PermissionDenied(override val message: String) : AppError(message)
    data class AsrFailure(override val message: String) : AppError(message)
    data class Unknown(override val message: String) : AppError(message)
}
