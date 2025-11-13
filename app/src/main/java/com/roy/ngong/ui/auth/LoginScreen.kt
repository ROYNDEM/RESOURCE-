// In: ui/auth/LoginScreen.kt

package com.roy.ngong.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.roy.ngong.R
import com.roy.ngong.navigation.AppDestinations

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    val primaryColor = Color(0xFFD32F2F) // Red

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- Logo ---
            Image(
                painter = painterResource(id = R.drawable.citam_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(180.dp)
                    .padding(bottom = 40.dp)
            )

            // --- REMOVED THE SINGLE WRAPPING CARD ---

            // --- Email Field in its own Card ---
            Card(
                shape = RoundedCornerShape(48.dp),
                modifier = Modifier.fillMaxWidth(),
                // You can now change the color of this card individually
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Add a subtle shadow
            ) {
                OutlinedTextField(
                    shape = RoundedCornerShape(48.dp),
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    // Make the TextField transparent to show the Card's color
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent, // Hide the border
                        unfocusedBorderColor = Color.Transparent // Hide the border
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Password Field in its own Card ---
            Card(
                shape = RoundedCornerShape(48.dp),
                modifier = Modifier.fillMaxWidth(),
                // Example: Making this card a different color
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)), // A light grey
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                OutlinedTextField(
                    shape = RoundedCornerShape(48.dp),
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    // Make the TextField transparent to show the Card's color
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent, // Hide the border
                        unfocusedBorderColor = Color.Transparent // Hide the border
                    )
                )
            }


            Spacer(modifier = Modifier.height(32.dp))

            // --- Login Button ---
            if (isLoading) {
                CircularProgressIndicator(color = primaryColor)
            } else {
                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            isLoading = true
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        navController.navigate(AppDestinations.HOME_SCREEN_ROUTE) {
                                            popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                                        }
                                    } else {
                                        Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(48.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 4.dp)
                ) {
                    Text("Login", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- "Create Account" Text Button ---
            ClickableText(
                text = AnnotatedString("Don't have an account? Sign Up"),
                onClick = { navController.navigate(AppDestinations.SIGN_UP_ROUTE) },
                style = TextStyle(
                    color = Color.Blue,
                    textAlign = TextAlign.Center,
                    textDecoration = TextDecoration.Underline,
                    fontSize = 14.sp
                )
            )
        }
    }
}