package com.medix.mvppro.core.logging

import com.medix.mvppro.core.time.SystemTimeProvider
import com.medix.mvppro.core.time.TimeProvider

data class MetricsSummary(
    val totalInteractions: Int,
    val successfulInteractions: Int,
    val asrNoMatchCount: Int,
    val avgResponseTimeMs: Long,
    val last5Events: List<String>
)

class MetricsLogger(
    private val timeProvider: TimeProvider = SystemTimeProvider
) {
    private val events = mutableListOf<String>()
    private val responseTimes = mutableListOf<Long>()

    private var totalInteractions = 0
    private var successfulInteractions = 0
    private var asrNoMatchCount = 0
    private var partialCount = 0
    private var listeningStartedAtMs: Long? = null

    fun sessionStarted() = add("SessionStarted")
    fun permission(granted: Boolean) = add(if (granted) "PermissionGranted" else "PermissionDenied")
    fun listeningStarted() {
        listeningStartedAtMs = timeProvider.nowMs()
        totalInteractions++
        add("ListeningStarted")
    }
    fun listeningStopped() = add("ListeningStopped")
    fun asrPartialReceived() {
        partialCount++
        add("AsrPartialReceived#$partialCount")
    }
    fun asrFinalReceived(text: String) = add("AsrFinalReceived(len=${text.length})")
    fun intentDetected(intent: String) = add("IntentDetected($intent)")
    fun actionExecuted(action: String, success: Boolean) {
        if (success) successfulInteractions++
        add("ActionExecuted($action,success=$success)")
    }
    fun errorOccurred(type: String, code: Int) {
        if (type == "NO_MATCH") asrNoMatchCount++
        add("ErrorOccurred(type=$type,code=$code)")
    }
    fun ttsSpoken(textLength: Int) {
        add("TtsSpoken(len=$textLength)")
        listeningStartedAtMs?.let {
            responseTimes += (timeProvider.nowMs() - it)
            listeningStartedAtMs = null
        }
    }

    private fun add(event: String) {
        events += "${timeProvider.nowMs()}: $event"
    }

    fun summary(): MetricsSummary {
        val avg = if (responseTimes.isEmpty()) 0L else responseTimes.average().toLong()
        return MetricsSummary(
            totalInteractions = totalInteractions,
            successfulInteractions = successfulInteractions,
            asrNoMatchCount = asrNoMatchCount,
            avgResponseTimeMs = avg,
            last5Events = events.takeLast(5).reversed()
        )
    }
}
