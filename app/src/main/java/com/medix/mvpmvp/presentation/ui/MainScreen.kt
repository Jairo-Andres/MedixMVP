package com.medix.mvpmvp.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medix.mvpmvp.domain.model.Appointment
import com.medix.mvpmvp.presentation.components.StatusPulse
import com.medix.mvpmvp.presentation.state.MedixUiState
import com.medix.mvpmvp.voice.model.VoiceUiStatus

@Composable
fun MainScreen(
    state: MedixUiState,
    onToggleListen: () -> Unit,
    onChangeCedula: () -> Unit,
    onResetFromFile: () -> Unit,
    onSaveAvailable: (Int?, String, String, String, String) -> Unit,
    onDeleteAvailable: (Int) -> Unit,
    onToggleMetrics: () -> Unit
) {
    val editingId = remember { mutableStateOf<Int?>(null) }
    val date = remember { mutableStateOf("2026-02-20") }
    val time = remember { mutableStateOf("09:00") }
    val doctor = remember { mutableStateOf("") }
    val location = remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPulse(listening = state.status == VoiceUiStatus.Listening)
                Text("Estado: ${state.status}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
        }
        item { CardBlock("Medix dice", state.medixText) }
        item { CardBlock("Usted dijo", state.userText) }
        item {
            Button(onClick = onToggleListen, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.status == VoiceUiStatus.Listening) "🛑 Detener" else "🎙 Escuchar")
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Usuario actual: ${state.activeCedula.ifBlank { "(sin cédula)" }}")
                Button(onClick = onChangeCedula) { Text("Cambiar") }
            }
        }
        item { SectionAppointments("Mis citas", state.booked) }
        item {
            Text("Citas disponibles", style = MaterialTheme.typography.titleLarge)
            Button(onClick = onResetFromFile) { Text("📄 Recargar desde archivo") }
        }
        items(state.available, key = { it.id }) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("${it.dateTimeIso}")
                    Text(it.doctorName)
                    Text(it.location)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            editingId.value = it.id
                            val split = it.dateTimeIso.split("T")
                            date.value = split[0]
                            time.value = split[1].substring(0, 5)
                            doctor.value = it.doctorName
                            location.value = it.location
                        }) { Text("Editar") }
                        Button(onClick = { onDeleteAvailable(it.id) }) { Text("Eliminar") }
                    }
                }
            }
        }
        item {
            Text("➕ Agregar/editar cita", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(date.value, { date.value = it }, label = { Text("Fecha YYYY-MM-DD") })
            OutlinedTextField(time.value, { time.value = it }, label = { Text("Hora HH:MM") })
            OutlinedTextField(doctor.value, { doctor.value = it }, label = { Text("Doctor") })
            OutlinedTextField(location.value, { location.value = it }, label = { Text("Lugar") })
            Button(onClick = {
                onSaveAvailable(editingId.value, date.value, time.value, doctor.value, location.value)
                editingId.value = null
            }) { Text("Guardar") }
        }
        item {
            Button(onClick = onToggleMetrics) { Text("📊 Métricas (debug)") }
            if (state.expandedMetrics) {
                Text("Interacciones: ${state.metrics.interactions}")
                Text("Última respuesta: ${state.metrics.lastResponseMs}ms")
                Text("Errores ASR: ${state.metrics.asrErrors}")
                state.metrics.recentEvents.forEach { Text("• $it") }
            }
        }
    }
}

@Composable
private fun CardBlock(title: String, text: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(text.ifBlank { "..." })
        }
    }
}

@Composable
private fun SectionAppointments(title: String, list: List<Appointment>) {
    Text(title, style = MaterialTheme.typography.titleLarge)
    if (list.isEmpty()) {
        Text("Sin citas")
    } else {
        list.forEach {
            Text("• ${it.dateTimeIso} | ${it.doctorName} | ${it.location}")
        }
    }
}
