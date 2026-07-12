package com.roy.ngong.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@Entity(tableName = "dvbs_data")
data class DVBSRecord(
    @PrimaryKey
    val id: String = "",
    val date: String = "",
    val title: String = "",
    val attendeeCount: Int = 0,
    val topic: String = "",
    val location: String = "",
    val notes: String = "",
    val materialsUsed: List<String> = emptyList(),
    val recordedBy: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    val lastModified: Date? = null
)

// For displaying DVBS statistics
data class DVBSStatistics(
    val totalEvents: Int = 0,
    val totalAttendees: Int = 0,
    val averageAttendance: Float = 0f,
    val lastEventDate: String = ""
)

@Entity(tableName = "dvbs_registrations")
data class DVBSRegistration(
    @PrimaryKey
    val id: String = "",
    val childName: String = "",
    val age: Int = 0,
    val gradeClass: String = "",
    val parentGuardianName: String = "",
    val parentGuardianPhone: String = "",
    val registrationDate: String = "",
    val registeredBy: String = "",
    @ServerTimestamp
    val createdAt: Date? = null,
    val lastModified: Date? = null
)

