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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syntaxfitness.ui.theme.SyntaxFitnessTheme
import com.example.syntaxfitness.utils.LocationUtils

@Composable
fun RunningScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var isRunning by remember { mutableStateOf(false) }
    var startLatitude by remember { mutableStateOf("--") }
    var startLongitude by remember { mutableStateOf("--") }
    var endLatitude by remember { mutableStateOf("--") }
    var endLongitude by remember { mutableStateOf("--") }

    var hasLocationPermission by remember { mutableStateOf(false) }
    var showPermissionDeniedMessage by remember { mutableStateOf(false) }
    var isGettingLocation by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

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
            // Permission status display
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
            // Permission Request Button (only shown when no permission is granted)
            if (!hasLocationPermission) {
                OutlinedButton(
                    onClick = {
                        // Start the permission request process
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
                    // First check if we have location permission
                    if (!hasLocationPermission) {
                        // Request permission before starting the run
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                        return@Button
                    }

                    if (!isRunning) {
                        // Start run - get actual GPS location
                        isRunning = true
                        isGettingLocation = true

                        // Clear previous end location
                        endLatitude = "--"
                        endLongitude = "--"

                        // Use LocationUtils to get the current position
                        LocationUtils.getLocation(context) { location ->
                            // This callback runs when we successfully get the location
                            // Format the coordinates to 4 decimal places for display
                            startLatitude = String.format("%.4f", location.latitude)
                            startLongitude = String.format("%.4f", location.longitude)
                            isGettingLocation = false
                        }
                    } else {
                        // End run - get final GPS location
                        isGettingLocation = true

                        // Use LocationUtils to get the current position for end location
                        LocationUtils.getLocation(context) { location ->
                            // This callback runs when we successfully get the location
                            // Format the coordinates to 4 decimal places for display
                            endLatitude = String.format("%.4f", location.latitude)
                            endLongitude = String.format("%.4f", location.longitude)
                            isGettingLocation = false
                            isRunning = false
                        }
                    }
                },
                enabled = !isGettingLocation, // Disable button while getting location
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
                    isGettingLocation -> "GPS-Position wird ermittelt..."
                    isRunning -> "Lauf läuft..."
                    else -> "Bereit zum Starten"
                },
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
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