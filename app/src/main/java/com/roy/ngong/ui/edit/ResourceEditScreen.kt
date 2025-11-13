package com.roy.ngong.ui.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.roy.ngong.data.ClassSessionLog
import com.roy.ngong.ui.pending.PendingSubmissionsViewModel
import com.roy.ngong.ui.resource.viewmodel.ResourceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceEditScreen(
    pendingViewModel: PendingSubmissionsViewModel,
    resourceViewModel: ResourceViewModel,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit
) {
    // Collect the list of pending logs from the ViewModel. The 'initial' value prevents
    // a brief flicker before the first list is loaded from the database.
    val pendingLogs by pendingViewModel.pendingLogs.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    // This state tracks which card is currently expanded. Only one can be expanded at a time.
    var currentlyExpandedId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pending Entries for Review") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        // If the list of logs is empty, show a message in the center.
        if (pendingLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No pending entries found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // If there are logs, display them in a LazyColumn (a scrollable list).
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingLogs, key = { it.id }) { log ->
                    PendingEntryCard(
                        log = log,
                        isExpanded = currentlyExpandedId == log.id,
                        onExpand = {
                            // If the clicked card is already expanded, collapse it. Otherwise, expand it.
                            currentlyExpandedId = if (currentlyExpandedId == log.id) null else log.id
                        },
                        onUpdate = { updatedLog ->
                            pendingViewModel.saveLogLocally(updatedLog)
                            scope.launch {
                                snackbarHostState.showSnackbar("Entry updated locally.")
                            }
                        },
                        onSubmit = { finalLog ->
                            // When submitting:
                            // 1. Save to the online Firestore database.
                            resourceViewModel.saveResourceEntry(finalLog)
                            // 2. Delete from the local Room database.
                            pendingViewModel.deleteLog(finalLog)
                            scope.launch {
                                snackbarHostState.showSnackbar("Entry submitted successfully!")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingEntryCard(
    log: ClassSessionLog,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onUpdate: (ClassSessionLog) -> Unit,
    onSubmit: (ClassSessionLog) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            // This is the un-expanded, clickable part of the card.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExpand)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(log.className, style = MaterialTheme.typography.titleMedium)
                    val formattedDate = log.sessionDate?.let {
                        SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(it)
                    } ?: "No Date"
                    Text(
                        "${log.serviceName} - $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            // This is the expandable part with the editable form.
            AnimatedVisibility(visible = isExpanded) {
                EditableLogContent(log = log, onUpdate = onUpdate, onSubmit = onSubmit)
            }
        }
    }
}

@Composable
private fun EditableLogContent(
    log: ClassSessionLog,
    onUpdate: (ClassSessionLog) -> Unit,
    onSubmit: (ClassSessionLog) -> Unit
) {
    // State for all editable fields, initialized from the log data.
    // `remember(log.id)` ensures the state resets if a different log is displayed.
    var teacherNames by remember(log.id) { mutableStateOf(log.teacherNames.joinToString(", ")) }
    var teacherHelpers by remember(log.id) { mutableStateOf(log.teacherHelpers.joinToString(", ")) }
    var attendeeCount by remember(log.id) { mutableStateOf(log.attendeeCount.toString()) }
    var submittedBy by remember(log.id) { mutableStateOf(log.submittedBy) }

    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }

    // Helper function to create an updated log object from the current state.
    fun buildUpdatedLog(): ClassSessionLog {
        return log.copy(
            teacherNames = teacherNames.split(',').map { it.trim() }.filter { it.isNotEmpty() },
            teacherHelpers = teacherHelpers.split(',').map { it.trim() }.filter { it.isNotEmpty() },
            attendeeCount = attendeeCount.toIntOrNull() ?: 0,
            submittedBy = submittedBy
        )
    }

    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = teacherNames,
            onValueChange = { teacherNames = it },
            label = { Text("Teacher(s) Name(s)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = teacherHelpers,
            onValueChange = { teacherHelpers = it },
            label = { Text("Teacher Helper(s)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = attendeeCount,
            onValueChange = { if (it.all(Char::isDigit)) attendeeCount = it },
            label = { Text("Number of Attendees") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = submittedBy,
            onValueChange = { submittedBy = it },
            label = { Text("Submitted By") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Action buttons (Update and Submit)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { onUpdate(buildUpdatedLog()) },
                enabled = !isSubmitting
            ) {
                Text("Update")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    isSubmitting = true
                    // The onSubmit function will handle deleting the item,
                    // which will cause this composable to be removed from the list.
                    onSubmit(buildUpdatedLog())
                },
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(24.dp)
                            .width(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit to Database")
                }
            }
        }
    }
}