package com.example.medixmvp.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.medixmvp.data.model.Appointment

@Composable
fun MainScreen(
    recognizedText: String,
    responseText: String,
    isListening: Boolean,
    availableAppointments: List<Appointment>,
    bookedAppointments: List<Appointment>,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit
) {
    val context = LocalContext.current
    var permissionMessage by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionMessage = ""
            onStartListening()
        } else {
            permissionMessage = "Permiso de micrófono no concedido. No puedo iniciar SpeechRecognizer."
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Medix MVP",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Button(
                onClick = {
                    if (isListening) {
                        onStopListening()
                        return@Button
                    }

                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        onStartListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (isListening) "🛑 Detener" else "🎙 Escuchar",
                    fontSize = 20.sp
                )
            }

            if (permissionMessage.isNotBlank()) {
                Text(
                    text = permissionMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 18.sp
                )
            }

            Text(text = "Usted dijo:", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = if (recognizedText.isBlank()) "(Aún no hay texto reconocido)" else recognizedText,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(text = "Medix responde:", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(
                text = responseText,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(text = "Citas disponibles", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (availableAppointments.isEmpty()) {
                Text(text = "Sin horarios disponibles.", fontSize = 18.sp)
            } else {
                availableAppointments.forEach { appointment ->
                    Text(
                        text = "• ${appointment.fecha} - ${appointment.hora} (${appointment.estado})",
                        fontSize = 18.sp
                    )
                }
            }

            Text(text = "Citas agendadas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (bookedAppointments.isEmpty()) {
                Text(text = "Sin citas agendadas.", fontSize = 18.sp)
            } else {
                bookedAppointments.forEach { appointment ->
                    Text(
                        text = "• ${appointment.fecha} - ${appointment.hora} (${appointment.estado})",
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
