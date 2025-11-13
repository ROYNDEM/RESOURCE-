package com.roy.ngong.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingLogDao {
    // Gets all entries and orders them by most recent first. Flow automatically updates the UI.
    @Query("SELECT * FROM pending_logs ORDER BY submittedAt DESC")
    fun getAllPendingLogs(): Flow<List<ClassSessionLog>>

    // Inserts a new entry. If an entry with the same ID exists, it replaces it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: ClassSessionLog)

    // Deletes an entry.
    @Delete
    suspend fun deleteLog(log: ClassSessionLog)
}