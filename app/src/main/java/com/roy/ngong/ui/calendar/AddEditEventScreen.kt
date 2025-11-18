package com.roy.ngong.ui.calendar

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.roy.ngong.navigation.AppDestinations
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import androidx.compose.runtime.collectAsState

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventScreen(
    navController: NavController,
    dateString: String, // Will be "new_event" or a specific date like "2024-11-13"
    calendarViewModel: CalendarViewModel = viewModel()
) {
    val isNewEvent = dateString == "new_event"

    val initialDate = if (isNewEvent) {
        LocalDate.now()
    } else {
        try {
            LocalDate.parse(dateString)
        } catch (e: Exception) {
            LocalDate.now() // Fallback to today if parsing fails
        }
    }

    val existingEvent = if (!isNewEvent) {
        calendarViewModel.events.collectAsState().value[dateString]?.firstOrNull()
    } else {
        null
    }

    var title by remember { mutableStateOf(existingEvent?.title.orEmpty()) }
    var description by remember { mutableStateOf(existingEvent?.description.orEmpty()) }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val screenTitle = if (existingEvent == null) "Create New Event" else "Edit Event"
    val formattedDisplayDate = selectedDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // The Box makes the whole area clickable.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isNewEvent) {
                        // Only show the date picker if it's a new event where the date can be changed.
                        if (isNewEvent) {
                            showDatePicker = true
                        }
                    }
            ) {
                OutlinedTextField(
                    value = formattedDisplayDate,
                    onValueChange = {},
                    label = { Text("Event Date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    // Disable the text field to allow the Box's click to go through.
                    enabled = false,
                    // Override the colors to make it look like a normal, enabled text field.
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    trailingIcon = {
                        if (isNewEvent) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    }
                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Event Description (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val dateToSave = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    if (existingEvent == null) {
                        calendarViewModel.addEvent(title.trim(), description.trim(), dateToSave)
                    } else {
                        // Assuming you will add an updateEvent function to your ViewModel
                        // calendarViewModel.updateEvent(existingEvent.copy(title = title.trim(), description = description.trim(), date = dateToSave))
                    }

                    navController.popBackStack()
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(if (existingEvent == null) "Save Event" else "Update Event")
            }
        }
    }

    if (showDatePicker) {
        val year = selectedDate.year
        val month = selectedDate.monthValue - 1 // DatePickerDialog months are 0-based
        val day = selectedDate.dayOfMonth

        DatePickerDialog(
            context,
            { _, y, m, d ->
                selectedDate = LocalDate.of(y, m + 1, d)
                showDatePicker = false
            },
            year,
            month,
            day
        ).apply {
            setOnCancelListener { showDatePicker = false }
            show()
        }
    }
}