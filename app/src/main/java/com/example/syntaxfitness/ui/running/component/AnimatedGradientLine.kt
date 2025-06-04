package com.example.syntaxfitness.ui.running.component

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue

@Composable
fun AnimatedGradientLine() {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_line")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress"
    )

    Box(
        modifier = Modifier.Companion
            .width(212.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(
                brush = Brush.Companion.horizontalGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = animatedProgress),
                        Color(0xFF3B82F6),
                        Color(0xFF06B6D4).copy(alpha = animatedProgress)
                    )
                )
            )
    )
}