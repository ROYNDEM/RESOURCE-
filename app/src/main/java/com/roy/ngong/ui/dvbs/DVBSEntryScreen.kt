
package com.roy.ngong.ui.dvbs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.roy.ngong.data.DVBSRecord
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DVBSEntryScreen(
    dvbsViewModel: DVBSViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    isDarkMode: Boolean = false
) {
    var title by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var attendeeCount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(getCurrentDate()) }

    val primaryColor = Color(0xFFC62828)
    val lightModeBackground = Color(0xFFF0F0F0)
    val darkModeBackground = Color.Black

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add DVBS Record") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = primaryColor, navigationIconContentColor = Color.White,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = selectedDate,
                onValueChange = { selectedDate = it },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Topic/Theme") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = attendeeCount,
                onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) attendeeCount = it },
                label = { Text("Attendees") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isNotBlank() && selectedDate.isNotBlank()) {
                        val record = DVBSRecord(
                            id = UUID.randomUUID().toString(),
                            date = selectedDate,
                            title = title,
                            topic = topic,
                            location = location,
                            attendeeCount = attendeeCount.toIntOrNull() ?: 0,
                            notes = notes,
                            recordedBy = FirebaseAuth.getInstance().currentUser?.email ?: "Unknown",
                            createdAt = Date()
                        )
                        dvbsViewModel.addDVBSRecord(record)
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                enabled = title.isNotBlank() && selectedDate.isNotBlank()
            ) {
                Text("Save DVBS Record", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}

