package com.example.syntaxfitness.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syntaxfitness.ui.theme.SyntaxFitnessTheme

@Composable
fun RunningScreen(modifier: Modifier = Modifier) {
    var isRunning by remember { mutableStateOf(false) }
    var startLatitude by remember { mutableStateOf("--") }
    var startLongitude by remember { mutableStateOf("--") }
    var endLatitude by remember { mutableStateOf("--") }
    var endLongitude by remember { mutableStateOf("--") }

    // State für Permission Status
    var hasLocationPermission by remember { mutableStateOf(false) }
    var showPermissionDeniedMessage by remember { mutableStateOf(false) }

    // Permission Launcher für Standortberechtigungen
    // Dieser Launcher kann sowohl FINE als auch COARSE Location Permissions anfragen
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Callback wird ausgeführt, wenn der User auf "Erlauben" oder "Ablehnen" klickt
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        // Wir brauchen mindestens eine der beiden Berechtigungen
        hasLocationPermission = fineLocationGranted || coarseLocationGranted

        if (!hasLocationPermission) {
            showPermissionDeniedMessage = true
        } else {
            showPermissionDeniedMessage = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Text(
            text = "Lauf Tracker",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 32.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Permission Status Anzeige
            if (showPermissionDeniedMessage) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Standortberechtigung erforderlich für GPS-Tracking",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Start Location Card
            LocationCard(
                title = "Start Position",
                latitude = startLatitude,
                longitude = startLongitude,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer
            )

            // End Location Card
            LocationCard(
                title = "End Position",
                latitude = endLatitude,
                longitude = endLongitude,
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer
            )
        }

        // Control Button Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            // Permission Request Button (wird nur angezeigt wenn keine Berechtigung vorhanden)
            if (!hasLocationPermission) {
                OutlinedButton(
                    onClick = {
                        // Hier starten wir die Berechtigungsanfrage
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Standortberechtigung anfordern")
                }
            }

            // Start/Stop Button
            Button(
                onClick = {
                    if (!hasLocationPermission) {
                        // Erst Berechtigung anfordern, bevor der Lauf gestartet wird
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                        return@Button
                    }

                    if (!isRunning) {
                        // Lauf starten - hier würde später echter GPS-Abruf stattfinden
                        isRunning = true
                        startLatitude = "52.5200"
                        startLongitude = "13.4050"
                        endLatitude = "--"
                        endLongitude = "--"
                    } else {
                        // Lauf beenden
                        isRunning = false
                        endLatitude = "52.5190"
                        endLongitude = "13.4060"
                    }
                },
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(60.dp)
            ) {
                Text(
                    text = if (isRunning) "STOP" else "START",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Status Text
            Text(
                text = when {
                    !hasLocationPermission -> "Standortberechtigung erforderlich"
                    isRunning -> "Lauf läuft..."
                    else -> "Bereit zum Starten"
                },
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun LocationCard(
    title: String,
    latitude: String,
    longitude: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Breitengrad",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = latitude,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Längengrad",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = longitude,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RunningScreenPreview() {
    SyntaxFitnessTheme {
        RunningScreen()
    }
}