package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [TaskItem::class, ChatMessage::class, UserPreference::class],
    version = 1,
    exportSchema = false
)
abstract class SadbhavnaDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun userPreferenceDao(): UserPreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: SadbhavnaDatabase? = null

        fun getDatabase(context: Context): SadbhavnaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SadbhavnaDatabase::class.java,
                    "sadbhavna_database"
                )
                .fallbackToDestructiveMigration(true)
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: SadbhavnaDatabase) {
                val now = System.currentTimeMillis()
                val calendar = Calendar.getInstance()

                // Welcome message
                db.chatMessageDao().insertMessage(
                    ChatMessage(
                        role = "assistant",
                        content = "Namaste ji! Main Jarvis hoon. Aap bataiye, main aapki kaise help kar sakti hoon? 😊",
                        timestamp = now
                    )
                )

                // Starter sample tasks
                calendar.set(Calendar.HOUR_OF_DAY, 18)
                calendar.set(Calendar.MINUTE, 0)
                db.taskDao().insertTask(
                    TaskItem(
                        title = "Science Chapter Revision",
                        description = "Class 9 physics gravitation notes padhna hai",
                        category = "Study",
                        dueTimestamp = calendar.timeInMillis,
                        priority = "High"
                    )
                )

                calendar.set(Calendar.HOUR_OF_DAY, 20)
                calendar.set(Calendar.MINUTE, 30)
                db.taskDao().insertTask(
                    TaskItem(
                        title = "Family Call Reminder",
                        description = "Ghar par call karke update dena",
                        category = "Call",
                        dueTimestamp = calendar.timeInMillis,
                        priority = "Normal"
                    )
                )

                // Default preferences
                db.userPreferenceDao().setPreference(UserPreference("assistant_language", "hi-IN"))
                db.userPreferenceDao().setPreference(UserPreference("ai_mode", "hybrid")) // offline, online, hybrid
                db.userPreferenceDao().setPreference(UserPreference("speech_rate", "0.95"))
                db.userPreferenceDao().setPreference(UserPreference("speech_pitch", "1.15"))
                db.userPreferenceDao().setPreference(UserPreference("user_name", "Dost"))
            }
        }
    }
}
