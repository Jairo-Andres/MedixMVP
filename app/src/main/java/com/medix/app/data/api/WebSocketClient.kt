package com.medix.app.data.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class WebSocketClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build(),
) {

    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var latestUrl: String? = null

    fun connect(
        url: String,
        onMessage: (String) -> Unit,
        onStateChanged: (Boolean) -> Unit,
    ) {
        latestUrl = url
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    onStateChanged(true)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    onMessage(text)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    onStateChanged(false)
                    webSocket.close(code, reason)
                    scheduleReconnect(onMessage, onStateChanged)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    onStateChanged(false)
                    scheduleReconnect(onMessage, onStateChanged)
                }
            },
        )
    }

    fun send(text: String): Boolean = webSocket?.send(text) ?: false

    fun close() {
        reconnectJob?.cancel()
        reconnectJob = null
        webSocket?.close(1000, "Closed by client")
        webSocket = null
    }

    private fun scheduleReconnect(
        onMessage: (String) -> Unit,
        onStateChanged: (Boolean) -> Unit,
    ) {
        if (reconnectJob?.isActive == true) return
        val url = latestUrl ?: return

        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            delay(RECONNECT_DELAY_MS)
            connect(url, onMessage, onStateChanged)
        }
    }

    private companion object {
        const val RECONNECT_DELAY_MS = 2_500L
    }
}
