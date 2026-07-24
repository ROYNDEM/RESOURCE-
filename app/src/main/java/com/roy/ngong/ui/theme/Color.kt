package com.roy.ngong.ui.theme

import androidx.compose.ui.graphics.Color

// --- Brand color, defined once. Screens should reference
// MaterialTheme.colorScheme.primary instead of hardcoding this hex again. ---
val BrandRed = Color(0xFFC62828)
val BrandRedDark = Color(0xFFB71C1C) // slightly deeper, used for dark-theme primary/error accents if needed

// --- Light theme surfaces, matched to what HomeScreen/DVBSScreen already use ---
val LightBackground = Color(0xFFF0F0F0)
val LightSurface = Color.White
val LightSurfaceVariant = Color(0xFFF5F5F5)
val OnLight = Color.Black

// --- Dark theme surfaces, matched to what HomeScreen/DVBSScreen already use ---
val DarkBackground = Color.Black
val DarkSurface = Color(0xFF212121)
val DarkSurfaceVariant = Color(0xFF2C2C2C)
val OnDark = Color.White