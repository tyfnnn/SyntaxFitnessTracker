package com.example.syntaxfitness.ui.running.screen

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.syntaxfitness.ui.running.component.RunHistoryItem
import com.example.syntaxfitness.ui.running.viewmodel.RunningViewModel
import com.example.syntaxfitness.ui.theme.SyntaxFitnessTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun RunningScreen(
    modifier: Modifier = Modifier,
    viewModel: RunningViewModel = koinViewModel()
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
                val permissionsToRequest = viewModel.getMissingPermissions()
                if (permissionsToRequest.isNotEmpty()) {
                    requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
                }
            }
        }
    }

    // Permission dialogs (unchanged)
    if (uiState.showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setPermissionRationaleDialogVisibility(false) },
            title = { Text("Standortberechtigung erforderlich", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Diese App benötigt Zugriff auf Ihren Standort, um Ihre Laufstrecke zu verfolgen. " +
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
                ) { Text("Berechtigung erteilen") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setPermissionRationaleDialogVisibility(false) }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    if (uiState.showNotificationRationaleDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setNotificationRationaleDialogVisibility(false) },
            title = { Text("Benachrichtigungsberechtigung", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Diese App möchte Ihnen Benachrichtigungen senden, um Sie über " +
                            "den Start und das Ende Ihrer Läufe zu informieren.",
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
                ) { Text("Berechtigung erteilen") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setNotificationRationaleDialogVisibility(false)
                        if (uiState.hasLocationPermission) {
                            viewModel.toggleRunning(context)
                        }
                    }
                ) { Text("Ohne Benachrichtigungen fortfahren") }
            }
        )
    }

    // Main UI with scrollable content
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Text(
                text = "Lauf Tracker",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
            )
        }

        // Statistics Section
        if (uiState.totalRuns > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Statistiken",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${uiState.totalRuns}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("Läufe", fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${String.format("%.1f", uiState.totalDistance)}m",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("Gesamt", fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${String.format("%.1f", uiState.averageDistance)}m",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("Durchschnitt", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Permission info card
        if (uiState.showPermissionDeniedMessage) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                    }
                }
            }
        }

        // Location Cards
        item {
            LocationCard(
                title = "Start Position",
                latitude = uiState.startLatitude,
                longitude = uiState.startLongitude,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer
            )
        }

        item {
            LocationCard(
                title = "End Position",
                latitude = uiState.endLatitude,
                longitude = uiState.endLongitude,
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer
            )
        }

        // Distance Display
        val distance = viewModel.calculateDistance()
        if (distance != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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

        // Control Button
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        if (!uiState.hasLocationPermission) {
                            requestPermissionsIfNeeded()
                        } else {
                            viewModel.toggleRunning(context)
                        }
                    },
                    enabled = !uiState.isGettingLocation,
                    modifier = Modifier.size(140.dp),
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

                Text(
                    text = uiState.statusMessage,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Run History Section
        if (uiState.runHistory.isNotEmpty()) {
            item {
                Text(
                    text = "Lauf-Historie",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(
                count = minOf(uiState.runHistory.size, 10),
                key = { index -> uiState.runHistory[index].id }
            ) { index ->
                val run = uiState.runHistory[index]
                RunHistoryItem(
                    run = run,
                    onItemClick = {
                        // Handle click - could navigate to detail screen
                        // For now, just log
                        println("Clicked on run ${run.id}")
                    }
                )
            }

            if (uiState.runHistory.size > 10) {
                item {
                    TextButton(
                        onClick = { /* Navigate to full history screen */ },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Alle ${uiState.runHistory.size} Läufe anzeigen")
                    }
                }
            }
        }

        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(32.dp))
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