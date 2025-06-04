package com.example.syntaxfitness.ui.running.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.syntaxfitness.ui.running.component.LocationCard

@Composable
fun LocationCardsSection(
    startLat: String,
    startLng: String,
    endLat: String,
    endLng: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.Companion.fillMaxWidth()
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