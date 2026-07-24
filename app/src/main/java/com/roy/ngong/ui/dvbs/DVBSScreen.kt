package com.roy.ngong.ui.dvbs

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.roy.ngong.data.DVBSRegistration
import com.roy.ngong.data.DVBSResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import com.google.firebase.auth.FirebaseAuth

enum class DVBSViewMode {
    RESOURCES, REGISTRATIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DVBSScreen(
    dvbsViewModel: DVBSViewModel = viewModel(),
    isDarkMode: Boolean = false,
    mode: DVBSViewMode = DVBSViewMode.REGISTRATIONS,
    userRole: String = "general", // "admin", "resource", "registration", "general"
    onNavigateToResourceEntry: (String?) -> Unit = {},
    onNavigateToRegistrationEntry: (String?) -> Unit = {}
) {
    val registrations by dvbsViewModel.dvbsRegistrations.collectAsState()
    val resources by dvbsViewModel.dvbsResources.collectAsState()

    val primaryColor = Color(0xFFC62828)
    val lightModeBackground = Color(0xFFF0F0F0)
    val darkModeBackground = Color.Black
    val contentColor = if (isDarkMode) Color.White else Color.Black

    var fabExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDayFilter by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // --- INITIAL LOADING SKELETON ---
    var isInitialLoading by remember { mutableStateOf(true) }
    LaunchedEffect(registrations, resources) {
        if (registrations.isNotEmpty() || resources.isNotEmpty()) {
            isInitialLoading = false
        }
    }
    LaunchedEffect(Unit) {
        delay(2500)
        isInitialLoading = false
    }

    val onRefresh = {
        scope.launch {
            isRefreshing = true
            dvbsViewModel.startListening()
            delay(1000)
            isRefreshing = false
        }
    }

    // Determine the view mode if not explicitly set (for the swipeable page)
    val effectiveMode = when {
        userRole == "registration" -> DVBSViewMode.REGISTRATIONS
        userRole == "resource" -> DVBSViewMode.RESOURCES
        else -> mode // Admin sees whatever mode is passed (default registrations)
    }

    // --- DAY FILTER OPTIONS ---
    val availableDays = remember(registrations, resources, effectiveMode) {
        if (effectiveMode == DVBSViewMode.REGISTRATIONS) {
            registrations.map { it.dvbsDay }.distinct().sorted()
        } else {
            resources.map { it.dvbsDay }.distinct().sorted()
        }
    }

    // --- FILTERING LOGIC ---
    val filteredRegistrations = remember(registrations, searchQuery, selectedDayFilter) {
        registrations.filter { r ->
            (selectedDayFilter == null || r.dvbsDay == selectedDayFilter) &&
                    (searchQuery.isBlank() ||
                            r.childName.contains(searchQuery, ignoreCase = true) ||
                            r.parentGuardianName.contains(searchQuery, ignoreCase = true) ||
                            r.gradeClass.contains(searchQuery, ignoreCase = true) ||
                            r.dvbsDay.contains(searchQuery, ignoreCase = true))
        }
    }

    val filteredResources = remember(resources, searchQuery, selectedDayFilter) {
        resources.filter { r ->
            (selectedDayFilter == null || r.dvbsDay == selectedDayFilter) &&
                    (searchQuery.isBlank() ||
                            r.teacherName.contains(searchQuery, ignoreCase = true) ||
                            r.grade.contains(searchQuery, ignoreCase = true) ||
                            r.dvbsDay.contains(searchQuery, ignoreCase = true))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) darkModeBackground else lightModeBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val titleText = when (userRole) {
                "admin" -> "DVBS Admin Insights"
                "registration" -> "My Registrations"
                "resource" -> "My Resource Entries"
                else -> if (effectiveMode == DVBSViewMode.REGISTRATIONS) "Child Registrations" else "Resource Entries"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    modifier = Modifier.weight(1f)
                )
                if (userRole == "admin") {
                    val context = LocalContext.current
                    IconButton(onClick = {
                        if (effectiveMode == DVBSViewMode.REGISTRATIONS) {
                            exportRegistrationsToCSV(context, registrations, "all")
                        } else {
                            exportResourcesToCSV(context, resources, "all")
                        }
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Download All CSV", tint = primaryColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (userRole == "admin") {
                if (isInitialLoading) {
                    DVBSAnalyticsSkeleton(isDarkMode = isDarkMode)
                } else {
                    DVBSAdminAnalyticsDashboard(
                        registrations = registrations,
                        resources = resources,
                        contentColor = contentColor,
                        isDarkMode = isDarkMode
                    )
                }
            } else {
                // --- SEARCH BAR ---
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    placeholder = { Text("Search by name, grade, or day...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color.White,
                        unfocusedContainerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color.White,
                        focusedIndicatorColor = primaryColor,
                        cursorColor = primaryColor
                    )
                )

                // --- DAY FILTER CHIPS ---
                if (availableDays.isNotEmpty()) {
                    DayFilterChipRow(
                        days = availableDays,
                        selectedDay = selectedDayFilter,
                        onDaySelected = { day ->
                            selectedDayFilter = if (selectedDayFilter == day) null else day
                        },
                        primaryColor = primaryColor,
                        contentColor = contentColor,
                        isDarkMode = isDarkMode
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { onRefresh() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isInitialLoading) {
                        DVBSListSkeleton(isDarkMode = isDarkMode)
                    } else if (effectiveMode == DVBSViewMode.REGISTRATIONS) {
                        if (filteredRegistrations.isEmpty()) {
                            EmptyListPlaceholder(
                                if (searchQuery.isEmpty() && selectedDayFilter == null) "No registrations yet." else "No matches found.",
                                contentColor
                            )
                        } else {
                            val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
                            val myRegistrations = if (userRole == "registration") {
                                filteredRegistrations.filter { it.registeredBy == currentUserEmail }
                            } else filteredRegistrations

                            val groupedRegistrations = myRegistrations.groupBy { it.dvbsDay }
                                .toSortedMap()

                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                groupedRegistrations.forEach { (day, dayRegistrations) ->
                                    item(key = day) {
                                        DVBSRegistrationDayGroup(
                                            day = day,
                                            registrations = dayRegistrations,
                                            contentColor = contentColor,
                                            isDarkMode = isDarkMode,
                                            isSearchActive = searchQuery.isNotBlank() || selectedDayFilter != null,
                                            isAdmin = false, // Edit/Delete disabled here
                                            onEdit = { onNavigateToRegistrationEntry(it.id) },
                                            onDelete = { }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        if (filteredResources.isEmpty()) {
                            EmptyListPlaceholder(
                                if (searchQuery.isEmpty() && selectedDayFilter == null) "No resource entries yet." else "No matches found.",
                                contentColor
                            )
                        } else {
                            val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
                            val myResources = if (userRole == "resource") {
                                filteredResources.filter { it.recordedBy == currentUserEmail }
                            } else filteredResources

                            val groupedResources = myResources.groupBy { it.dvbsDay }
                                .toSortedMap()

                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 80.dp)
                            ) {
                                groupedResources.forEach { (day, dayResources) ->
                                    item(key = day) {
                                        DVBSResourceDayGroup(
                                            day = day,
                                            resources = dayResources,
                                            contentColor = contentColor,
                                            isDarkMode = isDarkMode,
                                            isSearchActive = searchQuery.isNotBlank() || selectedDayFilter != null,
                                            isAdmin = false, // Edit/Delete disabled here
                                            onEdit = { onNavigateToResourceEntry(it.id) },
                                            onDelete = { }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB logic based on role
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            when (userRole) {
                "admin" -> {
                    MultiFloatingActionButton(
                        expanded = fabExpanded,
                        onExpandedChange = { fabExpanded = it },
                        primaryColor = primaryColor,
                        onAddRegistration = { onNavigateToRegistrationEntry(null) },
                        onAddResource = { onNavigateToResourceEntry(null) }
                    )
                }
                "registration" -> {
                    FloatingActionButton(
                        onClick = { onNavigateToRegistrationEntry(null) },
                        containerColor = primaryColor,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Registration")
                    }
                }
                "resource" -> {
                    FloatingActionButton(
                        onClick = { onNavigateToResourceEntry(null) },
                        containerColor = primaryColor,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Resource")
                    }
                }
            }
        }
    }
}

/**
 * Horizontal row of tappable day chips. Tapping the active chip again clears the filter.
 */
@Composable
private fun DayFilterChipRow(
    days: List<String>,
    selectedDay: String?,
    onDaySelected: (String?) -> Unit,
    primaryColor: Color,
    contentColor: Color,
    isDarkMode: Boolean
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 8.dp)
    ) {
        items(days) { day ->
            val selected = selectedDay == day
            FilterChip(
                selected = selected,
                onClick = { onDaySelected(day) },
                label = { Text(day.ifBlank { "Unspecified" }) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color.White,
                    labelColor = contentColor,
                    selectedContainerColor = primaryColor,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    borderColor = contentColor.copy(alpha = 0.2f),
                    selectedBorderColor = primaryColor
                )
            )
        }
    }
}

/**
 * A subtle, looping shimmer brush for skeleton placeholders.
 */
@Composable
private fun rememberShimmerBrush(isDarkMode: Boolean): Brush {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    val baseColor = if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFE2E2E2)
    val highlightColor = if (isDarkMode) Color(0xFF3D3D3D) else Color(0xFFF2F2F2)
    return Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateAnim - 300f, 0f),
        end = Offset(translateAnim, 300f)
    )
}

@Composable
private fun ShimmerBlock(width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp, brush: Brush) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(brush)
    )
}

/**
 * Skeleton placeholder shown for the list screens while the first
 * Firestore snapshot is still loading.
 */
@Composable
private fun DVBSListSkeleton(isDarkMode: Boolean) {
    val brush = rememberShimmerBrush(isDarkMode)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ShimmerBlock(width = 140.dp, height = 20.dp, brush = brush)
                    Spacer(modifier = Modifier.height(8.dp))
                    ShimmerBlock(width = 90.dp, height = 14.dp, brush = brush)
                }
            }
        }
    }
}

/**
 * Skeleton placeholder shown for the admin analytics dashboard while
 * the first Firestore snapshot is still loading.
 */
@Composable
private fun DVBSAnalyticsSkeleton(isDarkMode: Boolean) {
    val brush = rememberShimmerBrush(isDarkMode)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ShimmerBlock(width = 160.dp, height = 18.dp, brush = brush)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(2) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ShimmerBlock(width = 24.dp, height = 24.dp, brush = brush)
                        Spacer(modifier = Modifier.height(8.dp))
                        ShimmerBlock(width = 40.dp, height = 22.dp, brush = brush)
                        Spacer(modifier = Modifier.height(6.dp))
                        ShimmerBlock(width = 60.dp, height = 12.dp, brush = brush)
                    }
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) {
                    ShimmerBlock(width = 220.dp, height = 18.dp, brush = brush)
                }
            }
        }
    }
}

/**
 * Text that counts up from 0 to [targetValue] whenever the target changes.
 */
@Composable
private fun AnimatedCounterText(
    targetValue: Int,
    style: androidx.compose.ui.text.TextStyle,
    color: Color = Color.Unspecified
) {
    var displayedValue by remember { mutableIntStateOf(0) }
    LaunchedEffect(targetValue) {
        val steps = 24
        val stepDelayMs = 600L / steps
        val startValue = displayedValue
        for (i in 1..steps) {
            displayedValue = startValue + ((targetValue - startValue) * i / steps)
            delay(stepDelayMs)
        }
        displayedValue = targetValue
    }
    Text(displayedValue.toString(), style = style, fontWeight = FontWeight.Bold, color = color)
}

@Composable
fun DVBSAdminAnalyticsDashboard(
    registrations: List<DVBSRegistration>,
    resources: List<DVBSResource>,
    contentColor: Color,
    isDarkMode: Boolean
) {
    // Deduplicate registrations by child name and parent phone (unique children)
    val uniqueRegistrations = remember(registrations) {
        registrations.distinctBy {
            it.childName.lowercase().trim() + it.parentGuardianPhone.trim()
        }
    }

    val totalChildrenRegistered = uniqueRegistrations.size
    val totalSalvations = resources.sumOf { it.numNewSalvations }
    val totalWorkers = resources.sumOf { it.numWorkers }

    var selectedDayForDistribution by remember { mutableStateOf("All Days") }
    val availableDays = remember(registrations) {
        listOf("All Days") + registrations.map { it.dvbsDay }.distinct().sorted()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                "Overall Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnalyticsStatCard(
                    label = "Unique Children",
                    value = totalChildrenRegistered,
                    icon = Icons.Default.Group,
                    modifier = Modifier.weight(1f),
                    isDarkMode = isDarkMode
                )
                AnalyticsStatCard(
                    label = "New Salvations",
                    value = totalSalvations,
                    icon = Icons.Default.Add,
                    modifier = Modifier.weight(1f),
                    isDarkMode = isDarkMode,
                    color = Color(0xFF43A047)
                )
            }
        }

        item {
            Text(
                "Unique Registrations by Day",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            // Logic to find first-time registrations per day
            val uniquePerDay = registrations
                .sortedBy { it.registrationDate } // Assuming registrationDate is sortable yyyy-MM-dd
                .distinctBy { it.childName.lowercase().trim() + it.parentGuardianPhone.trim() }
                .groupBy { it.dvbsDay }
                .mapValues { it.value.size }
                .toSortedMap()

            SimpleBarChart(
                data = uniquePerDay,
                isDarkMode = isDarkMode,
                barColor = Color(0xFF00796B) // Teal for unique growth
            )
        }

        item {
            Text(
                "Daily Total Attendance (Raw)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            SimpleBarChart(
                data = resources.groupBy { it.dvbsDay }
                    .mapValues { it.value.sumOf { r -> r.numChildren } }
                    .toSortedMap(),
                isDarkMode = isDarkMode
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Grade Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )

                var expanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(selectedDayForDistribution, color = Color(0xFFC62828))
                        Icon(Icons.Default.ExpandMore, contentDescription = null, tint = Color(0xFFC62828))
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        availableDays.forEach { day ->
                            DropdownMenuItem(
                                text = { Text(day) },
                                onClick = {
                                    selectedDayForDistribution = day
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            val displayRegistrations = if (selectedDayForDistribution == "All Days") {
                uniqueRegistrations
            } else {
                registrations.filter { it.dvbsDay == selectedDayForDistribution }
            }

            SimpleBarChart(
                data = displayRegistrations.groupBy { it.gradeClass }
                    .mapValues { it.value.size }
                    .toSortedMap(),
                isDarkMode = isDarkMode,
                barColor = Color(0xFFFFA000)
            )
        }
    }
}

@Composable
fun AnalyticsStatCard(
    label: String,
    value: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false,
    color: Color = Color(0xFFC62828)
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedCounterText(targetValue = value, style = MaterialTheme.typography.headlineMedium)
            Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun SimpleBarChart(
    data: Map<String, Int>,
    isDarkMode: Boolean,
    barColor: Color = Color(0xFFC62828)
) {
    val maxValue = data.values.maxOrNull() ?: 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (data.isEmpty()) {
                Text("No data available for chart", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            } else {
                data.forEach { (label, value) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(label, modifier = Modifier.width(80.dp), style = MaterialTheme.typography.labelMedium)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(20.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            // Background track
                            Box(modifier = Modifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)))
                            // Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (maxValue > 0) value.toFloat() / maxValue else 0f)
                                    .fillMaxHeight()
                                    .background(barColor, RoundedCornerShape(4.dp))
                            )
                        }
                        Text(value.toString(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun exportRegistrationsToCSV(context: Context, registrations: List<DVBSRegistration>, dayLabel: String) {
    val csvHeader = "Child Name,Age,Gender,Grade,Parent Name,Parent Phone,Event Date,DVBS Day,Registration Date,Registered By\n"
    val csvData = registrations.joinToString("\n") { r ->
        "${r.childName},${r.age},${r.gender},${r.gradeClass},${r.parentGuardianName},${r.parentGuardianPhone},${r.eventDate},${r.dvbsDay},${r.registrationDate},${r.registeredBy}"
    }
    val fileName = "dvbs_registrations_${dayLabel.replace(" ", "_").lowercase()}.csv"
    shareFile(context, fileName, csvHeader + csvData)
}

fun exportResourcesToCSV(context: Context, resources: List<DVBSResource>, dayLabel: String) {
    val csvHeader = "Date,Day,Grade,Teacher,Children,Salvations,Workers,Category,Recorded By\n"
    val csvData = resources.joinToString("\n") { r ->
        "${r.date},${r.dvbsDay},${r.grade},${r.teacherName},${r.numChildren},${r.numNewSalvations},${r.numWorkers},${r.genderCategory},${r.recordedBy}"
    }
    val fileName = "dvbs_resources_${dayLabel.replace(" ", "_").lowercase()}.csv"
    shareFile(context, fileName, csvHeader + csvData)
}

private fun shareFile(context: Context, fileName: String, content: String) {
    try {
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { it.write(content.toByteArray()) }

        val uri = FileProvider.getUriForFile(context, "com.roy.ngong.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export $fileName"))
    } catch (e: Exception) {
        android.util.Log.e("DVBSScreen", "Error exporting CSV", e)
    }
}

@Composable
fun DVBSRegistrationDayGroup(
    day: String,
    registrations: List<DVBSRegistration>,
    contentColor: Color,
    isDarkMode: Boolean,
    isSearchActive: Boolean = false,
    isAdmin: Boolean = false,
    onEdit: (DVBSRegistration) -> Unit = {},
    onDelete: (DVBSRegistration) -> Unit = {}
) {
    var isExpanded by remember(isSearchActive) { mutableStateOf(isSearchActive) }
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = day.ifBlank { "Unspecified Day" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        text = "Total: ${registrations.size} children",
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }

                if (isAdmin) {
                    IconButton(onClick = {
                        exportRegistrationsToCSV(context, registrations, day.ifBlank { "unspecified" })
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Download Day CSV", tint = contentColor.copy(alpha = 0.6f))
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = contentColor
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = contentColor.copy(alpha = 0.1f))
                    registrations.forEach { registration ->
                        DVBSRegistrationDetailItem(
                            registration = registration,
                            contentColor = contentColor,
                            isAdmin = isAdmin,
                            onEdit = { onEdit(registration) },
                            onDelete = { onDelete(registration) }
                        )
                        if (registration != registrations.last()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                color = contentColor.copy(alpha = 0.05f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DVBSRegistrationDetailItem(
    registration: DVBSRegistration,
    contentColor: Color,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = registration.childName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Age: ${registration.age}",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
                Text(
                    text = "Gender: ${registration.gender}",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
                Text(
                    text = "Grade: ${registration.gradeClass}",
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
            Text(
                text = "Guardian: ${registration.parentGuardianName} (${registration.parentGuardianPhone})",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f)
            )
        }

        if (isAdmin) {
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyListPlaceholder(message: String, contentColor: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DVBSResourceDayGroup(
    day: String,
    resources: List<DVBSResource>,
    contentColor: Color,
    isDarkMode: Boolean,
    isSearchActive: Boolean = false,
    isAdmin: Boolean = false,
    onEdit: (DVBSResource) -> Unit = {},
    onDelete: (DVBSResource) -> Unit = {}
) {
    var isExpanded by remember(isSearchActive) { mutableStateOf(isSearchActive) }
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = day.ifBlank { "Unspecified Day" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        text = "Total Entries: ${resources.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }

                if (isAdmin) {
                    IconButton(onClick = {
                        exportResourcesToCSV(context, resources, day.ifBlank { "unspecified" })
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Download Day CSV", tint = contentColor.copy(alpha = 0.6f))
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = contentColor
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider(color = contentColor.copy(alpha = 0.1f))
                    resources.forEach { resource ->
                        DVBSResourceItem(
                            resource = resource,
                            contentColor = contentColor,
                            isAdmin = isAdmin,
                            onEdit = { onEdit(resource) },
                            onDelete = { onDelete(resource) }
                        )
                        if (resource != resources.last()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                color = contentColor.copy(alpha = 0.05f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DVBSResourceItem(
    resource: DVBSResource,
    contentColor: Color,
    isAdmin: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Column(modifier = Modifier.padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Grade: ${resource.grade}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(resource.date, style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.6f))
                if (isAdmin) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Category: ${resource.genderCategory}", style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(alpha = 0.7f))
            Text("•", color = contentColor.copy(alpha = 0.5f))
            Text("Teacher: ${resource.teacherName}", style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(alpha = 0.7f))
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatItem("Children", resource.numChildren.toString())
            StatItem("Salvations", resource.numNewSalvations.toString())
            StatItem("Workers", resource.numWorkers.toString())
        }
    }
}

/**
 * Small stat readout with a press-scale + ripple so it reads as tappable
 * feedback even though it's purely informational today. Wire onClick to
 * something (e.g. a breakdown dialog) when you have a use for the tap.
 */
@Composable
fun StatItem(label: String, value: String, onClick: (() -> Unit)? = null) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.9f else 1f, label = "statItemScale")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = { onClick?.invoke() }
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun MultiFloatingActionButton(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    primaryColor: Color,
    onAddRegistration: () -> Unit,
    onAddResource: () -> Unit
) {
    val rotation by animateFloatAsState(if (expanded) 45f else 0f)

    Column(horizontalAlignment = Alignment.End) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FabOption(
                    icon = Icons.Default.Group,
                    label = "Add Registration",
                    onClick = {
                        onExpandedChange(false)
                        onAddRegistration()
                    },
                    primaryColor = primaryColor
                )
                FabOption(
                    icon = Icons.Default.ListAlt,
                    label = "Add Resource",
                    onClick = {
                        onExpandedChange(false)
                        onAddResource()
                    },
                    primaryColor = primaryColor
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            containerColor = primaryColor,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Menu",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
fun FabOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    primaryColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.6f),
            contentColor = Color.White
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 14.sp
            )
        }
        FloatingActionButton(
            onClick = onClick,
            containerColor = primaryColor,
            contentColor = Color.White,
            modifier = Modifier.size(48.dp),
            shape = CircleShape
        ) {
            Icon(icon, contentDescription = label)
        }
    }
}
