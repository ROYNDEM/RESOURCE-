package com.roy.ngong

import com.roy.ngong.navigation.AppDestinations
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch

private val primaryColor = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, isDarkMode: Boolean = false) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    val userEmail = auth.currentUser?.email ?: ""

    val backgroundColor = if (isDarkMode) Color.Black else Color(0xFFF0F0F0)
    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val contentColor = if (isDarkMode) Color.White else Color.Black

    // State for the user's name
    var nickname by remember { mutableStateOf("") }
    var editingName by remember { mutableStateOf("") }

    // Whether a name is already saved in Firestore
    var isNameSaved by remember { mutableStateOf(false) }
    // Whether we're still waiting on the initial Firestore read
    var isLoadingProfile by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // --- Data Fetching ---
    LaunchedEffect(userId) {
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val savedName = document?.getString("nickname") ?: ""
                    if (savedName.isNotBlank()) {
                        nickname = savedName
                        isNameSaved = true
                    }
                    isLoadingProfile = false
                }
                .addOnFailureListener {
                    isLoadingProfile = false
                    scope.launch { snackbarHostState.showSnackbar("Couldn't load your profile. Check your connection.") }
                }
        } else {
            isLoadingProfile = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = contentColor,
                    navigationIconContentColor = contentColor
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = isLoadingProfile,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "profileLoadingTransition"
            ) { loading ->
                if (loading) {
                    ProfileSkeleton(isDarkMode = isDarkMode)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ProfileAvatar(displayName = nickname, email = userEmail)

                        Spacer(modifier = Modifier.height(24.dp))

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AnimatedContent(
                                targetState = isNameSaved,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "nameStateTransition"
                            ) { saved ->
                                if (saved) {
                                    // --- STATE 1: name already saved ---
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Welcome, $nickname",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = contentColor
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (userEmail.isNotBlank()) {
                                            Text(
                                                text = userEmail,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = contentColor.copy(alpha = 0.6f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        TextButton(onClick = {
                                            editingName = nickname
                                            isNameSaved = false
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Change Name")
                                        }
                                    }
                                } else {
                                    // --- STATE 2: editing / first-time setup ---
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp)
                                    ) {
                                        Text(
                                            "Set your name here. It will be used to auto-fill the 'Compiled By' field.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = contentColor.copy(alpha = 0.8f)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))

                                        OutlinedTextField(
                                            value = editingName,
                                            onValueChange = { editingName = it },
                                            label = { Text("Your Name or Nickname") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = primaryColor,
                                                cursorColor = primaryColor
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (nickname.isNotBlank()) {
                                                OutlinedButton(
                                                    onClick = { isNameSaved = true },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text("Cancel")
                                                }
                                            }
                                            Button(
                                                onClick = {
                                                    if (userId != null && editingName.isNotBlank()) {
                                                        isSaving = true
                                                        val userProfile = mapOf("nickname" to editingName)
                                                        firestore.collection("users").document(userId)
                                                            .set(userProfile, SetOptions.merge())
                                                            .addOnSuccessListener {
                                                                isSaving = false
                                                                nickname = editingName
                                                                isNameSaved = true
                                                                scope.launch { snackbarHostState.showSnackbar("Name saved.") }
                                                            }
                                                            .addOnFailureListener { e ->
                                                                isSaving = false
                                                                scope.launch { snackbarHostState.showSnackbar("Error: ${e.message}") }
                                                            }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                enabled = editingName.isNotBlank() && !isSaving,
                                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                if (isSaving) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(18.dp),
                                                        color = Color.White,
                                                        strokeWidth = 2.dp
                                                    )
                                                } else {
                                                    Text("Save Name")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Spacer to push the logout button to the bottom
            Spacer(modifier = Modifier.weight(1f))

            // --- Log Out Button ---
            OutlinedButton(
                onClick = { showLogoutConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out")
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log out?") },
            text = { Text("You'll need to sign in again to access your account.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        auth.signOut()
                        navController.navigate(AppDestinations.LOGIN_ROUTE) {
                            popUpTo(0)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Circular avatar showing the user's initial (from their saved nickname, falling
 * back to their email, falling back to a generic icon-free "?").
 */
@Composable
private fun ProfileAvatar(displayName: String, email: String) {
    val initial = when {
        displayName.isNotBlank() -> displayName.trim().first().uppercaseChar().toString()
        email.isNotBlank() -> email.trim().first().uppercaseChar().toString()
        else -> "?"
    }

    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(CircleShape)
            .background(primaryColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Shimmer placeholder shown while the initial Firestore profile read is in flight.
 */
@Composable
private fun ProfileSkeleton(isDarkMode: Boolean) {
    val transition = rememberInfiniteTransition(label = "profileShimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "profileShimmerTranslate"
    )
    val baseColor = if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFE2E2E2)
    val highlightColor = if (isDarkMode) Color(0xFF3D3D3D) else Color(0xFFF2F2F2)
    val brush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateAnim - 300f, 0f),
        end = Offset(translateAnim, 300f)
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(brush)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(brush)
        )
    }
}