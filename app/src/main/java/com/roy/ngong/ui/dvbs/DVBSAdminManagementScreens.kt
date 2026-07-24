package com.roy.ngong.ui.dvbs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DVBSAdminRegistrationScreen(
    dvbsViewModel: DVBSViewModel = viewModel(),
    isDarkMode: Boolean = false,
    onNavigateToEntry: (String?) -> Unit = {}
) {
    val registrations by dvbsViewModel.dvbsRegistrations.collectAsState()
    val primaryColor = Color(0xFFC62828)
    val contentColor = if (isDarkMode) Color.White else Color.Black
    val background = if (isDarkMode) Color.Black else Color(0xFFF0F0F0)
    
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val onRefresh = {
        scope.launch {
            isRefreshing = true
            dvbsViewModel.startListening()
            delay(1000)
            isRefreshing = false
        }
    }

    val filtered = remember(registrations, searchQuery) {
        if (searchQuery.isBlank()) registrations
        else registrations.filter {
            it.childName.contains(searchQuery, ignoreCase = true) ||
            it.parentGuardianName.contains(searchQuery, ignoreCase = true) ||
            it.dvbsDay.contains(searchQuery, ignoreCase = true)
        }
    }

    val grouped = filtered.groupBy { it.dvbsDay }.toSortedMap()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "All Registrations",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            IconButton(onClick = { exportRegistrationsToCSV(context, registrations, "all") }) {
                Icon(Icons.Default.Download, contentDescription = "Download All", tint = primaryColor)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search registrations...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, null) }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { onRefresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            if (filtered.isEmpty()) {
                EmptyListPlaceholder("No registrations found.", contentColor)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    grouped.forEach { (day, dayRegistrations) ->
                        item(key = day) {
                            DVBSRegistrationDayGroup(
                                day = day,
                                registrations = dayRegistrations,
                                contentColor = contentColor,
                                isDarkMode = isDarkMode,
                                isAdmin = true, // Admin sees edit/delete
                                onEdit = { onNavigateToEntry(it.id) },
                                onDelete = { dvbsViewModel.deleteDVBSRegistration(it.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DVBSAdminResourceScreen(
    dvbsViewModel: DVBSViewModel = viewModel(),
    isDarkMode: Boolean = false,
    onNavigateToEntry: (String?) -> Unit = {}
) {
    val resources by dvbsViewModel.dvbsResources.collectAsState()
    val primaryColor = Color(0xFFC62828)
    val contentColor = if (isDarkMode) Color.White else Color.Black
    val background = if (isDarkMode) Color.Black else Color(0xFFF0F0F0)
    
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val onRefresh = {
        scope.launch {
            isRefreshing = true
            dvbsViewModel.startListening()
            delay(1000)
            isRefreshing = false
        }
    }

    val filtered = remember(resources, searchQuery) {
        if (searchQuery.isBlank()) resources
        else resources.filter {
            it.teacherName.contains(searchQuery, ignoreCase = true) ||
            it.grade.contains(searchQuery, ignoreCase = true) ||
            it.dvbsDay.contains(searchQuery, ignoreCase = true)
        }
    }

    val grouped = filtered.groupBy { it.dvbsDay }.toSortedMap()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Resource Entries",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            IconButton(onClick = { exportResourcesToCSV(context, resources, "all") }) {
                Icon(Icons.Default.Download, contentDescription = "Download All", tint = primaryColor)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search resource entries...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, null) }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { onRefresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            if (filtered.isEmpty()) {
                EmptyListPlaceholder("No entries found.", contentColor)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    grouped.forEach { (day, dayResources) ->
                        item(key = day) {
                            DVBSResourceDayGroup(
                                day = day,
                                resources = dayResources,
                                contentColor = contentColor,
                                isDarkMode = isDarkMode,
                                isAdmin = true, // Admin sees edit/delete
                                onEdit = { onNavigateToEntry(it.id) },
                                onDelete = { dvbsViewModel.deleteDVBSResource(it.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
