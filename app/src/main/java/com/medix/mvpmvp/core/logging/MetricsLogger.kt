package com.medix.mvpmvp.core.logging

import android.util.Log

data class MetricsSummary(
    val interactions: Int,
    val asrErrors: Map<String, Int>,
    val lastResponseMs: Long,
    val recentEvents: List<String>
)

class MetricsLogger {
    private var interactions = 0
    private val asrErrors = mutableMapOf<String, Int>()
    private var listenStartMs: Long = 0L
    private var lastResponseMs: Long = 0L
    private val events = ArrayDeque<String>()

    fun onInteraction() {
        interactions++
        addEvent("Interacción #$interactions")
    }

    fun onAsrError(type: String) {
        asrErrors[type] = (asrErrors[type] ?: 0) + 1
        addEvent("ASR error: $type")
    }

    fun markListeningStart() { listenStartMs = System.currentTimeMillis() }

    fun markTtsDone() {
        if (listenStartMs > 0L) {
            lastResponseMs = System.currentTimeMillis() - listenStartMs
            addEvent("Respuesta en ${lastResponseMs}ms")
        }
    }

    fun addEvent(event: String) {
        if (events.size >= 10) events.removeFirst()
        events.addLast(event)
        Log.d("MedixMetrics", event)
    }

    fun summary(): MetricsSummary = MetricsSummary(
        interactions = interactions,
        asrErrors = asrErrors.toMap(),
        lastResponseMs = lastResponseMs,
        recentEvents = events.toList().reversed()
    )
}
