package com.example.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.TaskItem

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleTaskReminder(task: TaskItem) {
        if (!task.hasReminder || task.isCompleted || task.dueTimestamp <= System.currentTimeMillis()) {
            return
        }

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TITLE, task.title)
            putExtra(EXTRA_TASK_DESC, task.description)
            putExtra(EXTRA_TASK_CATEGORY, task.category)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        task.dueTimestamp,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        task.dueTimestamp,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    task.dueTimestamp,
                    pendingIntent
                )
            }
            Log.d("ReminderScheduler", "Scheduled reminder for task ${task.id} at ${task.dueTimestamp}")
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Failed to schedule reminder", e)
        }
    }

    fun cancelTaskReminder(taskId: Long) {
        try {
            val intent = Intent(context, ReminderBroadcastReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Error cancelling reminder", e)
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_DESC = "extra_task_desc"
        const val EXTRA_TASK_CATEGORY = "extra_task_category"
    }
}
