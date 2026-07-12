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
    isDarkMode: Boolean = false
) {
    var childName by rememberSaveable { mutableStateOf("") }
    var ageString by rememberSaveable { mutableStateOf("") }
    var gradeClass by rememberSaveable { mutableStateOf("") }
    var parentGuardianName by rememberSaveable { mutableStateOf("") }
    var parentGuardianPhone by rememberSaveable { mutableStateOf("") }

    var childNameError by rememberSaveable { mutableStateOf<String?>(null) }
    var ageError by rememberSaveable { mutableStateOf<String?>(null) }
    var gradeClassError by rememberSaveable { mutableStateOf<String?>(null) }
    var parentNameError by rememberSaveable { mutableStateOf<String?>(null) }
    var parentPhoneError by rememberSaveable { mutableStateOf<String?>(null) }

    var gradeExpanded by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFFC62828)
    val lightModeBackground = Color(0xFFF0F0F0)
    val darkModeBackground = Color.Black

    val gradeOptions = listOf(
        "Playgroup", "PP1", "PP2", "Grade 1", "Grade 2", "Grade 3",
        "Grade 4", "Grade 5", "Grade 6", "Grade 7", "Grade 8"
    )

    fun clearForm() {
        childName = ""
        ageString = ""
        gradeClass = ""
        parentGuardianName = ""
        parentGuardianPhone = ""
        childNameError = null
        ageError = null
        gradeClassError = null
        parentNameError = null
        parentPhoneError = null
    }

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

        return childNameError == null &&
                ageError == null &&
                gradeClassError == null &&
                parentNameError == null &&
                parentPhoneError == null
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("DVBS Registration") },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 2.dp, bottom = 6.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 2.dp, bottom = 6.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 2.dp, bottom = 6.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 2.dp, bottom = 6.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 2.dp, bottom = 6.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (validateFields()) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val registrationDate = sdf.format(Date())

                        val newRegistration = DVBSRegistration(
                            id = UUID.randomUUID().toString(),
                            childName = childName,
                            age = ageString.toIntOrNull() ?: 0,
                            gradeClass = gradeClass,
                            parentGuardianName = parentGuardianName,
                            parentGuardianPhone = parentGuardianPhone,
                            registrationDate = registrationDate,
                            registeredBy = FirebaseAuth.getInstance().currentUser?.email ?: "Unknown",
                            createdAt = Date()
                        )

                        dvbsViewModel.addDVBSRegistration(newRegistration)
                        clearForm()
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                enabled = childName.isNotBlank() && ageString.isNotBlank() && gradeClass.isNotBlank()
                        && parentGuardianName.isNotBlank() && parentGuardianPhone.isNotBlank()
            ) {
                Text("Register Child", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
