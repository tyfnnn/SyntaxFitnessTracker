package com.example.syntaxfitness.ui.running.screen

import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.syntaxfitness.ui.running.component.LocationCard
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
    var hasNotificationPermission by remember { mutableStateOf(true) } // Default true for older versions
    var showPermissionDeniedMessage by remember { mutableStateOf(false) }
    var isGettingLocation by remember { mutableStateOf(false) }

    // New state for permission rationale dialog
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: true // Default true for older versions

        hasLocationPermission = fineLocationGranted || coarseLocationGranted
        hasNotificationPermission = notificationGranted

        if (!hasLocationPermission) {
            showPermissionDeniedMessage = true

            // Check if we should show rationale for the next time
            // This helps us prepare for future permission requests
            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                context as ComponentActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                context as ComponentActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

            // Note: At this point, we've already been denied, so we could show
            // additional explanation UI if needed
        } else {
            showPermissionDeniedMessage = false
        }
    }

    // Function to handle permission requests with rationale check
    fun handlePermissionRequest() {
        // Build the list of permissions to request
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Only add notification permission for Android 13 (API 33) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Check if we should show rationale before requesting permission
        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            context as ComponentActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) || ActivityCompat.shouldShowRequestPermissionRationale(
            context as ComponentActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.shouldShowRequestPermissionRationale(
                    context as ComponentActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ))

        if (shouldShowRationale) {
            // Show rationale dialog first
            showPermissionRationaleDialog = true
        } else {
            // Request permission directly
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // Permission Rationale Dialog
    if (showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = {
                showPermissionRationaleDialog = false
            },
            title = {
                Text(
                    text = "Berechtigungen erforderlich",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = buildString {
                        append("Diese App benötigt folgende Berechtigungen:\n\n")
                        append("• Standort: Um Ihre Laufstrecke zu verfolgen\n")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            append("• Benachrichtigungen: Um Sie über Laufstatus zu informieren\n")
                        }
                        append("\nIhre Daten werden nur lokal verwendet und nicht weitergegeben.")
                    },
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationaleDialog = false
                        // Now request the permission after showing rationale
                        val permissionsToRequest = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                        }

                        requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                    }
                ) {
                    Text("Berechtigungen erteilen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPermissionRationaleDialog = false
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
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
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = buildString {
                                append("Berechtigungen erforderlich:\n")
                                if (!hasLocationPermission) append("• Standort für GPS-Tracking\n")
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                    append("• Benachrichtigungen für Updates")
                                }
                            },
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Add explanation text in the error card
                        Text(
                            text = "Tippen Sie auf 'Berechtigung anfordern' für weitere Informationen.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
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
            val needsPermissions = !hasLocationPermission ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission)

            if (needsPermissions) {
                OutlinedButton(
                    onClick = {
                        // Use our new permission handling function
                        handlePermissionRequest()
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Berechtigungen anfordern")
                }
            }

            // Start/Stop Button
            Button(
                onClick = {
                    // First check if we have location permission
                    if (!hasLocationPermission) {
                        // Use our new permission handling function
                        handlePermissionRequest()
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
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission ->
                        "Benachrichtigungsberechtigung empfohlen"
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

@Preview(showBackground = true)
@Composable
fun RunningScreenPreview() {
    SyntaxFitnessTheme {
        RunningScreen()
    }
}