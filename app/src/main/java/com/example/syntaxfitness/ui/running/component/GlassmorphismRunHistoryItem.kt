package com.example.syntaxfitness.ui.running.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syntaxfitness.data.local.entity.RunEntity
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassmorphismRunHistoryItem(
    modifier: Modifier = Modifier,
    run: RunEntity,
    onDelete: (RunEntity) -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(true) }
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd,
                SwipeToDismissBoxValue.EndToStart -> {
                    isVisible = false
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    // Trigger delete after animation
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            kotlinx.coroutines.delay(300) // Wait for exit animation
            onDelete(run)
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
    ) {
        SwipeToDismissBox(
            state = swipeState,
            modifier = modifier.fillMaxWidth(),
            backgroundContent = {
                DeleteBackground(
                    swipeProgress = when (swipeState.dismissDirection) {
                        SwipeToDismissBoxValue.StartToEnd -> swipeState.progress
                        SwipeToDismissBoxValue.EndToStart -> swipeState.progress
                        else -> 0f
                    }
                )
            }
        ) {
            RunHistoryCard(run = run)
        }
    }
}

@Composable
private fun DeleteBackground(swipeProgress: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFFEF4444).copy(alpha = 0.1f + swipeProgress * 0.3f),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
    }
}

@Composable
private fun RunHistoryCard(
    run: RunEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
                    text = SimpleDateFormat(
                        "dd.MM.yyyy",
                        Locale.getDefault()
                    ).format(run.startTime),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(run.startTime) +
                            " - " +
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(run.endTime),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.US, "%.1f m", run.distance),
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