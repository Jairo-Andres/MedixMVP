package com.medix.mvp.core.logging

import com.medix.mvp.core.time.TimeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Duration
import java.time.LocalDateTime

data class MetricsSnapshot(
    val turns: Int = 0,
    val avgResponseMs: Long = 0,
    val slotReprompts: Int = 0,
    val asrErrors: Int = 0,
    val successCount: Int = 0,
)

class MetricsLogger(private val timeProvider: TimeProvider) {
    private val _snapshot = MutableStateFlow(MetricsSnapshot())
    val snapshot: StateFlow<MetricsSnapshot> = _snapshot.asStateFlow()

    private val responseDurations = mutableListOf<Long>()
    private var listeningStartedAt: LocalDateTime? = null

    fun markListeningStarted() {
        listeningStartedAt = timeProvider.nowDateTime()
    }

    fun markTurnCompleted() {
        val start = listeningStartedAt ?: return
        val duration = Duration.between(start, timeProvider.nowDateTime()).toMillis().coerceAtLeast(0)
        responseDurations += duration
        val avg = if (responseDurations.isNotEmpty()) responseDurations.average().toLong() else 0L
        _snapshot.value = _snapshot.value.copy(
            turns = _snapshot.value.turns + 1,
            avgResponseMs = avg,
        )
        listeningStartedAt = null
    }

    fun incrementReprompt() {
        _snapshot.value = _snapshot.value.copy(slotReprompts = _snapshot.value.slotReprompts + 1)
    }

    fun incrementAsrError() {
        _snapshot.value = _snapshot.value.copy(asrErrors = _snapshot.value.asrErrors + 1)
    }

    fun incrementSuccess() {
        _snapshot.value = _snapshot.value.copy(successCount = _snapshot.value.successCount + 1)
    }
}
