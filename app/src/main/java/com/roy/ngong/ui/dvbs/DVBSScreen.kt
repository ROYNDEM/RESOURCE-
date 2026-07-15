package com.roy.ngong.ui.dvbs

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.roy.ngong.data.DVBSRegistration
import com.roy.ngong.data.DVBSResource

enum class DVBSViewMode {
    RESOURCES, REGISTRATIONS
}

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

    // State for delete confirmation
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDeleteId by remember { mutableStateOf<String?>(null) }
    var isDeletingRegistration by remember { mutableStateOf(true) }

    // Determine the view mode if not explicitly set (for the swipeable page)
    val effectiveMode = when {
        userRole == "registration" -> DVBSViewMode.REGISTRATIONS
        userRole == "resource" -> DVBSViewMode.RESOURCES
        else -> mode // Admin sees whatever mode is passed (default registrations)
    }

    // --- FILTERING LOGIC ---
    val filteredRegistrations = remember(registrations, searchQuery) {
        if (searchQuery.isBlank()) registrations
        else registrations.filter {
            it.childName.contains(searchQuery, ignoreCase = true) ||
            it.parentGuardianName.contains(searchQuery, ignoreCase = true) ||
            it.gradeClass.contains(searchQuery, ignoreCase = true) ||
            it.dvbsDay.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredResources = remember(resources, searchQuery) {
        if (searchQuery.isBlank()) resources
        else resources.filter {
            it.teacherName.contains(searchQuery, ignoreCase = true) ||
            it.grade.contains(searchQuery, ignoreCase = true) ||
            it.dvbsDay.contains(searchQuery, ignoreCase = true)
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
            val titleText = if (effectiveMode == DVBSViewMode.REGISTRATIONS) 
                "Child Registrations" 
            else 
                "Resource Entries"

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
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- SEARCH BAR ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
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

            if (effectiveMode == DVBSViewMode.REGISTRATIONS) {
                if (filteredRegistrations.isEmpty()) {
                    EmptyListPlaceholder(
                        if (searchQuery.isEmpty()) "No registrations yet." else "No matches found for \"$searchQuery\"",
                        contentColor
                    )
                } else {
                    val groupedRegistrations = filteredRegistrations.groupBy { it.dvbsDay }
                        .toSortedMap(compareBy { it })

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
                                    isSearchActive = searchQuery.isNotBlank(),
                                    isAdmin = userRole == "admin",
                                    onEdit = { onNavigateToRegistrationEntry(it.id) },
                                    onDelete = { 
                                        itemToDeleteId = it.id
                                        isDeletingRegistration = true
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                if (filteredResources.isEmpty()) {
                    EmptyListPlaceholder(
                        if (searchQuery.isEmpty()) "No resource entries yet." else "No matches found for \"$searchQuery\"",
                        contentColor
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredResources) { resource ->
                            DVBSResourceItem(
                                resource = resource,
                                contentColor = contentColor,
                                isAdmin = userRole == "admin",
                                onEdit = { onNavigateToResourceEntry(resource.id) },
                                onDelete = {
                                    itemToDeleteId = resource.id
                                    isDeletingRegistration = false
                                    showDeleteDialog = true
                                }
                            )
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

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this entry? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDeleteId?.let { id ->
                            if (isDeletingRegistration) {
                                dvbsViewModel.deleteDVBSRegistration(id)
                            } else {
                                dvbsViewModel.deleteDVBSResource(id)
                            }
                        }
                        showDeleteDialog = false
                        itemToDeleteId = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
                Column {
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
private fun DVBSResourceItem(
    resource: DVBSResource,
    contentColor: Color,
    isAdmin: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(resource.dvbsDay, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
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
                Text("Grade: ${resource.grade}", fontWeight = FontWeight.Medium)
                Text("(${resource.genderCategory})", fontWeight = FontWeight.Medium, color = contentColor.copy(alpha = 0.7f))
            }
            Text("Teacher: ${resource.teacherName}")
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("Children", resource.numChildren.toString())
                StatItem("Salvations", resource.numNewSalvations.toString())
                StatItem("Workers", resource.numWorkers.toString())
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
