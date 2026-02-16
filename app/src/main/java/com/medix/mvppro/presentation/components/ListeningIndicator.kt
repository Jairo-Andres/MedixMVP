package com.medix.mvppro.presentation.components

import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

@Composable
fun ListeningIndicator() {
    val transition = rememberInfiniteTransition(label = "listening")
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(3) { index ->
            val progress = transition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 700, delayMillis = index * 120, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse$index"
            )
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(14.dp)
                    .scale(progress.value)
                    .alpha(progress.value / 1.2f)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}
