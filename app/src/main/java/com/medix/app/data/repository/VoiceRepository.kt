package com.medix.app.data.repository

import com.medix.app.data.api.ApiService
import com.medix.app.data.api.WebSocketClient
import com.medix.app.data.models.ConversationRequest
import com.medix.app.data.models.ConversationResponse
import com.medix.app.utils.Constants
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

class VoiceRepository(
    private val apiService: ApiService,
    private val webSocketClient: WebSocketClient,
) {

    suspend fun transcribeAudio(audioFile: File): String {
        val requestBody = audioFile.asRequestBody("audio/mp4".toMediaType())
        val part = MultipartBody.Part.createFormData("audio", audioFile.name, requestBody)
        return apiService.transcribeAudio(part).text
    }

    suspend fun sendConversationMessage(text: String, sessionId: String): ConversationResponse {
        return apiService.sendMessage(
            ConversationRequest(
                text = text,
                session_id = sessionId,
            ),
        )
    }

    fun connectWebSocket(
        sessionId: String,
        onMessage: (String) -> Unit,
        onStateChanged: (Boolean) -> Unit,
    ) {
        webSocketClient.connect(Constants.webSocketUrl(sessionId), onMessage, onStateChanged)
    }

    fun sendWebSocketMessage(text: String, sessionId: String) {
        val payload = JSONObject()
            .put("text", text)
            .put("session_id", sessionId)
            .toString()

        webSocketClient.send(payload)
    }

    fun closeWebSocket() {
        webSocketClient.close()
    }
}
