package com.roy.ngong.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ClassSessionLog::class], version = 1, exportSchema = false)
@TypeConverters(ListConverter::class, DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pendingLogDao(): PendingLogDao

    companion object {
        // The @Volatile annotation ensures that the INSTANCE variable is always up-to-date.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Return the existing instance if it's not null, otherwise create the database.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // The name of the database file.
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}