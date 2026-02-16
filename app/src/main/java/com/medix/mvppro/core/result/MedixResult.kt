package com.medix.mvppro.core.result

sealed class MedixResult<out T> {
    data class Success<T>(val data: T) : MedixResult<T>()
    data class Failure(val reason: String) : MedixResult<Nothing>()
}
