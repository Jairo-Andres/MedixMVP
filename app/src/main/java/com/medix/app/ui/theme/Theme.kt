package com.medix.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = MedixPrimary,
    onPrimary = MedixOnPrimary,
    background = MedixBackground,
    onBackground = MedixOnBackground,
    surface = MedixSurface,
    error = MedixError,
)

private val DarkColors = darkColorScheme(
    primary = MedixPrimary,
    onPrimary = MedixOnPrimary,
)

@Composable
fun MedixTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content,
    )
}
