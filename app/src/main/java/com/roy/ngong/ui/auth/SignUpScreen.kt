package com.roy.ngong.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.roy.ngong.navigation.AppDestinations
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val primaryColor = Color(0xFFD32F2F) // Red

    // Define colors for TextFields to remove their default background and border
    val transparentTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        errorBorderColor = Color.Transparent, // Also hide border on error
        // Keep default text and label colors which will be black/gray on a white card
        focusedLabelColor = Color.Gray,
        unfocusedLabelColor = Color.Gray,
        cursorColor = primaryColor
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp)) // Add some space at the top

            // --- Email Field in its own Card ---
            Card(
                shape = RoundedCornerShape(48.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = transparentTextFieldColors,
                    shape = RoundedCornerShape(48.dp) // Match shape
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- Password Field in its own Card ---
            Card(
                shape = RoundedCornerShape(48.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password (min. 6 characters)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    colors = transparentTextFieldColors,
                    shape = RoundedCornerShape(48.dp) // Match shape
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- Confirm Password Field in its own Card ---
            Card(
                shape = RoundedCornerShape(48.dp),
                modifier = Modifier.fillMaxWidth(),
                // Change card color based on error state for better feedback
                colors = CardDefaults.cardColors(
                    containerColor = if (password != confirmPassword && confirmPassword.isNotEmpty()) {
                        primaryColor.copy(alpha = 0.1f) // Light red background on error
                    } else {
                        Color.White
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = password != confirmPassword && confirmPassword.isNotEmpty(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    colors = transparentTextFieldColors,
                    shape = RoundedCornerShape(48.dp) // Match shape
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Sign Up Button ---
            if (isLoading) {
                CircularProgressIndicator(color = primaryColor)
            } else {
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Email and password cannot be empty.")
                            }
                            return@Button
                        }
                        if (password != confirmPassword) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Passwords do not match.")
                            }
                            return@Button
                        }
                        isLoading = true
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                scope.launch {
                                    if (task.isSuccessful) {
                                        snackbarHostState.showSnackbar("Account created! Please log in.")
                                        navController.popBackStack()
                                    } else {
                                        snackbarHostState.showSnackbar("Sign Up Failed: ${task.exception?.message}")
                                    }
                                }
                            }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(48.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 16.dp) // Added elevation
                ) {
                    Text("Create Account", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(modifier = Modifier.height(20.dp)) // Add some space at the bottom
        }
    }
}