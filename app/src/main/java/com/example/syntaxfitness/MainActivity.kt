package com.example.syntaxfitness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.syntaxfitness.navigation.SyntaxFitnessNavigation
import com.example.syntaxfitness.ui.theme.SyntaxFitnessTheme
import com.example.syntaxfitness.work.AfterRunWorker
import java.util.concurrent.TimeUnit

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

    private fun scheduleReminder() {
        val reminderWork = OneTimeWorkRequestBuilder<AfterRunWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .addTag("post_app_closure_reminder")
            .build()

        WorkManager.getInstance(this)
            .enqueue(reminderWork)

        android.util.Log.d("MainActivity", "scheduleReminder: Reminder scheduled")
    }

    override fun onStop() {
        scheduleReminder()
        android.util.Log.d("MainActivity", "onStop: Reminder scheduled")
        super.onStop()
    }
}