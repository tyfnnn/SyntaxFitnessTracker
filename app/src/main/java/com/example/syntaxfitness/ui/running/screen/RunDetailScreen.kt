package com.example.syntaxfitness.ui.running.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syntaxfitness.data.local.entity.RunEntity
import com.example.syntaxfitness.ui.running.component.AnimatedGradientBackground
import com.example.syntaxfitness.ui.running.component.AnimatedGradientLine
import com.example.syntaxfitness.ui.running.viewmodel.RunningViewModel
import com.example.syntaxfitness.utils.CoordinateUtils
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RunDetailScreen(
    runId: Long,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: RunningViewModel = koinViewModel()
) {
    var currentRun by remember { mutableStateOf<RunEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }


    LaunchedEffect(runId) {
        try {
            isLoading = true
            hasError = false

            kotlinx.coroutines.delay(500)


            viewModel.uiState.value.runHistory.find { it.id == runId }?.let { run ->
                currentRun = run
            } ?: run {
                hasError = true
            }
        } catch (e: Exception) {
            hasError = true
        } finally {
            isLoading = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        when {
            isLoading -> {
                LoadingContent()
            }

            hasError -> {
                ErrorContent(onNavigateBack = onNavigateBack)
            }

            currentRun != null -> {
                RunDetailContent(
                    run = currentRun!!,
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }
}

/**
 * Loading-Komponente mit ansprechender Animation.
 * Zeigt dem Benutzer, dass Daten geladen werden.
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFF8B5CF6),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Lauf wird geladen...",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
        }
    }
}


@Composable
private fun ErrorContent(onNavigateBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Lauf nicht gefunden",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Der angeforderte Lauf konnte nicht geladen werden.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .background(
                            color = Color(0xFF8B5CF6),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück",
                        tint = Color.White
                    )
                }
            }
        }
    }
}


@Composable
private fun RunDetailContent(
    run: RunEntity,
    onNavigateBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            RunDetailHeader(onNavigateBack = onNavigateBack)
        }

        item {
            RunSummaryCard(run = run)
        }

        item {
            TimeInfoCard(run = run)
        }

        item {
            LocationDetailsCard(run = run)
        }

        item {
            PerformanceMetricsCard(run = run)
        }
    }
}

@Composable
private fun RunDetailHeader(onNavigateBack: () -> Unit) {
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

            Box(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        AnimatedGradientLine()
    }
}


@Composable
private fun RunSummaryCard(run: RunEntity) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "summary_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format(Locale.US, "%.1f", run.distance),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B5CF6)
                )
                Text(
                    text = "Meter",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    icon = Icons.Default.AccessTime,
                    value = formatDuration(run.duration),
                    label = "Dauer"
                )

                MetricItem(
                    icon = Icons.Default.Speed,
                    value = String.format(Locale.US, "%.1f", calculateAverageSpeed(run)),
                    label = "km/h"
                )
            }
        }
    }
}

@Composable
private fun TimeInfoCard(run: RunEntity) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(600)) + slideInVertically(tween(600))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = "Zeitinformationen",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TimeInfoRow(
                        label = "Datum",
                        value = SimpleDateFormat("EEEE, dd. MMMM yyyy", Locale.GERMAN).format(run.startTime)
                    )
                    TimeInfoRow(
                        label = "Startzeit",
                        value = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(run.startTime)
                    )
                    TimeInfoRow(
                        label = "Endzeit",
                        value = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(run.endTime)
                    )
                    TimeInfoRow(
                        label = "Dauer",
                        value = formatDuration(run.duration)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}


@Composable
private fun LocationDetailsCard(run: RunEntity) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(800)) + slideInVertically(tween(800))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF06B6D4),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = "Standortdetails",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Startposition
                LocationSection(
                    title = "Startposition",
                    latitude = run.startLatitude,
                    longitude = run.startLongitude,
                    color = Color(0xFF10B981)
                )

                // Endposition
                LocationSection(
                    title = "Endposition",
                    latitude = run.endLatitude,
                    longitude = run.endLongitude,
                    color = Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
private fun LocationSection(
    title: String,
    latitude: Double,
    longitude: Double,
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )

        // Dezimalformat (Standard)
        CoordinateRow(
            label = "Dezimal",
            value = "${CoordinateUtils.formatCoordinate(latitude, 6)}, ${CoordinateUtils.formatCoordinate(longitude, 6)}"
        )

        // Grad/Minuten-Format
        CoordinateRow(
            label = "Grad/Min",
            value = "${CoordinateUtils.formatCoordinateAdvanced(latitude, CoordinateUtils.CoordinateFormat.DEGREES_MINUTES, true)}, " +
                    CoordinateUtils.formatCoordinateAdvanced(longitude, CoordinateUtils.CoordinateFormat.DEGREES_MINUTES, false)
        )
    }
}


@Composable
private fun CoordinateRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

@Composable
private fun PerformanceMetricsCard(run: RunEntity) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(1000)) + slideInVertically(tween(1000))
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = "Leistungsmetriken",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PerformanceMetricItem(
                            icon = Icons.Default.Straighten,
                            value = String.format(Locale.US, "%.0f", run.distance),
                            unit = "m",
                            label = "Distanz",
                            modifier = Modifier.weight(1f)
                        )

                        PerformanceMetricItem(
                            icon = Icons.Default.Speed,
                            value = String.format(Locale.US, "%.1f", calculateAverageSpeed(run)),
                            unit = "km/h",
                            label = "Ø-Tempo",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PerformanceMetricItem(
                            icon = Icons.Default.AccessTime,
                            value = String.format(Locale.US, "%.1f", run.duration / 1000.0 / 60.0),
                            unit = "min",
                            label = "Zeit",
                            modifier = Modifier.weight(1f)
                        )

                        PerformanceMetricItem(
                            icon = Icons.Default.Speed,
                            value = if (run.distance > 0) String.format(Locale.US, "%.1f", calculatePacePerKm(run)) else "--",
                            unit = "min/km",
                            label = "Pace",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PerformanceMetricItem(
    icon: ImageVector,
    value: String,
    unit: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(16.dp)
            )

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = " $unit",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun MetricItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF8B5CF6),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

// Hilfsfunktionen für Berechnungen

private fun calculateAverageSpeed(run: RunEntity): Double {
    if (run.duration <= 0) return 0.0
    val distanceInKm = run.distance / 1000.0
    val timeInHours = run.duration / 1000.0 / 3600.0
    return distanceInKm / timeInHours
}

private fun calculatePacePerKm(run: RunEntity): Double {
    if (run.distance <= 0) return 0.0
    val distanceInKm = run.distance / 1000.0
    val timeInMinutes = run.duration / 1000.0 / 60.0
    return timeInMinutes / distanceInKm
}

private fun formatDuration(durationMillis: Long): String {
    val seconds = (durationMillis / 1000) % 60
    val minutes = (durationMillis / (1000 * 60)) % 60
    val hours = (durationMillis / (1000 * 60 * 60))

    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}