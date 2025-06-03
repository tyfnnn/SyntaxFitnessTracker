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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
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

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawAnimatedBackground(offset1, offset2)
    }
}

private fun DrawScope.drawAnimatedBackground(offset1: Float, offset2: Float) {
    val brush = Brush.radialGradient(
        colors = listOf(
            Color(0xFF6B46C1),
            Color(0xFF3B82F6),
            Color(0xFF1E40AF)
        )
    )
    drawRect(brush = brush)

    // Animated circles
    val centerX = size.width / 2
    val centerY = size.height / 2

    drawCircle(
        color = Color(0x33A855F7),
        radius = 200f,
        center = androidx.compose.ui.geometry.Offset(
            centerX + cos(Math.toRadians(offset1.toDouble())).toFloat() * 100,
            centerY + sin(Math.toRadians(offset1.toDouble())).toFloat() * 100
        )
    )

    drawCircle(
        color = Color(0x333B82F6),
        radius = 150f,
        center = androidx.compose.ui.geometry.Offset(
            centerX + cos(Math.toRadians(offset2.toDouble())).toFloat() * 150,
            centerY + sin(Math.toRadians(offset2.toDouble())).toFloat() * 150
        )
    )
}

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