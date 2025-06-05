package com.example.syntaxfitness.ui.settings.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syntaxfitness.ui.running.component.AnimatedGradientBackground
import com.example.syntaxfitness.ui.running.component.AnimatedGradientLine
import com.example.syntaxfitness.ui.running.component.GlassmorphismDialog
import com.example.syntaxfitness.ui.running.viewmodel.RunningViewModel
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

    // Settings items data
    val settingsItems = listOf(
        SettingsItem(
            title = "Standortberechtigung",
            description = "GPS-Zugriff für Laufverfolgung",
            icon = Icons.Default.LocationOn,
            hasSwitch = true,
            isEnabled = uiState.hasLocationPermission,
            onToggle = { viewModel.checkAllPermissions(context) }
        ),
        SettingsItem(
            title = "Benachrichtigungen",
            description = "Lauf-Updates und Erinnerungen",
            icon = Icons.Default.Notifications,
            hasSwitch = true,
            isEnabled = uiState.hasNotificationPermission,
            onToggle = { viewModel.checkAllPermissions(context) }
        ),
        SettingsItem(
            title = "Alle Läufe löschen",
            description = "Entfernt alle gespeicherten Laufdaten",
            icon = Icons.Default.Delete,
            hasSwitch = false,
            isDestructive = true,
            onClick = { showDeleteAllDialog = true }
        ),
        SettingsItem(
            title = "Datenschutz",
            description = "Ihre Daten bleiben lokal gespeichert",
            icon = Icons.Default.Security,
            hasSwitch = false,
            onClick = { /* TODO: Show privacy info */ }
        ),
        SettingsItem(
            title = "App-Info",
            description = "Version 1.0 • SyntaxFitness",
            icon = Icons.Default.Info,
            hasSwitch = false,
            onClick = { /* TODO: Show app info */ }
        )
    )

    // Delete all runs confirmation dialog
    if (showDeleteAllDialog) {
        GlassmorphismDialog(
            title = "Alle Läufe löschen?",
            text = "Diese Aktion kann nicht rückgängig gemacht werden. Alle Ihre Laufdaten werden dauerhaft entfernt.",
            onConfirm = {
                viewModel.deleteAllRuns()
                showDeleteAllDialog = false
            },
            onDismiss = { showDeleteAllDialog = false }
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
                // Header with back button
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

                        // Placeholder for symmetry
                        Box(modifier = Modifier.size(48.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedGradientLine()
                }
            }

            // Statistics summary
            if (uiState.totalRuns > 0) {
                item {
                    StatsSummaryCard(
                        totalRuns = uiState.totalRuns,
                        totalDistance = uiState.totalDistance
                    )
                }
            }

            // Settings items
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

@Composable
private fun StatsSummaryCard(
    totalRuns: Int,
    totalDistance: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Deine Statistiken",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalRuns",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B5CF6)
                    )
                    Text(
                        text = "Läufe",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f km", totalDistance / 1000),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = "Gesamt",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(item: SettingsItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (item.onClick != null) {
                    Modifier.clickable { item.onClick.invoke() }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = if (item.isDestructive) {
                        Color(0xFFEF4444)
                    } else {
                        Color(0xFF8B5CF6)
                    },
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 16.dp)
                )

                Column {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (item.isDestructive) {
                            Color(0xFFEF4444)
                        } else {
                            Color.White
                        }
                    )
                    Text(
                        text = item.description,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            if (item.hasSwitch) {
                Switch(
                    checked = item.isEnabled,
                    onCheckedChange = { item.onToggle?.invoke() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF8B5CF6),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

private data class SettingsItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val hasSwitch: Boolean = false,
    val isEnabled: Boolean = false,
    val isDestructive: Boolean = false,
    val onToggle: (() -> Unit)? = null,
    val onClick: (() -> Unit)? = null
)