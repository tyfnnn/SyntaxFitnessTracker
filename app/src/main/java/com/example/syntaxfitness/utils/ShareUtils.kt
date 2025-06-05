package com.example.syntaxfitness.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.FileProvider
import com.example.syntaxfitness.data.local.entity.RunEntity
import com.example.syntaxfitness.ui.running.component.ShareableRunDetailCard
import com.example.syntaxfitness.ui.theme.SyntaxFitnessTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
                Log.d("ShareUtils", "Successfully created shareable image: $imageUri")
            } catch (e: Exception) {
                Log.e("ShareUtils", "Error creating shareable image", e)
                // Fallback to text only
                shareIntent.type = "text/plain"
            }
        }

        Intent.createChooser(shareIntent, "Lauf teilen")
    }

    private suspend fun createShareableImage(
        context: Context,
        run: RunEntity,
    ): Uri = withContext(Dispatchers.Main) {
        // Get the current activity to attach ComposeView properly
        val activity = context as? android.app.Activity
            ?: throw IllegalStateException("Context must be an Activity")

        // Calculate dimensions in pixels
        val density = context.resources.displayMetrics.density
        val widthPx = (400 * density).toInt()
        val heightPx = (600 * density).toInt()

        // Create ComposeView and add to activity's content view temporarily
        val composeView = ComposeView(context).apply {
            layoutParams = ViewGroup.LayoutParams(widthPx, heightPx)
            setContent {
                SyntaxFitnessTheme {
                    ShareableRunDetailCard(run = run)
                }
            }
        }

        // Get the root view and add our ComposeView
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        rootView.addView(composeView)

        // Measure and layout
        val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)

        composeView.measure(widthSpec, heightSpec)
        composeView.layout(0, 0, widthPx, heightPx)

        // Allow composition to complete
        delay(200)

        // Create bitmap and draw
        val bitmap = createBitmap(widthPx, heightPx)
        val canvas = Canvas(bitmap)

        // Fill with background color first
        canvas.drawColor(android.graphics.Color.BLACK)
        composeView.draw(canvas)

        // Clean up - remove from root view
        rootView.removeView(composeView)

        Log.d("ShareUtils", "Bitmap created: ${bitmap.width}x${bitmap.height}")

        // Save and return URI
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

        // Ensure images directory exists
        val imagesDir = File(context.cacheDir, "images")
        if (!imagesDir.exists()) {
            val created = imagesDir.mkdirs()
            Log.d("ShareUtils", "Images directory created: $created, path: ${imagesDir.absolutePath}")
        }

        val imageFile = File(imagesDir, fileName)

        try {
            FileOutputStream(imageFile).use { out ->
                val compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                Log.d("ShareUtils", "Bitmap compressed successfully: $compressed, file size: ${imageFile.length()}")
            }
        } catch (e: Exception) {
            Log.e("ShareUtils", "Error saving bitmap to file", e)
            throw e
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )

        Log.d("ShareUtils", "File URI created: $uri")
        return@withContext uri
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