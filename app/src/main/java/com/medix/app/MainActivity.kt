package com.medix.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.medix.app.audio.AudioPlayer
import com.medix.app.audio.AudioRecorder
import com.medix.app.data.api.ApiService
import com.medix.app.data.api.WebSocketClient
import com.medix.app.data.repository.VoiceRepository
import com.medix.app.ui.screens.VoiceScreen
import com.medix.app.ui.theme.MedixTheme
import com.medix.app.utils.Constants
import com.medix.app.viewmodel.VoiceViewModel
import com.medix.app.viewmodel.VoiceViewModelFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val voiceViewModel: VoiceViewModel by viewModels {
        VoiceViewModelFactory(
            repository = provideRepository(),
            recorder = AudioRecorder(this),
            player = AudioPlayer(this),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MedixTheme {
                VoiceScreen(viewModel = voiceViewModel)
            }
        }
    }

    private fun provideRepository(): VoiceRepository {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return VoiceRepository(
            apiService = retrofit.create(ApiService::class.java),
            webSocketClient = WebSocketClient(okHttpClient),
        )
    }
}
