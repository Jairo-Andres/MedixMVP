package com.medix.app.utils

object Constants {
    const val BASE_URL = "http://192.168.20.10:8000"

    fun webSocketUrl(sessionId: String): String =
        "ws://192.168.20.10:8000/ws/conversation/$sessionId"
}
