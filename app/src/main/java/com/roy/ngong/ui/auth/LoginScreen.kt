// In: ui/auth/LoginScreen.kt

package com.roy.ngong.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.roy.ngong.R
import com.roy.ngong.navigation.AppDestinations
import com.roy.ngong.ui.admin.AuthViewModel
import com.roy.ngong.ui.admin.AuthStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalTextApi::class)
@Composable
fun LoginScreen(
    navController: NavController,
    onGoogleSignInClick: () -> Unit,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val authStatus by authViewModel.authStatus.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val isLoading = authStatus == AuthStatus.LOADING

    // --- Annotated Strings for Links ---
    val signUpAnnotatedString = buildAnnotatedString {
        append("Don't have an account? ")
        pushLink(LinkAnnotation.Clickable("signup") {
            navController.navigate(AppDestinations.SIGN_UP_ROUTE)
        })
        withStyle(style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
            append("Sign Up")
        }
        pop()
    }

    val forgotPasswordAnnotatedString = buildAnnotatedString {
        pushLink(LinkAnnotation.Clickable("forgot") {
            showForgotPasswordDialog = true
        })
        withStyle(style = SpanStyle(color = Color.Gray, textDecoration = TextDecoration.Underline)) {
            append("Forgot Password?")
        }
        pop()
    }

    LaunchedEffect(authStatus) {
        if (authStatus == AuthStatus.SUCCESS) {
            navController.navigate(AppDestinations.HOME_SCREEN_ROUTE) {
                popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
            }
            authViewModel.resetStatus()
        } else if (authStatus == AuthStatus.ERROR) {
            scope.launch {
                snackbarHostState.showSnackbar(errorMessage ?: "An error occurred")
            }
            authViewModel.resetStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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

                // --- Email Field in its own Card ---
                Card(
                    shape = RoundedCornerShape(48.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    OutlinedTextField(
                        shape = RoundedCornerShape(48.dp),
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Password Field in its own Card ---
                Card(
                    shape = RoundedCornerShape(48.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    OutlinedTextField(
                        shape = RoundedCornerShape(48.dp),
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- Login Button ---
                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFFD32F2F))
                } else {
                    ElevatedButton(
                        onClick = {
                            if (email.isNotBlank() && password.isNotBlank()) {
                                authViewModel.login(email, password)
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please enter your email and password.")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.elevatedButtonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(48.dp),
                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 8.dp, pressedElevation = 4.dp)
                    ) {
                        Text("Login", style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Google Sign-In Button ---
                ElevatedButton(
                    onClick = onGoogleSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.elevatedButtonColors(containerColor = Color.White, contentColor = Color.Black),
                    shape = RoundedCornerShape(48.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 8.dp, pressedElevation = 4.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign in with Google")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- "Create Account" and "Forgot Password" Text Buttons ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = signUpAnnotatedString, fontSize = 14.sp)
                    Text(text = forgotPasswordAnnotatedString, fontSize = 14.sp)
                }
            }
        }
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotPasswordDialog = false },
            onConfirm = { resetEmail ->
                auth.sendPasswordResetEmail(resetEmail)
                    .addOnCompleteListener { task ->
                        scope.launch {
                            if (task.isSuccessful) {
                                snackbarHostState.showSnackbar("Password reset email sent.")
                            } else {
                                snackbarHostState.showSnackbar("Failed to send reset email: ${task.exception?.message}")
                            }
                        }
                    }
                showForgotPasswordDialog = false
            }
        )
    }
}

@Composable
fun ForgotPasswordDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password") },
        text = {
            Column {
                Text("Enter your email address to receive a password reset link.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(email) }) {
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}