package com.example.syntaxfitness.ui.settings.component

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

@Composable
fun StatsSummaryCard(
    totalRuns: Int,
    totalDistance: Float
) {
    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Companion.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.Companion
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            Text(
                text = "Ihre Lauf-Statistiken",
                fontSize = 18.sp,
                fontWeight = FontWeight.Companion.SemiBold,
                color = Color.Companion.White,
                modifier = Modifier.Companion.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                    Text(
                        text = "$totalRuns",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Companion.Bold,
                        color = Color(0xFF8B5CF6)
                    )
                    Text(
                        text = if (totalRuns == 1) "Lauf" else "Läufe",
                        fontSize = 14.sp,
                        color = Color.Companion.White.copy(alpha = 0.7f)
                    )
                }

                Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f km", totalDistance / 1000),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Companion.Bold,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = "Gesamt",
                        fontSize = 14.sp,
                        color = Color.Companion.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}