package com.nexusai.assistant.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ConversationEntity::class, ChatMessageEntity::class, CallRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NexusDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun callDao(): CallDao

    companion object {
        fun create(context: Context): NexusDatabase =
            Room.databaseBuilder(context, NexusDatabase::class.java, "nexus.db").build()
    }
}
