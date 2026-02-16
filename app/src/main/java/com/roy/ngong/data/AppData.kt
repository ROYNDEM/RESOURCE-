package com.roy.ngong.data

// This data class will represent the document in Firestore
data class GeneralData(
    val verseOfTheDay: String = "",
    val announcement: String = ""
)

data class SundayResourceInventory(
    val date: String = "",
    val biscuitBoxes: Int = 0,
    val printingReams: Int = 0,
    val recordedBy: String = ""
)