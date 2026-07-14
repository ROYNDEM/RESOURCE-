// In: ui/admin/AuthViewModel.kt

package com.roy.ngong.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Represents the possible states of an authentication action
enum class AuthStatus {
    IDLE,
    LOADING,
    SUCCESS,
    ERROR
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore // Add Firestore instance

    // Holds the current authentication status
    private val _authStatus = MutableStateFlow(AuthStatus.IDLE)
    val authStatus = _authStatus.asStateFlow()

    // Holds any error message from Firebase
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // --- UPDATED: Holds the user's role and admin status ---
    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _userRole = MutableStateFlow("general") // "admin", "resource", "registration", "general"
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    // Exposes the currently logged-in user
    val currentUser get() = auth.currentUser

    init {
        // Listen for changes in authentication state (login/logout)
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in, fetch their profile data
                fetchUserProfile(user.uid)
            } else {
                // User is signed out, reset data
                _isAdmin.value = false
                _userRole.value = "general"
            }
        }
    }

    // --- UPDATED: Function to fetch user data from Firestore ---
    private fun fetchUserProfile(uid: String) {
        viewModelScope.launch {
            try {
                val document = db.collection("users").document(uid).get().await()
                if (document != null && document.exists()) {
                    _isAdmin.value = document.getBoolean("isAdmin") ?: false
                    _userRole.value = document.getString("role") ?: "general"
                } else {
                    _isAdmin.value = false
                    _userRole.value = "general"
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error fetching user profile", e)
                _isAdmin.value = false
                _userRole.value = "general"
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authStatus.value = AuthStatus.LOADING
            _errorMessage.value = null
            try {
                auth.createUserWithEmailAndPassword(email.trim(), password.trim()).await()
                // Auth state listener will handle checking admin status
                _authStatus.value = AuthStatus.SUCCESS
            } catch (e: Exception) {
                _authStatus.value = AuthStatus.ERROR
                _errorMessage.value = e.message ?: "An unknown error occurred."
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authStatus.value = AuthStatus.LOADING
            _errorMessage.value = null
            try {
                auth.signInWithEmailAndPassword(email.trim(), password.trim()).await()
                // Auth state listener will handle checking admin status
                _authStatus.value = AuthStatus.SUCCESS
            } catch (e: Exception) {
                _authStatus.value = AuthStatus.ERROR
                _errorMessage.value = e.message ?: "An unknown error occurred."
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authStatus.value = AuthStatus.LOADING
            _errorMessage.value = null
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(credential).await()
                // Auth state listener will handle checking admin status
                _authStatus.value = AuthStatus.SUCCESS
            } catch (e: Exception) {
                _authStatus.value = AuthStatus.ERROR
                _errorMessage.value = e.message ?: "An unknown error occurred during Google Sign-In."
            }
        }
    }

    fun setSignInError(message: String) {
        _authStatus.value = AuthStatus.ERROR
        _errorMessage.value = message
    }

    fun logout() {
        auth.signOut()
        // Auth state listener will set isAdmin to false automatically
    }

    fun resetStatus() {
        _authStatus.value = AuthStatus.IDLE
        _errorMessage.value = null
    }
}