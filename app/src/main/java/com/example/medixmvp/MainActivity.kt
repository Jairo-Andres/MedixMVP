package com.example.medixmvp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
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
            var speechCompletionToken by remember { mutableIntStateOf(0) }
            val updatedOnCompletion by rememberUpdatedState(newValue = {
                runOnUiThread {
                    speechCompletionToken += 1
                }
            })

            LaunchedEffect(voiceInteractor) {
                voiceInteractor.setOnSpeechCompletedListener(updatedOnCompletion)
            }

            Surface(color = MaterialTheme.colorScheme.background) {
                MainScreen(
                    state = uiState,
                    onRecognizedText = viewModel::onSpeechRecognized,
                    onSpeak = voiceInteractor::speak,
                    onPatientIdChange = viewModel::onPatientIdChange,
                    speechCompletionToken = speechCompletionToken
                )
            }

            androidx.compose.runtime.DisposableEffect(Unit) {
                onDispose {
                    voiceInteractor.setOnSpeechCompletedListener(null)
                    voiceInteractor.shutdown()
                }
            }
        }
    }
}
