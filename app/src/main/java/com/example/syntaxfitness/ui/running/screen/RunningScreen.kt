@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.syntaxfitness.ui.running.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.syntaxfitness.ui.running.viewmodel.RunningViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RunningScreen(
    modifier: Modifier = Modifier,
    viewModel: RunningViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkAllPermissions(context)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        viewModel.updateLocationPermission(fineLocationGranted, coarseLocationGranted)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
            viewModel.updateNotificationPermission(notificationGranted)
        }

        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.toggleRunning(context)
        }
    }

    fun requestPermissionsIfNeeded() {
        val shouldShowLocationRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            context as androidx.activity.ComponentActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val shouldShowNotificationRationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                context as androidx.activity.ComponentActivity,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else false

        when {
            shouldShowLocationRationale -> {
                viewModel.setPermissionRationaleDialogVisibility(true)
            }
            shouldShowNotificationRationale -> {
                viewModel.setNotificationRationaleDialogVisibility(true)
            }
            else -> {
                val permissionsToRequest = viewModel.getMissingPermissions()
                if (permissionsToRequest.isNotEmpty()) {
                    requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                }
            }
        }
    }

    // Permission dialogs
    if (uiState.showPermissionRationaleDialog) {
        GlassmorphismDialog(
            title = "Standortberechtigung erforderlich",
            text = "Diese App benötigt Zugriff auf Ihren Standort, um Ihre Laufstrecke zu verfolgen.",
            onConfirm = {
                viewModel.setPermissionRationaleDialogVisibility(false)
                val permissionsToRequest = viewModel.getMissingPermissions()
                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            },
            onDismiss = { viewModel.setPermissionRationaleDialogVisibility(false) }
        )
    }

    if (uiState.showNotificationRationaleDialog) {
        GlassmorphismDialog(
            title = "Benachrichtigungsberechtigung",
            text = "Diese App möchte Ihnen Benachrichtigungen über Ihre Läufe senden.",
            onConfirm = {
                viewModel.setNotificationRationaleDialogVisibility(false)
                val permissionsToRequest = viewModel.getMissingPermissions()
                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            },
            onDismiss = {
                viewModel.setNotificationRationaleDialogVisibility(false)
                if (uiState.hasLocationPermission) {
                    viewModel.toggleRunning(context)
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Animated gradient background
        AnimatedGradientBackground()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header with animated gradient line
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
                ) {
                    Text(
                        text = "SyntaxFitness",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    AnimatedGradientLine()
                }
            }

            // Statistics cards
            if (uiState.totalRuns > 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Läufe",
                            value = "${uiState.totalRuns}",
                            icon = Icons.AutoMirrored.Filled.DirectionsRun,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Gesamt",
                            value = "${String.format("%.1f", uiState.totalDistance)}km",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Main running control card
            item {
                MainRunningCard(
                    isRunning = uiState.isRunning,
                    isGettingLocation = uiState.isGettingLocation,
                    statusMessage = uiState.statusMessage,
                    onToggleRun = {
                        if (!uiState.hasLocationPermission) {
                            requestPermissionsIfNeeded()
                        } else {
                            viewModel.toggleRunning(context)
                        }
                    }
                )
            }

            // Location cards
            item {
                LocationCardsSection(
                    startLat = uiState.startLatitude,
                    startLng = uiState.startLongitude,
                    endLat = uiState.endLatitude,
                    endLng = uiState.endLongitude
                )
            }

            // Distance display
            val distance = viewModel.calculateDistance()
            if (distance != null) {
                item {
                    DistanceCard(distance = distance)
                }
            }

            // Run history
            if (uiState.runHistory.isNotEmpty()) {
                item {
                    Text(
                        text = "Lauf-Historie",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                itemsIndexed(
                    items = uiState.runHistory.take(5),
                    key = { _, run -> run.id }
                ) { index, run ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = index * 100
                            )
                        ) + slideInVertically(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = index * 100
                            )
                        )
                    ) {
                        GlassmorphismRunHistoryItem(run = run)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AnimatedGradientBackground() {
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

    Canvas(modifier = Modifier.fillMaxSize()) {
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
    val baseGradient = Brush.radialGradient(
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
        // Tertiary orb with wave motion
//        GradientOrb(
//            colors = listOf(
//                Color(0x4A10B981),  // Green
//                Color(0x3A059669),  // Darker green
//                Color(0x20000000)
//            ),
//            center = Offset(
//                centerX + cos(Math.toRadians(tertiaryOffset.toDouble())).toFloat() * width * 0.25f +
//                        sin(waveOffset * Math.PI * 2).toFloat() * 100f,
//                centerY + sin(Math.toRadians(tertiaryOffset.toDouble())).toFloat() * height * 0.15f +
//                        cos(waveOffset * Math.PI * 3).toFloat() * 80f
//            ),
//            radius = 200f
//        ),
        // Additional accent orbs
//        GradientOrb(
//            colors = listOf(
//                Color(0x3AF59E0B),  // Amber
//                Color(0x2AD97706),  // Orange
//                Color(0x20000000)
//            ),
//            center = Offset(
//                centerX + cos(Math.toRadians(primaryOffset * 1.5 + 90).toDouble()).toFloat() * width * 0.2f,
//                centerY + sin(Math.toRadians(primaryOffset * 1.5 + 90).toDouble()).toFloat() * height * 0.2f
//            ),
//            radius = 150f * breathingScale
//        ),
//        GradientOrb(
//            colors = listOf(
//                Color(0x3AEC4899),  // Pink
//                Color(0x2ADB2777),  // Rose
//                Color(0x20000000)
//            ),
//            center = Offset(
//                centerX + cos(Math.toRadians(secondaryOffset * 0.8 + 180).toDouble()).toFloat() * width * 0.35f,
//                centerY + sin(Math.toRadians(secondaryOffset * 0.8 + 180).toDouble()).toFloat() * height * 0.25f
//            ),
//            radius = 180f * (2f - breathingScale)
//        )
    )

    // Draw all orbs with blur effect
    orbs.forEach { orb ->
        // Create multiple overlapping circles for blur effect
        val blurLayers = 35
        for (layer in 0 until blurLayers) {
            val layerRadius = orb.radius * (1f + layer * 0.3f)
            val layerAlpha = (blurLayers - layer).toFloat() / blurLayers * 0.9f

            val gradient = Brush.radialGradient(
                colors = orb.colors.map { color ->
                    color.copy(alpha = color.alpha * layerAlpha)
                },
                center = orb.center,
                radius = layerRadius,
                tileMode = TileMode.Clamp
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
    val random = kotlin.random.Random(42) // Fixed seed for consistency

    repeat(noisePoints) { i ->
        val angle = (offset + i * 3.6f) % 360f
        val distance = random.nextFloat() * minOf(width, height) * 0.4f
        val centerX = width / 2
        val centerY = height / 2

        val x = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * distance
        val y = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * distance

        drawCircle(
            color = Color.White.copy(alpha = 0.02f + random.nextFloat() * 0.03f),
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

@Composable
private fun AnimatedGradientLine() {
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
        modifier = Modifier
            .width(80.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = animatedProgress),
                        Color(0xFF3B82F6),
                        Color(0xFF06B6D4).copy(alpha = animatedProgress)
                    )
                )
            )
    )
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable { },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF8B5CF6),
                modifier = Modifier
                    .size(24.dp)
                    .padding(bottom = 8.dp)
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun MainRunningCard(
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status text
            Text(
                text = statusMessage,
                fontSize = 16.sp,
                color = when {
                    isRunning -> Color(0xFF10B981)
                    isGettingLocation -> Color(0xFFF59E0B)
                    else -> Color.White.copy(alpha = 0.8f)
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Main action button
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Pulse ring for running state
                if (isRunning) {
                    Box(
                        modifier = Modifier
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
                    modifier = Modifier
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
                            color = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationCardsSection(
    startLat: String,
    startLng: String,
    endLat: String,
    endLng: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        LocationCard(
            title = "Start Position",
            latitude = startLat,
            longitude = startLng,
            iconTint = Color(0xFF10B981)
        )

        LocationCard(
            title = "End Position",
            latitude = endLat,
            longitude = endLng,
            iconTint = Color(0xFFEF4444)
        )
    }
}

@Composable
private fun LocationCard(
    title: String,
    latitude: String,
    longitude: String,
    iconTint: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Breitengrad",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = latitude,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Längengrad",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = longitude,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun DistanceCard(distance: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF8B5CF6).copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Distanz",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = "${String.format("%.1f", distance)} m",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun GlassmorphismRunHistoryItem(
    run: com.example.syntaxfitness.data.local.entity.RunEntity
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(run.startTime),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(run.startTime) +
                            " - " +
                            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(run.endTime),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.1f m", run.distance),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color(0xFF8B5CF6)
                )
                Text(
                    text = formatDuration(run.duration),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun GlassmorphismDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Berechtigung erteilen", color = Color(0xFF8B5CF6))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = Color.White.copy(alpha = 0.7f))
            }
        },
        containerColor = Color.Black.copy(alpha = 0.8f),
        shape = RoundedCornerShape(20.dp)
    )
}

private fun formatDuration(durationMillis: Long): String {
    val seconds = (durationMillis / 1000) % 60
    val minutes = (durationMillis / (1000 * 60)) % 60
    val hours = (durationMillis / (1000 * 60 * 60))

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}