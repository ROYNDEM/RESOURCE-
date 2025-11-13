package com.roy.ngong.ui.resource.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.roy.ngong.data.ClassSessionLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.jvm.java

class ResourceViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // This will hold the listener registration so we can detach it later
    private var logsListener: ListenerRegistration? = null

    // This map will cache the logs once fetched.
    private val _logsByDate = MutableStateFlow<Map<String, List<ClassSessionLog>>>(emptyMap())

    // Exposes just the dates and their totals for the list screen
    private val _dateTotals = MutableStateFlow<Map<String, Int>>(emptyMap())
    val dateTotals: StateFlow<Map<String, Int>> = _dateTotals.asStateFlow()

    // Holds the detailed logs for only one selected date
    private val _selectedDateLogs = MutableStateFlow<List<ClassSessionLog>>(emptyList())
    val selectedDateLogs: StateFlow<List<ClassSessionLog>> = _selectedDateLogs.asStateFlow()

    // The init block is now empty. No work is done on startup.
    init {
        Log.d("ResourceViewModel", "ViewModel initialized. No data fetched yet.")
    }

    /**
     * This function is now called ON-DEMAND from the UI (e.g., AdminReportsListScreen).
     * It attaches the listener to Firestore.
     */
    fun startListeningForLogs() {
        // If the listener is already active, don't create a new one.
        if (logsListener != null) return

        Log.d("ResourceViewModel", "Starting to listen for session logs.")
        logsListener = db.collection("class_session_logs")
            .orderBy("sessionDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("ResourceViewModel", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    viewModelScope.launch { // Process data inside a coroutine
                        val logs = snapshots.toObjects(ClassSessionLog::class.java)
                        processLogsByDate(logs)
                        Log.d("ResourceViewModel", "Fetched and processed ${logs.size} logs.")
                    }
                }
            }
    }

    private fun processLogsByDate(logs: List<ClassSessionLog>) {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val groupedByDate = logs.filter { it.sessionDate != null }.groupBy {
            dateFormatter.format(it.sessionDate!!)
        }
        _logsByDate.value = groupedByDate
        _dateTotals.value = groupedByDate.mapValues { (_, dateLogs) ->
            dateLogs.sumOf { it.attendeeCount }
        }
    }

    /**
     * Prepares the report details for a single date.
     */
    fun loadReportForDate(dateString: String) {
        _selectedDateLogs.value = _logsByDate.value[dateString] ?: emptyList()
    }

    fun saveResourceEntry(log: ClassSessionLog) {
        db.collection("class_session_logs").document().set(log)
            .addOnSuccessListener { Log.d("Firestore", "Log saved successfully!") }
            .addOnFailureListener { e -> Log.e("Firestore", "Error saving log", e) }
    }

    /**
     * This is a crucial cleanup function. It detaches the Firestore listener
     * when the ViewModel is about to be destroyed, preventing memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        Log.d("ResourceViewModel", "ViewModel cleared. Detaching Firestore listener.")
        logsListener?.remove()
    }
}