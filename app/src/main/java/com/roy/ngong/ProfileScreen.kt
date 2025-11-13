package com.roy.ngong

import com.roy.ngong.navigation.AppDestinations
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val userId = auth.currentUser?.uid

    // State for the user's name
    var nickname by remember { mutableStateOf("") }

    // NEW STATE: Track if a name is already saved in Firestore.
    // We start with `false` and set it to `true` after fetching.
    var isNameSaved by remember { mutableStateOf(false) }

    // --- Data Fetching ---
    LaunchedEffect(userId) {
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val savedName = document.getString("nickname") ?: ""
                        if (savedName.isNotBlank()) {
                            nickname = savedName
                            isNameSaved = true // A name exists in the database!
                        }
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- DYNAMIC CONTENT BLOCK ---
            if (isNameSaved) {
                // STATE 1: A name is already saved. Show a welcome message.
                Text(
                    text = "Welcome, $nickname",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Button to allow user to change their name
                TextButton(onClick = { isNameSaved = false }) {
                    Text("Change Name?")
                }

            } else {
                // STATE 2: No name is saved yet. Show the input form.
                Text(
                    "Set your name here. It will be used to auto-fill the 'Compiled By' field.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Your Name or Nickname") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (userId != null && nickname.isNotBlank()) {
                            val userProfile = mapOf("nickname" to nickname)
                            firestore.collection("users").document(userId).set(userProfile)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Name Saved!", Toast.LENGTH_SHORT).show()
                                    isNameSaved = true // Switch to the "Welcome" view!
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Name")
                }
            }

            // Spacer to push the logout button to the bottom
            Spacer(modifier = Modifier.weight(1f))

            // --- Log Out Button (always visible) ---
            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(0)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Out")
            }
        }
    }
}