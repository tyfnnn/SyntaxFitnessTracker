package com.example.syntaxfitness.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.syntaxfitness.service.RunNotificationService

class AfterRunWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val notificationService = RunNotificationService(applicationContext)

            notificationService.showRunInfoNotification(
                message = "Wie war dein Lauf heute? 🏃‍♂️"
            )

            Result.success()
        } catch (exception: Exception) {
            android.util.Log.e(
                "AfterRunWorker",
                "doWork: ",
                exception
            )
            Result.failure()
        }


    }

}