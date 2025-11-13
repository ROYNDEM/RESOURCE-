package com.roy.ngong

import android.os.Build
import com.roy.ngong.ui.calendar.CalendarScreen
import com.roy.ngong.ui.calendar.AddEditEventScreen
import com.roy.ngong.ui.calendar.CalendarViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.roy.ngong.navigation.AppDestinations
import com.roy.ngong.ui.SplashScreen
import com.roy.ngong.ui.admin.AdminDashboardScreen
import com.roy.ngong.ui.admin.AdminManageControlsScreen
import com.roy.ngong.ui.admin.AdminReportDetailScreen
import com.roy.ngong.ui.admin.AdminReportsListScreen
// import com.roy.ngong.ui.admin.AdminSignUpScreen // <-- REMOVED
import com.roy.ngong.ui.admin.AppDataViewModel
import com.roy.ngong.ui.admin.AuthViewModel
import com.roy.ngong.ui.auth.LoginScreen // Keep the main login screen
import com.roy.ngong.ui.auth.SignUpScreen
import com.roy.ngong.ui.edit.ResourceEditScreen
import com.roy.ngong.ui.home.HomeScreen
import com.roy.ngong.ui.pending.PendingSubmissionsViewModel
import com.roy.ngong.ui.resource.ResourceEntryScreen
import com.roy.ngong.ui.resource.viewmodel.ResourceViewModel
import com.roy.ngong.ui.theme.NGONGTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            val onThemeToggle = { isDarkMode = !isDarkMode }
            val snackbarHostState = remember { SnackbarHostState() }

            NGONGTheme(darkTheme = isDarkMode) {
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    AppNavigator(
                        modifier = Modifier.padding(innerPadding),
                        isDarkMode = isDarkMode,
                        onThemeToggle = onThemeToggle,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigator(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val navController = rememberNavController()

    // --- VIEW MODELS ---
    val resourceViewModel: ResourceViewModel = viewModel()
    val appDataViewModel: AppDataViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val pendingViewModel: PendingSubmissionsViewModel = viewModel()
    val calendarViewModel: CalendarViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.SPLASH_ROUTE,
        modifier = modifier
    ) {
        composable(AppDestinations.SIGN_UP_ROUTE) {
            SignUpScreen(navController = navController)
        }

        composable(AppDestinations.SPLASH_ROUTE) {
            SplashScreen(navController = navController)
        }

        composable(AppDestinations.LOGIN_ROUTE) {
            LoginScreen(navController = navController)
        }

        composable(AppDestinations.HOME_SCREEN_ROUTE) {
            HomeScreen(
                navController = navController,
                appDataViewModel = appDataViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(0)
                    }
                },
                onProfileClick = { navController.navigate(AppDestinations.PROFILE_ROUTE) },
                onNavigateToPending = { navController.navigate(AppDestinations.RESOURCE_EDIT_ROUTE) },
                onNavigateToEntry = { navController.navigate(AppDestinations.RESOURCE_ENTRY_ROUTE) },
                isDarkMode = isDarkMode,
                onThemeToggle = onThemeToggle
            )
        }

        composable(AppDestinations.PROFILE_ROUTE) {
            // You will need to create this ProfileScreen composable
            ProfileScreen(navController = navController)
        }

        composable(AppDestinations.RESOURCE_ENTRY_ROUTE) {
            ResourceEntryScreen(
                pendingViewModel = pendingViewModel,
                snackbarHostState = snackbarHostState,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = AppDestinations.RESOURCE_EDIT_ROUTE) {
            ResourceEditScreen(
                pendingViewModel = pendingViewModel,
                resourceViewModel = resourceViewModel,
                snackbarHostState = snackbarHostState,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(AppDestinations.CALENDAR_ROUTE) {
            CalendarScreen(
                navController = navController,
                calendarViewModel = calendarViewModel,
            )
        }
        
        composable(route = AppDestinations.ADMIN_ADD_ENTRY_ROUTE) {
            // Use the correct, existing screen and ViewModel
            AddEditEventScreen(
                navController = navController,
                // Pass "new_event" to indicate a new event is being created from the admin dashboard
                dateString = "new_event",
                calendarViewModel = calendarViewModel // Use the existing ViewModel instance
            )
        }

        composable(
            route = AppDestinations.ADD_EDIT_EVENT_ROUTE,
            arguments = listOf(navArgument("dateString") { type = NavType.StringType })
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("dateString")
            if (date != null) {
                AddEditEventScreen(
                    navController = navController,
                    dateString = date,
                    calendarViewModel = calendarViewModel // Pass the ViewModel
                )
            } else {
                navController.popBackStack() // Go back if date is missing
            }
        }

        // --- Admin Navigation Graph ---
        adminNavGraph(
            navController = navController,
            // startDestination is no longer passed from here
            resourceViewModel = resourceViewModel,
            appDataViewModel = appDataViewModel,
            authViewModel = authViewModel,
            isDarkMode = isDarkMode,
            onThemeToggle = onThemeToggle
        )
    }
}

fun NavGraphBuilder.adminNavGraph(
    navController: NavHostController,
    // The startDestination is now fixed and doesn't need to be a parameter
    // startDestination: String,
    resourceViewModel: ResourceViewModel,
    appDataViewModel: AppDataViewModel,
    authViewModel: AuthViewModel,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    // The start destination for the admin flow is now ALWAYS the dashboard.
    navigation(startDestination = AppDestinations.ADMIN_DASHBOARD_ROUTE, route = "admin_flow") {

        // --- DELETED ADMIN_LOGIN_ROUTE COMPOSABLE ---

        // --- DELETED ADMIN_SIGN_UP_ROUTE COMPOSABLE ---

        composable(AppDestinations.ADMIN_DASHBOARD_ROUTE) {
            AdminDashboardScreen(
                navController = navController,
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.logout()
                    // Navigate back to the single login screen
                    navController.navigate(AppDestinations.LOGIN_ROUTE) {
                        popUpTo(0) // Clear the entire back stack
                    }
                },
                isDarkMode = isDarkMode,
                onThemeToggle = onThemeToggle,
                onProfileClick = { /* Can be implemented later */ }
            )
        }
        composable(AppDestinations.ADMIN_MANAGE_CONTROLS_ROUTE) {
            AdminManageControlsScreen(
                appDataViewModel = appDataViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppDestinations.ADMIN_REPORTS_LIST_ROUTE) {
            AdminReportsListScreen(
                navController = navController,
                resourceViewModel = resourceViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = AppDestinations.ADMIN_REPORTS_DETAIL_ROUTE,
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date")
            if (date != null) {
                AdminReportDetailScreen(
                    resourceViewModel = resourceViewModel,
                    date = date,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                navController.popBackStack()
            }
        }
    }
}