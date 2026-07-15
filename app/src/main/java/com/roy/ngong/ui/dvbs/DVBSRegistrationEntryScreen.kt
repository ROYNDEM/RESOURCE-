package com.roy.ngong.ui.dvbs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.roy.ngong.data.DVBSRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DVBSRegistrationEntryScreen(
    dvbsViewModel: DVBSViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    isDarkMode: Boolean = false,
    registrationId: String? = null
) {
    var childName by rememberSaveable { mutableStateOf("") }
    var ageString by rememberSaveable { mutableStateOf("") }
    var gradeClass by rememberSaveable { mutableStateOf("") }
    var parentGuardianName by rememberSaveable { mutableStateOf("") }
    var parentGuardianPhone by rememberSaveable { mutableStateOf("") }
    var selectedGender by rememberSaveable { mutableStateOf("Boy") }
    
    // Auto-populate event date with today's date
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var eventDate by rememberSaveable { mutableStateOf(sdf.format(Date())) }
    var dvbsDay by rememberSaveable { mutableStateOf("") }

    var childNameError by rememberSaveable { mutableStateOf<String?>(null) }
    var ageError by rememberSaveable { mutableStateOf<String?>(null) }
    var gradeClassError by rememberSaveable { mutableStateOf<String?>(null) }
    var parentNameError by rememberSaveable { mutableStateOf<String?>(null) }
    var parentPhoneError by rememberSaveable { mutableStateOf<String?>(null) }
    var dvbsDayError by rememberSaveable { mutableStateOf<String?>(null) }

    var gradeExpanded by remember { mutableStateOf(false) }

    val registrations by dvbsViewModel.dvbsRegistrations.collectAsState()

    // Pre-populate if editing
    LaunchedEffect(registrationId, registrations) {
        if (registrationId != null) {
            val existing = registrations.find { it.id == registrationId }
            if (existing != null) {
                childName = existing.childName
                ageString = existing.age.toString()
                gradeClass = existing.gradeClass
                parentGuardianName = existing.parentGuardianName
                parentGuardianPhone = existing.parentGuardianPhone
                eventDate = existing.eventDate
                dvbsDay = existing.dvbsDay
                selectedGender = existing.gender
            }
        }
    }

    val primaryColor = Color(0xFFC62828)
    val lightModeBackground = Color(0xFFF0F0F0)
    val darkModeBackground = Color.Black

    val gradeOptions = listOf(
        "Playgroup", "PP1", "PP2", "Grade 1", "Grade 2", "Grade 3",
        "Grade 4", "Grade 5", "Grade 6", "Grade 7", "Grade 8"
    )

    fun validateFields(): Boolean {
        childNameError = if (childName.isBlank()) "Child's name cannot be empty" else null
        ageError = when {
            ageString.isBlank() -> "Age cannot be empty"
            ageString.toIntOrNull() == null -> "Age must be a valid number"
            else -> null
        }
        gradeClassError = if (gradeClass.isBlank()) "Grade/Class cannot be empty" else null
        parentNameError = if (parentGuardianName.isBlank()) "Parent/Guardian name cannot be empty" else null
        parentPhoneError = when {
            parentGuardianPhone.isBlank() -> "Phone number cannot be empty"
            parentGuardianPhone.length < 9 -> "Phone number must be at least 9 digits"
            else -> null
        }
        dvbsDayError = if (dvbsDay.isBlank()) "DVBS day cannot be empty" else null

        return childNameError == null &&
                ageError == null &&
                gradeClassError == null &&
                parentNameError == null &&
                parentPhoneError == null &&
                dvbsDayError == null
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (registrationId == null) "DVBS Registration" else "Edit Registration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = primaryColor,
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = if (isDarkMode) darkModeBackground else lightModeBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = childName,
                onValueChange = {
                    childName = it
                    childNameError = null
                },
                label = { Text("Child's Name") },
                placeholder = { Text("e.g., John Doe") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = childNameError != null,
                shape = RoundedCornerShape(12.dp)
            )
            if (childNameError != null) {
                Text(
                    text = childNameError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            OutlinedTextField(
                value = ageString,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        ageString = newValue
                    }
                    ageError = null
                },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = ageError != null,
                shape = RoundedCornerShape(12.dp)
            )
            if (ageError != null) {
                Text(
                    text = ageError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            // Gender Selection
            Text("Gender", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedGender == "Boy",
                        onClick = { selectedGender = "Boy" }
                    )
                    Text("Boy")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedGender == "Girl",
                        onClick = { selectedGender = "Girl" }
                    )
                    Text("Girl")
                }
            }

            ExposedDropdownMenuBox(
                expanded = gradeExpanded,
                onExpandedChange = { gradeExpanded = !gradeExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = gradeClass,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Grade/Class") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    isError = gradeClassError != null,
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = gradeExpanded,
                    onDismissRequest = { gradeExpanded = false }
                ) {
                    gradeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                gradeClass = option
                                gradeExpanded = false
                                gradeClassError = null
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
            if (gradeClassError != null) {
                Text(
                    text = gradeClassError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            OutlinedTextField(
                value = parentGuardianName,
                onValueChange = {
                    parentGuardianName = it
                    parentNameError = null
                },
                label = { Text("Parent/Guardian's Name") },
                placeholder = { Text("e.g., Jane Doe") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = parentNameError != null,
                shape = RoundedCornerShape(12.dp)
            )
            if (parentNameError != null) {
                Text(
                    text = parentNameError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            OutlinedTextField(
                value = parentGuardianPhone,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { it.isDigit() || it == '+' || it == '-' || it == ' ' }) {
                        parentGuardianPhone = newValue
                    }
                    parentPhoneError = null
                },
                label = { Text("Parent/Guardian's Phone Number") },
                placeholder = { Text("e.g., +254712345678") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = parentPhoneError != null,
                shape = RoundedCornerShape(12.dp)
            )
            if (parentPhoneError != null) {
                Text(
                    text = parentPhoneError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            OutlinedTextField(
                value = eventDate,
                onValueChange = { },
                readOnly = true,
                label = { Text("DVBS Event Date") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = dvbsDay,
                onValueChange = {
                    dvbsDay = it
                    dvbsDayError = null
                },
                label = { Text("DVBS Day") },
                placeholder = { Text("e.g., Day 1 or Monday") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = dvbsDayError != null,
                shape = RoundedCornerShape(12.dp)
            )
            if (dvbsDayError != null) {
                Text(
                    text = dvbsDayError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (validateFields()) {
                        val newRegistration = DVBSRegistration(
                            id = registrationId ?: UUID.randomUUID().toString(),
                            childName = childName,
                            age = ageString.toIntOrNull() ?: 0,
                            gradeClass = gradeClass,
                            parentGuardianName = parentGuardianName,
                            parentGuardianPhone = parentGuardianPhone,
                            eventDate = eventDate,
                            dvbsDay = dvbsDay,
                            gender = selectedGender,
                            registrationDate = if (registrationId == null) sdf.format(Date()) else eventDate, // Use existing if editing
                            registeredBy = FirebaseAuth.getInstance().currentUser?.email ?: "Unknown",
                            createdAt = Date()
                        )

                        dvbsViewModel.addDVBSRegistration(newRegistration)
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                enabled = childName.isNotBlank() && ageString.isNotBlank() && gradeClass.isNotBlank()
                        && parentGuardianName.isNotBlank() && parentGuardianPhone.isNotBlank()
                        && eventDate.isNotBlank() && dvbsDay.isNotBlank()
            ) {
                Text(if (registrationId == null) "Register Child" else "Update Registration", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
