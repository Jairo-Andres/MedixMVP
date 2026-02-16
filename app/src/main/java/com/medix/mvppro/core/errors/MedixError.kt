package com.medix.mvppro.core.errors

data class MedixError(
    val code: Int,
    val message: String,
    val suggestion: String
)
