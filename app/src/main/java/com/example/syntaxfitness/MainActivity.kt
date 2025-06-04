package com.example.syntaxfitness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.syntaxfitness.navigation.SyntaxFitnessNavigation
import com.example.syntaxfitness.ui.running.screen.RunningScreen
import com.example.syntaxfitness.ui.theme.SyntaxFitnessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SyntaxFitnessTheme {
                SyntaxFitnessNavigation(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}