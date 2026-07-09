package com.roy.ngong.ui.dvbs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.roy.ngong.data.DVBSRecord
import com.roy.ngong.data.DVBSStatistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

class DVBSViewModel : ViewModel() {
    private val db = Firebase.firestore

    private val _dvbsRecords = MutableStateFlow<List<DVBSRecord>>(emptyList())
    val dvbsRecords: StateFlow<List<DVBSRecord>> = _dvbsRecords

    private val _dvbsStatistics = MutableStateFlow(DVBSStatistics())
    val dvbsStatistics: StateFlow<DVBSStatistics> = _dvbsStatistics

    init {
        fetchDVBSRecords()
    }

    fun fetchDVBSRecords() {
        viewModelScope.launch {
            try {
                db.collection("dvbs_records")
                    .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val records = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(DVBSRecord::class.java)
                        }
                        _dvbsRecords.value = records
                        calculateStatistics(records)
                    }
                    .addOnFailureListener { e ->
                        Log.e("DVBSViewModel", "Error fetching DVBS records", e)
                    }
            } catch (e: Exception) {
                Log.e("DVBSViewModel", "Exception in fetchDVBSRecords", e)
            }
        }
    }

    private fun calculateStatistics(records: List<DVBSRecord>) {
        if (records.isEmpty()) {
            _dvbsStatistics.value = DVBSStatistics()
            return
        }

        val totalAttendees = records.sumOf { it.attendeeCount }
        val averageAttendance = totalAttendees.toFloat() / records.size

        _dvbsStatistics.value = DVBSStatistics(
            totalEvents = records.size,
            totalAttendees = totalAttendees,
            averageAttendance = averageAttendance,
            lastEventDate = records.firstOrNull()?.date ?: ""
        )
    }

    fun addDVBSRecord(record: DVBSRecord) {
        viewModelScope.launch {
            try {
                db.collection("dvbs_records")
                    .document(record.id)
                    .set(record)
                    .addOnSuccessListener {
                        Log.d("DVBSViewModel", "Record added successfully")
                        fetchDVBSRecords()
                    }
                    .addOnFailureListener { e ->
                        Log.e("DVBSViewModel", "Error adding DVBS record", e)
                    }
            } catch (e: Exception) {
                Log.e("DVBSViewModel", "Exception in addDVBSRecord", e)
            }
        }
    }
}
