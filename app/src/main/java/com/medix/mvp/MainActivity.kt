package com.medix.mvp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.medix.mvp.presentation.ui.MedixApp
import com.medix.mvp.presentation.ui.theme.MedixTheme
import com.medix.mvp.presentation.viewmodel.MedixViewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MedixViewModel>()

    private val micPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.attachAsr()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            viewModel.onPermissionResult(true)
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            MedixTheme {
                MedixApp(viewModel = viewModel)
            }
        }
    }
}
