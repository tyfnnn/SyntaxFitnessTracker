package com.example.syntaxfitness.ui.settings.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syntaxfitness.data.model.SettingsItem

@Composable
fun SettingsCard(item: SettingsItem) {
    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .then(
                if (item.onClick != null) {
                    Modifier.Companion.clickable { item.onClick.invoke() }
                } else {
                    Modifier.Companion
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Companion.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.Companion.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.Companion.CenterVertically,
                modifier = Modifier.Companion.weight(1f)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = if (item.isDestructive) {
                        Color(0xFFEF4444)
                    } else {
                        Color(0xFF8B5CF6)
                    },
                    modifier = Modifier.Companion
                        .size(24.dp)
                        .padding(end = 16.dp)
                )

                Column {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Companion.Medium,
                        color = if (item.isDestructive) {
                            Color(0xFFEF4444)
                        } else {
                            Color.Companion.White
                        }
                    )
                    Text(
                        text = item.description,
                        fontSize = 14.sp,
                        color = Color.Companion.White.copy(alpha = 0.6f)
                    )
                }
            }

            if (item.hasSwitch) {
                Switch(
                    checked = item.isEnabled,
                    onCheckedChange = { item.onToggle?.invoke() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Companion.White,
                        checkedTrackColor = Color(0xFF8B5CF6),
                        uncheckedThumbColor = Color.Companion.White,
                        uncheckedTrackColor = Color.Companion.White.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}