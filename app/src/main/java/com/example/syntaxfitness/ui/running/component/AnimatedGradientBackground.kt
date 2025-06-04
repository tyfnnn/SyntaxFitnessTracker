package com.example.syntaxfitness.ui.running.component

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.runtime.getValue

@Composable
fun AnimatedGradientBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    // Multiple animated values for complex movement
    val primaryOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "primaryOffset"
    )

    val secondaryOffset by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "secondaryOffset"
    )

    val tertiaryOffset by infiniteTransition.animateFloat(
        initialValue = 180f,
        targetValue = 540f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tertiaryOffset"
    )

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveOffset"
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingScale"
    )

    Canvas(modifier = Modifier.Companion.fillMaxSize()) {
        drawEnhancedMeshGradient(
            primaryOffset = primaryOffset,
            secondaryOffset = secondaryOffset,
            tertiaryOffset = tertiaryOffset,
            waveOffset = waveOffset,
            breathingScale = breathingScale
        )
    }
}

private fun DrawScope.drawEnhancedMeshGradient(
    primaryOffset: Float,
    secondaryOffset: Float,
    tertiaryOffset: Float,
    waveOffset: Float,
    breathingScale: Float
) {
    val width = size.width
    val height = size.height
    val centerX = width / 2
    val centerY = height / 2

    // Base gradient background
    val baseGradient = Brush.Companion.radialGradient(
        colors = listOf(
            Color(0xFF1A1B3E),  // Deep purple-blue
            Color(0xFF0F0F2A),  // Darker blue
            Color(0xFF000814)   // Almost black
        ),
        center = Offset(centerX, centerY),
        radius = maxOf(width, height) * 0.8f
    )
    drawRect(brush = baseGradient)

    // Multiple gradient orbs with different behaviors
    val orbs = listOf(
        // Large primary orb
        GradientOrb(
            colors = listOf(
                Color(0x4A8B5CF6),  // Purple
                Color(0x3A6366F1),  // Indigo
                Color(0x20000000)   // Transparent
            ),
            center = Offset(
                centerX + cos(Math.toRadians(primaryOffset.toDouble())).toFloat() * width * 0.3f,
                centerY + sin(Math.toRadians(primaryOffset.toDouble())).toFloat() * height * 0.2f
            ),
            radius = 300f * breathingScale
        ),
        // Secondary orb with counter-rotation
        GradientOrb(
            colors = listOf(
                Color(0x4A3B82F6),  // Blue
                Color(0x3A06B6D4),  // Cyan
                Color(0x20000000)
            ),
            center = Offset(
                centerX + cos(Math.toRadians(secondaryOffset.toDouble())).toFloat() * width * 0.4f,
                centerY + sin(Math.toRadians(secondaryOffset.toDouble())).toFloat() * height * 0.3f
            ),
            radius = 250f * (1.5f - breathingScale * 0.5f)
        ),
    )

    // Draw all orbs with blur effect
    orbs.forEach { orb ->
        // Create multiple overlapping circles for blur effect
        val blurLayers = 35
        for (layer in 0 until blurLayers) {
            val layerRadius = orb.radius * (1f + layer * 0.3f)
            val layerAlpha = (blurLayers - layer).toFloat() / blurLayers * 0.9f

            val gradient = Brush.Companion.radialGradient(
                colors = orb.colors.map { color ->
                    color.copy(alpha = color.alpha * layerAlpha)
                },
                center = orb.center,
                radius = layerRadius,
                tileMode = TileMode.Companion.Clamp
            )

            // Irregular shape using multiple offset circles
            for (i in 0..8) {
                val angle = i * 45f
                val offsetDistance = layerRadius * 0.1f * sin(waveOffset * Math.PI + i).toFloat()
                val offsetX = cos(Math.toRadians(angle.toDouble())).toFloat() * offsetDistance
                val offsetY = sin(Math.toRadians(angle.toDouble())).toFloat() * offsetDistance

                drawCircle(
                    brush = gradient,
                    radius = layerRadius * (0.1f + sin(waveOffset * Math.PI + i).toFloat() * 0.1f),
                    center = Offset(orb.center.x + offsetX, orb.center.y + offsetY)
                )
            }
        }
    }

    // Add flowing wave patterns
    drawWavePatterns(
        width = width,
        height = height,
        waveOffset = waveOffset,
        primaryOffset = primaryOffset
    )

    // Add subtle noise texture
    drawNoiseTexture(
        width = width,
        height = height,
        offset = primaryOffset
    )
}

private fun DrawScope.drawWavePatterns(
    width: Float,
    height: Float,
    waveOffset: Float,
    primaryOffset: Float
) {
    val waveHeight = 30f
    val waveLength = width / 3f

    // Create flowing wave lines
    for (i in 0..2) {
        val yPosition = height * (0.2f + i * 0.3f)
        val path = Path().apply {
            moveTo(0f, yPosition)

            var x = 0f
            while (x <= width) {
                val waveY = yPosition +
                        sin(((x / waveLength) + waveOffset + i * 0.5f) * Math.PI * 2).toFloat() * waveHeight *
                        sin((primaryOffset + i * 60f) * Math.PI / 180f).toFloat() * 0.5f
                lineTo(x, waveY)
                x += 10f
            }
        }

        drawPath(
            path = path,
            color = Color(0x15FFFFFF),
            style = Stroke(width = 2f)
        )
    }
}

private fun DrawScope.drawNoiseTexture(
    width: Float,
    height: Float,
    offset: Float
) {
    // Simple procedural noise effect
    val noisePoints = 100
    val random = Random(42) // Fixed seed for consistency

    repeat(noisePoints) { i ->
        val angle = (offset + i * 3.6f) % 360f
        val distance = random.nextFloat() * minOf(width, height) * 0.4f
        val centerX = width / 2
        val centerY = height / 2

        val x = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * distance
        val y = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * distance

        drawCircle(
            color = Color.Companion.White.copy(alpha = 0.02f + random.nextFloat() * 0.03f),
            radius = random.nextFloat() * 3f + 1f,
            center = Offset(x, y)
        )
    }
}

private data class GradientOrb(
    val colors: List<Color>,
    val center: Offset,
    val radius: Float
)