package com.medix.mvp.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MedixColors = darkColorScheme()

@Composable
fun MedixTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MedixColors,
        typography = androidx.compose.material3.Typography(),
        content = content,
    )
}
