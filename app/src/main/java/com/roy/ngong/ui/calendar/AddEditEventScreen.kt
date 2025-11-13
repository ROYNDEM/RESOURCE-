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
    // Determine mode first
    val isNewEventFromAdmin = (dateString == "new_event")

    // Load event data BEFORE creating state - use the first event for the date (if any)
    val existingEvent = if (!isNewEventFromAdmin) {
        calendarViewModel.events.collectAsState().value[dateString]?.firstOrNull()
    } else {
        null
    }

    // Initialize state with loaded data
    var title by remember { mutableStateOf(existingEvent?.title.orEmpty()) }
    var description by remember { mutableStateOf(existingEvent?.description.orEmpty()) }

    // Determine mode and manage the selected date
    var selectedDate by remember {
        mutableStateOf(
            if (isNewEventFromAdmin) LocalDate.now() else LocalDate.parse(dateString)
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val screenTitle = if (isNewEventFromAdmin) "Create New Event" else "Edit Event"
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
            // Conditionally show date picker or static date
            if (isNewEventFromAdmin) {
                // If creating from admin, allow changing the date
                OutlinedTextField(
                    value = formattedDisplayDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Event Date") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    trailingIcon = { Icon(Icons.Default.DateRange, "Select Date") }
                )
            } else {
                // If adding from calendar, just display the date
                Text(
                    text = "Adding event for: $formattedDisplayDate",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
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
                    // Use the dynamic `selectedDate`
                    val dateToSave = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    calendarViewModel.addEvent(title.trim(), description.trim(), dateToSave)

                    // Navigate based on where the user came from
                    if (isNewEventFromAdmin) {
                        navController.navigate(AppDestinations.ADMIN_DASHBOARD_ROUTE) {
                            popUpTo(AppDestinations.ADMIN_DASHBOARD_ROUTE) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Save Event")
            }
        }
    }

    // Use Android's DatePickerDialog for compatibility
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