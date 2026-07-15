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
import com.roy.ngong.data.DVBSResource
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DVBSEntryScreen(
    dvbsViewModel: DVBSViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    isDarkMode: Boolean = false,
    resourceId: String? = null
) {
    var dvbsDay by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("") }
    var numChildren by remember { mutableStateOf("") }
    var numNewSalvations by remember { mutableStateOf("") }
    var numWorkers by remember { mutableStateOf("") }
    
    // Grade selection
    var selectedGradeBase by remember { mutableStateOf("Playgroup") }
    var selectedStream by remember { mutableStateOf("A") }
    
    val gradeBases = listOf("Playgroup", "PP1", "PP2") + (1..8).map { "Grade $it" }
    val streams = listOf("A", "B", "C", "D")
    
    var selectedDate by remember { mutableStateOf(getCurrentDate()) }

    val resources by dvbsViewModel.dvbsResources.collectAsState()

    // Pre-populate if editing
    LaunchedEffect(resourceId, resources) {
        if (resourceId != null) {
            val existing = resources.find { it.id == resourceId }
            if (existing != null) {
                dvbsDay = existing.dvbsDay
                teacherName = existing.teacherName
                numChildren = existing.numChildren.toString()
                numNewSalvations = existing.numNewSalvations.toString()
                numWorkers = existing.numWorkers.toString()
                selectedDate = existing.date
                
                // Parse grade into base and stream
                val parts = existing.grade.split(" ")
                if (parts.size >= 2) {
                    selectedStream = parts.last()
                    selectedGradeBase = existing.grade.removeSuffix(" $selectedStream")
                }
            }
        }
    }

    val primaryColor = Color(0xFFC62828)
    val lightModeBackground = Color(0xFFF0F0F0)
    val darkModeBackground = Color.Black

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (resourceId == null) "Add DVBS Resource Entry" else "Edit Resource Entry") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Date: $selectedDate", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = dvbsDay,
                onValueChange = { dvbsDay = it },
                label = { Text("DVBS Day (e.g., Day 1)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Grade Base Dropdown
                var baseExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = baseExpanded,
                    onExpandedChange = { baseExpanded = !baseExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedGradeBase,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grade") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = baseExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = baseExpanded,
                        onDismissRequest = { baseExpanded = false }
                    ) {
                        gradeBases.forEach { base ->
                            DropdownMenuItem(
                                text = { Text(base) },
                                onClick = {
                                    selectedGradeBase = base
                                    baseExpanded = false
                                }
                            )
                        }
                    }
                }

                // Stream Dropdown
                var streamExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = streamExpanded,
                    onExpandedChange = { streamExpanded = !streamExpanded },
                    modifier = Modifier.weight(0.5f)
                ) {
                    OutlinedTextField(
                        value = selectedStream,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Stream") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = streamExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = streamExpanded,
                        onDismissRequest = { streamExpanded = false }
                    ) {
                        streams.forEach { stream ->
                            DropdownMenuItem(
                                text = { Text(stream) },
                                onClick = {
                                    selectedStream = stream
                                    streamExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = teacherName,
                onValueChange = { teacherName = it },
                label = { Text("Teacher's Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = numChildren,
                onValueChange = { if (it.all { c -> c.isDigit() }) numChildren = it },
                label = { Text("Number of Children") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = numNewSalvations,
                onValueChange = { if (it.all { c -> c.isDigit() }) numNewSalvations = it },
                label = { Text("Number of New Salvations") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = numWorkers,
                onValueChange = { if (it.all { c -> c.isDigit() }) numWorkers = it },
                label = { Text("Number of Workers") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val resource = DVBSResource(
                        id = resourceId ?: UUID.randomUUID().toString(),
                        date = selectedDate,
                        dvbsDay = dvbsDay,
                        grade = "$selectedGradeBase $selectedStream",
                        teacherName = teacherName,
                        numChildren = numChildren.toIntOrNull() ?: 0,
                        numNewSalvations = numNewSalvations.toIntOrNull() ?: 0,
                        numWorkers = numWorkers.toIntOrNull() ?: 0,
                        recordedBy = FirebaseAuth.getInstance().currentUser?.email ?: "Unknown",
                        createdAt = Date()
                    )
                    dvbsViewModel.addDVBSResource(resource)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                enabled = teacherName.isNotBlank() && dvbsDay.isNotBlank()
            ) {
                Text(if (resourceId == null) "Save DVBS Resource Entry" else "Update Resource Entry", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}
