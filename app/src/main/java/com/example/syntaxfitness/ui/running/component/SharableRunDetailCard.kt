package com.example.syntaxfitness.ui.running.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syntaxfitness.data.local.entity.RunEntity
import com.example.syntaxfitness.utils.CoordinateUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ShareableRunDetailCard(
    run: RunEntity,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(400.dp)
            .height(600.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        // Hintergrund mit Gradient
        ShareableBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header mit App-Branding
            ShareableHeader()

            // Datum und Zeit prominent
            ShareableDateSection(run)

            // Hauptstatistiken Grid
            ShareableStatsGrid(run)

            // GPS-Koordinaten kompakt
            ShareableLocationSection(run)

            // Footer mit App-Info
            ShareableFooter()
        }
    }
}

@Composable
private fun ShareableBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Gradient Background ähnlich der App
        val gradient = Brush.radialGradient(
            colors = listOf(
                Color(0xFF1A1B3E),
                Color(0xFF0F0F2A),
                Color(0xFF000814)
            ),
            center = Offset(size.width / 2, size.height / 3),
            radius = maxOf(size.width, size.height) * 0.8f
        )

        drawRect(brush = gradient)

        // Dezente Orbs für visuellen Appeal
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x308B5CF6),
                    Color(0x10000000)
                ),
                radius = 120f
            ),
            radius = 120f,
            center = Offset(size.width * 0.8f, size.height * 0.2f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x3010B981),
                    Color(0x10000000)
                ),
                radius = 100f
            ),
            radius = 100f,
            center = Offset(size.width * 0.2f, size.height * 0.7f)
        )
    }
}

@Composable
private fun ShareableHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
            contentDescription = null,
            tint = Color(0xFF8B5CF6),
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "SyntaxFitness",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun ShareableDateSection(run: RunEntity) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = SimpleDateFormat("EEEE", Locale.getDefault()).format(run.startTime),
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = SimpleDateFormat("dd. MMMM yyyy", Locale.getDefault()).format(run.startTime),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${SimpleDateFormat("HH:mm", Locale.getDefault()).format(run.startTime)} - ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(run.endTime)}",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ShareableStatsGrid(run: RunEntity) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShareableStatCard(
                title = "Distanz",
                value = String.format(Locale.US, "%.1f m", run.distance),
                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                color = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f)
            )
            ShareableStatCard(
                title = "Dauer",
                value = formatDuration(run.duration),
                icon = Icons.Default.AccessTime,
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShareableStatCard(
                title = "Geschw.",
                value = calculateAverageSpeed(run.distance, run.duration),
                icon = Icons.Default.Speed,
                color = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
            ShareableStatCard(
                title = "Pace",
                value = calculatePace(run.distance, run.duration),
                icon = Icons.Default.Timer,
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ShareableStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ShareableLocationSection(run: RunEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Route",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Start",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${CoordinateUtils.formatCoordinate(run.startLatitude, 2)}, ${CoordinateUtils.formatCoordinate(run.startLongitude, 2)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Ende",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${CoordinateUtils.formatCoordinate(run.endLatitude, 2)}, ${CoordinateUtils.formatCoordinate(run.endLongitude, 2)}",
                        fontSize = 10.sp,
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
private fun ShareableFooter() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Aufgezeichnet mit SyntaxFitness",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

// Hilfsfunktionen
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

private fun calculateAverageSpeed(distanceMeters: Float, durationMillis: Long): String {
    if (durationMillis == 0L) return "0.0 m/s"

    val durationSeconds = durationMillis / 1000.0
    val speedMeterPerSecond = distanceMeters / durationSeconds

    return String.format(Locale.US, "%.1f m/s", speedMeterPerSecond)
}

private fun calculatePace(distanceMeters: Float, durationMillis: Long): String {
    if (distanceMeters == 0f) return "--:--"

    val distanceKm = distanceMeters / 1000.0
    val durationMinutes = durationMillis / (1000.0 * 60.0)
    val paceMinutesPerKm = durationMinutes / distanceKm

    val minutes = paceMinutesPerKm.toInt()
    val seconds = ((paceMinutesPerKm - minutes) * 60).toInt()

    return String.format(Locale.US, "%d:%02d/km", minutes, seconds)
}