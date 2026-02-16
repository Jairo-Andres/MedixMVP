package com.medix.mvppro.core.time

interface TimeProvider {
    fun nowMs(): Long
}

object SystemTimeProvider : TimeProvider {
    override fun nowMs(): Long = System.currentTimeMillis()
}
