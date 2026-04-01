package com.medix.app.data.api

import com.medix.app.data.models.ConversationRequest
import com.medix.app.data.models.ConversationResponse
import com.medix.app.data.models.TranscriptionResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @POST("/conversation")
    suspend fun sendMessage(
        @Body request: ConversationRequest,
    ): ConversationResponse

    @Multipart
    @POST("/asr/transcribe")
    suspend fun transcribeAudio(
        @Part audio: MultipartBody.Part,
    ): TranscriptionResponse
}
