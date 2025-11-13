
package com.roy.ngong.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dateString: String = "", // The date in "YYYY-MM-DD" format for easy querying
    @ServerTimestamp
    val createdAt: Date? = null,
    val createdBy: String = "" // Email or UID of the admin who created it
)