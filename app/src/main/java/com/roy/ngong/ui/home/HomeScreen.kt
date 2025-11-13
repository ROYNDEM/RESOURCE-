package com.roy.ngong.ui.home

// --- MAKE SURE YOU HAVE THESE IMPORTS ---
// --- End of required imports ---

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.roy.ngong.R
import com.roy.ngong.navigation.AppDestinations
import com.roy.ngong.ui.admin.AppDataViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    appDataViewModel: AppDataViewModel,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onNavigateToPending: () -> Unit,
    onNavigateToEntry: () -> Unit
) {
    val primaryColor = Color(0xFFC62828)
    val lightModeBackground = Color(0xFFF0F0F0)
    val darkModeBackground = Color.Black
    val lightModeSurface = Color.White
    val darkModeSurface = Color(0xFF212121)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // Get context for toasts

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                primaryColor = primaryColor,
                onAdminClick = {
                    // --- SECURE ADMIN CHECK ---
                    scope.launch { drawerState.close() }
                    checkAdminStatus { isAdmin ->
                        if (isAdmin) {
                            navController.navigate("admin_flow")
                        } else {
                            Toast.makeText(context, "Access Denied: Admin role required.", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onEntryClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToEntry()
                },
                onPendingClick = {
                    scope.launch { drawerState.close() }
                    onNavigateToPending()
                },
                        onCalendarClick = { // <-- 3. IMPLEMENT THE CLICK
                    scope.launch { drawerState.close() }
                    navController.navigate(AppDestinations.CALENDAR_ROUTE)
                }
            )
        }
    ) {
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
            },
            containerColor = if (isDarkMode) darkModeBackground else lightModeBackground
        ) { paddingValues ->
            val data by appDataViewModel.generalData.collectAsState()

            Column(
                modifier = Modifier
                    .padding(paddingValues)
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
            }
        }
    }
}

// --- ADD THIS HELPER FUNCTION AT THE END OF THE FILE ---
private fun checkAdminStatus(onResult: (Boolean) -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        onResult(false)
        return
    }

    val db = Firebase.firestore
    db.collection("users").document(currentUser.uid)
        .get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Successfully found the document. Check the isAdmin field.
                val isAdmin = document.getBoolean("isAdmin") ?: false
                onResult(isAdmin)
            } else {
                // This means the Firestore document for the logged-in user doesn't exist.
                Log.w("AdminCheck", "User document not found for UID: ${currentUser.uid}")
                onResult(false)
            }
        }
        .addOnFailureListener { exception ->
            // The database call failed. Log the specific error from Firebase.
            Log.e("AdminCheck", "Failed to check admin status. Error: ", exception)
            onResult(false)
        }
}


/**
 * The Top App Bar for the Home Screen.
 */
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
                modifier = Modifier.fillMaxHeight(), // Allow the box to fill the height of the title area
                contentAlignment = Alignment.Center // Center the content (the Image) vertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.citam_logo),
                    contentDescription = "Church Logo",
                    modifier = Modifier.size(80.dp), // Increase the size for better visibility
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
                    Divider()
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
        },
        modifier = Modifier
            .padding(horizontal = 0.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
    )
}

/**
 * The content of the navigation drawer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDrawerContent(
    primaryColor: Color,
    onAdminClick: () -> Unit,
    onEntryClick: () -> Unit,
    onPendingClick: () -> Unit,
    onCalendarClick: () -> Unit
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
                icon = Icons.Default.NoteAdd,
                onClick = onEntryClick
            )

            DrawerButton(
                text = "Event Calendar", // <-- 2. ADD THE NEW BUTTON
                icon = Icons.Default.CalendarMonth, // You may need to add this import
                onClick = onCalendarClick
            )

            DrawerButton(
                text = "Review Pending Entries",
                icon = Icons.Default.Edit,
                onClick = onPendingClick
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            DrawerButton(
                text = "Admin Dashboard",
                icon = Icons.Default.AdminPanelSettings,
                onClick = onAdminClick
            )
        }
    }
}

/**
 * A reusable, elevated button styled as a card for the navigation drawer.
 */
@Composable
private fun DrawerButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(imageVector = icon, contentDescription = text)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
        }
    }
}


/**
 * A visually appealing card with a 3D effect and click interaction.
 */
@Composable
private fun InfoCard(
    title: String,
    content: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDarkMode: Boolean,
    surfaceColor: Color,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1f, label = "cardScale")
    val contentColor = if (isDarkMode) Color.White else Color.Black

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 4.dp else 12.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(36.dp),
                    tint = contentColor
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}