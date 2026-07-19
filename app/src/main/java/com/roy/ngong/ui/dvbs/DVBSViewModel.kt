package com.roy.ngong.ui.dvbs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.roy.ngong.data.DVBSRecord
import com.roy.ngong.data.DVBSRegistration
import com.roy.ngong.data.DVBSResource
import com.roy.ngong.data.DVBSStatistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log

class DVBSViewModel : ViewModel() {
    private val db = Firebase.firestore

    private var recordsListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var resourcesListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var registrationsListener: com.google.firebase.firestore.ListenerRegistration? = null

    private val _dvbsRecords = MutableStateFlow<List<DVBSRecord>>(emptyList())
    val dvbsRecords: StateFlow<List<DVBSRecord>> = _dvbsRecords

    private val _dvbsResources = MutableStateFlow<List<DVBSResource>>(emptyList())
    val dvbsResources: StateFlow<List<DVBSResource>> = _dvbsResources

    private val _dvbsRegistrations = MutableStateFlow<List<DVBSRegistration>>(emptyList())
    val dvbsRegistrations: StateFlow<List<DVBSRegistration>> = _dvbsRegistrations

    private val _dvbsStatistics = MutableStateFlow(DVBSStatistics())
    val dvbsStatistics: StateFlow<DVBSStatistics> = _dvbsStatistics

    init {
        startListening()
    }

    fun startListening() {
        listenForRecords()
        listenForResources()
        listenForRegistrations()
    }

    fun stopListening() {
        recordsListener?.remove()
        resourcesListener?.remove()
        registrationsListener?.remove()
        recordsListener = null
        resourcesListener = null
        registrationsListener = null
    }

    private fun listenForRecords() {
        recordsListener?.remove()
        recordsListener = db.collection("dvbs_records")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("DVBSViewModel", "Error listening for DVBS records", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val records = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(DVBSRecord::class.java)
                    }
                    _dvbsRecords.value = records
                    calculateStatistics(records)
                }
            }
    }

    private fun listenForResources() {
        resourcesListener?.remove()
        resourcesListener = db.collection("dvbs_resources")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("DVBSViewModel", "Error listening for DVBS resources", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val resources = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(DVBSResource::class.java)
                    }
                    _dvbsResources.value = resources
                }
            }
    }

    private fun listenForRegistrations() {
        registrationsListener?.remove()
        registrationsListener = db.collection("dvbs_registrations")
            .orderBy("registrationDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("DVBSViewModel", "Error listening for DVBS registrations", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val registrations = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(DVBSRegistration::class.java)
                    }
                    _dvbsRegistrations.value = registrations
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }

    // Keep old fetch methods for manual refresh if needed, but updated to use listeners internally or force refresh
    fun fetchDVBSRecords() = listenForRecords()
    fun fetchDVBSResources() = listenForResources()
    fun fetchDVBSRegistrations() = listenForRegistrations()

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
                    }
                    .addOnFailureListener { e ->
                        Log.e("DVBSViewModel", "Error adding DVBS record", e)
                    }
            } catch (e: Exception) {
                Log.e("DVBSViewModel", "Exception in addDVBSRecord", e)
            }
        }
    }

    fun addDVBSResource(resource: DVBSResource) {
        viewModelScope.launch {
            try {
                db.collection("dvbs_resources")
                    .document(resource.id)
                    .set(resource)
                    .addOnSuccessListener {
                        Log.d("DVBSViewModel", "Resource added successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("DVBSViewModel", "Error adding DVBS resource", e)
                    }
            } catch (e: Exception) {
                Log.e("DVBSViewModel", "Exception in addDVBSResource", e)
            }
        }
    }

    fun addDVBSRegistration(registration: DVBSRegistration) {
        viewModelScope.launch {
            try {
                db.collection("dvbs_registrations")
                    .document(registration.id)
                    .set(registration)
                    .addOnSuccessListener {
                        Log.d("DVBSViewModel", "Registration added successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("DVBSViewModel", "Error adding DVBS registration", e)
                    }
            } catch (e: Exception) {
                Log.e("DVBSViewModel", "Exception in addDVBSRegistration", e)
            }
        }
    }

    fun deleteDVBSResource(id: String) {
        viewModelScope.launch {
            try {
                db.collection("dvbs_resources").document(id).delete()
                    .addOnSuccessListener {
                        Log.d("DVBSViewModel", "Resource deleted successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("DVBSViewModel", "Error deleting DVBS resource", e)
                    }
            } catch (e: Exception) {
                Log.e("DVBSViewModel", "Exception in deleteDVBSResource", e)
            }
        }
    }

    fun deleteDVBSRegistration(id: String) {
        viewModelScope.launch {
            try {
                db.collection("dvbs_registrations").document(id).delete()
                    .addOnSuccessListener {
                        Log.d("DVBSViewModel", "Registration deleted successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("DVBSViewModel", "Error deleting DVBS registration", e)
                    }
            } catch (e: Exception) {
                Log.e("DVBSViewModel", "Exception in deleteDVBSRegistration", e)
            }
        }
    }
}
