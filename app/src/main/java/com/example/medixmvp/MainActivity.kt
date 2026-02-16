package com.example.medixmvp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.medixmvp.ui.MainScreen
import com.example.medixmvp.ui.theme.MedixMVPTheme
import com.example.medixmvp.viewmodel.MedixViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MedixViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MedixMVPTheme {
                MainScreen(
                    recognizedText = viewModel.recognizedText.value,
                    responseText = viewModel.responseText.value,
                    isListening = viewModel.isListening.value,
                    availableAppointments = viewModel.availableAppointments.value,
                    bookedAppointments = viewModel.bookedAppointments.value,
                    onStartListening = viewModel::startListening,
                    onStopListening = viewModel::stopListening
                )
            }
        }
    }
}
