package com.example.syntaxfitness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.syntaxfitness.navigation.SyntaxFitnessNavigation
import com.example.syntaxfitness.ui.theme.SyntaxFitnessTheme
import com.example.syntaxfitness.work.AfterRunWorker
import com.example.syntaxfitness.work.RunReminderWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    companion object {
        private const val DAILY_REMINDER_WORK_NAME = "daily_run_reminder"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        scheduleDailyReminder()

        setContent {
            SyntaxFitnessTheme {
                SyntaxFitnessNavigation(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    private fun scheduleDailyReminder() {
        try {
            val dailyReminderRequest = PeriodicWorkRequestBuilder<RunReminderWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setInitialDelay(10, TimeUnit.SECONDS)
                .addTag("daily_run_reminder_tag")
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                DAILY_REMINDER_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                dailyReminderRequest
            )
        } catch (exception: Exception) {
            android.util.Log.e(
                "MainActivity",
                "Fehler beim Planen der täglichen Erinnerung: ${exception.message}",
                exception
            )
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