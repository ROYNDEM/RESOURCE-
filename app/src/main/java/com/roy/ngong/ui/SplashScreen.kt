package com.roy.ngong.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.roy.ngong.R
import com.roy.ngong.navigation.AppDestinations
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    // --- State for the progress bar ---
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "SplashScreenProgress"
    )

    // This effect runs when the screen is first displayed
    LaunchedEffect(key1 = true) {
        // Set progress to 1f to start the animation
        progress = 1f

        // Wait for 5 seconds (5000 milliseconds)
        delay(5000L)

        // Your existing navigation logic remains the same
        val destination = if (FirebaseAuth.getInstance().currentUser != null) {
            AppDestinations.HOME_SCREEN_ROUTE
        } else {
            // Ensure this route name matches the one in your NavHost
            AppDestinations.LOGIN_ROUTE
        }

        // Navigate and clear the splash screen from the back stack
        navController.navigate(destination) {
            popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 40.dp) // Add some padding
        ) {
            // Your CITAM Logo
            Image(
                painter = painterResource(id = R.drawable.citam_logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Name Text
            Text(
                text = "CHRIST IS THE ANSWER MINISTRIES",
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(48.dp))

            // --- REPLACED INDICATOR ---
            // A linear progress bar that fills up
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFC62828), // The color of the progress
                trackColor = Color.LightGray, // The color of the background track
            )
        }
    }
}