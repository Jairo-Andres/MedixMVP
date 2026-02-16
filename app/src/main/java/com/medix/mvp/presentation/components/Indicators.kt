package com.medix.mvp.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp

@Composable
fun ListeningIndicator() {
    val transition = rememberInfiniteTransition(label = "listening")
    val scale = transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(700), repeatMode = RepeatMode.Reverse),
        label = "pulse"
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) {
            androidx.compose.foundation.layout.Box(
                Modifier
                    .size(14.dp)
                    .scale(scale.value)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

@Composable
fun SpeakingIndicator() {
    val transition = rememberInfiniteTransition(label = "speaking")
    val scale = transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(300), repeatMode = RepeatMode.Reverse),
        label = "speak"
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.scale(scale.value), tint = Color.Cyan)
        Text("Hablando...", color = Color.Cyan)
    }
}
