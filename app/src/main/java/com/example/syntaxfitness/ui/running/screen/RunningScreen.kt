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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.syntaxfitness.ui.running.component.LocationCard
import com.example.syntaxfitness.ui.running.viewmodel.RunningViewModel
import com.example.syntaxfitness.ui.theme.SyntaxFitnessTheme

@Composable
fun RunningScreen(
    modifier: Modifier = Modifier,
    viewModel: RunningViewModel = viewModel() // Automatische ViewModel-Erstellung
) {
    val context = LocalContext.current

    // StateFlow beobachten - automatische Rekomposition bei Änderungen
    val uiState by viewModel.uiState.collectAsState()

    // Permission Launcher - bleibt in der UI, da es UI-spezifisch ist
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        // Berechtigung an ViewModel weiterleiten
        viewModel.updateLocationPermission(fineLocationGranted, coarseLocationGranted)
    }

    // Funktion für Berechtigungsanfragen - vereinfacht durch ViewModel
    fun handlePermissionRequest() {
        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            context as androidx.activity.ComponentActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) || ActivityCompat.shouldShowRequestPermissionRationale(
            context as androidx.activity.ComponentActivity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (shouldShowRationale) {
            viewModel.setPermissionRationaleDialogVisibility(true)
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Permission Rationale Dialog - gesteuert durch ViewModel-Zustand
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
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
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

    // Haupt-UI - komplett zustandslos, alle Daten kommen vom ViewModel
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
            // Permission Fehlermeldung - gesteuert durch ViewModel
            if (uiState.showPermissionDeniedMessage) {
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
                            text = "Standortberechtigung erforderlich für GPS-Tracking",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Tippen Sie auf 'Berechtigung anfordern' für weitere Informationen.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            // Start Location Card - Daten direkt aus dem ViewModel
            LocationCard(
                title = "Start Position",
                latitude = uiState.startLatitude,
                longitude = uiState.startLongitude,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer
            )

            // End Location Card - Daten direkt aus dem ViewModel
            LocationCard(
                title = "End Position",
                latitude = uiState.endLatitude,
                longitude = uiState.endLongitude,
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer
            )

            // Distanzanzeige - neue Funktion durch ViewModel möglich
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
            // Permission Request Button - nur anzeigen wenn nötig
            if (!uiState.hasLocationPermission) {
                OutlinedButton(
                    onClick = { handlePermissionRequest() },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Standortberechtigung anfordern")
                }
            }

            // Start/Stop Button - komplett über ViewModel gesteuert
            Button(
                onClick = {
                    if (!uiState.hasLocationPermission) {
                        handlePermissionRequest()
                    } else {
                        // Einfacher Aufruf - alle Logik im ViewModel
                        viewModel.toggleRunning(context)
                    }
                },
                enabled = !uiState.isGettingLocation,
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isRunning)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(60.dp)
            ) {
                Text(
                    text = if (uiState.isRunning) "STOP" else "START",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Status Text - direkt aus dem ViewModel
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