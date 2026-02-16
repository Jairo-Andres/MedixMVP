package com.medix.mvpmvp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.medix.mvpmvp.core.storage.JsonStorage
import com.medix.mvpmvp.data.datasource.AppointmentLocalDataSource
import com.medix.mvpmvp.data.repository.AppointmentRepositoryImpl
import com.medix.mvpmvp.domain.usecase.DialogManager
import com.medix.mvpmvp.presentation.ui.MainScreen
import com.medix.mvpmvp.presentation.viewmodel.MedixViewModel
import com.medix.mvpmvp.voice.asr.SpeechRecognizerManager
import com.medix.mvpmvp.voice.model.VoiceUiStatus
import com.medix.mvpmvp.voice.tts.TtsManager

class MainActivity : ComponentActivity() {

    private lateinit var asrManager: SpeechRecognizerManager
    private lateinit var ttsManager: TtsManager
    private var isSpeaking = false

    private val viewModel by viewModels<MedixViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val storage = JsonStorage(this@MainActivity)
                val ds = AppointmentLocalDataSource(this@MainActivity, storage)
                val repo = AppointmentRepositoryImpl(ds)
                @Suppress("UNCHECKED_CAST")
                return MedixViewModel(repo, DialogManager(repo)) as T
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            viewModel.boot()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        asrManager = SpeechRecognizerManager(
            context = this,
            onPartial = { if (!isSpeaking) viewModel.onAsrPartial(it) },
            onFinal = { if (!isSpeaking) viewModel.onAsrFinal(it) },
            onError = { viewModel.onAsrError(it) }
        )

        ttsManager = TtsManager(this) {
            Handler(Looper.getMainLooper()).post {
                isSpeaking = false
                viewModel.onTtsDone()
                if (viewModel.shouldAutoRestartListening()) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        asrManager.startListening()
                        viewModel.onListeningStart()
                    }, 300)
                }
            }
        }

        ensurePermissionAndBoot()

        setContent {
            val state by viewModel.uiState.collectAsState()
            val speak by viewModel.speakQueue.collectAsState()

            LaunchedEffect(speak) {
                if (!speak.isNullOrBlank()) {
                    asrManager.stopListening()
                    isSpeaking = true
                    ttsManager.speak(speak!!)
                    viewModel.consumeSpeakQueue()
                }
            }

            MainScreen(
                state = state,
                onToggleListen = {
                    if (state.status == VoiceUiStatus.Listening) {
                        asrManager.stopListening()
                    } else {
                        asrManager.startListening()
                        viewModel.onListeningStart()
                    }
                },
                onChangeCedula = {
                    viewModel.onAsrFinal("cambiar cédula")
                },
                onResetFromFile = { viewModel.resetAvailableFromFile() },
                onSaveAvailable = viewModel::addOrUpdateAvailable,
                onDeleteAvailable = viewModel::deleteAvailable,
                onToggleMetrics = viewModel::toggleMetrics
            )
        }
    }

    private fun ensurePermissionAndBoot() {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.boot() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    override fun onDestroy() {
        asrManager.destroy()
        ttsManager.shutdown()
        super.onDestroy()
    }
}
