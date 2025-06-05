package com.example.syntaxfitness.ui.running.screen

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syntaxfitness.data.local.entity.RunEntity
import com.example.syntaxfitness.ui.running.component.AnimatedGradientBackground
import com.example.syntaxfitness.ui.running.component.AnimatedGradientLine
import com.example.syntaxfitness.ui.running.component.LocationCardsSection
import com.example.syntaxfitness.ui.running.viewmodel.RunningViewModel
import com.example.syntaxfitness.utils.CoordinateUtils
import com.example.syntaxfitness.utils.ShareUtils
import com.example.syntaxfitness.utils.ShareUtils.calculateAverageSpeed
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RunDetailScreen(
    runId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RunningViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val runDetails by viewModel.selectedRun.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var isSharing by remember { mutableStateOf(false) }
    var showShareOptions by remember { mutableStateOf(false) }

    LaunchedEffect(runId) {
        viewModel.loadRunDetails(runId)
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        runDetails?.let { run ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = Color.White.copy(alpha = 0.1f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Zurück",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = "Lauf Details",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            IconButton(
                                onClick = { showShareOptions = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = Color(0xFF8B5CF6).copy(alpha = 0.2f),
                                        shape = CircleShape
                                    ),
                                enabled = !isSharing
                            ) {
                                if (isSharing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Teilen",
                                        tint = Color(0xFF8B5CF6),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        AnimatedGradientLine()
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(300)) + slideInVertically(tween(300))
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = SimpleDateFormat("EEEE", Locale.getDefault()).format(run.startTime),
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = SimpleDateFormat("dd. MMMM yyyy", Locale.getDefault()).format(run.startTime),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(300, delayMillis = 100)) + slideInVertically(tween(300, delayMillis = 100))
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RunDetailStatCard(
                                    title = "Distanz",
                                    value = String.format(Locale.US, "%.1f m", run.distance),
                                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                                    modifier = Modifier.weight(1f)
                                )
                                RunDetailStatCard(
                                    title = "Dauer",
                                    value = formatDuration(run.duration),
                                    icon = Icons.Default.AccessTime,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RunDetailStatCard(
                                    title = "Geschw.",
                                    value = calculateAverageSpeed(run.distance, run.duration),
                                    icon = Icons.Default.Speed,
                                    modifier = Modifier.weight(1f)
                                )
                                RunDetailStatCard(
                                    title = "Datum",
                                    value = SimpleDateFormat("dd.MM", Locale.getDefault()).format(run.startTime),
                                    icon = Icons.Default.CalendarToday,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(300, delayMillis = 200)) + slideInVertically(tween(300, delayMillis = 200))
                    ) {
                        TimeDetailsCard(run = run)
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(300, delayMillis = 300)) + slideInVertically(tween(300, delayMillis = 300))
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "GPS-Koordinaten",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LocationCardsSection(
                                startLat = CoordinateUtils.formatCoordinate(run.startLatitude),
                                startLng = CoordinateUtils.formatCoordinate(run.startLongitude),
                                endLat = CoordinateUtils.formatCoordinate(run.endLatitude),
                                endLng = CoordinateUtils.formatCoordinate(run.endLongitude)
                            )
                        }
                    }
                }
            }
        } ?: run {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Lauf nicht gefunden",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        if (showShareOptions && runDetails != null) {
            ShareOptionsDialog(
                onDismiss = { showShareOptions = false },
                onShareWithImage = {
                    showShareOptions = false
                    isSharing = true
                    coroutineScope.launch {
                        try {
                            val shareIntent = ShareUtils.shareRun(
                                context = context,
                                run = runDetails!!,
                                includeImage = true
                            )
                            context.startActivity(shareIntent)
                        } catch (e: Exception) {
                            val shareIntent = ShareUtils.shareRun(
                                context = context,
                                run = runDetails!!,
                                includeImage = false
                            )
                            context.startActivity(shareIntent)
                        } finally {
                            isSharing = false
                        }
                    }
                },
                onShareTextOnly = {
                    showShareOptions = false
                    coroutineScope.launch {
                        val shareIntent = ShareUtils.shareRun(
                            context = context,
                            run = runDetails!!,
                            includeImage = false
                        )
                        context.startActivity(shareIntent)
                    }
                }
            )
        }
    }
}

@Composable
private fun ShareOptionsDialog(
    onDismiss: () -> Unit,
    onShareWithImage: () -> Unit,
    onShareTextOnly: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Lauf teilen",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column {
                Text(
                    text = "Wie möchten Sie Ihren Lauf teilen?",
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    onClick = onShareWithImage,
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF8B5CF6).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Mit Bild teilen",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            Text(
                                text = "Erstellt ein schönes Bild Ihres Laufs",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    onClick = onShareTextOnly,
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Nur Text teilen",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            Text(
                                text = "Teilt nur die Lauf-Statistiken",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = Color.White.copy(alpha = 0.7f))
            }
        },
        containerColor = Color.Black.copy(alpha = 0.8f),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun RunDetailStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
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
                fontSize = 18.sp,
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
private fun TimeDetailsCard(run: RunEntity) {
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
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp)
                )
                Text(
                    text = "Zeitdetails",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Start",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(run.startTime),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Ende",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(run.endTime),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

fun formatDuration(durationMillis: Long): String {
    val seconds = (durationMillis / 1000) % 60
    val minutes = (durationMillis / (1000 * 60)) % 60
    val hours = (durationMillis / (1000 * 60 * 60))

    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}

fun calculateAverageSpeed(distanceMeters: Float, durationMillis: Long): String {
    if (durationMillis == 0L) return "0.0 m/s"

    val durationSeconds = durationMillis / 1000.0
    val speedMeterPerSecond = distanceMeters / durationSeconds

    return String.format(Locale.US, "%.1f m/s", speedMeterPerSecond)
}