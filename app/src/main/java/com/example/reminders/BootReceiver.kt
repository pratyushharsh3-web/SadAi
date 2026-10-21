package com.example.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.SadbhavnaDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootReceiver", "Device rebooted, rescheduling active reminders...")
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = SadbhavnaDatabase.getDatabase(context)
                    val upcoming = db.taskDao().getUpcomingReminders(System.currentTimeMillis())
                    val scheduler = ReminderScheduler(context)
                    for (task in upcoming) {
                        scheduler.scheduleTaskReminder(task)
                    }
                    Log.d("BootReceiver", "Successfully restored ${upcoming.size} reminders.")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to restore reminders on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
