package com.example.syntaxfitness.ui.running.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syntaxfitness.data.local.entity.RunEntity
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun GlassmorphismRunHistoryItem(
    run: RunEntity
) {
    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .clickable { },
        colors = CardDefaults.cardColors(
            containerColor = Color.Companion.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Companion.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.Companion.weight(1f)) {
                Text(
                    text = SimpleDateFormat(
                        "dd.MM.yyyy",
                        Locale.getDefault()
                    ).format(run.startTime),
                    fontWeight = FontWeight.Companion.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Companion.White
                )
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(run.startTime) +
                            " - " +
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(run.endTime),
                    fontSize = 14.sp,
                    color = Color.Companion.White.copy(alpha = 0.7f)
                )
            }

            Column(horizontalAlignment = Alignment.Companion.End) {
                Text(
                    text = String.Companion.format(Locale.US, "%.1f m", run.distance),
                    fontWeight = FontWeight.Companion.Medium,
                    fontSize = 16.sp,
                    color = Color(0xFF8B5CF6)
                )
                Text(
                    text = formatDuration(run.duration),
                    fontSize = 14.sp,
                    color = Color.Companion.White.copy(alpha = 0.7f)
                )
            }
        }
    }
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
