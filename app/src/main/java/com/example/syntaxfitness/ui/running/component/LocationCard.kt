package com.example.syntaxfitness.ui.running.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LocationCard(
    title: String,
    latitude: String,
    longitude: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier.Companion
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Companion.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.Companion.padding(bottom = 12.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.Companion.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                    Text(
                        text = "Breitengrad",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = latitude,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Companion.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Companion.Center
                    )
                }

                Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                    Text(
                        text = "Längengrad",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = longitude,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Companion.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Companion.Center
                    )
                }
            }
        }
    }
}