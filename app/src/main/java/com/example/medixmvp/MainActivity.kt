package com.example.medixmvp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import com.example.medixmvp.ui.MainScreen
import com.example.medixmvp.viewmodel.MedixViewModel
import com.example.medixmvp.voice.VoiceInteractor

class MainActivity : ComponentActivity() {

    private val viewModel: MedixViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val voiceInteractor = remember { VoiceInteractor(applicationContext) }

            Surface(color = MaterialTheme.colorScheme.background) {
                MainScreen(
                    state = uiState,
                    onRecognizedText = viewModel::onSpeechRecognized,
                    onSpeak = voiceInteractor::speak
                )
            }

            androidx.compose.runtime.DisposableEffect(Unit) {
                onDispose {
                    voiceInteractor.shutdown()
                }
            }
        }
    }
}
