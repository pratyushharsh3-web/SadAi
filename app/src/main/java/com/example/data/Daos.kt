package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueTimestamp ASC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueTimestamp ASC")
    fun getPendingTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE dueTimestamp >= :startOfDay AND dueTimestamp <= :endOfDay ORDER BY dueTimestamp ASC")
    fun getTasksForDay(startOfDay: Long, endOfDay: Long): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskItem?

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND hasReminder = 1 AND dueTimestamp > :now ORDER BY dueTimestamp ASC")
    suspend fun getUpcomingReminders(now: Long): List<TaskItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT 50")
    fun getRecentMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface UserPreferenceDao {
    @Query("SELECT * FROM user_preferences")
    fun getAllPreferences(): Flow<List<UserPreference>>

    @Query("SELECT value FROM user_preferences WHERE `key` = :key LIMIT 1")
    suspend fun getPreference(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPreference(pref: UserPreference)

    @Query("DELETE FROM user_preferences WHERE `key` = :key")
    suspend fun deletePreference(key: String)
}
