package com.example.syntaxfitness.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.syntaxfitness.MainActivity

class RunNotificationService(private val context: Context) {

    // NotificationManager ist das System-Service für alle Benachrichtigungen
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Konstanten für bessere Wartbarkeit und Typsicherheit
    companion object {
        private const val CHANNEL_ID = "run_notifications"
        private const val CHANNEL_NAME = "Lauf Benachrichtigungen"
        private const val CHANNEL_DESCRIPTION = "Benachrichtigungen für Lauf-bezogene Informationen"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createRunNotificationChannel()
    }

    private fun createRunNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT // Mittlere Priorität - zeigt Benachrichtigung an, aber unterbricht nicht
            ).apply {
                description = CHANNEL_DESCRIPTION

                enableVibration(true) // Kurze Vibration bei neuen Benachrichtigungen
                setShowBadge(true)    // App-Icon Badge auf unterstützten Launchers
            }

            // Channel beim System registrieren
            notificationManager.createNotificationChannel(channel)
        }
    }


    fun showRunInfoNotification(message: String) {
        // PendingIntent erstelle - definiert was passiert, wenn User auf Benachrichtigung tippt
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // PendingIntent mit UpdateCurrent Flag - ersetzt bestehende PendingIntents mit derselben Request Code
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // FLAG_IMMUTABLE für Android 12+ Sicherheit
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Temporäres Icon - sollte durch App-spezifisches Icon ersetzt werden
            .setContentTitle("SyntaxFitness") // App-Name als Titel
            .setContentText(message) // Der übergebene Text als Hauptinhalt
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Standard-Priorität für ausgewogene Aufmerksamkeit
            .setContentIntent(pendingIntent) // Was passiert beim Antippen
            .setAutoCancel(true) // Benachrichtigung wird automatisch entfernt wenn angetippt
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Standard Sound, Vibration, und Lichter
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}