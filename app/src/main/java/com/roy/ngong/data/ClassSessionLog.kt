package com.roy.ngong.data // Or your chosen package for data models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp // <-- ADD THIS IMPORT
import java.util.Date

@Entity(tableName = "pending_logs")
@TypeConverters(ListConverter::class, DateConverter::class)
data class ClassSessionLog(
    @PrimaryKey
    @DocumentId
    val id: String = "",
    val serviceName: String = "",
    val sessionDate: Date? = null,
    val className: String = "",
    val teacherNames: List<String> = emptyList(),
    val teacherHelpers: List<String> = emptyList(),
    val attendeeCount: Int = 0,
    val submittedBy: String = "",

    @ServerTimestamp
    val submittedAt: Date? = null
)
