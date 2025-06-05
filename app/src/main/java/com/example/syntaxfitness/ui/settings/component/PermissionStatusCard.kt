package com.example.syntaxfitness.ui.settings.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PermissionStatusCard(
    hasLocationPermission: Boolean,
    hasNotificationPermission: Boolean,
    notificationsEnabledAtSystemLevel: Boolean
) {
    val effectiveNotificationStatus = hasNotificationPermission && notificationsEnabledAtSystemLevel
    val allPermissionsOptimal = hasLocationPermission && effectiveNotificationStatus

    val statusColor = when {
        allPermissionsOptimal -> Color(0xFF10B981)
        hasLocationPermission && hasNotificationPermission -> Color(0xFFF59E0B)
        hasLocationPermission -> Color(0xFF3B82F6)
        else -> Color(0xFFEF4444)
    }

    val statusText = when {
        allPermissionsOptimal -> "Alle Funktionen verfügbar"
        hasLocationPermission && hasNotificationPermission && !notificationsEnabledAtSystemLevel ->
            "Benachrichtigungen in Systemeinstellungen deaktiviert"
        hasLocationPermission && !hasNotificationPermission ->
            "Laufverfolgung aktiv, Benachrichtigungen optional"
        hasLocationPermission ->
            "Laufverfolgung aktiv"
        else ->
            "Standortberechtigung für Kernfunktionen erforderlich"
    }

    val statusDescription = when {
        allPermissionsOptimal -> "Laufverfolgung und Benachrichtigungen funktionieren einwandfrei"
        hasLocationPermission && hasNotificationPermission && !notificationsEnabledAtSystemLevel ->
            "Tippen Sie auf System-Einstellungen, um Benachrichtigungen zu aktivieren"
        hasLocationPermission ->
            "Sie können Läufe verfolgen. Benachrichtigungen sind optional"
        else ->
            "Ohne Standortberechtigung können keine Läufe aufgezeichnet werden"
    }

    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    allPermissionsOptimal -> Icons.Default.Security
                    hasLocationPermission -> Icons.Default.LocationOn
                    else -> Icons.Default.NotificationsOff
                },
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.Companion.size(24.dp)
            )

            Column(
                modifier = Modifier.Companion
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = statusText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Companion.Medium,
                    color = statusColor
                )
                Text(
                    text = statusDescription,
                    fontSize = 14.sp,
                    color = Color.Companion.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}