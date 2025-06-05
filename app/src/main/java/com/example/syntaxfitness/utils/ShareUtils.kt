package com.example.syntaxfitness.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.syntaxfitness.data.local.entity.RunEntity
import com.example.syntaxfitness.ui.running.component.ShareableRunDetailCard
import com.example.syntaxfitness.ui.theme.SyntaxFitnessTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.createBitmap

object ShareUtils {

    suspend fun shareRun(
        context: Context,
        run: RunEntity,
        includeImage: Boolean = true,
    ): Intent = withContext(Dispatchers.IO) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = if (includeImage) "image/*" else "text/plain"
        }

        val shareText = generateShareText(run)
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)

        if (includeImage) {
            try {
                val imageUri = createShareableImage(context, run)
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                Log.e("ShareUtils", "Fehler beim Erstellen des Share-Bildes", e)
                shareIntent.type = "text/plain"
            }
        }

        Intent.createChooser(shareIntent, "Lauf teilen")
    }

    suspend fun createInstagramStoryImage(
        context: Context,
        run: RunEntity,
    ): Uri = withContext(Dispatchers.Main) {
        val composeView = ComposeView(context).apply {
            setContent {
                SyntaxFitnessTheme {
                    InstagramStoryRunCard(run = run)
                }
            }
        }

        val widthSpec = android.view.View.MeasureSpec.makeMeasureSpec(
            (1080 * context.resources.displayMetrics.density / 4).toInt(),
            android.view.View.MeasureSpec.EXACTLY
        )
        val heightSpec = android.view.View.MeasureSpec.makeMeasureSpec(
            (1920 * context.resources.displayMetrics.density / 4).toInt(),
            android.view.View.MeasureSpec.EXACTLY
        )

        composeView.measure(widthSpec, heightSpec)
        composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

        val bitmap = createBitmap(composeView.measuredWidth, composeView.measuredHeight)

        val canvas = Canvas(bitmap)
        composeView.draw(canvas)

        saveBitmapToTempFile(context, bitmap, run, "story_")
    }

    private suspend fun createShareableImage(
        context: Context,
        run: RunEntity,
    ): Uri = withContext(Dispatchers.Main) {
        val composeView = ComposeView(context).apply {
            setContent {
                SyntaxFitnessTheme {
                    ShareableRunDetailCard(run = run)
                }
            }
        }

        val widthSpec = android.view.View.MeasureSpec.makeMeasureSpec(
            (400 * context.resources.displayMetrics.density).toInt(),
            android.view.View.MeasureSpec.EXACTLY
        )
        val heightSpec = android.view.View.MeasureSpec.makeMeasureSpec(
            (600 * context.resources.displayMetrics.density).toInt(),
            android.view.View.MeasureSpec.EXACTLY
        )

        composeView.measure(widthSpec, heightSpec)
        composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)

        val bitmap = createBitmap(composeView.measuredWidth, composeView.measuredHeight)

        val canvas = Canvas(bitmap)
        composeView.draw(canvas)

        saveBitmapToTempFile(context, bitmap, run)
    }

    private suspend fun saveBitmapToTempFile(
        context: Context,
        bitmap: Bitmap,
        run: RunEntity,
        prefix: String = "run_",
    ): Uri = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val fileName = "${prefix}${dateFormat.format(run.startTime)}.png"

        val imagesDir = File(context.cacheDir, "images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        val imageFile = File(imagesDir, fileName)

        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
        }

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    private fun generateShareText(run: RunEntity): String {
        val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        val duration = formatDuration(run.duration)
        val distance = String.format(Locale.US, "%.1f", run.distance)
        val speed = calculateAverageSpeed(run.distance, run.duration)
        val pace = calculatePace(run.distance, run.duration)

        return buildString {
            appendLine("🏃‍♂️ Lauf von heute!")
            appendLine()
            appendLine("📅 ${dateFormatter.format(run.startTime)}")
            appendLine("⏰ ${timeFormatter.format(run.startTime)} - ${timeFormatter.format(run.endTime)}")
            appendLine()
            appendLine("📊 Statistiken:")
            appendLine("🛣️  Distanz: ${distance}m")
            appendLine("⏱️  Dauer: $duration")
            appendLine("💨  Geschwindigkeit: $speed")
            appendLine("🏃  Pace: $pace")
            appendLine()
            appendLine("Aufgezeichnet mit SyntaxFitness 📱")
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

    private fun calculatePace(distanceMeters: Float, durationMillis: Long): String {
        if (distanceMeters == 0f) return "--:--"

        val distanceKm = distanceMeters / 1000.0
        val durationMinutes = durationMillis / (1000.0 * 60.0)
        val paceMinutesPerKm = durationMinutes / distanceKm

        val minutes = paceMinutesPerKm.toInt()
        val seconds = ((paceMinutesPerKm - minutes) * 60).toInt()

        return String.format(Locale.US, "%d:%02d/km", minutes, seconds)
    }
}

@Composable
fun InstagramStoryRunCard(
    run: RunEntity,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1B3E),
                        Color(0xFF0F0F2A),
                        Color(0xFF000814)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "🏃‍♂️ Mein Lauf",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                StoryStatItem(
                    label = "Distanz",
                    value = "${String.format(Locale.US, "%.1f", run.distance)} m",
                    emoji = "🛣️"
                )

                StoryStatItem(
                    label = "Zeit",
                    value = ShareUtils.formatDuration(run.duration),
                    emoji = "⏱️"
                )

                StoryStatItem(
                    label = "Geschwindigkeit",
                    value = ShareUtils.calculateAverageSpeed(run.distance, run.duration),
                    emoji = "💨"
                )
            }

            Text(
                text = SimpleDateFormat("dd. MMMM yyyy", Locale.getDefault()).format(run.startTime),
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Text(
                text = "SyntaxFitness",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8B5CF6)
            )
        }
    }
}

@Composable
private fun StoryStatItem(
    label: String,
    value: String,
    emoji: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 40.sp
        )
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}