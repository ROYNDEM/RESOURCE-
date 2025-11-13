package com.roy.ngong.ui.resource

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.roy.ngong.data.ClassSessionLog
import com.roy.ngong.ui.pending.PendingSubmissionsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceEntryScreen(
    modifier: Modifier = Modifier,
    pendingViewModel: PendingSubmissionsViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var serviceName by rememberSaveable { mutableStateOf("") }
    var className by rememberSaveable { mutableStateOf("") }
    var teacherNames by rememberSaveable { mutableStateOf("") }
    var teacherHelpers by rememberSaveable { mutableStateOf("") }
    var attendeeCountString by rememberSaveable { mutableStateOf("") }
    val initialDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var sessionDateString by rememberSaveable { mutableStateOf(initialDate) }
    var submittedBy by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        submittedBy = document.getString("nickname") ?: ""
                    }
                }
        }
    }

    var serviceNameError by rememberSaveable { mutableStateOf<String?>(null) }
    var sessionDateError by rememberSaveable { mutableStateOf<String?>(null) }
    var classNameError by rememberSaveable { mutableStateOf<String?>(null) }
    var attendeeCountError by rememberSaveable { mutableStateOf<String?>(null) }
    var submittedByError by rememberSaveable { mutableStateOf<String?>(null) }

    var serviceExpanded by remember { mutableStateOf(false) }
    var classExpanded by remember { mutableStateOf(false) }

    val serviceOptions = listOf("First Service", "Second Service")
    val classOptions = listOf(
        "Playgroup", "PP1", "PP2", "Grade 1", "Grade 2", "Grade 3",
        "Grade 4", "Grade 5", "Grade 6", "Grade 7", "Grade 8"
    )

    fun clearForm() {
        serviceName = ""
        sessionDateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        className = ""
        teacherNames = ""
        teacherHelpers = ""
        attendeeCountString = ""
        serviceNameError = null
        sessionDateError = null
        classNameError = null
        attendeeCountError = null
        submittedByError = null
    }

    fun validateFields(): Boolean {
        serviceNameError = if (serviceName.isBlank()) "Service cannot be empty" else null
        sessionDateError = if (sessionDateString.isBlank()) {
            "Date cannot be empty"
        } else if (!sessionDateString.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            "Invalid date format (YYYY-MM-DD)"
        } else {
            null
        }
        classNameError = if (className.isBlank()) "Class name cannot be empty" else null
        attendeeCountError = when {
            attendeeCountString.isBlank() -> "Attendee count cannot be empty"
            attendeeCountString.toIntOrNull() == null -> "Invalid number for attendees"
            else -> null
        }
        submittedByError = if (submittedBy.isBlank()) "Submitted by cannot be empty" else null

        return serviceNameError == null &&
                sessionDateError == null &&
                classNameError == null &&
                attendeeCountError == null &&
                submittedByError == null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resource Entry") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
            modifier = modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = serviceExpanded,
                onExpandedChange = { serviceExpanded = !serviceExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = serviceName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Service") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    isError = serviceNameError != null,
                    shape = RoundedCornerShape(24.dp) // --- CHANGE 1: ADD SHAPE ---
                )
                ExposedDropdownMenu(
                    expanded = serviceExpanded,
                    onDismissRequest = { serviceExpanded = false }
                ) {
                    serviceOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                serviceName = option
                                serviceExpanded = false
                                serviceNameError = null
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
            if (serviceNameError != null) {
                Text(
                    text = serviceNameError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 2.dp, bottom = 6.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = sessionDateString,
                onValueChange = {
                    sessionDateString = it
                    sessionDateError = null
                },
                label = { Text("Date (e.g., YYYY-MM-DD)") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                isError = sessionDateError != null,
                shape = RoundedCornerShape(24.dp) // --- CHANGE 1: ADD SHAPE ---
            )
            if (sessionDateError != null) {
                Text(
                    text = sessionDateError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 2.dp, bottom = 6.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            ExposedDropdownMenuBox(
                expanded = classExpanded,
                onExpandedChange = { classExpanded = !classExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = className,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Class/Group Name") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    isError = classNameError != null,
                    shape = RoundedCornerShape(24.dp) // --- CHANGE 1: ADD SHAPE ---
                )
                ExposedDropdownMenu(
                    expanded = classExpanded,
                    onDismissRequest = { classExpanded = false }
                ) {
                    classOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                className = option
                                classExpanded = false
                                classNameError = null
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
            if (classNameError != null) {
                Text(
                    text = classNameError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 2.dp, bottom = 6.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = teacherNames,
                onValueChange = { teacherNames = it },
                label = { Text("Teacher(s) Name(s)") },
                placeholder = { Text("e.g., Jane Doe, John Smith") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                shape = RoundedCornerShape(24.dp) // --- CHANGE 1: ADD SHAPE ---
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = teacherHelpers,
                onValueChange = { teacherHelpers = it },
                label = { Text("Teacher Helper(s) Name(s)") },
                placeholder = { Text("e.g., Helper One, Helper Two") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                shape = RoundedCornerShape(24.dp) // --- CHANGE 1: ADD SHAPE ---
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = attendeeCountString,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        attendeeCountString = newValue
                    }
                    attendeeCountError = null
                },
                label = { Text("Number of Attendees") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = attendeeCountError != null,
                shape = RoundedCornerShape(24.dp) // --- CHANGE 1: ADD SHAPE ---
            )
            if (attendeeCountError != null) {
                Text(
                    text = attendeeCountError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 2.dp, bottom = 6.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = submittedBy,
                onValueChange = {
                    submittedBy = it
                    submittedByError = null
                },
                label = { Text("Submitted By") },
                placeholder = { Text("Your Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = submittedByError != null,
                shape = RoundedCornerShape(24.dp) // --- CHANGE 1: ADD SHAPE ---
            )
            if (submittedByError != null) {
                Text(
                    text = submittedByError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 2.dp, bottom = 6.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (validateFields()) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val sessionDate: Date? = try { sdf.parse(sessionDateString) } catch (_: Exception) { null }

                        val newLogEntry = ClassSessionLog(
                            id = UUID.randomUUID().toString(),
                            serviceName = serviceName,
                            sessionDate = sessionDate,
                            className = className,
                            teacherNames = teacherNames.split(',').map { it.trim() }.filter { it.isNotEmpty() },
                            teacherHelpers = teacherHelpers.split(',').map { it.trim() }.filter { it.isNotEmpty() },
                            attendeeCount = attendeeCountString.toIntOrNull() ?: 0,
                            submittedBy = submittedBy,
                            submittedAt = Date()
                        )

                        pendingViewModel.saveLogLocally(newLogEntry)

                        scope.launch {
                            snackbarHostState.showSnackbar("Entry saved for review.")
                        }

                        clearForm()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                // --- CHANGE 2: ADD COLORS ---
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F), // Use your app's red color
                    contentColor = Color.White
                )
            ) {
                Text("Save for Review")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}