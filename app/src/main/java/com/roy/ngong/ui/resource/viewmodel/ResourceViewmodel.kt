package com.roy.ngong.ui.resource.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.roy.ngong.data.ClassSessionLog
import com.roy.ngong.data.SundayResourceInventory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ResourceViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // This will hold the listener registration so we can detach it later
    private var logsListener: ListenerRegistration? = null
    private var inventoryListener: ListenerRegistration? = null

    // This map will cache the logs once fetched.
    private val _logsByDate = MutableStateFlow<Map<String, List<ClassSessionLog>>>(emptyMap())

    // Exposes just the dates and their totals for the list screen
    private val _dateTotals = MutableStateFlow<Map<String, Int>>(emptyMap())
    val dateTotals: StateFlow<Map<String, Int>> = _dateTotals.asStateFlow()

    // Holds the detailed logs for only one selected date
    private val _selectedDateLogs = MutableStateFlow<List<ClassSessionLog>>(emptyList())
    val selectedDateLogs: StateFlow<List<ClassSessionLog>> = _selectedDateLogs.asStateFlow()

    // Inventory Data
    private val _inventoryRecords = MutableStateFlow<List<SundayResourceInventory>>(emptyList())
    val inventoryRecords: StateFlow<List<SundayResourceInventory>> = _inventoryRecords.asStateFlow()

    init {
        Log.d("ResourceViewModel", "ViewModel initialized.")
    }

    fun startListeningForLogs() {
        logsListener?.remove()
        logsListener = db.collection("class_session_logs")
            .orderBy("sessionDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("ResourceViewModel", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    viewModelScope.launch {
                        val logs = snapshots.toObjects(ClassSessionLog::class.java)
                        processLogsByDate(logs)
                    }
                }
            }
    }

    fun startListeningForInventory() {
        inventoryListener?.remove()
        inventoryListener = db.collection("sunday_inventory")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("ResourceViewModel", "Inventory listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    _inventoryRecords.value = snapshots.toObjects(SundayResourceInventory::class.java)
                }
            }
    }

    fun refreshAll() {
        startListeningForLogs()
        startListeningForInventory()
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

    fun loadReportForDate(dateString: String) {
        _selectedDateLogs.value = _logsByDate.value[dateString] ?: emptyList()
    }

    fun saveResourceEntry(log: ClassSessionLog) {
        db.collection("class_session_logs").document().set(log)
            .addOnSuccessListener { Log.d("Firestore", "Log saved successfully!") }
            .addOnFailureListener { e -> Log.e("Firestore", "Error saving log", e) }
    }

    fun saveInventoryRecord(record: SundayResourceInventory, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("sunday_inventory").document(record.date).set(record)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    override fun onCleared() {
        super.onCleared()
        logsListener?.remove()
        inventoryListener?.remove()
    }
}