package com.example.syntaxfitness.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class SettingsItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val hasSwitch: Boolean = false,
    val isEnabled: Boolean = false,
    val isDestructive: Boolean = false,
    val onToggle: (() -> Unit)? = null,
    val onClick: (() -> Unit)? = null
)