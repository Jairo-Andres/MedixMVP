package com.example.medixmvp.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.medixmvp.viewmodel.MedixUiState
import java.util.Locale

@Composable
fun MainScreen(
    state: MedixUiState,
    onRecognizedText: (String) -> Unit,
    onSpeak: (String) -> Unit
) {
    val context = LocalContext.current
    var permissionMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                .orEmpty()
            if (text.isNotBlank()) {
                onRecognizedText(text)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionMessage = null
            launchSpeechRecognition(context, speechLauncher::launch)
        } else {
            permissionMessage = "Necesito permiso de micrófono para escuchar su solicitud."
        }
    }

    LaunchedEffect(state.responseText) {
        if (state.responseText.isNotBlank()) {
            onSpeak(state.responseText)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Medix (MVP)",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp
                )
            )

            Button(
                onClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        launchSpeechRecognition(context, speechLauncher::launch)
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Text(text = "🎙 Hablar", fontSize = 24.sp)
            }

            permissionMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 20.sp
                )
            }

            SectionLabel(title = "Usted dijo:")
            Text(
                text = state.recognizedText.ifBlank { "(Esperando su voz...)" },
                fontSize = 24.sp,
                lineHeight = 30.sp
            )

            SectionLabel(title = "Medix responde:")
            Text(
                text = state.responseText,
                fontSize = 24.sp,
                lineHeight = 30.sp
            )

            HorizontalDivider()
            SectionLabel(title = "Citas")
            Text(text = "Disponibles:", fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
            if (state.availableSlots.isEmpty()) {
                Text(text = "- Sin horarios disponibles", fontSize = 20.sp)
            } else {
                state.availableSlots.forEach { slot ->
                    Text(text = "• $slot", fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Agendadas:", fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
            if (state.scheduledAppointments.isEmpty()) {
                Text(text = "- Sin citas agendadas", fontSize = 20.sp)
            } else {
                state.scheduledAppointments.forEach { slot ->
                    Text(text = "• $slot", fontSize = 20.sp)
                }
            }

            if (state.pendingConfirm) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tiene una cita pendiente por confirmar.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp
    )
}

private fun launchSpeechRecognition(
    context: Context,
    onLaunch: (Intent) -> Unit
) {
    val localeTag = when {
        Locale.getAvailableLocales().any { it.toLanguageTag().equals("es-CO", ignoreCase = true) } -> "es-CO"
        else -> "es-ES"
    }

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, localeTag)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga su solicitud médica")
    }
    onLaunch(intent)
}
