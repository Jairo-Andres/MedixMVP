package com.medix.app.data.models

import com.google.gson.annotations.SerializedName

data class ConversationRequest(
    val text: String,
    @SerializedName("session_id") val sessionId: String,
)
