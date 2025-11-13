package com.roy.ngong.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.roy.ngong.data.GeneralData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AppDataViewModel : ViewModel() {
    // Get the Firestore instance
    private val db = FirebaseFirestore.getInstance()
    private val docRef = db.collection("app_data").document("general")
    private val _generalData = MutableStateFlow(GeneralData())
    val generalData: StateFlow<GeneralData> = _generalData.asStateFlow()

    init {
        docRef.addSnapshotListener { snapshot: DocumentSnapshot?, error: FirebaseFirestoreException? ->
            if (error != null) {
                Log.w("AppDataViewModel", "Listen failed.", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                _generalData.value = snapshot.toObject(GeneralData::class.java) ?: GeneralData()
                Log.d("AppDataViewModel", "General data updated from Firestore.")
            } else {
                Log.d("AppDataViewModel", "The 'general' document does not exist yet.")
            }
        }
    }

    fun updateGeneralData(verse: String, announcement: String) {
        viewModelScope.launch {
            val newData = GeneralData(verseOfTheDay = verse, announcement = announcement)
            try {
                docRef.set(newData).await()
                Log.d("AppDataViewModel", "General data was successfully saved!")
            } catch (e: Exception) {
                Log.w("AppDataViewModel", "Failed to save general data.", e)
            }
        }
    }
}