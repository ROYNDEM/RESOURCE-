package com.roy.ngong.ui.dvbs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.roy.ngong.data.DVBSRecord
import com.roy.ngong.data.DVBSRegistration
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DVBSScreen(
    dvbsViewModel: DVBSViewModel = viewModel(),
    isDarkMode: Boolean = false,
    onNavigateToResourceEntry: () -> Unit = {},
    onNavigateToRegistrationEntry: () -> Unit = {}
) {
    val records by dvbsViewModel.dvbsRecords.collectAsState()
    val registrations by dvbsViewModel.dvbsRegistrations.collectAsState()
    val statistics by dvbsViewModel.dvbsStatistics.collectAsState()

    val primaryColor = Color(0xFFC62828)
    val lightModeBackground = Color(0xFFF0F0F0)
    val darkModeBackground = Color.Black
    val contentColor = if (isDarkMode) Color.White else Color.Black

    var selectedTab by remember { mutableIntStateOf(1) }  // Default to Registrations tab

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) {
                        onNavigateToResourceEntry()
                    } else {
                        onNavigateToRegistrationEntry()
                    }
                },
                containerColor = primaryColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkMode) darkModeBackground else lightModeBackground)
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                containerColor = primaryColor,
                contentColor = Color.White
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("DVBS Events") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Registrations") }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        Text(
                            text = "DVBS Events",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (statistics.totalEvents > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard("Events", statistics.totalEvents.toString(), primaryColor)
                                StatCard("Attendees", statistics.totalAttendees.toString(), Color(0xFF1976D2))
                                StatCard("Avg Attend", String.format(Locale.getDefault(), "%.1f", statistics.averageAttendance), Color(0xFF388E3C))
                            }
                        }

                        Text(
                            text = "Records (${records.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(records) { record ->
                                DVBSRecordItem(record, contentColor)
                            }
                        }
                    }
                    1 -> {
                        Text(
                            text = "Child Registrations",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Total Registrations: ${registrations.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(registrations) { registration ->
                                DVBSRegistrationItem(registration, contentColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, color: Color) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier.size(120.dp, 100.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Bold, color = Color.White)
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun DVBSRecordItem(record: DVBSRecord, contentColor: Color) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(record.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
            if (record.topic.isNotEmpty()) Text("Topic: ${record.topic}", style = MaterialTheme.typography.bodyMedium, color = contentColor)
            if (record.location.isNotEmpty()) Text("Location: ${record.location}", style = MaterialTheme.typography.bodyMedium, color = contentColor)
            Text("Attendees: ${record.attendeeCount}", style = MaterialTheme.typography.bodyMedium, color = contentColor)
            if (record.notes.isNotEmpty()) Text(record.notes, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.8f))
            Text("Date: ${record.date}", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun DVBSRegistrationItem(registration: DVBSRegistration, contentColor: Color) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(registration.childName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
            Text("Age: ${registration.age}", style = MaterialTheme.typography.bodyMedium, color = contentColor)
            Text("Grade/Class: ${registration.gradeClass}", style = MaterialTheme.typography.bodyMedium, color = contentColor)
            Text("Parent/Guardian: ${registration.parentGuardianName}", style = MaterialTheme.typography.bodyMedium, color = contentColor)
            Text("Phone: ${registration.parentGuardianPhone}", style = MaterialTheme.typography.bodyMedium, color = contentColor)
            Text("Date: ${registration.registrationDate}", style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.6f))
        }
    }
}
