package com.roy.ngong.ui.pending

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.roy.ngong.data.AppDatabase
import com.roy.ngong.data.ClassSessionLog
import com.roy.ngong.data.PendingLogDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// We use AndroidViewModel because we need the application context to create the database instance.
class PendingSubmissionsViewModel(application: Application) : AndroidViewModel(application) {

    // A private reference to the DAO from our AppDatabase.
    private val dao: PendingLogDao

    // A public Flow that the UI can observe. It will automatically emit a new list
    // of logs whenever the database changes.
    val pendingLogs: Flow<List<ClassSessionLog>>

    init {
        // Get a reference to the DAO from the AppDatabase singleton.
        dao = AppDatabase.getDatabase(application).pendingLogDao()
        // Initialize the flow of pending logs.
        pendingLogs = dao.getAllPendingLogs()
    }

    /**
     * Saves a log entry to the local Room database.
     * This is launched in the viewModelScope so it runs on a background thread.
     */
    fun saveLogLocally(log: ClassSessionLog) = viewModelScope.launch {
        dao.insertOrUpdateLog(log)
    }

    /**
     * Deletes a log entry from the local Room database.
     * This is launched in the viewModelScope so it runs on a background thread.
     */
    fun deleteLog(log: ClassSessionLog) = viewModelScope.launch {
        dao.deleteLog(log)
    }
}