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
    onNavigateToResourceEntry: () -> Unit = {},
    onNavigateToRegistrationEntry: () -> Unit = {}
) {
    val registrations by dvbsViewModel.dvbsRegistrations.collectAsState()
    val resources by dvbsViewModel.dvbsResources.collectAsState()

    val primaryColor = Color(0xFFC62828)
    val lightModeBackground = Color(0xFFF0F0F0)
    val darkModeBackground = Color.Black
    val contentColor = if (isDarkMode) Color.White else Color.Black

    var fabExpanded by remember { mutableStateOf(false) }

    // Determine the view mode if not explicitly set (for the swipeable page)
    val effectiveMode = when {
        userRole == "registration" -> DVBSViewMode.REGISTRATIONS
        userRole == "resource" -> DVBSViewMode.RESOURCES
        else -> mode // Admin sees whatever mode is passed (default registrations)
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
                "Child Registrations (${registrations.size})" 
            else 
                "Resource Entries (${resources.size})"

            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (effectiveMode == DVBSViewMode.REGISTRATIONS) {
                if (registrations.isEmpty()) {
                    EmptyListPlaceholder("No registrations yet.", contentColor)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(registrations) { registration ->
                            DVBSRegistrationItem(registration, contentColor)
                        }
                    }
                }
            } else {
                if (resources.isEmpty()) {
                    EmptyListPlaceholder("No resource entries yet.", contentColor)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(resources) { resource ->
                            DVBSResourceItem(resource, contentColor)
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
                        onAddRegistration = onNavigateToRegistrationEntry,
                        onAddResource = onNavigateToResourceEntry
                    )
                }
                "registration" -> {
                    FloatingActionButton(
                        onClick = onNavigateToRegistrationEntry,
                        containerColor = primaryColor,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Registration")
                    }
                }
                "resource" -> {
                    FloatingActionButton(
                        onClick = onNavigateToResourceEntry,
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

@Composable
private fun DVBSResourceItem(resource: DVBSResource, contentColor: Color) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(resource.dvbsDay, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
                Text(resource.date, style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.6f))
            }
            Text("Grade: ${resource.grade}", fontWeight = FontWeight.Medium)
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
