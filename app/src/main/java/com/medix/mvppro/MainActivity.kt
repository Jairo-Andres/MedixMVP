package com.medix.mvppro

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.medix.mvppro.presentation.state.MedixUiState
import com.medix.mvppro.presentation.ui.MainScreen
import com.medix.mvppro.presentation.viewmodel.MedixViewModel
import com.medix.mvppro.ui.theme.MedixMVPTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MedixViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
        if (granted) viewModel.startListening()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.viewData.collectAsState()
            MedixMVPTheme {
                MainScreen(
                    viewData = state,
                    onMainButtonClick = {
                        when (state.uiState) {
                            is MedixUiState.Listening -> viewModel.stopListening()
                            else -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onRetry = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
            }
        }
    }
}
