package com.example.syntaxfitness.ui.running.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syntaxfitness.ui.theme.SyntaxFitnessTheme

@Composable
fun GlassmorphismDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Berechtigung erteilen", color = Color(0xFF8B5CF6))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = Color.White.copy(alpha = 0.7f))
            }
        },
        containerColor = Color.Black.copy(alpha = 0.8f),
        shape = RoundedCornerShape(20.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun GlassmorphismDialogPreview() {
    SyntaxFitnessTheme {
        // Dunkler Hintergrund für bessere Sichtbarkeit des Glassmorphism-Effekts
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GlassmorphismDialog(
                title = "Standortberechtigung erforderlich",
                text = "Diese App benötigt Zugriff auf Ihren Standort, um Ihre Laufstrecke zu verfolgen.",
                onConfirm = { /* Preview - keine Aktion */ },
                onDismiss = { /* Preview - keine Aktion */ }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GlassmorphismDialogNotificationPreview() {
    SyntaxFitnessTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GlassmorphismDialog(
                title = "Benachrichtigungsberechtigung",
                text = "Diese App möchte Ihnen Benachrichtigungen über Ihre Läufe senden.",
                onConfirm = { /* Preview - keine Aktion */ },
                onDismiss = { /* Preview - keine Aktion */ }
            )
        }
    }
}

@Preview(showBackground = true, name = "Kurzer Text")
@Composable
fun GlassmorphismDialogShortTextPreview() {
    SyntaxFitnessTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GlassmorphismDialog(
                title = "Hallo",
                text = "Welt",
                onConfirm = { /* Preview - keine Aktion */ },
                onDismiss = { /* Preview - keine Aktion */ }
            )
        }
    }
}