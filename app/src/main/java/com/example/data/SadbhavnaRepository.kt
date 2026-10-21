package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class SadbhavnaRepository(private val database: SadbhavnaDatabase) {

    val allTasks: Flow<List<TaskItem>> = database.taskDao().getAllTasks()
    val pendingTasks: Flow<List<TaskItem>> = database.taskDao().getPendingTasks()
    val allMessages: Flow<List<ChatMessage>> = database.chatMessageDao().getAllMessages()
    val allPreferences: Flow<List<UserPreference>> = database.userPreferenceDao().getAllPreferences()

    fun getTodayTasks(): Flow<List<TaskItem>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.timeInMillis
        return database.taskDao().getTasksForDay(startOfDay, endOfDay)
    }

    suspend fun insertTask(task: TaskItem): Long {
        return database.taskDao().insertTask(task)
    }

    suspend fun updateTask(task: TaskItem) {
        database.taskDao().updateTask(task)
    }

    suspend fun deleteTask(task: TaskItem) {
        database.taskDao().deleteTask(task)
    }

    suspend fun deleteTaskById(id: Long) {
        database.taskDao().deleteTaskById(id)
    }

    suspend fun getTaskById(id: Long): TaskItem? {
        return database.taskDao().getTaskById(id)
    }

    suspend fun getUpcomingReminders(now: Long = System.currentTimeMillis()): List<TaskItem> {
        return database.taskDao().getUpcomingReminders(now)
    }

    suspend fun insertMessage(message: ChatMessage): Long {
        return database.chatMessageDao().insertMessage(message)
    }

    suspend fun clearChatHistory() {
        database.chatMessageDao().clearHistory()
    }

    suspend fun getPreference(key: String, defaultValue: String): String {
        return database.userPreferenceDao().getPreference(key) ?: defaultValue
    }

    suspend fun setPreference(key: String, value: String) {
        database.userPreferenceDao().setPreference(UserPreference(key, value))
    }
}
