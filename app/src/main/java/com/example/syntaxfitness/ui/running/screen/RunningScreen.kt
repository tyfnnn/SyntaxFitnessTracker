@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.syntaxfitness.ui.running.screen

import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.syntaxfitness.ui.running.component.AnimatedGradientBackground
import com.example.syntaxfitness.ui.running.component.AnimatedGradientLine
import com.example.syntaxfitness.ui.running.component.DistanceCard
import com.example.syntaxfitness.ui.running.component.GlassmorphismDialog
import com.example.syntaxfitness.ui.running.component.GlassmorphismRunHistoryItem
import com.example.syntaxfitness.ui.running.component.LocationCardsSection
import com.example.syntaxfitness.ui.running.component.MainRunningCard
import com.example.syntaxfitness.ui.running.component.StatCard
import com.example.syntaxfitness.ui.running.viewmodel.RunningViewModel
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun RunningScreen(
    modifier: Modifier = Modifier,
    viewModel: RunningViewModel = koinViewModel(),
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkAllPermissions(context)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.updateLocationPermission(fineLocationGranted, coarseLocationGranted)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] == true
            viewModel.updateNotificationPermission(notificationGranted)
        }

        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.toggleRunning(context)
        }
    }

    fun requestPermissionsIfNeeded() {
        val shouldShowLocationRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            context as ComponentActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val shouldShowNotificationRationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                context,
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

    // Permission dialogs
    if (uiState.showPermissionRationaleDialog) {
        GlassmorphismDialog(
            title = "Standortberechtigung erforderlich",
            text = "Diese App benötigt Zugriff auf Ihren Standort, um Ihre Laufstrecke zu verfolgen.",
            onConfirm = {
                viewModel.setPermissionRationaleDialogVisibility(false)
                val permissionsToRequest = viewModel.getMissingPermissions()
                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            },
            onDismiss = { viewModel.setPermissionRationaleDialogVisibility(false) }
        )
    }

    if (uiState.showNotificationRationaleDialog) {
        GlassmorphismDialog(
            title = "Benachrichtigungsberechtigung",
            text = "Diese App möchte Ihnen Benachrichtigungen über Ihre Läufe senden.",
            onConfirm = {
                viewModel.setNotificationRationaleDialogVisibility(false)
                val permissionsToRequest = viewModel.getMissingPermissions()
                requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
            },
            onDismiss = {
                viewModel.setNotificationRationaleDialogVisibility(false)
                if (uiState.hasLocationPermission) {
                    viewModel.toggleRunning(context)
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Animated gradient background
        AnimatedGradientBackground()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header with settings button and animated gradient line
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Placeholder für symmetrisches Layout
                        Box(modifier = Modifier.size(48.dp))

                        Text(
                            text = "SyntaxFitness",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // Settings Button
                        IconButton(
                            onClick = {
                                onNavigateToSettings()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.1f),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Einstellungen",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedGradientLine()
                }
            }

            // Statistics cards
            if (uiState.totalRuns > 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Läufe",
                            value = "${uiState.totalRuns}",
                            icon = Icons.AutoMirrored.Filled.DirectionsRun,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Gesamt",
                            value = "${String.format(Locale.US, "%.1f", uiState.totalDistance)}km",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Main running control card
            item {
                MainRunningCard(
                    isRunning = uiState.isRunning,
                    isGettingLocation = uiState.isGettingLocation,
                    statusMessage = uiState.statusMessage,
                    onToggleRun = {
                        if (!uiState.hasLocationPermission) {
                            requestPermissionsIfNeeded()
                        } else {
                            viewModel.toggleRunning(context)
                        }
                    }
                )
            }

            // Location cards
            item {
                LocationCardsSection(
                    startLat = uiState.startLatitude,
                    startLng = uiState.startLongitude,
                    endLat = uiState.endLatitude,
                    endLng = uiState.endLongitude
                )
            }

            // Distance display
            val distance = viewModel.calculateDistance()
            if (distance != null) {
                item {
                    DistanceCard(distance = distance)
                }
            }

            // Run history
            if (uiState.runHistory.isNotEmpty()) {
                item {
                    Text(
                        text = "Lauf-Historie",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                itemsIndexed(
                    items = uiState.runHistory.take(5),
                    key = { _, run -> run.id }
                ) { index, run ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = index * 100
                            )
                        ) + slideInVertically(
                            animationSpec = tween(
                                durationMillis = 300,
                                delayMillis = index * 100
                            )
                        )
                    ) {
                        GlassmorphismRunHistoryItem(
                            run = run,
                            onDelete = { deletedRun ->
                                viewModel.deleteRun(deletedRun)
                            }
                        )
                    }
                }
            }
        }
    }
}