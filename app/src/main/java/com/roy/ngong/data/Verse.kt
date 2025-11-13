package com.roy.ngong.data

data class Verse(
    val id: String = "", // Useful for Firestore later
    val text: String,
    val reference: String // e.g., "John 3:16"
)