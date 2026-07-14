package com.roy.ngong.ui.home

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.roy.ngong.R
import com.roy.ngong.navigation.AppDestinations
import com.roy.ngong.ui.admin.AppDataViewModel
import com.roy.ngong.ui.admin.AuthViewModel
import com.roy.ngong.ui.dvbs.DVBSScreen
import com.roy.ngong.ui.dvbs.DVBSViewModel
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    appDataViewModel: AppDataViewModel,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onNavigateToPending: () -> Unit,
    onNavigateToEntry: () -> Unit,
    onNavigateToDVBSResourceEntry: () -> Unit = {},
    onNavigateToDVBSRegistrationEntry: () -> Unit = {}
) {
    val primaryColor = Color(0xFFC62828)
    val lightModeBackground = Color(0xFFF0F0F0)
    val darkModeBackground = Color.Black
    val lightModeSurface = Color.White
    val darkModeSurface = Color(0xFF212121)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Observe User Role and Admin Status from AuthViewModel
    val userRole by authViewModel.userRole.collectAsState()
    val isAdmin by authViewModel.isAdmin.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                primaryColor = primaryColor,
                userRole = userRole,
                isAdmin = isAdmin,
                onAdminClick = {
                    scope.launch { drawerState.close() }
                    if (isAdmin) {
                        navController.navigate("admin_flow")
                    } else {
                        Toast.makeText(context, "Access Denied: Admin role required.", Toast.LENGTH_LONG).show()
                    }
                },
                onDVBSRegistrationsClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppDestinations.DVBS_ADMIN_REGISTRATIONS_ROUTE)
                },
                onDVBSResourcesClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppDestinations.DVBS_ADMIN_RESOURCES_ROUTE)
                },
                onEntryClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToEntry()
                },
                onPendingClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToPending()
                },
                onCalendarClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppDestinations.CALENDAR_ROUTE)
                }
            )
        }
    ) {
        val data by appDataViewModel.generalData.collectAsState()
        val pagerState = rememberPagerState(pageCount = { 2 })
        val dvbsViewModel: DVBSViewModel = viewModel()

        Scaffold(
            topBar = {
                HomeTopAppBar(
                    primaryColor = primaryColor,
                    isDarkMode = isDarkMode,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onProfileClick = onProfileClick,
                    onThemeToggle = onThemeToggle,
                    onLogoutClick = onLogout
                )
            },
            floatingActionButton = {
                if (pagerState.currentPage == 0) {
                    FloatingActionButton(
                        onClick = { navController.navigate(AppDestinations.RESOURCE_ENTRY_ROUTE) },
                        containerColor = primaryColor
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add resource entry",
                            tint = Color.White
                        )
                    }
                }
            },
            containerColor = if (isDarkMode) darkModeBackground else lightModeBackground
        ) { paddingValues ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            InfoCard(
                                title = "Verse of the Day",
                                content = data.verseOfTheDay,
                                icon = Icons.Default.Book,
                                isDarkMode = isDarkMode,
                                surfaceColor = if (isDarkMode) darkModeSurface else lightModeSurface
                            )

                            InfoCard(
                                title = "Announcements",
                                content = data.announcement,
                                icon = Icons.Default.Campaign,
                                isDarkMode = isDarkMode,
                                surfaceColor = if (isDarkMode) darkModeSurface else lightModeSurface
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "← Swipe left for DVBS →",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDarkMode) Color.White.copy(alpha = 0.5f) 
                                           else Color.Black.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    1 -> {
                        DVBSScreen(
                            dvbsViewModel = dvbsViewModel,
                            isDarkMode = isDarkMode,
                            userRole = userRole,
                            onNavigateToResourceEntry = onNavigateToDVBSResourceEntry,
                            onNavigateToRegistrationEntry = onNavigateToDVBSRegistrationEntry
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar(
    primaryColor: Color,
    isDarkMode: Boolean,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit,
    onThemeToggle: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var settingsExpanded by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.citam_logo),
                    contentDescription = "Church Logo",
                    modifier = Modifier.size(80.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = primaryColor),
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = { settingsExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Settings",
                        tint = Color.White,
                    )
                }
                DropdownMenu(
                    expanded = settingsExpanded,
                    onDismissRequest = { settingsExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Profile") },
                        onClick = {
                            settingsExpanded = false
                            onProfileClick()
                        },
                        leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isDarkMode) "Light Mode" else "Dark Mode") },
                        onClick = {
                            settingsExpanded = false
                            onThemeToggle()
                        },
                        leadingIcon = {
                            Icon(
                                if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme"
                            )
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = {
                            settingsExpanded = false
                            onLogoutClick()
                        },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout") }
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDrawerContent(
    primaryColor: Color,
    userRole: String,
    isAdmin: Boolean,
    onAdminClick: () -> Unit,
    onEntryClick: () -> Unit,
    onPendingClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onDVBSRegistrationsClick: () -> Unit,
    onDVBSResourcesClick: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Color(0xFFF0F0F0)
    ) {
        Box(
            modifier = Modifier
                .padding(0.dp)
                .fillMaxWidth()
                .background(
                    color = primaryColor,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.citam_logo),
                    contentDescription = "Church Logo",
                    modifier = Modifier.size(90.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "CHRIST IS THE ANSWER MINISTRIES",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DrawerButton(
                text = "Add New Entry",
                icon = Icons.AutoMirrored.Filled.NoteAdd,
                onClick = onEntryClick
            )

            DrawerButton(
                text = "Event Calendar",
                icon = Icons.Default.CalendarMonth,
                onClick = onCalendarClick
            )

            DrawerButton(
                text = "Review Pending Entries",
                icon = Icons.Default.Edit,
                onClick = onPendingClick
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Only show DVBS management menu to Admins
            if (isAdmin || userRole == "admin") {
                DVBSCollapsibleMenu(
                    onDVBSRegistrationsClick = onDVBSRegistrationsClick,
                    onDVBSResourcesClick = onDVBSResourcesClick
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Only show Admin Dashboard to system admins
            if (isAdmin) {
                DrawerButton(
                    text = "Admin Dashboard",
                    icon = Icons.Default.AdminPanelSettings,
                    onClick = onAdminClick
                )
            }
        }
    }
}

@Composable
private fun DVBSCollapsibleMenu(
    onDVBSRegistrationsClick: () -> Unit,
    onDVBSResourcesClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { isExpanded = !isExpanded },
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(Icons.Default.Event, contentDescription = "DVBS")
                Spacer(modifier = Modifier.width(16.dp))
                Text("DVBS", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle"
                )
            }
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(8.dp))

            DVBSDrawerSubButton(
                text = "Registrations",
                icon = Icons.Default.Person,
                onClick = onDVBSRegistrationsClick
            )

            Spacer(modifier = Modifier.height(6.dp))

            DVBSDrawerSubButton(
                text = "Resources",
                icon = Icons.Default.Build,
                onClick = onDVBSResourcesClick
            )
        }
    }
}

@Composable
private fun DVBSDrawerSubButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = Color.Black),
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = text, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun DrawerButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = text)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
        }
    }
}

@Composable
private fun InfoCard(title: String, content: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isDarkMode: Boolean, surfaceColor: Color) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1f, label = "cardScale")
    val contentColor = if (isDarkMode) Color.White else Color.Black

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        modifier = Modifier.fillMaxWidth().scale(scale).shadow(elevation = if (isPressed) 4.dp else 12.dp, shape = RoundedCornerShape(24.dp)).clickable(interactionSource = interactionSource, indication = null, onClick = {})
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(icon, contentDescription = title, modifier = Modifier.size(36.dp), tint = contentColor)
                Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = contentColor)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = content, style = MaterialTheme.typography.bodyLarge, color = contentColor.copy(alpha = 0.8f))
        }
    }
}
