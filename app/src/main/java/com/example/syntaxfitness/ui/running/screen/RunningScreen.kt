package com.example.syntaxfitness.ui.running.screen

import android.Manifest
import android.os.Build
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.syntaxfitness.ui.running.component.LocationCard
import com.example.syntaxfitness.ui.running.viewmodel.RunningViewModel
import com.example.syntaxfitness.ui.theme.SyntaxFitnessTheme

@Composable
fun RunningScreen(
    modifier: Modifier = Modifier,
    viewModel: RunningViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkAllPermissions(context)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        viewModel.updateLocationPermission(fineLocationGranted, coarseLocationGranted)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
            viewModel.updateNotificationPermission(notificationGranted)
        }

        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.toggleRunning(context)
        }
    }

    fun requestPermissionsIfNeeded() {
        val shouldShowLocationRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            context as androidx.activity.ComponentActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val shouldShowNotificationRationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                context as androidx.activity.ComponentActivity,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else false

        when {
            shouldShowLocationRationale -> {
                viewModel.setPermissionRationaleDialogVisibility(true)
            }
            shouldShowNotificationRationale -> {
                viewModel.setNotificationRationaleDialogVisibility(true)
            }
            else -> {
                // Direkte Berechtigungsanfrage ohne Rationale
                val permissionsToRequest = viewModel.getMissingPermissions()
                if (permissionsToRequest.isNotEmpty()) {
                    requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                }
            }
        }
    }

    // Dialog für Standort-Berechtigung Rationale
    if (uiState.showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.setPermissionRationaleDialogVisibility(false)
            },
            title = {
                Text(
                    text = "Standortberechtigung erforderlich",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Diese App benötigt Zugriff auf Ihren Standort, um Ihre Laufstrecke zu verfolgen. " +
                            "Wir verwenden GPS-Daten nur während aktiver Läufe, um Start- und Endpositionen " +
                            "zu erfassen. Ihre Standortdaten werden nicht gespeichert oder weitergegeben.",
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setPermissionRationaleDialogVisibility(false)
                        val permissionsToRequest = viewModel.getMissingPermissions()
                        requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                    }
                ) {
                    Text("Berechtigung erteilen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setPermissionRationaleDialogVisibility(false)
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Dialog für Notification-Berechtigung Rationale
    if (uiState.showNotificationRationaleDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.setNotificationRationaleDialogVisibility(false)
            },
            title = {
                Text(
                    text = "Benachrichtigungsberechtigung",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Diese App möchte Ihnen Benachrichtigungen senden, um Sie über " +
                            "den Start und das Ende Ihrer Läufe zu informieren. " +
                            "Diese Benachrichtigungen helfen Ihnen dabei, Ihre Trainingsfortschritte " +
                            "im Blick zu behalten. Sie können diese Berechtigung jederzeit in den " +
                            "App-Einstellungen ändern.",
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setNotificationRationaleDialogVisibility(false)
                        val permissionsToRequest = viewModel.getMissingPermissions()
                        requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                    }
                ) {
                    Text("Berechtigung erteilen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setNotificationRationaleDialogVisibility(false)
                        // Auch ohne Notification-Berechtigung kann die App funktionieren
                        if (uiState.hasLocationPermission) {
                            viewModel.toggleRunning(context)
                        }
                    }
                ) {
                    Text("Ohne Benachrichtigungen fortfahren")
                }
            }
        )
    }

    // Haupt-UI
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
            // Erweiterte Berechtigungs-Informationskarte
            if (uiState.showPermissionDeniedMessage) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when {
                                !uiState.hasLocationPermission && !uiState.hasNotificationPermission ->
                                    "Standort- und Benachrichtigungsberechtigungen werden benötigt"
                                !uiState.hasLocationPermission ->
                                    "Standortberechtigung wird für GPS-Tracking benötigt"
                                !uiState.hasNotificationPermission ->
                                    "Benachrichtigungsberechtigung empfohlen für Lauf-Updates"
                                else -> "Alle Berechtigungen erteilt"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = if (!uiState.hasLocationPermission) {
                                "Tippen Sie auf 'Lauf starten' um die Berechtigung anzufordern."
                            } else if (!uiState.hasNotificationPermission) {
                                "Die App funktioniert auch ohne Benachrichtigungen."
                            } else {
                                "Sie können jetzt mit dem Laufen beginnen!"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                latitude = uiState.startLatitude,
                longitude = uiState.startLongitude,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer
            )

            // End Location Card
            LocationCard(
                title = "End Position",
                latitude = uiState.endLatitude,
                longitude = uiState.endLongitude,
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer
            )

            // Distanzanzeige
            val distance = viewModel.calculateDistance()
            if (distance != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Distanz",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "${String.format("%.1f", distance)} m",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        // Control Button Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            // Haupt Start/Stop Button
            Button(
                onClick = {
                    if (!uiState.hasLocationPermission) {
                        requestPermissionsIfNeeded()
                    } else {
                        viewModel.toggleRunning(context)
                    }
                },
                enabled = !uiState.isGettingLocation,
                modifier = Modifier
                    .size(140.dp)
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isRunning)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(70.dp)
            ) {
                Text(
                    text = if (uiState.isRunning) "Lauf beenden" else "Lauf starten",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            // Status Text
            Text(
                text = uiState.statusMessage,
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