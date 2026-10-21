package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class SadbhavnaApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Sadbhavna Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled tasks, alarms, and study reminders"
                enableVibration(true)
            }

            val assistantChannel = NotificationChannel(
                CHANNEL_ASSISTANT,
                "Sadbhavna Assistant Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General notifications from Sadbhavna AI"
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(assistantChannel)
        }
    }

    companion object {
        const val CHANNEL_REMINDERS = "sadbhavna_reminders"
        const val CHANNEL_ASSISTANT = "sadbhavna_assistant"
    }
}
