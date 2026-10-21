package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "General", // "Study", "Routine", "Call", "Work", "General"
    val dueTimestamp: Long, // Epoch millis
    val isCompleted: Boolean = false,
    val hasReminder: Boolean = true,
    val priority: String = "Normal" // "Low", "Normal", "High"
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String? = null, // e.g., "APP_OPEN", "CALL", "SMS", "ALARM", "BATTERY", "INFO"
    val actionDetails: String? = null,
    val isSuccess: Boolean = true
)

@Entity(tableName = "user_preferences")
data class UserPreference(
    @PrimaryKey
    val key: String,
    val value: String
)
