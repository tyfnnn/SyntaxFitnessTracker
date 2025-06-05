package com.example.syntaxfitness.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.syntaxfitness.service.RunNotificationService

class RunReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun  doWork(): Result {
        return try {
            val notificationService = RunNotificationService(applicationContext)
            notificationService.showRunInfoNotification(
                message = "Zeit für deinen täglichen Lauf! 🏃‍♂️💪"
            )
            android.util.Log.d(
                "RunReminderWorker",
                "Tägliche Lauf-Erinnerung erfolgreich gesendet"
            )
            Result.success()
        } catch (exception: Exception) {
            android.util.Log.e(
                "RunReminderWorker",
                "Fehler beim Senden der täglichen Erinnerung: ${exception.message}",
                exception
            )
            Result.failure()
        }
    }
}