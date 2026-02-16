package com.medix.mvppro.presentation.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medix.mvppro.presentation.components.ListeningIndicator
import com.medix.mvppro.presentation.state.MedixUiState
import com.medix.mvppro.presentation.state.MedixViewData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewData: MedixViewData,
    onMainButtonClick: () -> Unit,
    onRetry: () -> Unit
) {
    var showMetrics by remember { mutableStateOf(true) }

    Scaffold(topBar = { TopAppBar(title = { Text("Medix MVP Pro") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Medix MVP Pro", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

            Button(onClick = onMainButtonClick, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text(
                    when (viewData.uiState) {
                        is MedixUiState.Listening -> "🛑 Detener"
                        else -> "🎙 Escuchar"
                    }
                )
            }

            when (val state = viewData.uiState) {
                is MedixUiState.Listening -> {
                    ListeningIndicator()
                    Text(state.message)
                }
                is MedixUiState.Processing -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator()
                        Text("Procesando… ${state.finalText}")
                    }
                }
                is MedixUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text("Error (${state.errorCode})", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                        Text(state.message, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text("Sugerencia: ${state.suggestion}", color = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onRetry) { Text("Reintentar") }
                    }
                }
                MedixUiState.Idle -> Unit
            }

            Panel("Usted dijo", viewData.recognizedText.ifBlank { "(Sin texto aún)" })
            Panel("Medix responde", viewData.responseText)
            Panel("Disponibles", viewData.availableAppointments.joinToString("\n") { "• ${it.dateLabel} ${it.timeLabel}" }.ifBlank { "Sin citas" })
            Panel("Agendadas", viewData.bookedAppointments.joinToString("\n") { "• ${it.dateLabel} ${it.timeLabel}" }.ifBlank { "Sin citas" })

            Text(
                text = if (showMetrics) "Ocultar 📊 Métricas (debug)" else "Mostrar 📊 Métricas (debug)",
                modifier = Modifier.clickable { showMetrics = !showMetrics },
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            if (showMetrics) {
                val m = viewData.metricsSummary
                val successPct = if (m.totalInteractions == 0) 0 else (m.successfulInteractions * 100 / m.totalInteractions)
                Panel(
                    "📊 Métricas (debug)",
                    "Interacciones totales: ${m.totalInteractions}\n" +
                        "% éxito: $successPct%\n" +
                        "Errores NO_MATCH: ${m.asrNoMatchCount}\n" +
                        "Promedio tiempo respuesta: ${m.avgResponseTimeMs} ms\n" +
                        "Últimos 5 eventos:\n${m.last5Events.joinToString("\n") { "- $it" }}"
                )
            }
        }
    }
}

@Composable
private fun Panel(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text(content)
    }
}
