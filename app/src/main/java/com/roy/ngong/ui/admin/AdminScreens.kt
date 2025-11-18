@file:Suppress("DEPRECATION")

package com.roy.ngong.ui.admin

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.roy.ngong.data.ClassSessionLog
import com.roy.ngong.navigation.AppDestinations
import com.roy.ngong.ui.resource.viewmodel.ResourceViewModel
import java.text.SimpleDateFormat
import java.util.Locale

import androidx.compose.material.icons.filled.NoteAdd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    isDarkMode: Boolean = false,
    onThemeToggle: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null
) {
    val userEmail = authViewModel.currentUser?.email ?: "Admin"
    val welcomeMessage = "Welcome, $userEmail"
    val churchRed = Color(0xFFD32F2F)
    val churchBlack = Color.Black
    val churchWhite = Color.White
    var settingsExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Admin Dashboard",
                        style = MaterialTheme.typography.titleMedium,
                        color = churchWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = churchWhite
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { settingsExpanded = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = churchWhite)
                        }
                        DropdownMenu(
                            expanded = settingsExpanded,
                            onDismissRequest = { settingsExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Profile") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                onClick = {
                                    settingsExpanded = false
                                    onProfileClick?.invoke()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isDarkMode) "Light Mode" else "Dark Mode") },
                                leadingIcon = {
                                    Icon(
                                        if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    settingsExpanded = false
                                    onThemeToggle?.invoke()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                                onClick = {
                                    settingsExpanded = false
                                    authViewModel.logout()
                                    onLogout()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = churchRed
                )
            )
        },
        containerColor = if (isDarkMode) churchBlack else MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = welcomeMessage,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isDarkMode) churchWhite else churchBlack
            )

            AdminDashboardCard(
                title = "View Resource Reports",
                icon = Icons.Default.BarChart,
                iconTint = churchRed,
                onClick = { navController.navigate(AppDestinations.ADMIN_REPORTS_LIST_ROUTE) },
                isDarkMode = isDarkMode
            )

            AdminDashboardCard(
                title = "Manage App Content",
                icon = Icons.Default.Edit,
                iconTint = churchBlack,
                onClick = { navController.navigate(AppDestinations.ADMIN_MANAGE_CONTROLS_ROUTE) },
                isDarkMode = isDarkMode
            )
                 AdminDashboardCard(
                title = "Create Calendar Entry",
                icon = Icons.Default.NoteAdd, // You will need to import this
                iconTint = Color(0xFF006400), // A dark green color
                onClick = { navController.navigate(AppDestinations.addEditEventRoute("new_event")) },
                isDarkMode = isDarkMode
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminDashboardCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    isDarkMode: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF212121) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = iconTint
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = if (isDarkMode) Color.White else Color.Black
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Navigate",
                tint = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManageControlsScreen(
    appDataViewModel: AppDataViewModel,
    onNavigateBack: () -> Unit
) {
    val generalData by appDataViewModel.generalData.collectAsState()
    var verseText by remember(generalData.verseOfTheDay) { mutableStateOf(generalData.verseOfTheDay) }
    var announcementText by remember(generalData.announcement) { mutableStateOf(generalData.announcement) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage App Controls") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = verseText,
                onValueChange = { verseText = it },
                label = { Text("Verse of the Day") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            OutlinedTextField(
                value = announcementText,
                onValueChange = { announcementText = it },
                label = { Text("Announcement") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            Button(
                onClick = { appDataViewModel.updateGeneralData(verseText, announcementText) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save Changes")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsListScreen(
    navController: NavController,
    resourceViewModel: ResourceViewModel,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        resourceViewModel.startListeningForLogs()
    }

    val dateTotals by resourceViewModel.dateTotals.collectAsState()
    val sortedDates = dateTotals.keys.sortedDescending()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resource Reports by Date") },
                navigationIcon = { IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                } }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (dateTotals.isEmpty()){
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center){
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Loading reports...")
                        }
                    }
                }
            } else {
                items(sortedDates, key = { it }) { dateString ->
                    val total = dateTotals[dateString] ?: 0
                    ReportDateCard(
                        dateString = dateString,
                        totalAttendees = total,
                        onClick = {
                            resourceViewModel.loadReportForDate(dateString)
                            navController.navigate(AppDestinations.reportDetailRoute(dateString))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReportDateCard(dateString: String, totalAttendees: Int, onClick: () -> Unit) {
    val displayDate = remember(dateString) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val formatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
            parser.parse(dateString)?.let { formatter.format(it) } ?: dateString
        } catch (_: Exception) {
            dateString
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(displayDate, style = MaterialTheme.typography.titleLarge)
                Text("Total Attendance: $totalAttendees", style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "View Details")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportDetailScreen(
    resourceViewModel: ResourceViewModel,
    date: String,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(date) {
        resourceViewModel.loadReportForDate(date)
    }

    val logsForDate by resourceViewModel.selectedDateLogs.collectAsState()
    val grandTotal = logsForDate.sumOf { it.attendeeCount }

    val groupedLogs = logsForDate.groupBy { it.serviceName }.toSortedMap()

    val displayDate = remember(date) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.US)
            parser.parse(date)?.let { formatter.format(it) } ?: date
        } catch (_: Exception) { date }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report for $displayDate") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { GrandTotalCard(total = grandTotal) }

            groupedLogs.forEach { (serviceName, logsInService) ->
                val serviceTotal = logsInService.sumOf { it.attendeeCount }
                val compilers = logsInService.map { it.submittedBy }.distinct().joinToString(", ")

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(serviceName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                                Text("$serviceTotal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(Modifier.height(8.dp))

                            Text(
                                buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Compiled by: ")
                                    }
                                    append(compilers)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.height(16.dp))

                            val classOrder = listOf(
                                "playgroup", "pp1", "pp2", "grade one", "grade 2", "grade 3",
                                "grade 4", "grade 5", "grade 6", "grade 7", "hopes"
                            )
                            val sortedLogs = logsInService.sortedWith(compareBy { log ->
                                val index = classOrder.indexOf(log.className.trim().lowercase(Locale.ROOT))
                                if (index != -1) index else Int.MAX_VALUE
                            })

                            sortedLogs.forEach { log ->
                                ReportLogItem(log)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GrandTotalCard(total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Grand Total Attendees", style = MaterialTheme.typography.titleMedium)
            Text(total.toString(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ReportLogItem(log: ClassSessionLog) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(start = 8.dp)
    ) {
        Text(log.className, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)

        val teachers = log.teacherNames.joinToString()
        val helpers = log.teacherHelpers.joinToString()

        if (teachers.isNotBlank()) {
            Text("Teacher(s): $teachers", style = MaterialTheme.typography.bodyMedium)
        }
        if (helpers.isNotBlank()) {
            Text("Helper(s): $helpers", style = MaterialTheme.typography.bodyMedium)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Attendees:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text("${log.attendeeCount}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}