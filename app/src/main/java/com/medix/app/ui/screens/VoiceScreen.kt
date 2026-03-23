package com.medix.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.medix.app.ui.components.MicButton
import com.medix.app.viewmodel.ConversationStatus
import com.medix.app.viewmodel.VoiceViewModel

@Composable
fun VoiceScreen(viewModel: VoiceViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasMicPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasMicPermission) {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Medix Voice Assistant",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = statusLabel(state.status),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (state.wsConnected) "WebSocket conectado" else "WebSocket desconectado",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TranscriptCard(
                    title = "Tu mensaje",
                    content = state.userText.ifBlank { "Aún no hay transcripción." },
                )
                TranscriptCard(
                    title = "Asistente Medix",
                    content = state.assistantText,
                )

                state.errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MicButton(
                    isRecording = state.status == ConversationStatus.LISTENING,
                    enabled = hasMicPermission,
                    onRecordStart = { viewModel.startRecording() },
                    onRecordStop = { viewModel.stopRecordingAndSend() },
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (hasMicPermission) {
                        "Mantén presionado para hablar"
                    } else {
                        "Debes conceder permiso de micrófono"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.sendTextByWebSocket("Necesito una cita médica") }) {
                    Text("Probar por WebSocket")
                }
            }
        }
    }
}

@Composable
private fun TranscriptCard(
    title: String,
    content: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(16.dp)
            .semantics { contentDescription = title },
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun statusLabel(status: ConversationStatus): String {
    return when (status) {
        ConversationStatus.IDLE -> "En espera"
        ConversationStatus.LISTENING -> "Escuchando..."
        ConversationStatus.PROCESSING -> "Procesando..."
        ConversationStatus.RESPONDING -> "Respondiendo..."
        ConversationStatus.ERROR -> "Ocurrió un error"
    }
}
