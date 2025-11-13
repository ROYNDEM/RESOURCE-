// In: ui/calendar/CalendarViewModel.kt

package com.roy.ngong.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.roy.ngong.data.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CalendarViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    // Holds all events fetched from Firestore, grouped by their date string
    private val _events = MutableStateFlow<Map<String, List<Event>>>(emptyMap())
    val events = _events.asStateFlow()

    init {
        // Start listening for event changes as soon as the ViewModel is created
        listenForEvents()
    }

    private fun listenForEvents() {
        // Listen to the 'events' collection, ordering by date
        db.collection("events").orderBy("dateString", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    // Convert query snapshot to a list of Event objects
                    val eventList = snapshot.toObjects(Event::class.java)
                    // Group events by their date string (e.g., "2024-12-25")
                    _events.value = eventList.groupBy { it.dateString }
                }
            }
    }

    fun addEvent(title: String, description: String, dateString: String) {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch // Ensure user is logged in
            val newEvent = Event(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                dateString = dateString,
                createdBy = user.email ?: "Unknown Admin"
            )
            // Save the new event to Firestore
            db.collection("events").document(newEvent.id).set(newEvent).await()
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            // Delete the event document from Firestore
            db.collection("events").document(event.id).delete().await()
        }
    }
}