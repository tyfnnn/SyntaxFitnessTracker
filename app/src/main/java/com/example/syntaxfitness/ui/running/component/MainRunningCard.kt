package com.example.syntaxfitness.ui.running.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue

@Composable
fun MainRunningCard(
    isRunning: Boolean,
    isGettingLocation: Boolean,
    statusMessage: String,
    onToggleRun: () -> Unit
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (isRunning) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "button_scale"
    )

    val pulseInfinite = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseInfinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Companion.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.Companion
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            // Status text
            Text(
                text = statusMessage,
                fontSize = 16.sp,
                color = when {
                    isRunning -> Color(0xFF10B981)
                    isGettingLocation -> Color(0xFFF59E0B)
                    else -> Color.Companion.White.copy(alpha = 0.8f)
                },
                textAlign = TextAlign.Companion.Center,
                modifier = Modifier.Companion.padding(bottom = 24.dp)
            )

            // Main action button
            Box(
                contentAlignment = Alignment.Companion.Center
            ) {
                // Pulse ring for running state
                if (isRunning) {
                    Box(
                        modifier = Modifier.Companion
                            .size(160.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Color(0xFF10B981).copy(alpha = 0.3f)
                            )
                    )
                }

                FloatingActionButton(
                    onClick = onToggleRun,
                    modifier = Modifier.Companion
                        .size(120.dp)
                        .scale(buttonScale),
                    containerColor = if (isRunning) {
                        Color(0xFFEF4444)
                    } else {
                        Color(0xFF10B981)
                    },
                    shape = CircleShape
                ) {
                    if (isGettingLocation) {
                        CircularProgressIndicator(
                            color = Color.Companion.White,
                            modifier = Modifier.Companion.size(32.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Companion.White,
                            modifier = Modifier.Companion.size(40.dp)
                        )
                    }
                }
            }
        }
    }
}