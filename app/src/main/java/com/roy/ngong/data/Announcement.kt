package com.roy.ngong.data

import java.util.Date // For the announcement date

data class Announcement(
    val id: String = "", // Useful for Firestore later
    val title: String,
    val message: String,
    val date: Date = Date() // Default to current date/time when created
    // You could also use a String for the date if you prefer to format it upfront
    // or Long for a timestamp
)