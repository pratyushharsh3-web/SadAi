package com.example.reminders

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.SadbhavnaApp

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(ReminderScheduler.EXTRA_TASK_ID, 0L)
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TASK_TITLE) ?: "Sadbhavna Reminder"
        val desc = intent.getStringExtra(ReminderScheduler.EXTRA_TASK_DESC) ?: "Aapka ek zaroori task scheduled hai."
        val category = intent.getStringExtra(ReminderScheduler.EXTRA_TASK_CATEGORY) ?: "Reminder"

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, SadbhavnaApp.CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("⏰ $title")
            .setContentText(desc)
            .setSubText(category)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setColor(Color.parseColor("#00E5FF"))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(taskId.toInt(), notification)
    }
}
