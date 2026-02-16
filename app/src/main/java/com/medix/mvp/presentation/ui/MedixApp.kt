package com.medix.mvp.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medix.mvp.presentation.components.ListeningIndicator
import com.medix.mvp.presentation.components.SpeakingIndicator
import com.medix.mvp.presentation.state.MedixUiState
import com.medix.mvp.presentation.viewmodel.MedixViewModel

@Composable
fun MedixApp(viewModel: MedixViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val assistantText by viewModel.assistantText.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val metrics by viewModel.metrics.collectAsState()
    val debugOpen = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101114))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("MedixMVP", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Usuario: ${viewModel.getActiveUser() ?: "Sin sesión"}", color = Color.White, fontSize = 24.sp)

        when (uiState) {
            is MedixUiState.Listening -> ListeningIndicator()
            is MedixUiState.Speaking -> SpeakingIndicator()
            else -> Spacer(modifier = Modifier.height(20.dp))
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                assistantText.ifBlank { "Presiona Hablar para empezar" },
                fontSize = 26.sp,
                modifier = Modifier.padding(16.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = viewModel::onMainButtonClick, modifier = Modifier.weight(1f)) {
                Text(
                    when (uiState) {
                        is MedixUiState.Listening -> "🛑 Detener"
                        else -> "🎙 Hablar"
                    },
                    fontSize = 22.sp,
                )
            }
            Button(onClick = viewModel::changeUser, modifier = Modifier.weight(1f)) {
                Text("Cambiar usuario", fontSize = 20.sp)
            }
        }

        Text("Tus citas", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(appointments) { ap ->
                Card {
                    Text(
                        "${ap.fecha} ${ap.hora} · ${ap.doctor} · ${ap.lugar}",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 18.sp,
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().clickable { debugOpen.value = !debugOpen.value }) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Panel debug ${if (debugOpen.value) "▲" else "▼"}", fontWeight = FontWeight.Bold)
                if (debugOpen.value) {
                    Text("Turnos: ${metrics.turns}")
                    Text("Tiempo promedio: ${metrics.avgResponseMs} ms")
                    Text("Repreguntas slots: ${metrics.slotReprompts}")
                    Text("Errores ASR: ${metrics.asrErrors}")
                    Text("Acciones exitosas: ${metrics.successCount}")
                }
            }
        }
    }
}
