package com.example.syntaxfitness.ui.settings.screen

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.example.syntaxfitness.data.model.SettingsItem
import com.example.syntaxfitness.ui.running.component.AnimatedGradientBackground
import com.example.syntaxfitness.ui.running.component.AnimatedGradientLine
import com.example.syntaxfitness.ui.running.component.GlassmorphismDialog
import com.example.syntaxfitness.ui.running.viewmodel.RunningViewModel
import com.example.syntaxfitness.ui.settings.component.PermissionStatusCard
import com.example.syntaxfitness.ui.settings.component.SettingsCard
import com.example.syntaxfitness.ui.settings.component.StatsSummaryCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    viewModel: RunningViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showLocationRationaleDialog by remember { mutableStateOf(false) }
    var showNotificationRationaleDialog by remember { mutableStateOf(false) }
    var showNotificationSettingsDialog by remember { mutableStateOf(false) }

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
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkAllPermissions(context)
    }


    LaunchedEffect(Unit) {
        viewModel.checkAllPermissions(context)
    }

    fun handleLocationPermissionRequest() {
        val activity = context as? ComponentActivity ?: return

        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (shouldShowRationale) {
            showLocationRationaleDialog = true
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    fun handleNotificationPermissionRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val activity = context as? ComponentActivity ?: return

            if (uiState.hasNotificationPermission) {
                showNotificationSettingsDialog = true
            } else {
                val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                )

                if (shouldShowRationale) {
                    showNotificationRationaleDialog = true
                } else {
                    requestPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                }
            }
        } else {
            // For Android 12 and below, notifications are enabled by default
            showNotificationSettingsDialog = true
        }
    }

    fun openNotificationSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        settingsLauncher.launch(intent)
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        settingsLauncher.launch(intent)
    }

    fun areNotificationsEnabledAtSystemLevel(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    val settingsItems = listOf(
        SettingsItem(
            title = "Standortberechtigung",
            description = if (uiState.hasLocationPermission) {
                "GPS-Zugriff aktiv für Laufverfolgung"
            } else {
                "GPS-Zugriff für Laufverfolgung erforderlich"
            },
            icon = Icons.Default.LocationOn,
            hasSwitch = true,
            isEnabled = uiState.hasLocationPermission,
            onToggle = {
                if (!uiState.hasLocationPermission) {
                    handleLocationPermissionRequest()
                } else {
                    openAppSettings()
                }
            }
        ),
        SettingsItem(
            title = "Benachrichtigungen",
            description = when {
                uiState.hasNotificationPermission && areNotificationsEnabledAtSystemLevel() ->
                    "Lauf-Updates und Erinnerungen aktiviert"

                uiState.hasNotificationPermission && !areNotificationsEnabledAtSystemLevel() ->
                    "Berechtigung erteilt, aber in Systemeinstellungen deaktiviert"

                else ->
                    "Benachrichtigungen für Lauf-Updates aktivieren"
            },
            icon = if (uiState.hasNotificationPermission && areNotificationsEnabledAtSystemLevel()) {
                Icons.Default.Notifications
            } else {
                Icons.Default.NotificationsOff
            },
            hasSwitch = true,
            isEnabled = uiState.hasNotificationPermission && areNotificationsEnabledAtSystemLevel(),
            onToggle = {
                handleNotificationPermissionRequest()
            }
        ),
        SettingsItem(
            title = "Alle Läufe löschen",
            description = "Entfernt alle gespeicherten Laufdaten permanent",
            icon = Icons.Default.Delete,
            hasSwitch = false,
            isDestructive = true,
            onClick = { showDeleteAllDialog = true }
        ),
        SettingsItem(
            title = "Datenschutz",
            description = "Ihre Daten bleiben lokal auf diesem Gerät",
            icon = Icons.Default.Security,
            hasSwitch = false,
            onClick = { /* TODO: Show privacy policy/info */ }
        ),
        SettingsItem(
            title = "App-Informationen",
            description = "Version 1.0.0 • SyntaxFitness by [Your Name]",
            icon = Icons.Default.Info,
            hasSwitch = false,
            onClick = { /* TODO: Show detailed app info, licenses, etc. */ }
        )
    )

    if (showDeleteAllDialog) {
        GlassmorphismDialog(
            title = "Alle Läufe löschen?",
            text = "Diese Aktion kann nicht rückgängig gemacht werden. Alle Ihre gespeicherten Laufdaten werden dauerhaft von diesem Gerät entfernt.",
            onConfirm = {
                viewModel.deleteAllRuns()
                showDeleteAllDialog = false
            },
            onDismiss = { showDeleteAllDialog = false }
        )
    }

    if (showLocationRationaleDialog) {
        GlassmorphismDialog(
            title = "Standortberechtigung erforderlich",
            text = "SyntaxFitness benötigt Zugriff auf Ihren Standort, um Ihre Laufstrecke präzise zu verfolgen und die zurückgelegte Distanz zu berechnen. Ihre Standortdaten bleiben lokal auf Ihrem Gerät gespeichert.",
            onConfirm = {
                showLocationRationaleDialog = false
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onDismiss = { showLocationRationaleDialog = false }
        )
    }

    if (showNotificationRationaleDialog) {
        GlassmorphismDialog(
            title = "Benachrichtigungen aktivieren",
            text = "Erlauben Sie Benachrichtigungen, um wichtige Updates über Ihre Läufe zu erhalten. Sie können diese jederzeit in den Einstellungen deaktivieren.",
            onConfirm = {
                showNotificationRationaleDialog = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                }
            },
            onDismiss = { showNotificationRationaleDialog = false }
        )
    }

    if (showNotificationSettingsDialog) {
        GlassmorphismDialog(
            title = "Benachrichtigungseinstellungen",
            text = "Um Benachrichtigungseinstellungen zu ändern, werden Sie zu den Systemeinstellungen weitergeleitet. Dort können Sie Benachrichtigungen für SyntaxFitness aktivieren oder deaktivieren.",
            onConfirm = {
                showNotificationSettingsDialog = false
                openNotificationSettings()
            },
            onDismiss = { showNotificationSettingsDialog = false }
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
                // Header with navigation and system settings access
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Zurück",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Text(
                            text = "Einstellungen",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // System settings button - provides direct access to app settings
                        IconButton(
                            onClick = { openAppSettings() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.1f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "System-Einstellungen öffnen",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedGradientLine()
                }
            }

            // Statistics summary - only show if user has recorded runs
            if (uiState.totalRuns > 0) {
                item {
                    StatsSummaryCard(
                        totalRuns = uiState.totalRuns,
                        totalDistance = uiState.totalDistance
                    )
                }
            }

            // Enhanced permission status indicator
            item {
                PermissionStatusCard(
                    hasLocationPermission = uiState.hasLocationPermission,
                    hasNotificationPermission = uiState.hasNotificationPermission,
                    notificationsEnabledAtSystemLevel = areNotificationsEnabledAtSystemLevel()
                )
            }

            // Settings items with enhanced interactivity
            itemsIndexed(
                items = settingsItems,
                key = { _, item -> item.title }
            ) { index, item ->
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
                    SettingsCard(item = item)
                }
            }
        }
    }
}

